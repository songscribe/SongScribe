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

import songscribe.music.KeyType;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.Utilities;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Csaba Kávai
 */
public class MaestroMsDrawer extends BaseMsDrawer{    
    private static final float size8 = size/8;
    private static final Font maestro = new Font("Maestro", 0, (int)size);

    private static final Map<NoteType, String> upNoteMap = new HashMap<NoteType, String>(10);
    private static final Map<NoteType, String> downNoteMap = new HashMap<NoteType, String>(10);
    private static final Map<NoteType, String> noteMap = new HashMap<NoteType, String>(10);
    static{
        upNoteMap.put(NoteType.SEMIBREVE, "\uf077");
        upNoteMap.put(NoteType.MINIM, "\uf068");
        upNoteMap.put(NoteType.CROTCHET, "\uf071");
        upNoteMap.put(NoteType.QUAVER, "\uf065");
        upNoteMap.put(NoteType.SEMIQUAVER, "\uf078");
        upNoteMap.put(NoteType.DEMISEMIQUAVER, "\uf078");
        upNoteMap.put(NoteType.GRACEQUAVER, "\uf0c9");

        downNoteMap.put(NoteType.SEMIBREVE, "\uf077");
        downNoteMap.put(NoteType.MINIM, "\uf048");
        downNoteMap.put(NoteType.CROTCHET, "\uf051");
        downNoteMap.put(NoteType.QUAVER, "\uf045");
        downNoteMap.put(NoteType.SEMIQUAVER, "\uf058");
        downNoteMap.put(NoteType.DEMISEMIQUAVER, "\uf058");
        downNoteMap.put(NoteType.GRACEQUAVER, "\uf0c9");

        noteMap.put(NoteType.SEMIBREVEREST, "\uf0ee");
        noteMap.put(NoteType.MINIMREST, "\uf0ee");
        noteMap.put(NoteType.CROTCHETREST, "\uf0ce");
        noteMap.put(NoteType.QUAVERREST, "\uf0e4");
        noteMap.put(NoteType.SEMIQUAVERREST, "\uf0c5");
        noteMap.put(NoteType.DEMISEMIQUAVERREST, "\uf0a8");
        noteMap.put(NoteType.REPEATLEFT, "\uf05d");
        noteMap.put(NoteType.REPEATRIGHT, "\uf07d");
        noteMap.put(NoteType.SINGLEBARLINE, "\uf05c");
    }

    private static final String trebleclef = "\uf026";
    private static final String[] naturalFlatSharp = {"\uf06e", "\uf062", "\uf023"};
    private static final String[] naturalFlatSharpParenthesis = {"\uf04e", "\uf041", "\uf061"};
    private static final String doubleFlat = "\uf0ba";
    private static final String doubleFlatParenthesis = "\uf08c";
    private static final String crotchetHead = "\uf0cf";
    private static final String dot = "\uf06b";
    private static final String extraFlagUp = "\uf091";
    private static final String extraFlagDown = "\uf093";

    public MaestroMsDrawer(MusicSheet ms) throws FontFormatException, IOException {
        super(ms);
        crotchetWidth = 10;
        if(Utilities.arrayIndexOf(MainFrame.FONTFAMILIES, "Maestro")==-1){
            throw new IOException();
        }
    }

