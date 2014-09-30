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

import songscribe.data.MyDesktop;
import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class PDFTutorialOpenAction extends AbstractAction {
    public static final File TUTORIAL_FILE = new File("help/tutorial.pdf");
    private MainFrame mainFrame;

    public PDFTutorialOpenAction(MainFrame mainFrame, String name, String icon) {
        super(name, new ImageIcon(MainFrame.getImage(icon)));
        this.mainFrame = mainFrame;
    }

    public void actionPerformed(ActionEvent ae) {
        try {
            if (MyDesktop.isDesktopSupported()) {
                MyDesktop.getDesktop().open(TUTORIAL_FILE);
            }
            else {
                throw new RuntimeException();
            }
        }
        catch (Exception e) {
            mainFrame.showErrorMessage(String.format("Could not open the external PDF file.\n" +
                                                     "Please navigate to %s with %s and open it.\n" +
                                                     "Also you can try to upgrade your Java version to 1.6 (or higher).", TUTORIAL_FILE.getAbsolutePath(), Utilities.isMac() ? "Finder" : "Explorer"));
        }
    }
}
