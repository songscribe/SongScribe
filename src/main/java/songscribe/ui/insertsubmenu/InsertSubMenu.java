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

import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.ui.MainFrame;
import songscribe.ui.SelectionPanel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public abstract class InsertSubMenu extends JMenu implements ActionListener {
    protected MainFrame mainFrame;
    protected SelectionPanel selectionPanel;

    public InsertSubMenu(MainFrame mainFrame, String name, String icon, SelectionPanel selectionPanel) {
        super(name);
        this.mainFrame = mainFrame;
        this.selectionPanel = selectionPanel;
        setIcon(new ImageIcon(MainFrame.getImage(icon)));
    }

    public void actionPerformed(ActionEvent e) {
        // we don't want to receive actions when the focus is on text panels
        if (!(mainFrame.getFocusOwner() instanceof JTextComponent)) {
            realActionPerformed(e.getActionCommand());

            if (selectionPanel != null) {
                selectionPanel.setSelected(e.getActionCommand());
                mainFrame.setSelectedTool(selectionPanel);
            }
        }
    }

    public void doClickNote(String action) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i) != null && action.equals(getItem(i).getActionCommand())) {
                realActionPerformed(action);
                break;
            }
        }
    }

    protected void realActionPerformed(String actionCommand) {
        try {
            Note note = NoteType.valueOf(actionCommand).newInstance();
            mainFrame.getInsertMenu().dotPrefixNote(note);
            mainFrame.getMusicSheet().setActiveNote(note);
            mainFrame.getInsertMenu().updateState();
        }
        catch (IllegalArgumentException ex) {
            // no harm; handled in subclasses
        }
    }

    public void resetButtons() {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i) != null) {
                getItem(i).setSelected(false);
            }
        }
    }

    public abstract void updateState(Note note);

    protected void selectItem(String name) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i) != null && name.equals(getItem(i).getActionCommand())) {
                getItem(i).setSelected(true);
                break;
            }
        }
    }

    public abstract void dotPrefixNote(Note note);

    protected JCheckBoxMenuItem createCheckBoxMenuItem(String name, String actionCommand, String icon, KeyStroke acceleratorKey) {
        ImageIcon imageIcon = null;
        if (icon != null) {
            imageIcon = new ImageIcon(MainFrame.getImage(icon));
        }
        return createCheckBoxMenuItem(name, actionCommand, imageIcon, acceleratorKey);
    }

    protected JCheckBoxMenuItem createCheckBoxMenuItem(String name, String actionCommand, ImageIcon icon, KeyStroke acceleratorKey) {
        JCheckBoxMenuItem checkboxButton = new JCheckBoxMenuItem(name);
        checkboxButton.setActionCommand(actionCommand);

        if (icon != null) {
            checkboxButton.setIcon(icon);
        }

        if (acceleratorKey != null) {
            checkboxButton.setAccelerator(acceleratorKey);
            Object o = new Object();
            mainFrame.getMusicSheet().getInputMap(JComponent.WHEN_FOCUSED).put(acceleratorKey, o);
            mainFrame.getMusicSheet().getActionMap().put(o, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    realActionPerformed(actionCommand);
                }
            });
        }

        checkboxButton.addActionListener(this);
        add(checkboxButton);
        return checkboxButton;
    }
}
