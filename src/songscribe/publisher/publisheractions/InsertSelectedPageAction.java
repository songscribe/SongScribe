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

Created on Oct 3, 2006
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Publisher;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class InsertSelectedPageAction extends AbstractAction{
    Publisher publisher;
    int shift;

    public InsertSelectedPageAction(Publisher publisher, String name, String icon, int shift) {
        putValue(NAME, name);
        if(icon!=null)putValue(SMALL_ICON, new ImageIcon(Publisher.getImage(icon)));
        this.publisher = publisher;
        this.shift = shift;
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        int selectedPage = publisher.getBook().getPresentPage();
        if(selectedPage==-1){
            publisher.showErrorMessage("Please select a page or a component first.");
            return;
        }
        publisher.getBook().setSelectedPage(selectedPage+1-shift);
        publisher.getBook().addPage(selectedPage+shift);
        publisher.modifiedDocument();
    }
}
