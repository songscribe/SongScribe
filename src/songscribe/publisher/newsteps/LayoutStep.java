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

Created on Aug 8, 2006
*/
package songscribe.publisher.newsteps;

import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
class LayoutStep extends Step{
    private JComboBox layoutCombo = new JComboBox();

    public LayoutStep(Data data) {
        super(data);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        MyDialog.addLabelToBox(mainPanel, "Select the song-per-page format: ", 5);
        layoutCombo.addItem(new LayoutComboElement("At most one", 1));
        layoutCombo.addItem(new LayoutComboElement("At most two", 2));
        layoutCombo.addItem(new LayoutComboElement("At most three", 3));
        layoutCombo.addItem(new LayoutComboElement("At most four", 4));
        layoutCombo.addItem(new LayoutComboElement("As many as fit", Integer.MAX_VALUE));
        layoutCombo.addActionListener(new LayoutComboAction());
        layoutCombo.setAlignmentX(0f);
        mainPanel.add(layoutCombo);
        layoutCombo.setSelectedIndex(1);

        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(mainPanel);
    }

    public String getInfo() {
        return null;
    }

    public void start() {
    }

    public void end() {
    }

    private class LayoutComboElement{
        String str;
        int number;

        public LayoutComboElement(String str, int number) {
            this.str = str;
            this.number = number;
        }

        public String toString() {
            return str;
        }
    }

    private class LayoutComboAction implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            data.songsPerPage = ((LayoutComboElement)layoutCombo.getSelectedItem()).number;
        }
    }
}
