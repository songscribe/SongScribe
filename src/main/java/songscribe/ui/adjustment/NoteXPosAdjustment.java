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
public class NoteXPosAdjustment extends Adjustment {
    private static final int END_SNAP_LIMIT = 30;
    private ArrayList<AdjustRect> adjustRects = new ArrayList<AdjustRect>();
    private AdjustRect draggingRect;
    private float[] stretchHelper;

    public NoteXPosAdjustment(MusicSheet musicSheet) {
        super(musicSheet);
    }

    protected void startedDrag() {
        draggingRect = null;

        for (AdjustRect ar : adjustRects) {
            if (ar.rectangle.contains(startPoint)) {
                draggingRect = ar;
                break;
            }
        }

        if (draggingRect == null) {
            startedDrag = false;
        }
        else {
            Line line = musicSheet.getComposition().getLine(draggingRect.line);

            if (draggingRect.adjustType == AdjustType.ONE_NOTE) {
                upLeftDragBounds.setLocation(
                        (draggingRect.xIndex > 0 ? line.getNote(draggingRect.xIndex - 1).getXPos() : 20) +
                        draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.xIndex < line.noteCount() - 1 ?
                        line.getNote(draggingRect.xIndex + 1).getXPos() - draggingRect.rectangle.width :
                        musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }
            else if (draggingRect.adjustType == AdjustType.WHOLE_NOTE) {
                upLeftDragBounds.setLocation(line.getNote(draggingRect.xIndex - 1).getXPos() +
                                             draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.rectangle.x - draggingRect.rectangle.width +
                                                musicSheet.getComposition().getLineWidth() -
                                                line.getNote(line.noteCount() - 1).getXPos(), draggingRect.rectangle.y);
            }
            else if (draggingRect.adjustType == AdjustType.STRETCH) {
                upLeftDragBounds.setLocation(line.getNote(0).getXPos(), draggingRect.rectangle.y);
                downRightDragBounds.setLocation(musicSheet.getComposition().getLineWidth() +
                                                draggingRect.rectangle.width, draggingRect.rectangle.y);

                if (stretchHelper == null || stretchHelper.length < line.noteCount()) {
                    stretchHelper = new float[line.noteCount()];
                }

                for (int i = 0; i < line.noteCount(); i++) {
                    stretchHelper[i] = line.getNote(i).getXPos();
                }
            }
            else if (draggingRect.adjustType == AdjustType.FIRST_XPOS) {
                upLeftDragBounds.setLocation(draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.rectangle.x - draggingRect.rectangle.width +
                                                musicSheet.getComposition().getLineWidth() -
                                                line.getNote(line.noteCount() - 1).getXPos(), draggingRect.rectangle.y);
            }
            else if (draggingRect.adjustType == AdjustType.GLISSANDO_X1) {
                upLeftDragBounds.setLocation(line.getNote(draggingRect.xIndex).getXPos(), draggingRect.rectangle.y);
                downRightDragBounds.setLocation(adjustRects.get(
                        adjustRects.indexOf(draggingRect) + 1).rectangle.x, draggingRect.rectangle.y);
            }
            else if (draggingRect.adjustType == AdjustType.GLISSANDO_X2) {
                upLeftDragBounds.setLocation(adjustRects.get(adjustRects.indexOf(draggingRect) - 1).rectangle.x +
                                             draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }
            else if (draggingRect.adjustType == AdjustType.CRESCENDO_X1 ||
                     draggingRect.adjustType == AdjustType.CRESCENDO_X2 ||
                     draggingRect.adjustType == AdjustType.DIMINUENDO_X1 ||
                     draggingRect.adjustType == AdjustType.DIMINUENDO_X2) {
                upLeftDragBounds.setLocation(draggingRect.xIndex == 0 ?
                        0 :
                        line.getNote(draggingRect.xIndex - 1).getXPos(), draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.xIndex == line.noteCount() - 1 ?
                        musicSheet.getComposition().getLineWidth() :
                        line.getNote(draggingRect.xIndex + 1).getXPos(), draggingRect.rectangle.y);
            }
            else if (draggingRect.adjustType == AdjustType.GRACE_SEMIQUAVER_2ND_PART) {
                upLeftDragBounds.setLocation(line.getNote(draggingRect.xIndex).getXPos() +
                                             draggingRect.rectangle.width, draggingRect.rectangle.y);
                downRightDragBounds.setLocation(draggingRect.xIndex < line.noteCount() - 1 ?
                        line.getNote(draggingRect.xIndex + 1).getXPos() - draggingRect.rectangle.width :
                        musicSheet.getComposition().getLineWidth(), draggingRect.rectangle.y);
            }
        }
    }

    protected void drag() {
        if (draggingRect == null) {
            return;
        }

        Line line = musicSheet.getComposition().getLine(draggingRect.line);
        Note note = line.getNote(draggingRect.xIndex);

        if (note.getNoteType().snapToEnd() && musicSheet.getComposition().getLineWidth() - endPoint.x < END_SNAP_LIMIT) {
            endPoint.x = musicSheet.getComposition().getLineWidth() - Note.NORMAL_IMAGE_WIDTH;
        }

        int diffX = draggingRect.rectangle.x + draggingRect.rectangle.width / 2;
        draggingRect.rectangle.x = endPoint.x - draggingRect.rectangle.width / 2;

        if (draggingRect.adjustType == AdjustType.STRETCH) {
            float ratio = (float) endPoint.x / note.getXPos();
            int firstX = line.getNote(0).getXPos();

            for (int i = 0; i < line.noteCount(); i++) {
                stretchHelper[i] = firstX + (stretchHelper[i] - firstX) * ratio;
                line.getNote(i).setXPos(Math.round(stretchHelper[i]));
            }

            line.mulNoteDistChange(ratio);
        }
        else if (draggingRect.adjustType == AdjustType.WHOLE_NOTE) {
            for (int i = draggingRect.xIndex; i < line.noteCount(); i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos() + endPoint.x - diffX);
            }
        }
        else if (draggingRect.adjustType == AdjustType.ONE_NOTE) {
            note.setXPos(endPoint.x);
        }
        else if (draggingRect.adjustType == AdjustType.FIRST_XPOS) {
            MusicSheet.setStartLocX(endPoint.x);
            Composition c = musicSheet.getComposition();

            for (int i = 0; i < c.lineCount(); i++) {
                Line l = c.getLine(i);

                if (l.noteCount() > 0) {
                    int diff = endPoint.x - l.getNote(0).getXPos();
                    l.getNote(0).setXPos(endPoint.x);

                    for (int j = 1; j < l.noteCount(); j++) {
                        l.getNote(j).setXPos(l.getNote(j).getXPos() + diff);
                    }
                }
            }
        }
        else if (draggingRect.adjustType == AdjustType.GLISSANDO_X1) {
            note.getGlissando().x1Translate += endPoint.x - diffX;
        }
        else if (draggingRect.adjustType == AdjustType.GLISSANDO_X2) {
            note.getGlissando().x2Translate -= endPoint.x - diffX;
        }
        else if (draggingRect.adjustType == AdjustType.GRACE_SEMIQUAVER_2ND_PART) {
            ((GraceSemiQuaver) note).setX2DiffPos(endPoint.x - note.getXPos());
        }
        else if (draggingRect.adjustType == AdjustType.CRESCENDO_X1 ||
                 draggingRect.adjustType == AdjustType.DIMINUENDO_X1) {
            Interval interval = getCresDecrIntervalSet(line, draggingRect.adjustType).findInterval(draggingRect.xIndex);
            CrescendoDiminuendoIntervalData.setX1Shift(interval,
                    CrescendoDiminuendoIntervalData.getX1Shift(interval) + endPoint.x - diffX);
        }
        else if (draggingRect.adjustType == AdjustType.CRESCENDO_X2 ||
                 draggingRect.adjustType == AdjustType.DIMINUENDO_X2) {
            Interval interval = getCresDecrIntervalSet(line, draggingRect.adjustType).findInterval(draggingRect.xIndex);
            CrescendoDiminuendoIntervalData.setX2Shift(interval,
                    CrescendoDiminuendoIntervalData.getX2Shift(interval) + endPoint.x - diffX);
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
        for (AdjustRect ar : adjustRects) {
            g2.setPaint(ar.adjustType.getColor());
            g2.fill(ar.rectangle);
            g2.setPaint(Color.black);
            g2.draw(ar.rectangle);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            Composition c = musicSheet.getComposition();

            for (int l = 0; l < c.lineCount(); l++) {
                Line line = c.getLine(l);

                // add ONE_NOTE
                for (int n = 0; n < line.noteCount(); n++) {
                    adjustRects.add(new AdjustRect(l, n, AdjustType.ONE_NOTE));
                }

                // add WHOLE_NOTE
                for (int n = 1; n < line.noteCount() - 1; n++) {
                    adjustRects.add(new AdjustRect(l, n, AdjustType.WHOLE_NOTE));
                }

                // add special this: GLISSANDO, GRACE_SEMIQUAVER
                for (int n = 0; n < line.noteCount(); n++) {
                    if (line.getNote(n).getGlissando() != Note.NO_GLISSANDO) {
                        adjustRects.add(new AdjustRect(l, n, AdjustType.GLISSANDO_X1));
                        adjustRects.add(new AdjustRect(l, n, AdjustType.GLISSANDO_X2));
                    }

                    if (line.getNote(n).getNoteType() == NoteType.GRACE_SEMIQUAVER) {
                        adjustRects.add(new AdjustRect(l, n, AdjustType.GRACE_SEMIQUAVER_2ND_PART));
                    }
                }

                // add STRETCH
                if (line.noteCount() > 0) {
                    adjustRects.add(new AdjustRect(l, line.noteCount() - 1, AdjustType.STRETCH));
                }

                // add CRESCENDO
                for (ListIterator<Interval> li = line.getCrescendo().listIterator(); li.hasNext(); ) {
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, interval.getA(), AdjustType.CRESCENDO_X1));
                    adjustRects.add(new AdjustRect(l, interval.getB(), AdjustType.CRESCENDO_X2));
                }

                // add DIMINUENDO
                for (ListIterator<Interval> li = line.getDiminuendo().listIterator(); li.hasNext(); ) {
                    Interval interval = li.next();
                    adjustRects.add(new AdjustRect(l, interval.getA(), AdjustType.DIMINUENDO_X1));
                    adjustRects.add(new AdjustRect(l, interval.getB(), AdjustType.DIMINUENDO_X2));
                }
            }

            // add FIRST_NOTE
            if (c.getLine(0).noteCount() > 0) {
                adjustRects.add(new AdjustRect(0, 0, AdjustType.FIRST_XPOS));
            }
        }
        else {
            adjustRects.clear();
        }
    }

