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

    Created on May 13, 2007
*/
package songscribe.publisher.publisheractions;

import org.apache.log4j.Logger;
import songscribe.publisher.IO.BookIO;
import songscribe.publisher.Publisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class SaveAction extends AbstractAction {
    private static Logger logger = Logger.getLogger(SaveAction.class);
    private Publisher publisher;

    public SaveAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(Action.NAME, "Save");
        putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("filesave.png")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        if (publisher.isBookNull()) {
            return;
        }

        if (publisher.getSaveFile() == null) {
            publisher.getSaveAsAction().actionPerformed(e);
            return;
        }

        try {
            BookIO.writeBook(publisher.getBook(), publisher.getSaveFile(), true);
            publisher.unmodifiedDocument();
        }
        catch (IOException e1) {
            publisher.showErrorMessage(Publisher.COULD_NOT_SAVE_MESSAGE);
            logger.error("Saving songbook", e1);
        }
    }
}
