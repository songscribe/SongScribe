/* 
SongScribe song notation program
Copyright (C) 2006-2007 Csaba Kavai

This file is part of SongScribe.

SongScribe is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

SongScribe is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Created on Jun 24, 2006
*/
package songscribe.ui.musicsheetdrawer;

import songscribe.music.*;
import songscribe.ui.MusicSheet;
import songscribe.ui.Utilities;
import songscribe.ui.Constants;
import songscribe.data.Interval;

import java.awt.geom.*;
import java.awt.*;
import java.util.Vector;
import java.util.ListIterator;
import java.io.File;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public abstract class BaseMsDrawer {
    protected final int[][] FLATSHARPORDER = {{}, {0, -3, 1, -2, 2, -1, 3},{-4, -1, -5, -2, 1, -3, 0}};

    protected static final float size = 32;
    protected static final BasicStroke beamStroke = new BasicStroke(4.167f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    protected static final BasicStroke lineStroke = new BasicStroke(0.694f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    protected static final BasicStroke stemStroke = new BasicStroke(0.836f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    protected static final BasicStroke tenutoStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke dashStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3.937f, 5.9055f}, 0f);
    private static final BasicStroke longDashStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke underScoreStroke = new BasicStroke(0.836f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final Ellipse2D.Float staccatoEllipse = new Ellipse2D.Float(0f, 0f, 3.5f, 3.5f);
    private static final Color selectionColor = new Color(254, 45, 125);
    private static final float beamTranslateY = 7;
    private static final Font tripletFont = new Font("Times New Roman", Font.BOLD, 14);
    private static final Font fsEndingFont = new Font("Times New Roman", Font.BOLD, 14);
    protected static Font fughetta;
    private static final String GLISSANDO = "\uf07e";
    private static final double glissandoLength = size/2.6666667;
    private static final float longDashWidth = 7f;
    protected static final double tempoChangeZoom = 0.8;

    protected static final float spaceBtwNoteAndAccidental = 1.139f;
    protected static final float[] accidentalWidths = {0f, size/5.8850574f, size/4.571429f, size/4.740741f, size/2.737968f, size/2.485437f, size/3.9689922f, size/2.4150944f, size/2.4615386f};
    protected static final float[] accidentalParenthesisWidths = {0f, size/1.5657493f, size/1.6f, size/1.5753846f, size/1.2929293f, size/1.343832f, size/1.8754579f, size/1.3950953f, size/1.2736318f};


    private static final NoteType[] BEAMLEVELS = {NoteType.DEMISEMIQUAVER, NoteType.SEMIQUAVER, NoteType.QUAVER};

    //fields for the key signature change
    private KeyType[] keyTypes = new KeyType[2];
    private int[] keys = new int[2];
    private int[] froms = new int[2];
    private boolean[] isNaturals = new boolean[2];

    protected float crotchetWidth;

    protected MusicSheet ms;

    public BaseMsDrawer(MusicSheet ms) throws FontFormatException, IOException{
        this.ms = ms;
        if(fughetta==null){
            fughetta  = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/fughetta.ttf")).deriveFont(size);
        }
    }

    public void drawMusicSheet(Graphics2D g2, boolean drawEditingComponents, double scale){
        Composition composition = ms.getComposition();
        if(scale!=1d) g2.scale(scale, scale);
        if(!drawEditingComponents)g2.translate(0, -ms.getStartY());        
        //drawing the title
        g2.setPaint(Color.black);
        if(composition.getSongTitle().length()>0){
            g2.setFont(composition.getSongTitleFont());
            StringBuilder title = new StringBuilder(composition.getSongTitle().length()+10);
            if(composition.getNumber().length()>0){
                title.append(composition.getNumber());
                title.append(". ");
            }
            title.append(composition.getSongTitle());
            String titleString = title.toString();
            int titleWidth = g2.getFontMetrics().stringWidth(titleString);
            drawAntialiasedString(g2, titleString, (composition.getLineWidth()-titleWidth)/2, composition.getSongTitleFont().getSize());
        }

        //drawing the rightInfo
        g2.setFont(composition.getGeneralFont());
        if(composition.getRightInfo().length()>0){
            drawTextBox(g2, composition.getRightInfo(), composition.getRightInfoStartY()+composition.getGeneralFont().getSize(), Component.RIGHT_ALIGNMENT, -20);
        }

        g2.setFont(composition.getLyricsFont());
        //drawing the under lyrics and the translated lyrics
        if(composition.getUnderLyrics().length()>0){
            drawTextBox(g2, composition.getUnderLyrics(), ms.getUnderLyricsYPos(), Component.CENTER_ALIGNMENT, 0);
        }
        if(composition.getTranslatedLyrics().length()>0){
            drawTextBox(g2, composition.getTranslatedLyrics(), ms.getUnderLyricsYPos()+(Utilities.lineCount(composition.getUnderLyrics())+1)*g2.getFontMetrics().getAscent(), Component.CENTER_ALIGNMENT, 0);
        }

        //drawing the tempo
        if(composition.getLine(0).noteCount()>0){
            drawTempoChange(g2, composition.getTempo(), 0, 0);
        }

        //drawing the composition
        for (int l = 0; l < composition.lineCount(); l++) {
            Line line = composition.getLine(l);
            g2.setPaint(l!=ms.getSelectedLine() ? Color.black : selectionColor);
            g2.setStroke(lineStroke);
            //drawing the lines
            for (int i = -2; i <= 2; i++) {
                g2.drawLine(0, ms.getNoteYPos(i*2, l), composition.getLineWidth(), ms.getNoteYPos(i*2, l));
            }

            g2.drawLine(0, ms.getNoteYPos(-4, l), 0, ms.getNoteYPos(4, l));
            //g2.drawLine(ms.getLineWidth()-1, ms.getMiddleLine() + (-2 * LINEDIST) + l * ms.getRowHeight(), ms.getLineWidth()-1, ms.getMiddleLine() + (2 * LINEDIST) + l * ms.getRowHeight());

            g2.setPaint(Color.black);

            //drawing the trebleclef and the leading keys
            drawLineBeginning(g2, line, l);

            //drawing the notes
            int lyricsDrawn = 0;
            for (int n=0;n<line.noteCount();n++) {
                Note note = line.getNote(n);

                //drawing the tempochange
                if(note.getTempoChange()!=null){
                    drawTempoChange(g2, note.getTempoChange(), l, n);
                }

                //drawing the note
                boolean beamed = line.getBeamings().findInterval(n)!=null && note.getNoteType()!=NoteType.GRACEQUAVER;
                paintNote(g2, note, l, beamed, ms.isNoteSelected(n, l) && drawEditingComponents ? selectionColor : Color.black);

                //drawing the tie if any
                Interval tieVal = line.getTies().findInterval(n);
                if(tieVal!=null && n!=tieVal.getB()){
                    int yPos = ms.getNoteYPos(note.getYPos(), l)+(note.isUpper() ? MusicSheet.LINEDIST/2+2 : -MusicSheet.LINEDIST/2-2);
                    int xPos = note.getXPos()+getHalfNoteWidthForTie(note)+2;
                    int gap = line.getNote(n+1).getXPos()+getHalfNoteWidthForTie(line.getNote(n+1))-xPos-3;
                    g2.setStroke(lineStroke);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GeneralPath tie = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
                    tie.moveTo(xPos, yPos);
                    tie.quadTo(xPos+gap/2, yPos+(note.isUpper()?6:-6), xPos+gap, yPos);
                    tie.quadTo(xPos+gap/2, yPos+(note.isUpper()?8:-8), xPos, yPos);
                    tie.closePath();
                    g2.draw(tie);
                    g2.fill(tie);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    /*g2.setPaint(Color.red);
                    g2.drawRect(xPos, yPos, 1, 1);
                    g2.setPaint(Color.green);
                    g2.drawRect(note.getXPos(), yPos, 1, 1);
                    g2.drawRect(note.getXPos()+note.getRealUpNoteRect().width, yPos, 1, 1);
                    g2.setPaint(Color.black);*/
                }

                //drawing the lyrics
                int lyricsY = ms.getNoteYPos(0, l)+line.getLyricsYPos();
                g2.setFont(composition.getLyricsFont());
                int syllableWidth = 0;
                if(note.a.syllable!=null && note.a.syllable!=Constants.UNDERSCORE){
                    syllableWidth = g2.getFontMetrics().stringWidth(note.a.syllable);
                    drawAntialiasedString(g2, note.a.syllable, note.getXPos()+Note.HOTSPOT.x-syllableWidth/2+note.getSyllableMovement(), lyricsY);
                }
                if(lyricsDrawn<=n && (note.a.syllableRelation!=Note.SyllableRelation.NO || n==0 && line.beginRelation!=Note.SyllableRelation.NO)){
                    Note.SyllableRelation relation = note.a.syllableRelation!=Note.SyllableRelation.NO ? note.a.syllableRelation : line.beginRelation;
                    int c=0;
                    if(relation==Note.SyllableRelation.DASH){
                        for(c=n+1;c<line.noteCount() && line.getNote(c).a.syllable==Constants.UNDERSCORE;c++);
                    }else{
                        for(c=n;c<line.noteCount() && line.getNote(c).a.syllableRelation==relation;c++);
                    }
                    lyricsDrawn = c;
                    int startX = n==0 && line.beginRelation!=Note.SyllableRelation.NO ? note.getXPos()-10 : note.getXPos()+Note.HOTSPOT.x+syllableWidth/2+note.getSyllableMovement()+2;
                    int endX;
                    if(c==line.noteCount() || c==line.noteCount()-1 && l+1<composition.lineCount() && composition.getLine(l+1).beginRelation!=Note.SyllableRelation.NO){
                        endX = composition.getLineWidth();
                    }else{
                        endX = relation==Note.SyllableRelation.EXTENDER ? line.getNote(c).getXPos()+12 :
                                line.getNote(c).getXPos()+Note.HOTSPOT.x-g2.getFontMetrics().stringWidth(line.getNote(c).a.syllable)/2+line.getNote(c).getSyllableMovement()-2;
                    }
                    if(relation==Note.SyllableRelation.DASH){
                        g2.setStroke(dashStroke);
                        float dashPhase = dashStroke.getDashArray()[0]+dashStroke.getDashArray()[1];
                        int length = Math.round((float)Math.floor((endX-startX-dashStroke.getDashArray()[1])/dashPhase)*dashPhase+dashStroke.getDashArray()[0]);
                        int gap = (endX-startX-length)/2;
                        g2.drawLine(startX+gap, lyricsY-composition.getLyricsFont().getSize()/4, endX-gap, lyricsY-composition.getLyricsFont().getSize()/4);
                    }else if(relation==Note.SyllableRelation.EXTENDER){
                        g2.setStroke(underScoreStroke);
                        g2.drawLine(startX, lyricsY, endX, lyricsY);
                    }else if(relation==Note.SyllableRelation.ONEDASH){
                        g2.setStroke(longDashStroke);
                        float centerX = (endX-startX)/2f+startX;
                        g2.draw(new Line2D.Float(centerX-longDashWidth/2f, lyricsY-composition.getLyricsFont().getSize()/4, centerX+longDashWidth/2f, lyricsY-composition.getLyricsFont().getSize()/4));
                    }
                }

                //drawing the annotation
                if(note.getAnnotation()!=null){
                    g2.setFont(getAnnotationFont());
                    drawAntialiasedString(g2, note.getAnnotation().getAnnotation(), getAnnotationXPos(g2, note), getAnnotationYPos(l, note));
                }
            }

            //drawing beamings
            for(ListIterator<Interval> li = line.getBeamings().listIterator();li.hasNext();){
                Interval interval = li.next();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Line2D.Float beamLine;
                Note firstNote = line.getNote(interval.getA());
                Note lastNote = line.getNote(interval.getB());
                if(firstNote.isUpper()){
                    beamLine = new Line2D.Float(
                        firstNote.getXPos()+crotchetWidth,
                        ms.getNoteYPos(firstNote.getYPos(), l)-Note.HOTSPOT.y-firstNote.a.lengthening,
                        lastNote.getXPos()+crotchetWidth,
                        ms.getNoteYPos(lastNote.getYPos(), l)-Note.HOTSPOT.y-lastNote.a.lengthening);
                }else{
                    beamLine = new Line2D.Float(
                        firstNote.getXPos(),
                        ms.getNoteYPos(firstNote.getYPos(), l)+Note.HOTSPOT.y-firstNote.a.lengthening,
                        lastNote.getXPos()+1,
                        ms.getNoteYPos(lastNote.getYPos(), l)+Note.HOTSPOT.y-lastNote.a.lengthening);
                }
                Shape clip = g2.getClip();
                g2.setStroke(beamStroke);
                beamLine.setLine(beamLine.x1-10, beamLine.y1+10*(beamLine.y1-beamLine.y2)/(beamLine.x2-beamLine.x1),
                                            beamLine.x2+10, beamLine.y2-10*(beamLine.y1-beamLine.y2)/(beamLine.x2-beamLine.x1));
                drawBeaming(BEAMLEVELS.length-1, interval.getA(), interval.getB(), line, beamLine, g2, interval.getA(), interval.getB(), false);
                g2.setClip(clip);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //drawing the triplets
            for(ListIterator<Interval> li = line.getTriplets().listIterator();li.hasNext();){
                Interval iv = li.next();
                boolean odd = (iv.getB()-iv.getA()+1)%2==1;
                Note firstNote = line.getNote(iv.getA());
                int upper = firstNote.isUpper() ? -1 : 0;
                int lx = firstNote.getXPos()+(int)crotchetWidth;
                int ly = ms.getNoteYPos(firstNote.getYPos(), l)-Note.HOTSPOT.y+upper*firstNote.a.lengthening;
                ly-=5;

                int cx, cy;
                if(odd){
                    Note centerNote = line.getNote((iv.getB()-iv.getA())/2+iv.getA());
                    cx = centerNote.getXPos()+(int)crotchetWidth;
                    cy = ms.getNoteYPos(centerNote.getYPos(),l)-Note.HOTSPOT.y+upper*centerNote.a.lengthening;
                }else{
                    Note cn1 = line.getNote((iv.getB()-iv.getA())/2+iv.getA());
                    Note cn2 = line.getNote((iv.getB()-iv.getA())/2+iv.getA()+1);
                    cx = (cn2.getXPos()-cn1.getXPos())/2+cn1.getXPos()+(int)crotchetWidth;
                    cy = ms.getNoteYPos((cn2.getYPos()-cn1.getYPos())/2+cn1.getYPos(), l)-Note.HOTSPOT.y;
                }

                Note lastNote = line.getNote(iv.getB());
                int rx = lastNote.getXPos()+(int)crotchetWidth;
                int ry = ms.getNoteYPos(lastNote.getYPos(),l)-Note.HOTSPOT.y+upper*lastNote.a.lengthening;
                ry-=5;

                if(!firstNote.isUpper()){
                    lx-=(int)crotchetWidth/2;
                    ly+=Note.HOTSPOT.y-3;
                    cx-=(int)crotchetWidth/2;
                    cy+=Note.HOTSPOT.y-3;
                    rx-=(int)crotchetWidth/2;
                    ry+=Note.HOTSPOT.y-3;
                }
                g2.setStroke(lineStroke);
                CubicCurve2D triplet = new CubicCurve2D.Float(lx, ly, (float)(cx-lx)/4+lx, ly-10, (float)(rx-cx)*3/4+cx, ry-10, rx, ry);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape clip = g2.getClip();
                g2.setClip(lx, 0, cx-7-lx, Integer.MAX_VALUE);
                g2.draw(triplet);
                g2.setClip(cx+7, 0, rx-cx-7, Integer.MAX_VALUE);
                g2.draw(triplet);
                g2.setClip(clip);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setFont(tripletFont);
                drawAntialiasedString(g2, "3", cx-3, cy-8);

                /*g2.setColor(Color.red);
                g2.fill(new Rectangle2D.Double(triplet.getX1()-1, triplet.getY1()-1, 2, 2));
                g2.fill(new Rectangle2D.Double(triplet.getX2()-1, triplet.getY2()-1, 2, 2));
                g2.setColor(Color.orange);
                g2.fill(new Rectangle2D.Double(triplet.getCtrlX1()-1, triplet.getCtrlY1()-1, 2, 2));
                g2.fill(new Rectangle2D.Double(triplet.getCtrlX2()-1, triplet.getCtrlY2()-1, 2, 2));
                g2.setColor(Color.black);*/
            }

            //drawing the key signature changes
            if(l+1<composition.lineCount() && (composition.getLine(l+1).getKeys()!=line.getKeys() || composition.getLine(l+1).getKeyType()!=line.getKeyType())){
                Line nextLine = composition.getLine(l+1);
                if(nextLine.getKeyType()==line.getKeyType()){
                    keyTypes[0]=nextLine.getKeyType();keys[0]=nextLine.getKeys();froms[0]=0;isNaturals[0]=false;
                    if(nextLine.getKeys()>line.getKeys()){
                        keyTypes[1]=null;keys[1]=0;froms[1]=0;isNaturals[1]=false;
                    }else{
                        keyTypes[1]=line.getKeyType();keys[1]=line.getKeys()-nextLine.getKeys();froms[1]=nextLine.getKeys();isNaturals[1]=true;
                    }
                }else{
                    keyTypes[0]=line.getKeyType();keys[0]=line.getKeys();froms[0]=0;isNaturals[0]=true;
                    keyTypes[1]=nextLine.getKeyType();keys[1]=nextLine.getKeys();froms[1]=0;isNaturals[1]=false;
                }
                drawKeySignatureChange(g2, l, keyTypes, keys, froms, isNaturals);
            }

            //drawing the first-second endings
            for(ListIterator<Interval> li = line.getFsEndings().listIterator();li.hasNext();){
                Interval iv = li.next();
                int repeatRightPos = -1;
                for(int i=iv.getA();i<=iv.getB();i++){
                    if(line.getNote(i).getNoteType()==NoteType.REPEATRIGHT){
                        repeatRightPos = i;
                        break;
                    }
                }
                if(repeatRightPos!=iv.getA()){
                    drawEndings(g2, l, line.getNote(iv.getA()).getXPos(), repeatRightPos!=-1 ? line.getNote(repeatRightPos).getXPos() : line.getNote(iv.getB()).getXPos()+2*(int)crotchetWidth, "1.");
                }
                if(iv.getB()>repeatRightPos && repeatRightPos!=-1){
                    drawEndings(g2, l, line.getNote(repeatRightPos).getXPos()+10, line.getNote(iv.getB()).getXPos()+2*(int)crotchetWidth, "2.");
                }
            }
        }
    }

    public int getAnnotationYPos(int l, Note note) {
        return ms.getNoteYPos(0, l)+note.getAnnotation().getyPos();
    }

    public float getAnnotationXPos(Graphics2D g2, Note note) {
        Annotation a = note.getAnnotation();
        float xPos = note.getXPos()+crotchetWidth/2;
        if(a.getXalignment()==Component.CENTER_ALIGNMENT){
            xPos-=g2.getFontMetrics(getAnnotationFont()).stringWidth(a.getAnnotation())/2;
        }else if(a.getXalignment()==Component.RIGHT_ALIGNMENT){
            xPos-=g2.getFontMetrics(getAnnotationFont()).stringWidth(a.getAnnotation());
        }
        return xPos;
    }

    private Font oldGeneralFont, annotationFont;
    private Font getAnnotationFont(){
        if(oldGeneralFont!=ms.getComposition().getGeneralFont()){
            oldGeneralFont = ms.getComposition().getGeneralFont();
            annotationFont = oldGeneralFont.deriveFont(Font.ITALIC);
        }
        return annotationFont;
    }

    private int getHalfNoteWidthForTie(Note note){
        if(note.getNoteType()==NoteType.SEMIBREVE || note.getNoteType()==NoteType.MINIM){
            return note.getRealUpNoteRect().width/2;
        }else{
            return Math.round(crotchetWidth/2);
        }
    }

    private boolean isNoteTypeInLevel(NoteType nt, int level){
        for(int i=0;i<BEAMLEVELS.length;i++){
            if(BEAMLEVELS[i]==nt){
                return i<=level;
            }
        }
        return nt==NoteType.GRACEQUAVER;
    }

    private void drawBeaming(int level, int begin, int end, Line line, Line2D.Float beamLine, Graphics2D g2, int prevBegin, int prevEnd, boolean isPrevLeft){
        if(level==-1) return;
        boolean upper = line.getNote(begin).isUpper();
        //clipping
        if(begin==end){
            if(line.getNote(begin).getNoteType()==NoteType.GRACEQUAVER) return;
            float startBeamLineX;
            float endBeamLineX;
            if(upper){
                if(begin!=prevBegin || prevBegin==prevEnd && isPrevLeft){
                    startBeamLineX = line.getNote(begin).getXPos() - 1;
                    endBeamLineX = line.getNote(begin).getXPos() + crotchetWidth;
                }else{
                    startBeamLineX = line.getNote(begin).getXPos() + crotchetWidth;
                    endBeamLineX = line.getNote(begin).getXPos() + 2*crotchetWidth + 2;
                }
            }else{
                if(end!=prevEnd || prevBegin==prevEnd && !isPrevLeft){
                    startBeamLineX = line.getNote(end).getXPos();
                    endBeamLineX = line.getNote(end).getXPos() + crotchetWidth + 2;
                }else{
                    startBeamLineX = line.getNote(end).getXPos() - crotchetWidth - 2;
                    endBeamLineX = line.getNote(end).getXPos();
                }
            }
            g2.setClip(new Rectangle2D.Float(startBeamLineX, Math.min(beamLine.y1,beamLine.y2)-3,
                endBeamLineX-startBeamLineX, Math.abs(beamLine.y1-beamLine.y2)+6));
        }else{
            float startBeamLineX = line.getNote(begin).getXPos() + (upper ? crotchetWidth : 0) /*- stemStroke.getLineWidth()/2f*/;
            float endBeamLineX = line.getNote(end).getXPos() + (upper ? crotchetWidth : 0) + stemStroke.getLineWidth()/2f;
            g2.setClip(new Rectangle2D.Float(startBeamLineX, Math.min(beamLine.y1,beamLine.y2)-3,
                endBeamLineX-startBeamLineX, Math.abs(beamLine.y1-beamLine.y2)+6));
        }

        //drawing
        g2.draw(beamLine);

        //recursing
        float trans = upper ? beamTranslateY : -beamTranslateY;
        Line2D.Float subBeamLine = new Line2D.Float(beamLine.x1, beamLine.y1+trans, beamLine.x2, beamLine.y2+trans);
        int startSubBeam = -1;
        for(int i=begin;i<=end+1;i++){
            if(i<=end && isNoteTypeInLevel(line.getNote(i).getNoteType(), level-1)){
                if(startSubBeam==-1){
                    startSubBeam = i;
                }
            }else if(startSubBeam!=-1){
                drawBeaming(level-1, startSubBeam, i-1, line, subBeamLine, g2, begin, end,
                        upper && (begin != prevBegin || prevBegin == prevEnd && isPrevLeft) || !upper && end == prevEnd && (prevBegin != prevEnd || isPrevLeft));
                startSubBeam = -1;
            }
        }
    }

    public void drawGlissando(Graphics2D g2, int xIndex, int yPos, int l){
        Line line = ms.getComposition().getLine(l);
        int x1 = line.getNote(xIndex).getXPos()+15;
        int x2;
        if(xIndex+1<line.noteCount()){
            x2 = line.getNote(xIndex+1).getXPos()-3;
            int accNum = line.getNote(xIndex+1).getAccidental().ordinal();
            x2-=line.getNote(xIndex+1).isAccidentalInParenthesis() ? accidentalParenthesisWidths[accNum] : accidentalWidths[accNum];
        }else{
            x2 = x1 + 30;
        }
        if(line.getNote(xIndex).getNoteType()==NoteType.SEMIBREVE){
            x1+=3;
        }else if(line.getNote(xIndex).getNoteType()==NoteType.GRACEQUAVER){
            x1-=3;
        }
        x1+=line.getNote(xIndex).getDotted()*6;

        drawGlissando(g2, x1, ms.getNoteYPos(line.getNote(xIndex).getYPos(),l), x2, ms.getNoteYPos(yPos, l));

        g2.setStroke(lineStroke);
        //drawing the stave-longitude
        if (Math.abs(yPos) > 5 && (xIndex+1==line.noteCount() ||
                Math.abs(line.getNote(xIndex+1).getYPos())<Math.abs(yPos))) {
            for (int i = yPos + (yPos % 2 == 0 ? 0 : yPos > 0 ? -1 : 1); Math.abs(i) > 5; i += yPos > 0 ? -2 : 2)
                g2.drawLine(x2-5, ms.getNoteYPos(i, l),
                        x2+5, ms.getNoteYPos(i, l));
        }
    }

    private void drawGlissando(Graphics2D g2, int x1, int y1, int x2, int y2){
        double l = Math.sqrt(Math.abs(x1-x2)*Math.abs(x1-x2)+Math.abs(y1-y2)*Math.abs(y1-y2));
        int m = (int)Math.round(l/glissandoLength);
        if(m==0)m=1;
        g2.setFont(fughetta);
        AffineTransform at = g2.getTransform();
        g2.translate(x1, y1+2.25d);
        g2.rotate(Math.atan((double)(y2-y1)/(double)(x2-x1)));
        double scale = l/glissandoLength/m;
        g2.scale(scale, 1d);

        for(int i=0;i<m;i++){
            drawAntialiasedString(g2, GLISSANDO, (int)Math.round(i*glissandoLength), 0);
        }
        g2.setTransform(at);
    }

    private void drawTempoChange(Graphics2D g2, Tempo tempo, int line, int note){
        Line l = ms.getComposition().getLine(line);
        Note n = l.getNote(note);
        int yPos = ms.getMiddleLine()+l.getTempoChangeYPos()+line*ms.getRowHeight();
        StringBuilder tempoBuilder = new StringBuilder(25);
        if(tempo.isShowTempo()){
            drawTempoChangeNote(g2, tempo.getTempoType().getNote(), n.getXPos(), yPos);            
            tempoBuilder.append("= ");
            tempoBuilder.append(tempo.getVisibleTempo());
            tempoBuilder.append(' ');
        }
        tempoBuilder.append(tempo.getTempoDescription());
        g2.setFont(ms.getComposition().getGeneralFont());
        drawAntialiasedString(g2, tempoBuilder.toString(),
                n.getXPos()+(tempo.isShowTempo()?crotchetWidth+5+tempo.getTempoType().getNote().getDotted()*6:0),
                        yPos);        
    }

    private void drawEndings(Graphics2D g2, int line, int x1, int x2, String str){
        int y = ms.getNoteYPos(0, line)+ms.getComposition().getLine(line).getFsEndingYPos();
        int height = fsEndingFont.getSize()+2;
        g2.setStroke(stemStroke);
        g2.drawLine(x1, y, x1, y-height);
        g2.drawLine(x1, y-height, x2, y-height);
        g2.setFont(fsEndingFont);
        drawAntialiasedString(g2, str, x1+4, y);
    }

    protected void drawArticulation(Graphics2D g2, Note note, int line){
        //todo exact y2 for all articulations
        //drawing the accent
        int xPos = note.getXPos();
        if(note.getForceArticulation()==ForceArticulation.ACCENT){
            int x1 = xPos;
            int x2 = xPos+(int)crotchetWidth+2;
            int y;
            if(note.isUpper()){
                if(note.getYPos()<=3){
                    y = ms.getNoteYPos(6, line);
                }else{
                    y = ms.getNoteYPos(note.getYPos()+3, line);
                }
            }else{
                if(note.getYPos()>=-3){
                    y = ms.getNoteYPos(-6, line);
                }else{
                    y = ms.getNoteYPos(note.getYPos()-3, line);
                }
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawLine(x1, y-3, x2, y);
            g2.drawLine(x1, y+3, x2, y);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        int dir = note.isUpper() ? 1 : -1;
        int durY = ms.getNoteYPos(note.getYPos()+dir*2+dir*(1-note.getYPos()%2), line);
        if(note.getDurationArticulation()==DurationArticulation.STACCATO){
            AffineTransform at = g2.getTransform();
            g2.translate(xPos+getHalfNoteWidthForTie(note)-2, durY-2);
            g2.fill(staccatoEllipse);
            g2.setTransform(at);
        }else if(note.getDurationArticulation()==DurationArticulation.TENUTO){
            g2.setStroke(tenutoStroke);
            float width = note.getNoteType()==NoteType.SEMIBREVE || note.getNoteType()==NoteType.MINIM ? note.getRealUpNoteRect().width : crotchetWidth;
            g2.draw(new Line2D.Float(xPos, durY, xPos+width, durY));
        }
    }

    protected void drawAntialiasedString(Graphics2D g2, String str, int x, int y){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawString(str, x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    protected void drawAntialiasedString(Graphics2D g2, String str, float x, float y){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawString(str, x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    protected void drawAntialiasedStringZoomed(Graphics2D g2, String str, int x, int y, float zoom){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawString(str, x/zoom, y/zoom);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    protected void drawAntialiasedStringZoomed(Graphics2D g2, String str, float x, float y, float zoom){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawString(str, x/zoom, y/zoom);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawTextBox(Graphics2D g2, String str, int y, float xAlignment, int xTranslate){
        Vector<String> rightVector = new Vector<String>(4);
        int prevIndex = 0;
        int maxWidth = 0;
        for(int i=0;i<=str.length();i++){
            if(i==str.length() || str.charAt(i)=='\n' && i<str.length()-1){
                String tmp = str.substring(prevIndex, i);
                rightVector.add(tmp);
                int thisWidth = g2.getFontMetrics().stringWidth(tmp);
                if(maxWidth<thisWidth){
                    maxWidth = thisWidth;
                }
                prevIndex = i+1;
            }
        }
        int x=0;
        if(xAlignment==Component.RIGHT_ALIGNMENT){
            x =  ms.getComposition().getLineWidth()-maxWidth;
        }else if(xAlignment==Component.CENTER_ALIGNMENT){
            x = (ms.getComposition().getLineWidth()-maxWidth)/2;
        }
        x+=xTranslate;
        for(int i=0;i<rightVector.size();i++){
            drawAntialiasedString(g2, rightVector.get(i), x, y+i*g2.getFontMetrics().getAscent());
        }
    }

    public abstract void paintNote(Graphics2D g2, Note note, int line, boolean beamed, Color color);

    protected abstract void drawLineBeginning(Graphics2D g2, Line line, int l);

    protected abstract void drawKeySignatureChange(Graphics2D g2, int l, KeyType[] keyTypes, int[] keys, int[] froms, boolean[] isNatural);

    protected abstract void drawTempoChangeNote(Graphics2D g2, Note tempoNote, int x, int y);
}
