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

import songscribe.ui.MusicSheet;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public abstract class Adjustment implements MouseListener, MouseMotionListener{
    protected MusicSheet musicSheet;
    protected boolean enabled;
    protected boolean startedDrag;
    protected Point startPoint, endPoint = new Point();
    protected Point upLeftDragBounds = new Point();
    protected Point downRightDragBounds = new Point();

    public Adjustment(MusicSheet parent) {
        this.musicSheet = parent;
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (!enabled) return;
        if (!startedDrag) {
            startedDrag = true;
            startPoint = e.getPoint();
            startedDrag();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (!enabled) return;
        if (startedDrag) {
            startedDrag = false;
            finishedDrag();
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (!enabled) return;        
        if(startedDrag){
            int realX = e.getX() < upLeftDragBounds.x ? upLeftDragBounds.x : e.getX() >= downRightDragBounds.x ? downRightDragBounds.x - 1 : e.getX();
            int realY = e.getY() < upLeftDragBounds.y ? upLeftDragBounds.y : e.getY() >= downRightDragBounds.y ? downRightDragBounds.y - 1 : e.getY();
            endPoint.setLocation(realX, realY);
            drag();
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    protected abstract void startedDrag();
    protected abstract void drag();
    protected abstract void finishedDrag();
    public abstract void repaint(Graphics2D g2);
}
