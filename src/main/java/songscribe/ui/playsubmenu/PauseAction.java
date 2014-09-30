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

    Created on Oct 8, 2006
*/
package songscribe.ui.playsubmenu;

import songscribe.ui.AbstractTextFocusRejectingAction;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Csaba Kávai
 */
class PauseAction extends AbstractTextFocusRejectingAction {
    private PlayMenu playMenu;

    public PauseAction(PlayMenu playMenu) {
        super(playMenu.getMainFrame());
        this.playMenu = playMenu;
        putValue(Action.NAME, "Pause");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("player_pause.png")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        setEnabled(MainFrame.sequencer != null);
    }

    public void doActionPerformed(ActionEvent e) {
        if (MainFrame.sequencer == null) {
            return;
        }

        MainFrame.sequencer.stop();
        playMenu.enableAllComponents(true);

        if (playMenu.disableComponentsThread.isAlive()) {
            playMenu.disableComponentsThread.interrupt();
        }
    }
}