    public void paintNote(Graphics2D g2, Note note, int line, boolean beamed, Color color) {
        int xPos = note.getXPos();
        int yPos = ms.getNoteYPos(note.getYPos(), line);
        g2.setFont(maestro);
        g2.setPaint(color);

        //drawing the note
        int helpPos;
        if(note.getNoteType()==NoteType.SINGLEBARLINE){
            helpPos = ms.getNoteYPos(4, line);
        }else if(note.getNoteType()==NoteType.SEMIBREVE || note.getNoteType()==NoteType.GRACEQUAVER || beamed || note.getNoteType().isRest()){
            helpPos = yPos;
        }else{
            helpPos = yPos+(int)size8;
        }
        String noteString;
        if(beamed){
            noteString = crotchetHead;
        }else if(!note.getNoteType().isNote()){
            noteString = noteMap.get(note.getNoteType());
        }else if(note.isUpper()){
            noteString = upNoteMap.get(note.getNoteType());
        }else {
            noteString = downNoteMap.get(note.getNoteType());
        }
        if(note.getNoteType().isRepeat()){
            helpPos = ms.getNoteYPos(4, line);
            g2.setFont(fughetta);
            if(note.getNoteType()==NoteType.REPEATLEFTRIGHT){
                drawAntialiasedString(g2, noteMap.get(NoteType.REPEATRIGHT), xPos, helpPos);
                drawAntialiasedString(g2, noteMap.get(NoteType.REPEATLEFT), xPos+10, helpPos);
            }else{
                drawAntialiasedString(g2, noteString, xPos, helpPos);
            }
            g2.setFont(maestro);
        }else{
            drawAntialiasedString(g2, noteString, xPos, helpPos);
            if(note.getNoteType()==NoteType.DEMISEMIQUAVER && !beamed){
                if(note.isUpper()){
                    drawAntialiasedString(g2, extraFlagUp, xPos+9.625f, helpPos-27.125f);
                }else{
                    drawAntialiasedString(g2, extraFlagDown, xPos+0.125f, helpPos+24.625f);
                }
            }
        }

        g2.setStroke(lineStroke);
        //drawing the stave-longitude
        if (Math.abs(note.getYPos()) > 5) {
            for (int i = note.getYPos() + (note.getYPos() % 2 == 0 ? 0 : (note.getYPos() > 0 ? -1 : 1)); Math.abs(i) > 5; i += note.getYPos() > 0 ? -2 : 2)
                g2.drawLine(xPos + Note.HOTSPOT.x - 9, ms.getNoteYPos(i, line),
                        xPos + Note.HOTSPOT.x + 9, ms.getNoteYPos(i, line));
        }

        //drawing the dottes
        for(int i=0;i<note.getDotted();i++){
            drawAntialiasedString(g2, dot, xPos+13+i*6, (note.getYPos()%2==0 ? yPos+(note.isUpper()?-1:1)*(int)MusicSheet.HALFLINEDIST: yPos)+6);
        }

        //drawing prefixes
        Note.Accidental accidental = note.getAccidental();
        if(accidental.getNb()==1){
            if(note.isAccidentalInParenthesis()){
                drawAntialiasedString(g2, naturalFlatSharpParenthesis[accidental.getComponent(0)], xPos-15, yPos);
            }else{
                drawAntialiasedString(g2, naturalFlatSharp[accidental.getComponent(0)], xPos-9, yPos);
            }
        }else if(accidental.getNb()==2){
            if(accidental==Note.Accidental.DOUBLEFLAT){
                if(note.isAccidentalInParenthesis()){
                    drawAntialiasedString(g2, doubleFlatParenthesis, xPos-22, yPos);
                }else{
                    drawAntialiasedString(g2, doubleFlat, xPos-15, yPos);
                }
            }else{
                //todo parenthesis
                drawAntialiasedString(g2, naturalFlatSharp[accidental.getComponent(0)], xPos-17, yPos);
                drawAntialiasedString(g2, naturalFlatSharp[accidental.getComponent(1)], xPos-9, yPos);
            }
        }

        //drawing the lengthening
        if(beamed){
            if(note.isUpper()){
                g2.draw(new Line2D.Double(xPos+crotchetWidth, yPos-0.5f, xPos+crotchetWidth, yPos-Note.HOTSPOT.y-note.a.lengthening-1.5f));
            }else{
                g2.draw(new Line2D.Double(xPos, yPos+0.5f, xPos, yPos+Note.HOTSPOT.y-note.a.lengthening+1.5f));
            }
        }

        //drawing the glissando
        if(note.getGlissando()!=Note.NOGLISSANDO){
            drawGlissando(g2, ms.getComposition().getLine(line).getNoteIndex(note), note.getGlissando(), line);
        }

        //drawing the articulations
        drawArticulation(g2, note, line);

        g2.setPaint(Color.black);
    }

    protected void drawLineBeginning(Graphics2D g2, Line line, int l) {
        g2.setFont(maestro);
        //drawing the trebleClef
        drawAntialiasedString(g2, trebleclef, 5, ms.getMiddleLine()+MusicSheet.LINEDIST+l*ms.getRowHeight());

        //drawing the leading sharps or flats
        if(line.getKeys()>0){
            int fsPos = ms.getLeadingKeysPos();
            int fs = line.getKeyType().ordinal();
            for(int i=0;i<line.getKeys();i++){
                drawAntialiasedString(g2, naturalFlatSharp[fs], fsPos, ms.getNoteYPos(FLATSHARPORDER[fs][i%7], l));
                fsPos+=8;
            }
        }
    }

    protected void drawKeySignatureChange(Graphics2D g2, int l, KeyType[] keyTypes, int[] keys, int[] froms, boolean[] isNatural) {
    }

    protected void drawTempoChangeNote(Graphics2D g2, Note tempoNote, int x, int y) {
        int helpY = tempoNote.getNoteType()==NoteType.SEMIBREVE ? y-(int)size8 : y;
        g2.setFont(maestro);
        drawAntialiasedStringZoomed(g2, upNoteMap.get(tempoNote.getNoteType()), x, helpY, (float)tempoChangeZoomY);
        for(int i=0;i<tempoNote.getDotted();i++){
            drawAntialiasedStringZoomed(g2, dot, x+12+i*6, y+3, (float)tempoChangeZoomY);
        }
    }
}
