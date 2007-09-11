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
import songscribe.ui.*;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

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
        if(pfd.showDialog()){
            MyAcceptFilter maf = pfd.getFileFilter();
            mainFrame.getProperties().setProperty(Constants.IMAGEEXPORTFILTERPROP, Integer.toString(Utilities.arrayIndexOf(myAcceptFilters, maf)));
            File saveFile = pfd.getFile();
            if(!(saveFile.getName().toLowerCase().endsWith(maf.getExtension(0)) ||
                    maf.getExtension(0).equals("jpg") && saveFile.getName().toLowerCase().endsWith(".jpeg"))){
                saveFile = new File(saveFile.getAbsolutePath()+"."+maf.getExtension(0));
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(mainFrame, "The file "+saveFile.getName()+" already exists. Do you want to owerwrite it?",
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
            try {
                ImageIO.write(mainFrame.getMusicSheet().createMusicSheetImageForExport(Color.white, scale), maf.getExtension(0), saveFile);
                Utilities.openExportFile(mainFrame, saveFile);
            } catch (IOException e1) {
                mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                logger.error("Saving image", e1);
            } catch(OutOfMemoryError e1){
                mainFrame.showErrorMessage("There is no enough memory for this resolution.");
            }
        }
    }
}
