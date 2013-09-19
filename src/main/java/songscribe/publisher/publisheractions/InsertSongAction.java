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

Created on Sep 26, 2006
*/
package songscribe.publisher.publisheractions;

import songscribe.data.FileExtensions;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.Song;
import songscribe.ui.MusicSheet;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class InsertSongAction extends AbstractAction{
    private Publisher publisher;
    private PlatformFileDialog pfd;

    public InsertSongAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Song");
        putValue(SMALL_ICON, new ImageIcon(Publisher.getImage("addline.png")));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
        pfd = new PlatformFileDialog(publisher, "Open", true, new MyAcceptFilter("SongScribe song files", FileExtensions.SONGWRITER.substring(1)));
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        if(pfd.showDialog()){
            File openFile = pfd.getFile();
            MusicSheet musicSheet = publisher.openMusicSheet(openFile);
            publisher.getBook().insertPageComponent(new Song(musicSheet, 0, 0, publisher.getBook().getNotGreaterResolution(musicSheet.getSheetWidth(), musicSheet.getSheetHeight()), openFile));
        }
    }
}
