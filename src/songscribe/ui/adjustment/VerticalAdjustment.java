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

Created on Jul 22, 2006
*/
package songscribe.ui.adjustment;

import songscribe.ui.MusicSheet;
import songscribe.music.Composition;
import songscribe.music.Line;
import songscribe.music.Annotation;
import songscribe.music.Note;

import java.awt.*;
import java.util.Vector;

/**
 * @author Csaba Kávai
 */
public class VerticalAdjustment extends Adjustment{
    private enum AdjustType{
        RIGHTINFO(Color.blue),
        TOPSPACE(Color.cyan),
        ROWHEIGHT(Color.orange),
        TEMPOCHANGE(Color.red),
        BEATCHANGE(Color.pink),
        FSENDING(Color.green),
        ANNOTATION(Color.magenta),
        TRILL(Color.pink);

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

    public VerticalAdjustment(MusicSheet parent) {
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
            if(draggingRect.adjustType==AdjustType.RIGHTINFO){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, 0);
                downRightDragBounds.setLocation(draggingRect.rectangle.x, Integer.MAX_VALUE);
            }else if(draggingRect.adjustType==AdjustType.TOPSPACE){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, 0);
                downRightDragBounds.setLocation(draggingRect.rectangle.x, Integer.MAX_VALUE);
            }else if(draggingRect.adjustType==AdjustType.ROWHEIGHT){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(6, 0));
                downRightDragBounds.setLocation(draggingRect.rectangle.x, Integer.MAX_VALUE);
            }else if(draggingRect.adjustType==AdjustType.TEMPOCHANGE || draggingRect.adjustType==AdjustType.FSENDING || draggingRect.adjustType==AdjustType.TRILL || draggingRect.adjustType==AdjustType.BEATCHANGE){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(6, draggingRect.line-1));
                downRightDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(-4, draggingRect.line));
            }else if(draggingRect.adjustType==AdjustType.ANNOTATION){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(6, draggingRect.line-1));
                downRightDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(-6, draggingRect.line+1));
            }
        }
    }

    protected void drag() {
        if(draggingRect==null)return;
        int diffY = draggingRect.rectangle.y+draggingRect.rectangle.height/2;
        draggingRect.rectangle.y=endPoint.y-draggingRect.rectangle.height/2;
        if(draggingRect.adjustType==AdjustType.RIGHTINFO){
            musicSheet.getComposition().setRightInfoStartY(musicSheet.getComposition().getRightInfoStartY()+endPoint.y-diffY);
        }else if(draggingRect.adjustType==AdjustType.TOPSPACE){
            musicSheet.getComposition().setTopSpace(musicSheet.getComposition().getTopSpace()+endPoint.y-diffY, true);
        }else if(draggingRect.adjustType==AdjustType.ROWHEIGHT){
            musicSheet.getComposition().setRowHeight(musicSheet.getComposition().getRowHeight()+endPoint.y-diffY);
        }else if(draggingRect.adjustType==AdjustType.TEMPOCHANGE){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setTempoChangeYPos(line.getTempoChangeYPos()+endPoint.y-diffY);
        }else if(draggingRect.adjustType==AdjustType.BEATCHANGE){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setBeatChangeYPos(line.getBeatChangeYPos()+endPoint.y-diffY);
        }else if(draggingRect.adjustType==AdjustType.FSENDING){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setFsEndingYPos(line.getFsEndingYPos()+endPoint.y-diffY);
        }else if(draggingRect.adjustType==AdjustType.ANNOTATION){
            Annotation a = musicSheet.getComposition().getLine(draggingRect.line).getNote(draggingRect.xIndex).getAnnotation();
            a.setyPos(a.getyPos()+endPoint.y-diffY);
        }else if(draggingRect.adjustType==AdjustType.TRILL){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setTrillYPos(line.getTrillYPos()+endPoint.y-diffY);
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
            if(c.getRightInfo().length()>0)adjustRects.add(new AdjustRect(-1, AdjustType.RIGHTINFO, -1));
            if(c.lineCount()>0)adjustRects.add(new AdjustRect(0, AdjustType.TOPSPACE, -1));
            if(c.lineCount()>1)adjustRects.add(new AdjustRect(1, AdjustType.ROWHEIGHT, -1));
            for(int l=0;l<c.lineCount();l++){
                Line line = c.getLine(l);

                int firstTempoChange = line.getFirstTempoChange();
                if(firstTempoChange>-1)adjustRects.add(new AdjustRect(l, AdjustType.TEMPOCHANGE, firstTempoChange));

                for(int n=0;n<line.noteCount();n++){
                    if(line.getNote(n).getAnnotation()!=null){
                        adjustRects.add(new AdjustRect(l, AdjustType.ANNOTATION, n));
                    }
                }

                if(!line.getFsEndings().isEmpty()){
                    adjustRects.add(new AdjustRect(l, AdjustType.FSENDING, line.getFsEndings().listIterator().next().getA()));
                }

                int firstTrill = line.getFirstTrill();
                if(firstTrill>-1)adjustRects.add(new AdjustRect(l, AdjustType.TRILL, firstTrill));

                int firstBeatChange = line.getFirstBeatChange();
                if(firstBeatChange>-1)adjustRects.add(new AdjustRect(l, AdjustType.BEATCHANGE, firstBeatChange));
            }

        }else{
            adjustRects.clear();
        }
    }

    private void getRectangle(AdjustRect ar){
        if(ar.adjustType==AdjustType.RIGHTINFO){
            ar.rectangle.x = musicSheet.getSheetWidth()-8;
            ar.rectangle.y = musicSheet.getComposition().getRightInfoStartY();
        }else if(ar.adjustType==AdjustType.TOPSPACE || ar.adjustType==AdjustType.ROWHEIGHT){
            ar.rectangle.x = 0;
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)-4;
        }else if(ar.adjustType==AdjustType.TEMPOCHANGE){
            ar.rectangle.x = musicSheet.getComposition().getLine(ar.line).getNote(ar.xIndex).getXPos()-8;
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)+musicSheet.getComposition().getLine(ar.line).getTempoChangeYPos()-8;
        }else if(ar.adjustType==AdjustType.BEATCHANGE){
            ar.rectangle.x = musicSheet.getComposition().getLine(ar.line).getNote(ar.xIndex).getXPos()-8;
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)+musicSheet.getComposition().getLine(ar.line).getBeatChangeYPos()-8;
        }else if(ar.adjustType==AdjustType.FSENDING){
            ar.rectangle.x = musicSheet.getComposition().getLine(ar.line).getNote(ar.xIndex).getXPos()-12;
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)+musicSheet.getComposition().getLine(ar.line).getFsEndingYPos()-8;
        }else if(ar.adjustType==AdjustType.ANNOTATION){
            Note note = musicSheet.getComposition().getLine(ar.line).getNote(ar.xIndex);
            ar.rectangle.x = (int)Math.round(musicSheet.getDrawer().getAnnotationXPos((Graphics2D)musicSheet.getGraphics(), note))-8;
            ar.rectangle.y = musicSheet.getDrawer().getAnnotationYPos(ar.line, note)-8;
        }else if(ar.adjustType==AdjustType.TRILL){
            ar.rectangle.x = musicSheet.getComposition().getLine(ar.line).getNote(ar.xIndex).getXPos()-12;
            ar.rectangle.y = musicSheet.getNoteYPos(0, ar.line)+musicSheet.getComposition().getLine(ar.line).getTrillYPos()-8;
        }
        ar.rectangle.width = ar.rectangle.height = 8;
    }

    private void revalidateRects(){
        for(AdjustRect ar: adjustRects){
            getRectangle(ar);
        }
    }
}
