/*
    SongScribe song notation program
    Copyright (C) 2006 Csaba Kavai

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
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGUtils;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class ExportSVGAction extends AbstractAction {
    private static Logger logger = Logger.getLogger(ExportSVGAction.class);
    private PlatformFileDialog pfd;
    private MainFrame mainFrame;

    public ExportSVGAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as SVG...");
        pfd = new PlatformFileDialog(mainFrame, "Export as SVG", false, new MyAcceptFilter("Portable Document Format", "svg"));
    }

    public static void createSVG(MainFrame mainFrame, File outputFile, Boolean isGUI) {
        try {
            SVGGraphics2D g2 = new SVGGraphics2D(mainFrame.getMusicSheet().getSheetWidth(), mainFrame.getMusicSheet().getSheetHeight());
            mainFrame.getMusicSheet().getBestDrawer().drawMusicSheet(g2, false, 1d);
            SVGUtils.writeToSVG(outputFile, g2.getSVGElement());

            if (isGUI) {
                Utilities.openExportFile(mainFrame, outputFile);
            }
        }
        catch (IOException e1) {
            if (isGUI) {
                mainFrame.showErrorMessage("An unexpected error occurred and could not export as SVG.");
            }

            logger.error("SVG save", e1);
        }
    }

    public void actionPerformed(ActionEvent e) {
        pfd.setFile(Utilities.getSongTitleFileNameForFileChooser(mainFrame.getMusicSheet()));

        if (pfd.showDialog()) {
            File saveFile = pfd.getFile();

            if (!saveFile.getName().toLowerCase().endsWith(".svg")) {
                saveFile = new File(saveFile.getAbsolutePath() + ".svg");
            }

            if (saveFile.exists()) {
                int answer = JOptionPane.showConfirmDialog(mainFrame, "The file " + saveFile.getName() +
                                                                      " already exists. Do you want to overwrite it?", mainFrame.PROG_NAME, JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            createSVG(mainFrame, saveFile, true);
        }
    }
}
