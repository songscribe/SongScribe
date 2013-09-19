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

Created on Oct 2, 2006
*/
package songscribe.publisher.publisheractions;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.log4j.Logger;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.publisher.Book;
import songscribe.publisher.Page;
import songscribe.publisher.Publisher;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class ExportPDFAction extends AbstractAction{
    private static Logger logger = Logger.getLogger(songscribe.ui.mainframeactions.ExportPDFAction.class);
    private PlatformFileDialog pfd;
    private Publisher publisher;

    public ExportPDFAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Export as PDF...");
        putValue(SMALL_ICON, new ImageIcon(MainFrame.getImage("pdf.png")));
        pfd = new PlatformFileDialog(publisher, "Export as PDF", false, new MyAcceptFilter("Portable Document Format", "pdf"));
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        if(pfd.showDialog()){
            File saveFile = pfd.getFile();
            if(!saveFile.getName().toLowerCase().endsWith(".pdf")){
                saveFile = new File(saveFile.getAbsolutePath()+".pdf");
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(publisher, "The file "+saveFile.getName()+" already exists. Do you want to overwrite it?",
                        publisher.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }
            float resolution = 72f/ MusicSheet.RESOLUTION;
            Book book = publisher.getBook();
            Document document = new Document(new com.lowagie.text.Rectangle(book.getPageSize().x*resolution, book.getPageSize().y*resolution, book.getPageSize().width*resolution, book.getPageSize().height*resolution), 0, 0, 0, 0);
            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(saveFile));
                document.addCreator(publisher.PROGNAME);
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                for(ListIterator<Page> it = book.pageIterator();it.hasNext();){
                    Graphics2D g2 = cb.createGraphicsShapes(book.getPageSize().width*resolution, book.getPageSize().height*resolution);
                    g2.scale(resolution, resolution);
                    it.next().paint(g2, it.nextIndex()-1, false, 0, book.getPageSize().height);
                    g2.dispose();
                    if(it.hasNext()){
                        document.newPage();
                    }
                }
                document.close();
                Utilities.openExportFile(publisher, saveFile);
            } catch (DocumentException e1) {
                publisher.showErrorMessage("An unexprected error occured and could not export into PDF.");
                logger.error("PDF save", e1);
            } catch (FileNotFoundException e1) {
                publisher.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                logger.error("PDF save", e1);
            }
        }
    }
}
