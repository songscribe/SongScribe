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

    Created on Oct 3, 2006
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Publisher;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class InsertPageAction extends AbstractAction {
    private Publisher publisher;
    private Where where;

    public InsertPageAction(Publisher publisher, Where where, String name, String icon) {
        putValue(NAME, name);

        if (icon != null) {
            putValue(SMALL_ICON, new ImageIcon(Publisher.getImage(icon)));
        }

        this.publisher = publisher;
        this.where = where;
    }

    public void actionPerformed(ActionEvent e) {
        if (publisher.isBookNull()) {
            return;
        }

        if (where == Where.BEGINNING) {
            publisher.getBook().setSelectedPage(publisher.getBook().getSelectedPage() + 1);
            publisher.getBook().addPage(0);
        }
        else if (where == Where.END) {
            publisher.getBook().addPage();
        }

        publisher.setModifiedDocument(true);
    }

    public enum Where { BEGINNING, END }
}
