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

Created on May 13, 2007
*/
package songscribe.publisher.publisheractions;

import songscribe.data.FileExtensions;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.publisher.Publisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class OpenAction extends AbstractAction {
    private PlatformFileDialog pfd;
    private Publisher publisher;

    public OpenAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(Action.NAME, "Open...");
        putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("fileopen.png")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        pfd = new PlatformFileDialog(publisher, "Open", true, new MyAcceptFilter("SongScribe Song Book files", FileExtensions.SONGBOOK.substring(1)));
    }

    public void actionPerformed(ActionEvent e) {
        if(!publisher.showSaveDialog())return;
        if(pfd.showDialog()){
            File openFile = pfd.getFile();
            publisher.openBook(openFile);
            publisher.unmodifiedDocument();
        }
    }
}