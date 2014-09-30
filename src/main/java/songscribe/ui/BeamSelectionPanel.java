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

    Created on Aug 12, 2005
*/
package songscribe.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public class BeamSelectionPanel extends SelectionPanel implements ActionListener {
    private JButton[] buttons;

    public BeamSelectionPanel(MainFrame mainFrame) {
        super(mainFrame);
        String[] images = { "beam.gif", "unbeam.gif", "triplet.gif", "untriplet.gif", "tie.gif", "untie.gif" };
        String[] toolTips = { "Beam", "Unbeam", "Triplet", "Untriplet", "Tie", "Untie" };
        buttons = new JButton[toolTips.length];

        for (int i = 0; i < toolTips.length; i++) {
            buttons[i] = new JButton(new ImageIcon(MainFrame.getImage(images[i])));
            buttons[i].setToolTipText(toolTips[i]);
            buttons[i].setActionCommand(Integer.toString(i));
            buttons[i].addActionListener(this);
            addSelectionComponent(buttons[i]);

            if (i % 2 == 1) {
                addSeparator();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();

        if (source == buttons[0]) {
            mainFrame.getMusicSheet().beamSelectedNotes(true);
        }
        else if (source == buttons[1]) {
            mainFrame.getMusicSheet().beamSelectedNotes(false);
        }
        else if (source == buttons[2]) {
            mainFrame.getMusicSheet().tupletSelectedNotes(3);
        }
        else if (source == buttons[3]) {
            mainFrame.getMusicSheet().untupletSelectedNotes();
        }
        else if (source == buttons[4]) {
            mainFrame.getMusicSheet().tieSelectedNotes(true);
        }
        else if (source == buttons[5]) {
            mainFrame.getMusicSheet().tieSelectedNotes(false);
        }
    }

    public void setActive() {
        mainFrame.getMusicSheet().setActiveNote(null);

        if (mainFrame.getInsertMenu() != null) {
            mainFrame.getInsertMenu().updateState();
        }

        mainFrame.getMusicSheet().setInSelection(false);
    }
}
