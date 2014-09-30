/*
SongScribe song notation program
Copyright (C) 2006 Csaba Kavai

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

import songscribe.music.Crotchet;
import songscribe.music.GraceSemiQuaver;
import songscribe.music.KeyType;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * @author Csaba Kávai
 */
@Deprecated
/**
 * This class is set to deprecated, beacause we don't use anymore. We prefer the FughettaDrawer for edit mode, too.
 *
 */
public class ImageMsDrawer extends BaseMsDrawer{
    private static final Image TREBLECLEFIMAGE = MainFrame.getImage("trebleClef.gif");
    private static final Image BEGINPARENTHESISIMAGE = MainFrame.getImage("beginparenthesis.gif");
    private static final Image ENDPARENTHESISIMAGE = MainFrame.getImage("endparenthesis.gif");
    private static final Image FERMATAIMAGE = MainFrame.getImage("fermata32.png");
    private static final Dimension crotchetDim = new Dimension(Crotchet.REAL_UP_NOTE_RECT.width-1, Note.IMAGE_DIM.height);

    public ImageMsDrawer(MusicSheet ms) {
        super(ms);
        crotchetWidth = crotchetDim.width;
        beamX1Correction = 0.3;
        beamX2Correction = 0;
    }

    protected int drawLineBeginning(Graphics2D g2, Line line, int l) {
        //drawing the trebleClef
        int baseline = ms.getMiddleLine() - 32 + l * ms.getRowHeight();
        g2.drawImage(TREBLECLEFIMAGE, 5, baseline, null);
        FontMetrics metrics = g2.getFontMetrics();
        int maxY = baseline + metrics.getMaxDescent();

        //drawing the leading sharps or flats
        if(line.getKeys()>0){
            int fsPos = ms.getLeadingKeysPos();
            int fs = line.getKeyType().ordinal();
            for(int i=0;i<line.getKeys();i++){
                g2.drawImage(Note.NATURAL_FLAT_SHARP_IMAGE[fs], fsPos, ms.getNoteYPos(FLAT_SHARP_ORDER[fs][i%7], l)-Note.HOT_SPOT.y, null);
                fsPos+=8;
            }
        }

        return maxY;
    }

    protected void drawKeySignatureChange(Graphics2D g2, int l, KeyType[] keyTypes, int[] keys, int[] froms, boolean[] isNatural) {
        int fsPos = ms.getComposition().getLineWidth()-5;
        for(int key:keys)fsPos-=key*8;
        for(int kt=0;kt<keyTypes.length;kt++){
            if(keyTypes[kt]==null)break;
            int fs = keyTypes[kt].ordinal();
            for(int i=0;i<keys[kt];i++){
                g2.drawImage(Note.NATURAL_FLAT_SHARP_IMAGE[isNatural[kt] ? 0 : fs], fsPos, ms.getNoteYPos(FLAT_SHARP_ORDER[fs][(i+froms[kt])%7], l)-Note.HOT_SPOT.y, null);
                fsPos+=8;
            }
        }
    }

