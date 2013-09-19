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

import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * @author Csaba Kávai
 */
public class PrintAction extends AbstractAction implements Printable{
    private MainFrame mainFrame;

    public PrintAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Print");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("fileprint.png")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(this);
        if(printerJob.printDialog()){
            try {
                printerJob.print();
            } catch (PrinterException e1) {
                mainFrame.showErrorMessage("Could not print the song.");
            }
        }
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if(pageIndex>=1){
            return NO_SUCH_PAGE;
        }
        double scale = pageFormat.getImageableWidth()/mainFrame.getMusicSheet().getSheetWidth();
        graphics.translate((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY());
        mainFrame.getMusicSheet().getBestDrawer().drawMusicSheet((Graphics2D)graphics, false, scale);
        return PAGE_EXISTS;
    }
}