    private void getRectangle(AdjustRect ar) {
        Line line = musicSheet.getComposition().getLine(ar.line);
        Note note = line.getNote(ar.xIndex);
        ar.rectangle.y = musicSheet.getNoteYPos(ar.adjustType.getYPos(), ar.line) - Note.HOT_SPOT.y;

        switch (ar.adjustType) {
            case GLISSANDO_X1:
                ar.rectangle.x = musicSheet.getDrawer().getGlissandoX1Pos(ar.xIndex, note.getGlissando(), ar.line) - 4;
                break;

            case GLISSANDO_X2:
                ar.rectangle.x = musicSheet.getDrawer().getGlissandoX2Pos(ar.xIndex, note.getGlissando(), ar.line) - 4;
                break;

            case GRACE_SEMIQUAVER_2ND_PART:
                ar.rectangle.x = note.getXPos() + ((GraceSemiQuaver) note).getX2DiffPos() + 1;
                break;

            case CRESCENDO_X1:
            case DIMINUENDO_X1:
                Interval x1Interval = getCresDecrIntervalSet(line, ar.adjustType).findInterval(ar.xIndex);
                ar.rectangle.x =
                        line.getNote(ar.xIndex).getXPos() - 12 + CrescendoDiminuendoIntervalData.getX1Shift(x1Interval);
                ar.rectangle.y =
                        musicSheet.getNoteYPos(6, ar.line) - 4 + CrescendoDiminuendoIntervalData.getYShift(x1Interval);
                break;

            case CRESCENDO_X2:
            case DIMINUENDO_X2:
                Interval x2Interval = getCresDecrIntervalSet(line, ar.adjustType).findInterval(ar.xIndex);
                ar.rectangle.x =
                        line.getNote(ar.xIndex).getXPos() + 16 + CrescendoDiminuendoIntervalData.getX2Shift(x2Interval);
                ar.rectangle.y =
                        musicSheet.getNoteYPos(6, ar.line) - 4 + CrescendoDiminuendoIntervalData.getYShift(x2Interval);
                break;

            default:
                ar.rectangle.x = note.getXPos() + 1;
        }

        ar.rectangle.width = ar.rectangle.height = 8;
    }

