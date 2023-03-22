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

    Created on Feb 3, 2007
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.PageComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class PropertiesAction extends AbstractAction {
    private Publisher publisher;

    public PropertiesAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Properties");
        putValue(SMALL_ICON, new ImageIcon(Publisher.getImage("properties.png")));
    }

    public void actionPerformed(ActionEvent e) {
        if (publisher.isBookNull()) {
            return;
        }

        PageComponent pg = publisher.getBook().getSelectedComponent();

        if (pg != null) {
            if (pg.getPropertiesDialog(publisher) != null) {
                pg.getPropertiesDialog(publisher).setVisible(true);
            }
            else {
                publisher.showErrorMessage("No properties window yet.");
            }

            publisher.setModifiedDocument(true);
        }
        else if (publisher.getBook().getSelectedPage() > -1) {
            publisher.getPaperSizeDialogOpenAction().actionPerformed(null);
        }
        else {
            publisher.showErrorMessage("First select a component for its properties.");
        }
    }
}
