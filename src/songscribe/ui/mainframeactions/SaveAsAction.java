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

Created on Aug 6, 2006
*/
package songscribe.ui.mainframeactions;

import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.data.FileExtensions;
import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class SaveAsAction extends AbstractAction {
    private PlatformFileDialog pfd;
    private MainFrame mainFrame;

    public SaveAsAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Save as...");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("filesaveas.png")));
        pfd = new PlatformFileDialog(mainFrame, "Save As", false, new MyAcceptFilter("SongScribe song files", FileExtensions.SONGWRITER.substring(1)));
    }

    public void actionPerformed(ActionEvent e) {
        if(mainFrame.getSaveFile()==null){
            pfd.setFile(Utilities.getSongTitleFileNameForFileChooser(mainFrame.getMusicSheet()));
        }else{
            pfd.setFile("");
        }
        if(pfd.showDialog()){
            File saveFile = pfd.getFile();
            if(!saveFile.getName().toLowerCase().endsWith(FileExtensions.SONGWRITER)){
                saveFile = new File(saveFile.getAbsolutePath()+FileExtensions.SONGWRITER);
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(mainFrame, "The file "+saveFile.getName()+" already exists. Do you want to owerwrite it?",
                        mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }
            mainFrame.setSaveFile(saveFile);
            mainFrame.getSaveAction().actionPerformed(e);
        }
    }
}
