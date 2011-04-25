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

import songscribe.data.PlatformFileDialog;
import songscribe.data.MyAcceptFilter;
import songscribe.data.FileExtensions;
import songscribe.publisher.Publisher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class SaveAsAction extends AbstractAction{
    private PlatformFileDialog pfd;
    private Publisher publisher;

    public SaveAsAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(Action.NAME, "Save as...");
        putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("filesaveas.png")));
        pfd = new PlatformFileDialog(publisher, "Save As", false, new MyAcceptFilter("SongScribe Song Book files", FileExtensions.SONGBOOK.substring(1)));
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        if(pfd.showDialog()){
            File saveFile = pfd.getFile();
            if(!saveFile.getName().toLowerCase().endsWith(FileExtensions.SONGBOOK)){
                saveFile = new File(saveFile.getAbsolutePath()+FileExtensions.SONGBOOK);
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(publisher, "The file "+saveFile.getName()+" already exists. Do you want to overwrite it?",
                        publisher.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }
            publisher.setSaveFile(saveFile);
            publisher.getSaveAction().actionPerformed(e);
        }
    }
}
