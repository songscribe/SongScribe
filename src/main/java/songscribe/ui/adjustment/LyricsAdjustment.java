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

Created on Jan 3, 2007
*/
package songscribe.ui.adjustment;

import songscribe.music.Composition;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.ui.Constants;
import songscribe.ui.MusicSheet;

import java.awt.*;
import java.util.Vector;

/**
 * @author Csaba Kávai
 */
public class LyricsAdjustment extends Adjustment{
       private enum AdjustType{
        SYLLABLEMOVEMENT(Color.green),
        SYLLABLERELATIONMOVEMENT(Color.green),
        LYRICSYPOS(Color.magenta);



        private Color color;

        AdjustType(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    private class AdjustRect{
        Rectangle rectangle;
        int line, xIndex;
        AdjustType adjustType;

        public AdjustRect(int line, AdjustType adjustType, int xIndex) {
            this.line = line;
            this.adjustType = adjustType;
            this.xIndex = xIndex;
            rectangle = new Rectangle();
            getRectangle(this);
        }
    }

    private AdjustRect draggingRect;
    private Vector<AdjustRect> adjustRects = new Vector<AdjustRect>(50, 30);

    public LyricsAdjustment(MusicSheet parent) {
        super(parent);
    }

    protected void startedDrag() {
        draggingRect = null;
        for(AdjustRect ar:adjustRects){
            if(ar.rectangle.contains(startPoint)){
                draggingRect = ar;
                break;
            }
        }
        if(draggingRect==null){
            startedDrag = false;
        }else{
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            if(draggingRect.adjustType==AdjustType.SYLLABLEMOVEMENT){
                upLeftDragBounds.setLocation((draggingRect.xIndex>0 ? line.getNote(draggingRect.xIndex-1).getXPos() : 0)+draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.xIndex<line.noteCount()-1 ? line.getNote(draggingRect.xIndex+1).getXPos() : musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }else if(draggingRect.adjustType==AdjustType.SYLLABLERELATIONMOVEMENT){
                upLeftDragBounds.setLocation(line.getNote(draggingRect.xIndex).getXPos(), draggingRect.rectangle.y);
                downRightDragBounds.setLocation(musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }else if(draggingRect.adjustType==AdjustType.LYRICSYPOS){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(6, draggingRect.line));
                downRightDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(-4, draggingRect.line+1));
            }
        }
    }

    protected void drag() {
        if(draggingRect==null)return;
        if(draggingRect.adjustType==AdjustType.SYLLABLEMOVEMENT){
            draggingRect.rectangle.x=endPoint.x-draggingRect.rectangle.width/2;
            Note note = musicSheet.getComposition().getLine(draggingRect.line).getNote(draggingRect.xIndex);
            note.setSyllableMovement(endPoint.x-note.getXPos());
        }else if(draggingRect.adjustType==AdjustType.SYLLABLERELATIONMOVEMENT){
            draggingRect.rectangle.x=endPoint.x-draggingRect.rectangle.width/2;
            Note note = musicSheet.getComposition().getLine(draggingRect.line).getNote(draggingRect.xIndex);
            note.setSyllableRelationMovement(endPoint.x - note.getXPos());
        }else if(draggingRect.adjustType==AdjustType.LYRICSYPOS){
            int diffY = draggingRect.rectangle.y+draggingRect.rectangle.height/2;
            draggingRect.rectangle.y=endPoint.y-draggingRect.rectangle.height/2;
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setLyricsYPos(line.getLyricsYPos()+endPoint.y-diffY);
        }
        musicSheet.viewChanged();
        musicSheet.getComposition().modifiedComposition();
        revalidateRects();
        musicSheet.setRepaintImage(true);
        musicSheet.repaint();
    }

    protected void finishedDrag() {
        draggingRect = null;
    }

    public void repaint(Graphics2D g2) {
        for(AdjustRect ar:adjustRects){
            g2.setPaint(ar.adjustType.getColor());
            g2.fill(ar.rectangle);
            g2.setPaint(Color.black);
            g2.draw(ar.rectangle);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled){
            Composition c = musicSheet.getComposition();
            for(int l=0;l<c.lineCount();l++){
                Line line = c.getLine(l);
                int foundLyrics = -1;
                for(int n=0;n<line.noteCount();n++){
                    if(line.getNote(n).a.syllable!=null && line.getNote(n).a.syllable.length()>0){
                        if(line.getNote(n).a.syllable!=Constants.UNDERSCORE){
                            adjustRects.add(new AdjustRect(l, AdjustType.SYLLABLEMOVEMENT, n));
                        }
                        if (line.getNote(n).a.syllableRelation == Note.SyllableRelation.ONEDASH) {
                            adjustRects.add(new AdjustRect(l, AdjustType.SYLLABLERELATIONMOVEMENT, n));
                        }
                        if(foundLyrics==-1)foundLyrics = n;
                    }
                }
                if(foundLyrics>-1){
                    adjustRects.add(new AdjustRect(l, AdjustType.LYRICSYPOS, foundLyrics));
                }
            }
        }else{
            adjustRects.clear();
        }
    }

    private void getRectangle(AdjustRect ar){
        Line line = musicSheet.getComposition().getLine(ar.line);
        Note note = line.getNote(ar.xIndex);
        if(ar.adjustType==AdjustType.LYRICSYPOS){
            ar.rectangle.x = note.getXPos()-20;
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)+line.getLyricsYPos()-8;
        }else if(ar.adjustType==AdjustType.SYLLABLEMOVEMENT){
            ar.rectangle.x = note.getXPos()+note.getSyllableMovement();
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)+line.getLyricsYPos()+5;
        }else if(ar.adjustType==AdjustType.SYLLABLERELATIONMOVEMENT){
            ar.rectangle.x = (note.getSyllableRelationMovement() == 0 ? (int) note.a.longDashPosition : note.getXPos() + note.getSyllableRelationMovement()) - 4;
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)+line.getLyricsYPos()+5;
        }
        ar.rectangle.width = ar.rectangle.height = 8;
    }

    private void revalidateRects(){
        for(AdjustRect ar: adjustRects){
            getRectangle(ar);
        }
    }
}
