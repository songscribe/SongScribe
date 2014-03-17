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

Created on Mar 26, 2006
*/
package songscribe.ui.adjustment;

import songscribe.data.CrescendoDiminuendoIntervalData;
import songscribe.data.Interval;
import songscribe.data.IntervalSet;
import songscribe.music.*;
import songscribe.ui.MusicSheet;

import java.awt.*;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class NoteXPosAdjustment extends Adjustment{
    private enum AdjustType{
        ONENOTE(Color.white,  -1),
        WHOLENOTE(Color.blue, -4),
        STRETCH(Color.yellow, -4),
        FIRSTXPOS(Color.green, -4),
        GLISSANDOX1(Color.magenta, -2),
        GLISSANDOX2(Color.magenta, -2),
        GRACESEMIQUAVER2NDPART(Color.white, -1),
        CRESCENDOX1(Color.orange, 6),
        CRESCENDOX2(Color.orange, 6),
        DIMINUENDOX1(Color.orange, 6),
        DIMINUENDOX2(Color.orange, 6);



        private Color color;
        private int yPos;

        AdjustType(Color color, int yPos) {
            this.color = color;
            this.yPos = yPos;
        }

        public Color getColor() {
            return color;
        }

        public int getyPos() {
            return yPos;
        }
    }
    private class AdjustRect{
        Rectangle rectangle;
        int line, xIndex;
        AdjustType adjustType;

        public AdjustRect(int line, int xIndex, AdjustType adjustType) {
            this.line = line;
            this.xIndex = xIndex;
            this.adjustType = adjustType;
            rectangle = new Rectangle();
            getRectangle(this);
        }
    }
    private static final int ENDSNAPLIMIT = 30;

    private ArrayList<AdjustRect> adjustRects = new ArrayList<AdjustRect>();
    private AdjustRect draggingRect;
    private float[] stretchHelper;

    public NoteXPosAdjustment(MusicSheet musicSheet) {
        super(musicSheet);
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
            if(draggingRect.adjustType==AdjustType.ONENOTE){
                upLeftDragBounds.setLocation((draggingRect.xIndex>0 ? line.getNote(draggingRect.xIndex-1).getXPos() : 20)+draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.xIndex<line.noteCount()-1 ? line.getNote(draggingRect.xIndex+1).getXPos()-draggingRect.rectangle.width : musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }else if(draggingRect.adjustType==AdjustType.WHOLENOTE){
                upLeftDragBounds.setLocation(line.getNote(draggingRect.xIndex-1).getXPos()+draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.rectangle.x-draggingRect.rectangle.width+musicSheet.getComposition().getLineWidth()-line.getNote(line.noteCount()-1).getXPos(), draggingRect.rectangle.y);
            }else if(draggingRect.adjustType==AdjustType.STRETCH){
                upLeftDragBounds.setLocation(line.getNote(0).getXPos(), draggingRect.rectangle.y);
                downRightDragBounds.setLocation(musicSheet.getComposition().getLineWidth()+draggingRect.rectangle.width, draggingRect.rectangle.y);
                if(stretchHelper==null || stretchHelper.length<line.noteCount()){
                    stretchHelper = new float[line.noteCount()];
                }
                for(int i=0;i<line.noteCount();i++){
                    stretchHelper[i] = line.getNote(i).getXPos();
                }
            }else if(draggingRect.adjustType==AdjustType.FIRSTXPOS){
                upLeftDragBounds.setLocation(draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.rectangle.x-draggingRect.rectangle.width+musicSheet.getComposition().getLineWidth()-line.getNote(line.noteCount()-1).getXPos(), draggingRect.rectangle.y);
            }else if(draggingRect.adjustType==AdjustType.GLISSANDOX1){
                upLeftDragBounds.setLocation(line.getNote(draggingRect.xIndex).getXPos(), draggingRect.rectangle.y);
                downRightDragBounds.setLocation(adjustRects.get(adjustRects.indexOf(draggingRect)+1).rectangle.x, draggingRect.rectangle.y);
            }else if(draggingRect.adjustType==AdjustType.GLISSANDOX2){
                upLeftDragBounds.setLocation(adjustRects.get(adjustRects.indexOf(draggingRect)-1).rectangle.x+draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }else if(draggingRect.adjustType == AdjustType.CRESCENDOX1 || draggingRect.adjustType == AdjustType.CRESCENDOX2 ||
                    draggingRect.adjustType == AdjustType.DIMINUENDOX1 || draggingRect.adjustType == AdjustType.DIMINUENDOX2){
                upLeftDragBounds.setLocation(draggingRect.xIndex == 0 ? 0 : line.getNote(draggingRect.xIndex - 1).getXPos(),
                        draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.xIndex == line.noteCount() - 1 ?
                        musicSheet.getComposition().getLineWidth() :
                        line.getNote(draggingRect.xIndex + 1).getXPos(), draggingRect.rectangle.y);
            }else if(draggingRect.adjustType==AdjustType.GRACESEMIQUAVER2NDPART){
                upLeftDragBounds.setLocation(line.getNote(draggingRect.xIndex).getXPos()+draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.xIndex<line.noteCount()-1 ? line.getNote(draggingRect.xIndex+1).getXPos() - draggingRect.rectangle.width : musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }
        }
    }

    protected void drag() {
        if(draggingRect==null)return;
        Line line = musicSheet.getComposition().getLine(draggingRect.line);
        Note note = line.getNote(draggingRect.xIndex);
        if(note.getNoteType().snapToEnd() && musicSheet.getComposition().getLineWidth()-endPoint.x<ENDSNAPLIMIT)endPoint.x=musicSheet.getComposition().getLineWidth()-Note.NORMALIMAGEWIDTH;
        int diffX = draggingRect.rectangle.x+draggingRect.rectangle.width/2;
        draggingRect.rectangle.x=endPoint.x-draggingRect.rectangle.width/2;
        if(draggingRect.adjustType==AdjustType.STRETCH){
            float ratio = (float)endPoint.x/note.getXPos();
            int firstX = line.getNote(0).getXPos();
            for(int i=0;i<line.noteCount();i++){
                stretchHelper[i]=firstX+(stretchHelper[i]-firstX)*ratio;
                line.getNote(i).setXPos(Math.round(stretchHelper[i]));
            }
            line.mulNoteDistChange(ratio);
        }else if(draggingRect.adjustType==AdjustType.WHOLENOTE){
            for(int i=draggingRect.xIndex;i<line.noteCount();i++){
                line.getNote(i).setXPos(line.getNote(i).getXPos()+endPoint.x-diffX);
            }
        }else if(draggingRect.adjustType==AdjustType.ONENOTE){
            note.setXPos(endPoint.x);
        }else if(draggingRect.adjustType==AdjustType.FIRSTXPOS){
            MusicSheet.setStartLocX(endPoint.x);
            Composition c = musicSheet.getComposition();
            for(int i=0;i<c.lineCount();i++){
                Line l=c.getLine(i);
                if(l.noteCount()>0){
                    int diff = endPoint.x-l.getNote(0).getXPos();
                    l.getNote(0).setXPos(endPoint.x);
                    for(int j=1;j<l.noteCount();j++){
                        l.getNote(j).setXPos(l.getNote(j).getXPos()+diff);
                    }
                }
            }
        }else if(draggingRect.adjustType==AdjustType.GLISSANDOX1){
            note.getGlissando().x1Translate+=endPoint.x-diffX;

        }else if(draggingRect.adjustType==AdjustType.GLISSANDOX2){
            note.getGlissando().x2Translate-=endPoint.x-diffX;
        }else if(draggingRect.adjustType==AdjustType.GRACESEMIQUAVER2NDPART){
            ((GraceSemiQuaver) note).setX2DiffPos(endPoint.x - note.getXPos());
        }else if(draggingRect.adjustType==AdjustType.CRESCENDOX1 || draggingRect.adjustType==AdjustType.DIMINUENDOX1){
            Interval interval = getCresDecrIntervalSet(line, draggingRect.adjustType).findInterval(draggingRect.xIndex);
            CrescendoDiminuendoIntervalData.setX1Shift(interval, CrescendoDiminuendoIntervalData.getX1Shift(interval) + endPoint.x-diffX);
        }else if(draggingRect.adjustType==AdjustType.CRESCENDOX2 || draggingRect.adjustType==AdjustType.DIMINUENDOX2){
            Interval interval = getCresDecrIntervalSet(line, draggingRect.adjustType).findInterval(draggingRect.xIndex);
            CrescendoDiminuendoIntervalData.setX2Shift(interval, CrescendoDiminuendoIntervalData.getX2Shift(interval) + endPoint.x-diffX);
        }
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
                //adding ONENOTE
                for(int n=0;n<line.noteCount();n++){
                    adjustRects.add(new AdjustRect(l, n, AdjustType.ONENOTE));
                }
                
                //adding WHOLENOTE
                for(int n=1;n<line.noteCount()-1;n++){
                    adjustRects.add(new AdjustRect(l, n, AdjustType.WHOLENOTE));
                }

                //adding special this: GLISSANDO, GRACESEMIQUAVER
                for(int n=0;n<line.noteCount();n++){
                    if(line.getNote(n).getGlissando()!=Note.NOGLISSANDO){
                        adjustRects.add(new AdjustRect(l, n, AdjustType.GLISSANDOX1));
                        adjustRects.add(new AdjustRect(l, n, AdjustType.GLISSANDOX2));
                    }
                    if (line.getNote(n).getNoteType() == NoteType.GRACESEMIQUAVER) {
                        adjustRects.add(new AdjustRect(l, n, AdjustType.GRACESEMIQUAVER2NDPART));
                    }
                }

                //adding STRETCH
                if(line.noteCount()>0)adjustRects.add(new AdjustRect(l, line.noteCount()-1, AdjustType.STRETCH));

                //adding CRESCENDO
                for(ListIterator<Interval> li = line.getCrescendo().listIterator();li.hasNext();){
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, interval.getA(), AdjustType.CRESCENDOX1));
                    adjustRects.add(new AdjustRect(l, interval.getB(), AdjustType.CRESCENDOX2));
                }

                //adding DIMINUENDO
                for(ListIterator<Interval> li = line.getDiminuendo().listIterator();li.hasNext();){
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, interval.getA(), AdjustType.DIMINUENDOX1));
                    adjustRects.add(new AdjustRect(l, interval.getB(), AdjustType.DIMINUENDOX2));
                }
            }

            //adding FIRSTNOTE
            if(c.getLine(0).noteCount()>0)adjustRects.add(new AdjustRect(0, 0, AdjustType.FIRSTXPOS));
        }else{
            adjustRects.clear();
        }
    }

    private void getRectangle(AdjustRect ar){
        Line line = musicSheet.getComposition().getLine(ar.line);
        Note note = line.getNote(ar.xIndex);
        ar.rectangle.y = musicSheet.getNoteYPos(ar.adjustType.getyPos(), ar.line)-Note.HOTSPOT.y;
        switch(ar.adjustType){
            case GLISSANDOX1:
                ar.rectangle.x = musicSheet.getDrawer().getGlissandoX1Pos(ar.xIndex, note.getGlissando(), ar.line)-4;
                break;
            case GLISSANDOX2:
                ar.rectangle.x = musicSheet.getDrawer().getGlissandoX2Pos(ar.xIndex, note.getGlissando(), ar.line)-4;
                break;
            case GRACESEMIQUAVER2NDPART:
                ar.rectangle.x = note.getXPos() + ((GraceSemiQuaver) note).getX2DiffPos() + 1;
                break;
            case CRESCENDOX1:
            case DIMINUENDOX1:
                Interval x1Interval = getCresDecrIntervalSet(line, ar.adjustType).findInterval(ar.xIndex);
                ar.rectangle.x = line.getNote(ar.xIndex).getXPos()-12+CrescendoDiminuendoIntervalData.getX1Shift(x1Interval);
                ar.rectangle.y = musicSheet.getNoteYPos(6, ar.line)-4+CrescendoDiminuendoIntervalData.getYShift(x1Interval);
                break;
            case CRESCENDOX2:
            case DIMINUENDOX2:
                Interval x2Interval = getCresDecrIntervalSet(line, ar.adjustType).findInterval(ar.xIndex);
                ar.rectangle.x = line.getNote(ar.xIndex).getXPos()+16+CrescendoDiminuendoIntervalData.getX2Shift(x2Interval);
                ar.rectangle.y = musicSheet.getNoteYPos(6, ar.line)-4+CrescendoDiminuendoIntervalData.getYShift(x2Interval);
                break;
            default:
                ar.rectangle.x = note.getXPos()+1;
        }

        ar.rectangle.width = ar.rectangle.height = 8;
    }

    private void revalidateRects(){
        for(AdjustRect ar: adjustRects){
            getRectangle(ar);
        }
    }

    private IntervalSet getCresDecrIntervalSet(Line line, AdjustType adjustType) {
        switch (adjustType) {
            case CRESCENDOX1:
            case CRESCENDOX2:
                return line.getCrescendo();
            case DIMINUENDOX1:
            case DIMINUENDOX2:
                return line.getDiminuendo();
            default:
                throw new IllegalArgumentException(String.valueOf(adjustType));
        }
    }
}
