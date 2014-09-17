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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.log4j.Logger;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.publisher.newsteps.Data;
import songscribe.ui.ExportPDFDialog;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
                int answer = JOptionPane.showConfirmDialog(mainFrame, "The file "+saveFile.getName()+" already exists. Do you want to overwrite it?",
                        mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answer==JOptionPane.NO_OPTION){
                    return;
                }
            }

            if(exportPDFDialog==null){
                exportPDFDialog = new ExportPDFDialog(mainFrame);
            }
            exportPDFDialog.setVisible(true);
            Data data = exportPDFDialog.getPaperSizeData();
            if(data==null)return;

            createPDF(data, saveFile, true);
        }
    }

    public static void createPDF(Data data, File outputFile, Boolean isGUI) {
        float resolution = 72f / MusicSheet.RESOLUTION;
        float paperWidth = data.paperWidth * resolution;
        float paperHeight = data.paperHeight * resolution;
        MainFrame mainFrame = data.mainFrame;
        Document document = new Document(new Rectangle(0, 0, paperWidth, paperHeight), 0, 0, 0, 0);
        document.addCreator(mainFrame.PROGNAME);
        document.addTitle(mainFrame.getMusicSheet().getComposition().getSongTitle());

        // Scale to fit
        int sheetWidth = mainFrame.getMusicSheet().getSheetWidth();
        int sheetHeight = mainFrame.getMusicSheet().getSheetHeight();
        double horizontalMargin = (data.leftInnerMargin + data.rightOuterMargin) * resolution;
        double horizontalScale = (paperWidth - horizontalMargin) / sheetWidth;
        double verticalMargin = (data.topMargin + data.bottomMargin) * resolution;
        double verticalScale = (paperHeight - verticalMargin) / sheetHeight;
        double scale;
        double leftMargin = data.leftInnerMargin * resolution;

        if (horizontalScale < verticalScale) {
            scale = horizontalScale;
        }
        else {
            // If scaling vertically, the horizontal margin will be larger than
            // what is specified in Data. So we calculate the total margin available,
            // then give the left margin the same fraction of the total margin
            // it would have had before scaling.
            scale = verticalScale;
            double scaledMargin = paperWidth - (sheetWidth * scale);
            double leftMarginFactor = (double) data.leftInnerMargin / (double) (data.leftInnerMargin + data.rightOuterMargin);
            leftMargin = scaledMargin * leftMarginFactor;
        }

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            Graphics2D g2 = cb.createGraphicsShapes(paperWidth, paperHeight);
            g2.translate(leftMargin, data.topMargin * resolution);
            mainFrame.getMusicSheet().getBestDrawer().drawMusicSheet(g2, false, scale);
            g2.dispose();
            document.close();

            if (isGUI)
                Utilities.openExportFile(mainFrame, outputFile);
        } catch (DocumentException e1) {
            if (isGUI)
                mainFrame.showErrorMessage("An unexpected error occurred and could not export as PDF.");

            logger.error("PDF save", e1);
        } catch (FileNotFoundException e1) {
            if (isGUI)
                mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);

            logger.error("PDF save", e1);
        }
    }
}
