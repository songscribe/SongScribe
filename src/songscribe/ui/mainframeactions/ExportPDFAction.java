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
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.Utilities;
import songscribe.ui.ExportPDFDialog;
import songscribe.publisher.newsteps.Data;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.log4j.Logger;

/**
 * @author Csaba Kávai
 */
public class ExportPDFAction extends AbstractAction{
    private static Logger logger = Logger.getLogger(ExportPDFAction.class);
    private PlatformFileDialog pfd;
    private MainFrame mainFrame;
    private ExportPDFDialog exportPDFDialog;

    public ExportPDFAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as PDF...");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("pdf.png")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        pfd = new PlatformFileDialog(mainFrame, "Export as PDF", false, new MyAcceptFilter("Portable Document Format", "pdf"));
    }

    public void actionPerformed(ActionEvent e) {
        pfd.setFile(Utilities.getSongTitleFileNameForFileChooser(mainFrame.getMusicSheet()));
        if(pfd.showDialog()){
            File saveFile = pfd.getFile();
            if(!saveFile.getName().toLowerCase().endsWith(".pdf")){
                saveFile = new File(saveFile.getAbsolutePath()+".pdf");
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(mainFrame, "The file "+saveFile.getName()+" already exists. Do you want to overwrite it?",
                        mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }

            if(exportPDFDialog==null){
                exportPDFDialog = new ExportPDFDialog(mainFrame);
            }
            exportPDFDialog.setVisible(true);
            Data data = exportPDFDialog.getPaperSizeData();
            if(data==null)return;

            float msres = 72f/ MusicSheet.RESOLUTION;
            float paperWidth = data.paperWidth * msres;
            float paperHeight = data.paperHeight * msres;
            Document document = new Document(new com.lowagie.text.Rectangle(0, 0, paperWidth, paperHeight), 0, 0, 0, 0);
            document.addCreator(mainFrame.PROGNAME);
            document.addTitle(mainFrame.getMusicSheet().getComposition().getSongTitle());
            int sheetWidth = mainFrame.getMusicSheet().getSheetWidth();
            int sheetHeight = mainFrame.getMusicSheet().getSheetHeight();
            double resolution = Math.min((double)msres, Math.min((double)(paperWidth-(data.leftInnerMargin+data.rightOuterMargin)*msres)/sheetWidth,
                    (double)(paperHeight-(data.topMargin+data.bottomMargin)*msres)/sheetHeight));
            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(saveFile));
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                Graphics2D g2 = cb.createGraphicsShapes(paperWidth, paperHeight);
                g2.translate((paperWidth-(data.leftInnerMargin+data.rightOuterMargin+sheetWidth)*msres)/2+data.leftInnerMargin*msres, data.topMargin*msres);
                mainFrame.getMusicSheet().getBestDrawer().drawMusicSheet(g2, false, resolution);
                g2.dispose();
                document.close();
                Utilities.openExportFile(mainFrame, saveFile);
            } catch (DocumentException e1) {
                mainFrame.showErrorMessage("An unexprected error occured and could not export into PDF.");
                logger.error("PDF save", e1);
            } catch (FileNotFoundException e1) {
                mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                logger.error("PDF save", e1);
            }
        }
    }
}
