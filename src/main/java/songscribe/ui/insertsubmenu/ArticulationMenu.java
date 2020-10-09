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

    Created on May 27, 2006
*/
package songscribe.ui.insertsubmenu;

import songscribe.music.DurationArticulation;
import songscribe.music.ForceArticulation;
import songscribe.music.Note;
import songscribe.ui.MainFrame;

import javax.swing.*;

/**
 * @author Csaba Kávai
 */
public class ArticulationMenu extends InsertSubMenu {
    public ArticulationMenu(MainFrame mainFrame) {
        super(mainFrame, "Articulations", "accent22.gif", null);
        createCheckBoxMenuItem("Accent", ForceArticulation.ACCENT.name(), "accent16.gif", KeyStroke.getKeyStroke('>'));
        addSeparator();
        createCheckBoxMenuItem("Staccato", DurationArticulation.STACCATO.name(), "staccato16.gif", null);
        createCheckBoxMenuItem("Tenuto", DurationArticulation.TENUTO.name(), "tenuto16.gif", null);
    }

    protected void realActionPerformed(String actionCommand) {
        resetButtons();
        Note activeNote = mainFrame.getMusicSheet().getActiveNote();

        if (activeNote == null) {
            return;
        }

        if (ForceArticulation.isExists(actionCommand)) {
            activeNote.setForceArticulation(ForceArticulation.valueOf(actionCommand));
        }
        else {
            activeNote.setDurationArticulation(DurationArticulation.valueOf(actionCommand));
        }

        updateState(activeNote);
        mainFrame.getMusicSheet().repaint();
    }

    public void updateState(Note note) {
        if (note.getForceArticulation() != null) {
            selectItem(note.getForceArticulation().name());
        }

        if (note.getDurationArticulation() != null) {
            selectItem(note.getDurationArticulation().name());
        }
    }

    public void dotPrefixNote(Note note) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i) != null && getItem(i).isSelected()) {
                if (ForceArticulation.isExists(getItem(i).getActionCommand())) {
                    note.setForceArticulation(ForceArticulation.valueOf(getItem(i).getActionCommand()));
                }
                else {
                    note.setDurationArticulation(DurationArticulation.valueOf(getItem(i).getActionCommand()));
                }
            }
        }
    }
}
