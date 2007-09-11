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

Created on Aug 28, 2007
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Publisher;
import songscribe.data.PlatformFileDialog;
import songscribe.data.MyAcceptFilter;
import songscribe.data.FileExtensions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;
import java.util.zip.ZipEntry;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * @author Csaba Kávai
 */
public class ImportPortableAction extends AbstractAction {
    private static Logger logger = Logger.getLogger(ImportPortableAction.class);
    private Publisher publisher;
    private PlatformFileDialog pfd, openDir;

    public ImportPortableAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Import from portable");
        putValue(SMALL_ICON, publisher.blankIcon);
        putValue(SHORT_DESCRIPTION, "Opens and unpacks a Song Book portable file.");
        pfd = new PlatformFileDialog(publisher, "Import from portable", true, new MyAcceptFilter("Song Book portable", FileExtensions.SBPORTABLE.substring(1)));
        openDir = new PlatformFileDialog(publisher, "Save location", false, new MyAcceptFilter("Folder"), true);
    }

    public void actionPerformed(ActionEvent e) {
        if(!publisher.showSaveDialog())return;
        if(pfd.showDialog()){
            try {
                ZipFile openFile = new ZipFile(pfd.getFile());
                JOptionPane.showMessageDialog(publisher, "Please select the path where you want to unpack the book. It may contain a lot of files.", publisher.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
                if(openDir.showDialog()){
                    File dirToSave = openDir.getFile();
                    boolean overwriteAll = false, keepAll=false;
                    byte[] buf = new byte[1024];
                    File songBook = null;
                    entries:
                    for(Enumeration entries = openFile.entries();entries.hasMoreElements();){
                        ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                        File save = new File(dirToSave, zipEntry.getName());
                        if(save.exists() && !overwriteAll){
                            if(keepAll)continue;
                            int answ = JOptionPane.showOptionDialog(publisher, "The file "+save.getName()+" already exists in this folder. What would you like to do?", publisher.PROGNAME, 0, JOptionPane.QUESTION_MESSAGE, null,
                                    new String[]{"Overwrite", "Overwrite all", "Keep", "Keep all", "Cancel"}, 1);
                            switch(answ){
                                case 0: break;
                                case 1: overwriteAll = true; break;
                                case 3: keepAll = true;
                                case 2: continue entries;
                                default: return;
                            }
                        }
                        try{
                            InputStream is = openFile.getInputStream(zipEntry);
                            FileOutputStream os = new FileOutputStream(save);
                            int read;
                            while((read=is.read(buf))>0){
                                os.write(buf, 0, read);
                            }
                            is.close();
                            os.close();
                            if(save.getName().endsWith(FileExtensions.SONGBOOK))songBook = save;
                        } catch(FileNotFoundException e1){
                            publisher.showErrorMessage("Cannot write the file "+save.getName()+". Maybe you do not have the permission to write.");
                            logger.error("Cannot unpack a zip file", e1);
                        } catch(IOException e1){
                            publisher.showErrorMessage("Cannot write the file "+save.getName());
                            logger.error("Cannot unpack a zip file", e1);
                        }
                    }
                    if(songBook!=null){
                        publisher.openBook(songBook);
                        publisher.unmodifiedDocument();
                    }
                }
                openFile.close();
            } catch (ZipException e1) {
                publisher.showErrorMessage("The file you want to import is in bad format or damaged.");
                logger.error("Open a non-zip file", e1);
            } catch (IOException e1) {
                publisher.showErrorMessage("Cannot read the file. Maybe you do not have the permission to read.");
                logger.error("Cannot open a zip file", e1);
            }
        }
    }
}
