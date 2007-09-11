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

import org.apache.log4j.Logger;

import javax.swing.*;

import songscribe.data.PlatformFileDialog;
import songscribe.data.MyAcceptFilter;
import songscribe.data.FileExtensions;
import songscribe.publisher.Publisher;
import songscribe.publisher.Page;
import songscribe.publisher.IO.BookIO;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.Song;
import songscribe.publisher.pagecomponents.PImage;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Csaba Kávai
 */
public class ExportPortableAction extends AbstractAction {
    private static Logger logger = Logger.getLogger(ExportPortableAction.class);
    private PlatformFileDialog pfd;
    private Publisher publisher;

    private HashMap<String, Integer> fileNames = new HashMap<String, Integer>();

    private class OldFiles {
        PageComponent pageComponent;
        File oldFile;

        public OldFiles(PageComponent pageComponent, File oldFile) {
            this.pageComponent = pageComponent;
            this.oldFile = oldFile;
        }
    }
    private Vector<OldFiles> oldFiles = new Vector<OldFiles>();
    private byte[] buf = new byte[1024];

    public ExportPortableAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Export as portable");
        putValue(SMALL_ICON, publisher.blankIcon);
        putValue(SHORT_DESCRIPTION, "Packs all the songs and images in the song book into one file");
        pfd = new PlatformFileDialog(publisher, "Export as portable", false, new MyAcceptFilter("Song Book portable", FileExtensions.SBPORTABLE.substring(1)));
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        if(pfd.showDialog()){
            File saveFile = pfd.getFile();
            File saveParent = saveFile.getParentFile();
            if(!saveFile.getName().toLowerCase().endsWith(FileExtensions.SBPORTABLE)){
                saveFile = new File(saveFile.getAbsolutePath()+ FileExtensions.SBPORTABLE);
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(publisher, "The file "+saveFile.getName()+" already exists. Do you want to owerwrite it?",
                        publisher.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }
            try {
                fileNames.clear();
                oldFiles.clear();
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(saveFile));
                for(ListIterator<Page> pages = publisher.getBook().pageIterator();pages.hasNext();){
                    for(ListIterator<PageComponent> comps = pages.next().getPageComponentIterator();comps.hasNext();){
                        PageComponent c = comps.next();
                        if(c instanceof Song){
                            File oldFile = ((Song)c).getSongFile();
                            oldFiles.add(new OldFiles(c, oldFile));
                            String newFile = zipFile(zos, oldFile, null);
                            ((Song)c).setSongFile(new File(saveParent, newFile));
                        }else if(c instanceof PImage){
                            File oldFile = ((PImage)c).getImageFile();
                            oldFiles.add(new OldFiles(c, oldFile));
                            String newFile = zipFile(zos, oldFile, null);
                            ((PImage)c).setImageFile(new File(saveParent, newFile));
                        }
                    }
                }
                File bookFile = File.createTempFile("exp_port", FileExtensions.SONGBOOK.substring(1), saveParent);
                BookIO.writeBook(publisher.getBook(), bookFile, false);
                String saveName = saveFile.getName();
                zipFile(zos, bookFile, saveName.substring(0, saveName.lastIndexOf('.'))+ FileExtensions.SONGBOOK);
                bookFile.delete();
                zos.close();

                //restoring the old filenames
                for(OldFiles of:oldFiles){
                    if(of.pageComponent instanceof Song){
                        ((Song)of.pageComponent).setSongFile(of.oldFile);
                    }else if(of.pageComponent instanceof PImage){
                        ((PImage)of.pageComponent).setImageFile(of.oldFile);
                    }else throw new IOException("Unexpected pagecomponent. Programming error!");
                }
            } catch (IOException e1) {
                publisher.showErrorMessage(Publisher.COULDNOTSAVEMESSAGE);
                logger.error("Exporting portable", e1);
            }
        }
    }

    private String zipFile(ZipOutputStream zos, File file, String requestName) throws IOException {
        String fileName = requestName == null ? file.getName() : requestName;
        Integer value = fileNames.get(fileName);
        if(value==null){
            fileNames.put(fileName, 1);
        }else{
            fileNames.put(fileName, value+1);
            int lastdotIndex = fileName.lastIndexOf('.');
            fileName = fileName.substring(0, lastdotIndex)+"_"+value.toString()+fileName.substring(lastdotIndex);
        }
        zos.putNextEntry(new ZipEntry(fileName));
        FileInputStream fis = new FileInputStream(file);
        int read;
        while((read=fis.read(buf))>0){
            zos.write(buf, 0, read);
        }
        fis.close();
        return fileName;
    }
}
