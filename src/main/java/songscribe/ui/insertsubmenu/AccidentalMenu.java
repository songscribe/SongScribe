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
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * @author Csaba Kávai
 */
public class AccidentalMenu extends InsertSubMenu {
    private JMenuItem naturalButton, flatButton;
    private JCheckBoxMenuItem parenthesisButton;

    public AccidentalMenu(MainFrame mainFrame) {
        super(mainFrame, "Accidental", "natural22.gif", null);
        Image[] img16 = {
                MainFrame.getImage("natural16.gif"), MainFrame.getImage("flat16.gif"),
                MainFrame.getImage("sharp16.gif"), MainFrame.getImage("doublesharp16.gif")
        };
        int imgWidth = 5, imgSize = 16;
        String names[] = {
                "Natural",
                "Flat",
                "Sharp",
                "Double natural",
                "Double flat",
                "Double sharp",
                "Natural flat",
                "Natural sharp"
        };

        for (Note.Accidental accidental : Note.Accidental.values()) {
            if (accidental == Note.Accidental.NONE) {
                continue;
            }

            BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            for (int i = 0; i < accidental.getNb(); i++) {
                g2.drawImage(img16[accidental.getComponent(i)],
                        (imgSize - accidental.getNb() * imgWidth) / 2 + i * imgWidth, 0, null);
            }

            g2.dispose();
            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(names[accidental.ordinal() - 1], new ImageIcon(img));
            cbmi.setActionCommand(accidental.name());
            cbmi.addActionListener(this);
            add(cbmi);

            if (accidental == Note.Accidental.NATURAL) {
                naturalButton = cbmi;
            }
            else if (accidental == Note.Accidental.FLAT) {
                flatButton = cbmi;
            }
            else if (accidental == Note.Accidental.SHARP) {
                cbmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
            }
        }

        naturalButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        flatButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));
        addSeparator();
        parenthesisButton = new JCheckBoxMenuItem(new ParenthesisAction());
        add(parenthesisButton);
    }

    protected void realActionPerformed(String actionCommand) {
        resetButtons();
        Note activeNote = mainFrame.getMusicSheet().getActiveNote();

        if (activeNote == null) {
            return;
        }

        Note.Accidental accidental = Note.Accidental.valueOf(actionCommand);
        activeNote.setAccidental(activeNote.getAccidental() == accidental ? Note.Accidental.NONE : accidental);
        updateState(activeNote);
        mainFrame.getMusicSheet().repaint();
    }

    public void updateState(Note note) {
        selectItem(note.getAccidental().name());
        mainFrame.getNoteSelectionPanel().setNaturalSelected(naturalButton.isSelected());
        mainFrame.getNoteSelectionPanel().setFlatSelected(flatButton.isSelected());
        parenthesisButton.setSelected(note.isAccidentalInParenthesis());
    }

    public void dotPrefixNote(Note note) {
        for (int i = 0; i < getItemCount() - 2; i++) {
            if (getItem(i) != null && getItem(i).isSelected()) {
                note.setAccidental(Note.Accidental.valueOf(getItem(i).getActionCommand()));
                break;
            }
        }

        note.setAccidentalInParenthesis(parenthesisButton.isSelected());
    }

    private class ParenthesisAction extends AbstractAction {
        public ParenthesisAction() {
            putValue(Action.NAME, "In parenthesis");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("parenthesis16.png")));
        }

        public void actionPerformed(ActionEvent e) {
            Note activeNote = mainFrame.getMusicSheet().getActiveNote();

            if (activeNote == null) {
                return;
            }

            activeNote.setAccidentalInParenthesis(parenthesisButton.isSelected());
            resetButtons();
            updateState(activeNote);
            mainFrame.getMusicSheet().repaint();
        }
    }
}
