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

Created on Oct 09, 2007
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Book;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.PageComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class AlignToCenter extends AbstractAction {
    private Publisher publisher;

    public AlignToCenter(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Align to Center");
        putValue(SMALL_ICON, new ImageIcon(Publisher.getImage("centerAlignment.png")));
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        PageComponent pg = publisher.getBook().getSelectedComponent();
        if(pg!=null){
            Rectangle pos = pg.getPos();
            Book book = publisher.getBook();
            Rectangle margin = book.getMargin(book.getSelectedComponentsPage());
            pg.setPosition(new Point((margin.width-pos.width)/2, pos.y));
            book.repaintSelectedComponent();
        }else{
            publisher.showErrorMessage("First select a component to align.");
        }
    }
}
