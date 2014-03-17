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

import songscribe.data.*;
import songscribe.music.Annotation;
import songscribe.music.Composition;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.ui.MusicSheet;

import java.awt.*;
import java.util.ListIterator;
import java.util.ArrayList;

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
        TRILL(Color.pink),
        SLURPOS1(Color.orange),
        SLURPOS2(Color.orange),
        SLURCTRLY(Color.orange),
        CRESCENDOY(Color.green),
        DIMINUENDOY(Color.green),
        TUPLET(Color.pink);


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
    private ArrayList<AdjustRect> adjustRects = new ArrayList<AdjustRect>();

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
            }else if(draggingRect.adjustType==AdjustType.TEMPOCHANGE || draggingRect.adjustType==AdjustType.FSENDING
                    || draggingRect.adjustType==AdjustType.TRILL || draggingRect.adjustType==AdjustType.BEATCHANGE ){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(6, draggingRect.line-1));
                downRightDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(-4, draggingRect.line));
            }else if(draggingRect.adjustType==AdjustType.ANNOTATION){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(6, draggingRect.line-1));
                downRightDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(-6, draggingRect.line+1));
            }else if(draggingRect.adjustType==AdjustType.SLURCTRLY || draggingRect.adjustType == AdjustType.TUPLET ||
                    draggingRect.adjustType == AdjustType.CRESCENDOY || draggingRect.adjustType == AdjustType.DIMINUENDOY){
                upLeftDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(6, draggingRect.line-1));
                downRightDragBounds.setLocation(draggingRect.rectangle.x, musicSheet.getNoteYPos(-6, draggingRect.line+1));
            } else {
                upLeftDragBounds.setLocation(0, 0);
                downRightDragBounds.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        }
    }

    protected void drag() {
        if(draggingRect==null)return;
        int diffX = endPoint.x - draggingRect.rectangle.x+draggingRect.rectangle.width/2;
        int diffY = endPoint.y - draggingRect.rectangle.y+draggingRect.rectangle.height/2;
        draggingRect.rectangle.y=endPoint.y-draggingRect.rectangle.height/2;
        if(draggingRect.adjustType==AdjustType.RIGHTINFO){
            musicSheet.getComposition().setRightInfoStartY(musicSheet.getComposition().getRightInfoStartY()+diffY);
        }else if(draggingRect.adjustType==AdjustType.TOPSPACE){
            musicSheet.getComposition().setTopSpace(musicSheet.getComposition().getTopSpace()+diffY, true);
        }else if(draggingRect.adjustType==AdjustType.ROWHEIGHT){
            musicSheet.getComposition().setRowHeight(musicSheet.getComposition().getRowHeight()+diffY);
        }else if(draggingRect.adjustType==AdjustType.TEMPOCHANGE){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setTempoChangeYPos(line.getTempoChangeYPos()+diffY);
        }else if(draggingRect.adjustType==AdjustType.BEATCHANGE){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setBeatChangeYPos(line.getBeatChangeYPos()+diffY);
        }else if(draggingRect.adjustType==AdjustType.FSENDING){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setFsEndingYPos(line.getFsEndingYPos()+diffY);
        }else if(draggingRect.adjustType==AdjustType.ANNOTATION){
            Annotation a = musicSheet.getComposition().getLine(draggingRect.line).getNote(draggingRect.xIndex).getAnnotation();
            a.setyPos(a.getyPos() + diffY);
        }else if(draggingRect.adjustType==AdjustType.TRILL){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            line.setTrillYPos(line.getTrillYPos() + diffY);
        }else if(draggingRect.adjustType==AdjustType.CRESCENDOY || draggingRect.adjustType==AdjustType.DIMINUENDOY){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            Interval interval = getCresDecrIntervalSet(line, draggingRect.adjustType).findInterval(draggingRect.xIndex);
            CrescendoDiminuendoIntervalData.setYShift(interval, CrescendoDiminuendoIntervalData.getYShift(interval) + diffY);
        }else if(draggingRect.adjustType==AdjustType.SLURPOS1 || draggingRect.adjustType==AdjustType.SLURPOS2 || draggingRect.adjustType==AdjustType.SLURCTRLY){
            Line line = musicSheet.getComposition().getLine(draggingRect.line);
            Interval interval = line.getSlurs().findInterval(draggingRect.xIndex);
            SlurData slurData = new SlurData(interval.getData());
            switch (draggingRect.adjustType) {
                case SLURPOS1:
                    draggingRect.rectangle.x=endPoint.x-draggingRect.rectangle.width/2;
                    slurData.setxPos1(slurData.getxPos1()+diffX);
                    slurData.setyPos1(slurData.getyPos1()+diffY);
                    break;
                case SLURPOS2:
                    draggingRect.rectangle.x=endPoint.x-draggingRect.rectangle.width/2;
                    slurData.setxPos2(slurData.getxPos2()+diffX);
                    slurData.setyPos2(slurData.getyPos2()+diffY);
                    break;
                case SLURCTRLY:
                    slurData.setCtrly(slurData.getCtrly()+diffY);
            }
            interval.setData(slurData.toString());
        }else if(draggingRect.adjustType==AdjustType.TUPLET){
            Interval interval = musicSheet.getComposition().getLine(draggingRect.line).getTuplets().findInterval(draggingRect.xIndex);
            TupletIntervalData.setVerticalPosition(interval, TupletIntervalData.getVerticalPosition(interval) + diffY);
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

                for(ListIterator<Interval> li = line.getSlurs().listIterator();li.hasNext();){
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, AdjustType.SLURPOS1, interval.getA()));
                    adjustRects.add(new AdjustRect(l, AdjustType.SLURPOS2, interval.getB()));
                    adjustRects.add(new AdjustRect(l, AdjustType.SLURCTRLY, interval.getA()));
                }

                for(ListIterator<Interval> li = line.getCrescendo().listIterator();li.hasNext();){
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, AdjustType.CRESCENDOY, interval.getA()));
                }

                for(ListIterator<Interval> li = line.getDiminuendo().listIterator();li.hasNext();){
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, AdjustType.DIMINUENDOY, interval.getA()));
                }

                for(ListIterator<Interval> li = line.getTuplets().listIterator();li.hasNext();){
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, AdjustType.TUPLET, interval.getA()));
                }
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
        }else if(ar.adjustType==AdjustType.CRESCENDOY || ar.adjustType==AdjustType.DIMINUENDOY){
            Line line = musicSheet.getComposition().getLine(ar.line);
            Interval interval = getCresDecrIntervalSet(line, ar.adjustType).findInterval(ar.xIndex);
            ar.rectangle.x = (line.getNote(interval.getA()).getXPos()+line.getNote(interval.getB()).getXPos()+12)/2;
            ar.rectangle.y = musicSheet.getNoteYPos(6, ar.line)-4+CrescendoDiminuendoIntervalData.getYShift(interval);
        }else if(ar.adjustType==AdjustType.SLURPOS1 || ar.adjustType==AdjustType.SLURPOS2 || ar.adjustType==AdjustType.SLURCTRLY){
            Line line = musicSheet.getComposition().getLine(ar.line);
            Interval interval = line.getSlurs().findInterval(ar.xIndex);
            SlurData slurData = new SlurData(interval.getData());
            Note note = line.getNote(ar.xIndex);
            switch (ar.adjustType) {
                case SLURPOS1:
                    ar.rectangle.x = note.getXPos() + slurData.getxPos1();
                    ar.rectangle.y = musicSheet.getNoteYPos(note.getYPos(), ar.line) + slurData.getyPos1();
                    ar.rectangle.y += slurData.getCtrly() > 0 ? 8 : -20;
                    break;
                case SLURPOS2:
                    ar.rectangle.x = note.getXPos() + slurData.getxPos2();
                    ar.rectangle.y = musicSheet.getNoteYPos(note.getYPos(), ar.line) + slurData.getyPos2();
                    ar.rectangle.y += slurData.getCtrly() > 0 ? 8 : -20;
                    break;
                case SLURCTRLY:
                    Note lastNote = line.getNote(interval.getB());
                    ar.rectangle.x = (note.getXPos() + slurData.getxPos1() + lastNote.getXPos() + slurData.getxPos2()) / 2;
                    ar.rectangle.y = (musicSheet.getNoteYPos(note.getYPos(), ar.line) +
                            musicSheet.getNoteYPos(lastNote.getYPos(), ar.line)) / 2 + slurData.getCtrly();
                    ar.rectangle.y -= 4;
                    break;
            }
            ar.rectangle.x -= 4;
        }else if(ar.adjustType==AdjustType.TUPLET){
            Line line = musicSheet.getComposition().getLine(ar.line);
            Interval interval = line.getTuplets().findInterval(ar.xIndex);
            Note note = line.getNote(interval.getA());
            ar.rectangle.x = note.getXPos() + (note.isUpper() ? 0 : -10);
            ar.rectangle.y = musicSheet.getNoteYPos(note.getYPos() + (note.isUpper() ? -10 : -3), ar.line) + TupletIntervalData.getVerticalPosition(interval);
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
            case CRESCENDOY:
                return line.getCrescendo();
            case DIMINUENDOY:
                return line.getDiminuendo();
            default:
                throw new IllegalArgumentException(String.valueOf(adjustType));
        }
    }
}
