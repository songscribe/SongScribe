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

import songscribe.music.Line;
import songscribe.ui.AbstractTextFocusRejectingAction;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Csaba Kávai
 */
public class AddLineAction extends AbstractTextFocusRejectingAction {
    private MainFrame mainFrame;

    public AddLineAction(MainFrame mainFrame) {
        super(mainFrame);
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "At the end");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("addline.png")));
        putValue(Action.SHORT_DESCRIPTION, "Add a new line at the end");
    }

    public void doActionPerformed(ActionEvent e) {
        mainFrame.getMusicSheet().getComposition().addLine(new Line());
        mainFrame.setModifiedDocument(true);
        mainFrame.getMusicSheet().setRepaintImage(true);
        mainFrame.getMusicSheet().repaint();
    }
}
