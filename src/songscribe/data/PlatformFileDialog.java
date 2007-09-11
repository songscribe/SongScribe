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

/**
 * @author Csaba Kávai
 */
public class PlatformFileDialog {
    private JFileChooser jfc;
    private MainFrame mainFrame;
    private FileDialog fd;
    private boolean isOpen;
    private boolean directoriesOnly;
    private MyAcceptFilter[] mafs;
    private int initial;

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
            jfc = new JFileChooser();
            jfc.setDialogTitle(title);
        }
    }

    public void setFileFiler(MyAcceptFilter maf){
        if(Utilities.isMac()){
            fd.setFilenameFilter(maf);
        }else{
            jfc.setFileFilter(maf);
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
            jfc.setCurrentDirectory(mainFrame.getPreviousDirectory());
            if(directoriesOnly)jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            return (isOpen ? jfc.showOpenDialog(mainFrame) : jfc.showSaveDialog(mainFrame))==JFileChooser.APPROVE_OPTION;
        }
    }

    public File getFile(){
        File file;
        if(Utilities.isMac()){
            file = !directoriesOnly ? new File(fd.getDirectory(), fd.getFile()) : new File(fd.getDirectory());
        }else{
            file =  jfc.getSelectedFile();
        }
        mainFrame.setPreviousDirectory(!directoriesOnly ? file.getParentFile() : file);
        return file;
    }

    public File[] getFiles(){
        if(Utilities.isMac()){
            return new File[]{getFile()};
        }else{
            File[] files = jfc.getSelectedFiles();
            mainFrame.setPreviousDirectory(!directoriesOnly ? files[0].getParentFile() : files[0]);
            return files;
        }
    }

    public void setFile(String file){
        if(Utilities.isMac()) {
            fd.setFile(file);
        }else{
            if(jfc.getUI() instanceof BasicFileChooserUI){
                ((BasicFileChooserUI)jfc.getUI()).setFileName(file);
            }
        }
    }

    public void setMultiSelectionEnabled(boolean enabled){
        if(!Utilities.isMac())jfc.setMultiSelectionEnabled(enabled);
    }
}
