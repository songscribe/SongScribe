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
*/
package songscribe.ui.insertsubmenu;

import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FermataMenuItem extends JCheckBoxMenuItem implements ActionListener {
    private MainFrame mainFrame;

    public FermataMenuItem(MainFrame mainFrame) {
        super("Fermata", new ImageIcon(MainFrame.getImage("fermata22.png")));
        this.mainFrame = mainFrame;
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        mainFrame.getMusicSheet().getActiveNote().setFermata(isSelected());
        mainFrame.getMusicSheet().repaint();
    }
}
