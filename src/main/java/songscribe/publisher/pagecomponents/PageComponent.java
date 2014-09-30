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

    Created on Sep 25, 2006
*/
package songscribe.publisher.pagecomponents;

import songscribe.publisher.Publisher;
import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public abstract class PageComponent {
    Rectangle pos;
    double resolution = 1d;

    protected PageComponent(Rectangle pos, double resolution) {
        this.pos = pos;
        setResolution(resolution);
    }

    public abstract void paintComponent(Graphics2D g2);

    public abstract MyDialog getPropertiesDialog(Publisher publisher);

    public abstract JPopupMenu getPopupMenu(Publisher publisher);

    public Rectangle getPos() {
        return pos;
    }

    public void setPosition(Point position) {
        pos.setLocation(position);
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        double ratio = resolution / this.resolution;
        this.resolution = resolution;
        pos.width *= ratio;
        pos.height *= ratio;
    }

    protected void addCommonPopups(Publisher publisher, JPopupMenu editPopup) {
        editPopup.add(publisher.getPropertiesAction());
        editPopup.add(publisher.getRemoveAction());
        editPopup.add(publisher.orderMenuFactory());
        editPopup.add(publisher.insertMenuFactory());
    }
}
