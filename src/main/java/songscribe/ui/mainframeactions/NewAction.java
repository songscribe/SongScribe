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

import songscribe.music.Composition;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Csaba Kávai
 */
public class NewAction extends AbstractAction {
    private MainFrame mainFrame;

    public NewAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "New");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("filenew.png")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        if (!mainFrame.showSaveDialog()) {
            return;
        }

        mainFrame.setSaveFile(null);
        mainFrame.getMusicSheet().setComposition(new Composition(mainFrame));
        mainFrame.setSelectedTool(mainFrame.getNoteSelectionPanel());
        mainFrame.getNoteSelectionPanel().setActive();
        mainFrame.setModifiedDocument(false);
    }
}
