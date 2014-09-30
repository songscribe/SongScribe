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

    Created on Sep 23, 2005
*/
package songscribe.ui;

import songscribe.music.Note;
import songscribe.music.NoteType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public class OtherSelectionPanel extends SelectionPanel implements ActionListener {
    public OtherSelectionPanel(MainFrame mainFrame) {
        super(mainFrame);
        NoteType[] noteTypes = new NoteType[] {
                NoteType.REPEAT_LEFT, NoteType.REPEAT_RIGHT, NoteType.BREATH_MARK, NoteType.SINGLE_BARLINE,
                NoteType.DOUBLE_BARLINE, NoteType.FINAL_DOUBLE_BARLINE
        };

        for (NoteType nt : noteTypes) {
            JToggleButton otherButton = new JToggleButton();
            otherButton.setToolTipText(nt.getCompoundName());
            otherButton.setIcon(new ImageIcon(Note.clipNoteImage(nt.getInstance().getUpImage(), nt.getInstance().getRealUpNoteRect(), Color.yellow, SELECTION_IMAGE_DIM)));
            otherButton.addActionListener(this);
            otherButton.setActionCommand(nt.name());
            addSelectionComponent(otherButton);
            selectionGroup.add(otherButton);
        }

        ((AbstractButton) getComponent(0)).setSelected(true);
    }
}