    public void paintNote(Graphics2D g2, Note note, int line, boolean beamed, Color color) {
        int xPos = note.getXPos();
        int yPos = ms.getNoteYPos(note.getYPos(), line)-Note.HOT_SPOT.y;
        Image noteImg;
        if(color==Color.black){
            if(beamed){
                noteImg = note.isUpper() ? Crotchet.UP_IMAGE : Crotchet.DOWN_IMAGE;
            }else{
                noteImg = note.isUpper() ? note.getUpImage() : note.getDownImage();
            }
        }else{
            noteImg = Note.getColoredNote(beamed ? NoteType.CROTCHET : note.getNoteType(), color, note.isUpper());
        }
        if (note.getNoteType() != NoteType.GRACE_SEMIQUAVER) {
            g2.drawImage(noteImg, xPos, yPos, null);
        } else {
            g2.drawImage(noteImg, xPos, ms.getNoteYPos(((GraceSemiQuaver)note).getY0Pos(), line)-Note.HOT_SPOT.y, null);
            g2.drawImage(noteImg, xPos + ((GraceSemiQuaver)note).getX2DiffPos(), yPos, null);
            g2.setPaint(color);
            drawGraceSemiQuaverBeam(g2, note, line);
            g2.setPaint(Color.black);
        }

        g2.setPaint(Color.black);
        g2.setStroke(lineStroke);
        //drawing the stave-longitude
        if (note.getNoteType().drawStaveLongitude()) {
            drawStaveLongitude(g2, note.getYPos(), line, xPos+Note.HOT_SPOT.x-8, xPos + Note.HOT_SPOT.x+(note.getNoteType()!=NoteType.SEMIBREVE ? 8 : 12));
        }

        g2.setPaint(color);

        //drawing the dots
        for(int i=0;i<note.getDotted();i++){
            g2.drawImage(color==Color.black ? Note.DOT_IMAGE : Note.getColoredImage(Note.DOT_IMAGE, color),
                    xPos+i*4+(note.getNoteType()==NoteType.SEMIBREVE ? 4 : 0),
                    note.getYPos()%2==0 ? yPos-(int)MusicSheet.HALF_LINE_DIST : yPos, null);
        }

        //drawing accidentals
        Note.Accidental accidental = note.getAccidental();
        float resizeFactor = note.getNoteType().isGraceNote() ? graceAccidentalResizeFactor : 1;
        float prefXPos = xPos-(int)spaceBtwNoteAndAccidental;
        if(note.isAccidentalInParenthesis()){
            prefXPos-=4;
            g2.drawImage(ENDPARENTHESISIMAGE, Math.round(prefXPos), yPos, null);
            float startX = xPos - spaceBtwNoteAndAccidental - FughettaDrawer.getAccidentalWidth(note);
            g2.drawImage(BEGINPARENTHESISIMAGE, Math.round(startX), yPos, null);
            float width = Note.REAL_NATURAL_FLAT_SHARP_RECT[accidental.getComponent(0)].width;
            if(accidental.getNb()==2)width+=spaceBtwTwoAccidentals+Note.REAL_NATURAL_FLAT_SHARP_RECT[accidental.getComponent(1)].width;
            prefXPos = (prefXPos-startX)/2f+width/2f+startX+2.5f;
        }
        for(int i=accidental.getNb()-1;i>=0;i--){
            prefXPos-=Note.REAL_NATURAL_FLAT_SHARP_RECT[accidental.getComponent(i)].width;
            Image prefixImg = color==Color.black ? Note.NATURAL_FLAT_SHARP_IMAGE[accidental.getComponent(i)] :
                    Note.getColoredImage(Note.NATURAL_FLAT_SHARP_IMAGE[accidental.getComponent(i)], color);
            g2.drawImage(prefixImg, Math.round(prefXPos), yPos, null);
            prefXPos-=spaceBtwTwoAccidentals;
        }

        /*Rectangle r1 = new Rectangle(note.getRealUpNoteRect()), r2 = new Rectangle(note.getRealDownNoteRect());
        r1.translate(xPos,yPos);
        r2.translate(xPos,yPos);
        g2.draw(ni.upperNote ? r1:r2);*/

        //drawing the lengthening
        if(beamed && note.a.lengthening!=0){
            if(note.isUpper()){
                g2.drawLine(xPos+crotchetDim.width, yPos, xPos+crotchetDim.width, yPos-note.a.lengthening);
            }else{
                g2.drawLine(xPos, yPos+crotchetDim.height, xPos, yPos+crotchetDim.height-note.a.lengthening);
            }
        }

        //drawing the glissando
        if(note.getGlissando()!=Note.NO_GLISSANDO){
            drawGlissando(g2, ms.getComposition().getLine(line).getNoteIndex(note), note.getGlissando(), line);
        }

        //drawing the articulations
        drawArticulation(g2, note, line);

        //drawing the fermata
        if(note.isFermata()){
            g2.drawImage(FERMATAIMAGE, xPos-5, ms.getNoteYPos(getFermataYPos(note), line), null);
        }

        g2.setPaint(Color.black);
     }

    protected void drawTempoChangeNote(Graphics2D g2, Note tempoNote, int x, int y) {
        AffineTransform at = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.translate(x, y-19);
        g2.scale(tempoChangeZoomX, tempoChangeZoomY);
        g2.drawImage(tempoNote.getUpImage(), 0, 0, null);
        for(int i=0;i<tempoNote.getDotted();i++){
            g2.drawImage(Note.DOT_IMAGE, i*4, 0, null);
        }
        g2.setTransform(at);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
    }
}
