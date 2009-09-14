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

Created on Dec 26, 2006
*/
package songscribe.data;

import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import javax.swing.plaf.basic.BasicFileChooserUI;
import java.awt.*;
import java.io.File;

import com.jtechlabs.ui.widget.directorychooser.JDirectoryChooser;
import com.jtechlabs.ui.widget.directorychooser.DirectoryChooserDefaults;
import org.apache.log4j.Logger;

/**
 * @author Csaba Kávai
 */
public class PlatformFileDialog {
    private JFileChooser jfc;
    private FileDialog fd;
    private File selectedDirectory;
    private MainFrame mainFrame;
    private boolean isOpen;
    private boolean directoriesOnly;
    private MyAcceptFilter[] mafs;
    private int initial;

    private static Logger LOG = Logger.getLogger(PlatformFileDialog.class);

    public PlatformFileDialog(MainFrame mainFrame, String title, boolean isOpen, MyAcceptFilter maf) {
        this(mainFrame, title, isOpen, false);
        setFileFiler(maf);
    }

    public PlatformFileDialog(MainFrame mainFrame, String title, boolean isOpen, MyAcceptFilter maf, boolean directoriesOnly) {
        this(mainFrame, title, isOpen, directoriesOnly);
        setFileFiler(maf);
    }

    public PlatformFileDialog(MainFrame mainFrame, String title, boolean isOpen, MyAcceptFilter[] mafs, int initial) {
        this(mainFrame, title, isOpen, false);
        this.mafs = mafs;
        this.initial = initial;
        if(!Utilities.isMac()){
            for(MyAcceptFilter maf : mafs){
                jfc.addChoosableFileFilter(maf);
            }
            jfc.setAcceptAllFileFilterUsed(false);
            jfc.setFileFilter(mafs[initial]);
        }
    }

    private PlatformFileDialog(MainFrame mainFrame, String title, boolean isOpen, boolean directoriesOnly) {
        this.mainFrame = mainFrame;
        this.isOpen = isOpen;
        this.directoriesOnly = directoriesOnly;
        if(Utilities.isMac()){
            fd = new FileDialog(mainFrame, title, isOpen ? FileDialog.LOAD : FileDialog.SAVE);
        }else{
            if(!directoriesOnly){
                jfc = new JFileChooser();
                jfc.setDialogTitle(title);
            }else{
                DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_DIALOG_TEXT, title);
                DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_ACCESS, JDirectoryChooser.ACCESS_NEW|JDirectoryChooser.ACCESS_RENAME);
            }
        }
    }

    public void setFileFiler(MyAcceptFilter maf){
        if(Utilities.isMac()){
            fd.setFilenameFilter(maf);
        }else{
            if(!directoriesOnly) jfc.setFileFilter(maf);
        }
    }

    public MyAcceptFilter getFileFilter(){
        if(Utilities.isMac()){
            return (MyAcceptFilter)fd.getFilenameFilter();
        }else{
            return (MyAcceptFilter)jfc.getFileFilter();
        }        
    }

    public boolean showDialog(){
        if(Utilities.isMac()){
            if(mafs!=null){
                int answ = JOptionPane.showOptionDialog(mainFrame, "Select the file type", mainFrame.PROGNAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, mafs, mafs[initial]);
                if(answ==JOptionPane.CLOSED_OPTION)return false;
                setFileFiler(mafs[answ]);
            }
            fd.setDirectory(mainFrame.getPreviousDirectory().getAbsolutePath());
            if(directoriesOnly)System.setProperty("apple.awt.fileDialogForDirectories", "true");
            fd.setVisible(true);
            if(directoriesOnly)System.setProperty("apple.awt.fileDialogForDirectories", "false");
            return fd.getFile()!=null;
        }else{
            if(!directoriesOnly){
                jfc.setCurrentDirectory(mainFrame.getPreviousDirectory());
                return (isOpen ? jfc.showOpenDialog(mainFrame) : jfc.showSaveDialog(mainFrame))==JFileChooser.APPROVE_OPTION;
            }else{
                DirectoryChooserDefaults.putOption(DirectoryChooserDefaults.PROP_INITIAL_DIRECTORY, mainFrame.getPreviousDirectory());   
                selectedDirectory = JDirectoryChooser.showDialog(mainFrame);
                return selectedDirectory!=null;
            }
        }
    }

    public File getFile(){
        File file;
        if(Utilities.isMac()){
            file = new File(fd.getDirectory(), fd.getFile());
        }else{
            file = !directoriesOnly ? jfc.getSelectedFile() : selectedDirectory;
        }
        mainFrame.setPreviousDirectory(!directoriesOnly ? file.getParentFile() : file);
        return file;
    }

    public File[] getFiles(){
        if(Utilities.isMac()){
            return new File[]{getFile()};
        }else{
            if(directoriesOnly)return new File[]{getFile()};
            File[] files = jfc.getSelectedFiles();
            mainFrame.setPreviousDirectory(!directoriesOnly ? files[0].getParentFile() : files[0]);
            return files;
        }
    }

    public void setFile(String file){
        if(Utilities.isMac()) {
            fd.setFile(file);
        }else{
            if(!directoriesOnly){
                if(jfc.getUI() instanceof BasicFileChooserUI){
                    ((BasicFileChooserUI)jfc.getUI()).setFileName(file);
                }
            }
        }
    }

    public void setMultiSelectionEnabled(boolean enabled){
        if(!Utilities.isMac() && !directoriesOnly)jfc.setMultiSelectionEnabled(enabled);
    }
}
