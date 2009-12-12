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

import songscribe.IO.CompositionIO;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Csaba Kávai
 */
public class SaveAction extends AbstractAction {
    private static Logger logger = Logger.getLogger(SaveAction.class);
    private MainFrame mainFrame;

    public SaveAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Save");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("filesave.png")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        if(mainFrame.getSaveFile()==null){
            mainFrame.getSaveAsAction().actionPerformed(e);
            return;
        }
        try {
            PrintWriter pw = new PrintWriter(mainFrame.getSaveFile(), "UTF-8");
            CompositionIO.writeComposition(mainFrame.getMusicSheet().getComposition(), pw);
            pw.close();
        } catch (IOException e1) {
            mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
            logger.error("Saving song", e1);
        }
        mainFrame.unmodifiedDocument();
    }
}
