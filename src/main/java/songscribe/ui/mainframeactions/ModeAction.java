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

import songscribe.ui.MainFrame;
import songscribe.ui.MultiAccessibleAction;
import songscribe.ui.MusicSheet;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class ModeAction extends MultiAccessibleAction {
    private MusicSheet.Mode mode;
    private MainFrame mainFrame;

    public ModeAction(MainFrame mainFrame, MusicSheet.Mode mode) {
        this.mainFrame = mainFrame;
        this.mode = mode;
        putValue(Action.NAME, mode.getDescription());
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage(mode.name().toLowerCase() + ".png")));
        putValue(Action.SHORT_DESCRIPTION, mode.getDescription() + " mode");
    }

    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        mainFrame.getMusicSheet().setMode(mode);
        mainFrame.getControlMenu().setEnabled(mode == MusicSheet.Mode.NOTE_EDIT);
        mainFrame.getStatusBar().setModeLabel(mode.getDescription());

        if (mode != MusicSheet.Mode.NOTE_EDIT) {
            mainFrame.setSelectedTool(mainFrame.getSelectSelectionPanel());
        }
    }
}
