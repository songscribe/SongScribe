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

Created on 2005.01.25., 0:40:53
*/

package songscribe.ui;

import songscribe.music.Note;
import songscribe.music.NoteType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 *
 */
public class RestSelectionPanel extends SelectionPanel implements ActionListener{
    private JToggleButton dotButton = new JToggleButton();

    public RestSelectionPanel(MainFrame mainFrame) {
        super(mainFrame);
        for (NoteType nt : NoteType.values()) {
            if (nt.isRest()) {
                JToggleButton restButton = new JToggleButton();
                restButton.setToolTipText(nt.getCompoundName());
                restButton.setIcon(new ImageIcon(Note.clipNoteImage(nt.getInstance().getUpImage(), nt.getInstance().getRealUpNoteRect(), Color.yellow, SELECTIONIMAGEDIM)));
                restButton.addActionListener(this);
                restButton.setActionCommand(nt.name());
                addSelectionComponent(restButton);
                selectionGroup.add(restButton);
            }
        }

        
        ((AbstractButton)getComponent(4)).setSelected(true);
    }
}