    private void revalidateRects() {
        for (AdjustRect ar : adjustRects) {
            getRectangle(ar);
        }
    }

    private IntervalSet getCresDecrIntervalSet(Line line, AdjustType adjustType) {
        switch (adjustType) {
            case CRESCENDO_X1:
            case CRESCENDO_X2:
                return line.getCrescendo();

            case DIMINUENDO_X1:
            case DIMINUENDO_X2:
                return line.getDiminuendo();

            default:
                throw new IllegalArgumentException(String.valueOf(adjustType));
        }
    }

    private enum AdjustType {
        ONE_NOTE(Color.white, -1),
        WHOLE_NOTE(Color.blue, -4),
        STRETCH(Color.yellow, -4),
        FIRST_XPOS(Color.green, -4),
        GLISSANDO_X1(Color.magenta, -2),
        GLISSANDO_X2(Color.magenta, -2),
        GRACE_SEMIQUAVER_2ND_PART(Color.white, -1),
        CRESCENDO_X1(Color.orange, 6),
        CRESCENDO_X2(Color.orange, 6),
        DIMINUENDO_X1(Color.orange, 6),
        DIMINUENDO_X2(Color.orange, 6);

        private Color color;
        private int yPos;

        AdjustType(Color color, int yPos) {
            this.color = color;
            this.yPos = yPos;
        }

        public Color getColor() {
            return color;
        }

        public int getYPos() {
            return yPos;
        }
    }

    private class AdjustRect {
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
}
