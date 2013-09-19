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

import org.apache.log4j.Logger;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.music.Composition;
import songscribe.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class ExportMusicSheetImageAction extends AbstractAction{
    private static Logger logger = Logger.getLogger(ExportMusicSheetImageAction.class);
    private PlatformFileDialog pfd;
    private ResolutionDialog resolutionDialog;

    private MyAcceptFilter[] myAcceptFilters = {
        new MyAcceptFilter("JPEG Bitmap", "jpg"),
        new MyAcceptFilter("GIF Image", "gif"),
        new MyAcceptFilter("Portable Network Graphics", "png"),
        new MyAcceptFilter("Windows Bitmap", "bmp")
    };
    private MainFrame mainFrame;

    public ExportMusicSheetImageAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as Image...");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("imageexport.png")));
        pfd = new PlatformFileDialog(mainFrame, "Export as Image", false, myAcceptFilters, Integer.parseInt(mainFrame.getProperties().getProperty(Constants.IMAGEEXPORTFILTERPROP)));        
    }

    public void actionPerformed(ActionEvent e) {
        pfd.setFile(Utilities.getSongTitleFileNameForFileChooser(mainFrame.getMusicSheet()));
        if(pfd.showDialog()){
            MyAcceptFilter maf = pfd.getFileFilter();
            mainFrame.getProperties().setProperty(Constants.IMAGEEXPORTFILTERPROP, Integer.toString(Utilities.arrayIndexOf(myAcceptFilters, maf)));
            File saveFile = pfd.getFile();
            String extension = maf.getExtension(0);
            if(!(saveFile.getName().toLowerCase().endsWith(extension) ||
                    extension.equals("jpg") && saveFile.getName().toLowerCase().endsWith(".jpeg"))){
                saveFile = new File(saveFile.getAbsolutePath()+"."+ extension);
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(mainFrame, "The file "+saveFile.getName()+" already exists. Do you want to overwrite it?",
                        mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }
            double scale;
            if(resolutionDialog==null)resolutionDialog = new ResolutionDialog(mainFrame);
            resolutionDialog.setVisible(true);
            if(!resolutionDialog.isApproved()){
                return;
            }
            scale = (double)resolutionDialog.getResolution()/(double)MusicSheet.RESOLUTION;
            Composition composition = mainFrame.getMusicSheet().getComposition();
            String underLyrics = composition.getUnderLyrics();
            String transletedLyrics = composition.getTranslatedLyrics();
            String songTitle = composition.getSongTitle();
            if(resolutionDialog.isWithoutLyrics()){
                composition.setUnderLyrics("");
                composition.setTranslatedLyrics("");
            }
            if(resolutionDialog.isWithoutTitle()){
                composition.setSongTitle("");
            }
            try {
                BufferedImage sheetImageForExport = mainFrame.getMusicSheet().createMusicSheetImageForExport(Color.white,
                        scale, resolutionDialog.getBorder());
                boolean successful = Utilities.writeImage(sheetImageForExport, extension, saveFile);
                if (!successful) {
                    mainFrame.showErrorMessage("Could not export the image file.\n" +
                        "The " + extension + " image type might not be supported in your Java version.\n" +
                        "Try to upgrade your Java version.");
                } else {
                    Utilities.openExportFile(mainFrame, saveFile);
                }
            } catch (IOException e1) {
                mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                logger.error("Saving image", e1);
            } catch (AWTException e1) {
                 mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                 logger.error("Saving image", e1);
            } catch(OutOfMemoryError e1){
                mainFrame.showErrorMessage("There is not enough memory for this resolution.");
            }finally{
                if(resolutionDialog.isWithoutLyrics()){
                    composition.setUnderLyrics(underLyrics);
                    composition.setTranslatedLyrics(transletedLyrics);
                }
                if(resolutionDialog.isWithoutTitle()){
                    composition.setSongTitle(songTitle);
                }
                mainFrame.getMusicSheet().setRepaintImage(true);
                mainFrame.getMusicSheet().repaint();
            }
        }
    }
}
