/*
Music of The Supreme song notation program
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

Created on Nov 2, 2007
*/
package songscribe.ui;

import songscribe.data.DoNotShowException;
import songscribe.music.BeatChange;
import songscribe.music.Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

/**
 * @author Csaba Kávai
 */
public class BeatChangeDialog extends MyDialog{
    private Note selectedNote;
    private JButton removeButton;
    private ButtonGroup bg;

    public BeatChangeDialog(MainFrame mainFrame) {
        super(mainFrame, "Beat change");
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bg = new ButtonGroup();
        for(BeatChange bc:BeatChange.values()){
            final JRadioButton rb = new JRadioButton();
            rb.setActionCommand(bc.name());
            bg.add(rb);
            JPanel panel = new JPanel();
            panel.add(rb);
            JComponent component = getBeatChangeComponent(bc);
            component.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    bg.setSelected(rb.getModel(), true);
                }
                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
            });
            panel.add(component);
            center.add(panel);
        }
        dialogPanel.add(center);

        //----------------------south------------------------
        JPanel south = new JPanel();
        removeButton = new JButton("Remove", REMOVEICON);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MusicSheet ms = BeatChangeDialog.this.mainFrame.getMusicSheet();
                selectedNote.setBeatChange(null);
                setVisible(false);
                ms.setRepaintImage(true);
                ms.repaint();
                ms.getComposition().modifiedComposition();
            }
        });
        south.add(okButton);
        south.add(applyButton);
        south.add(removeButton);
        south.add(cancelButton);
        dialogPanel.add(BorderLayout.SOUTH, south);
    }

    protected void getData() throws DoNotShowException {
        MusicSheet ms = mainFrame.getMusicSheet();
        selectedNote = ms.getSingleSelectedNote();
        BeatChange bc = selectedNote.getBeatChange();
        boolean bcnull = bc==null;
        if(!bcnull){
            for(Enumeration<AbstractButton> e = bg.getElements();e.hasMoreElements();){
                AbstractButton ab = e.nextElement();
                if(ab.getActionCommand().equals(bc.name()))bg.setSelected(ab.getModel(), true);
            }
        }

        removeButton.setEnabled(!bcnull);
        if(bcnull){
            okButton.setText("Add");
            applyButton.setText("Apply addition");
        }else{
            okButton.setText("Modify");
            applyButton.setText("Apply modification");
        }
    }

    protected void setData() {
        if(bg.getSelection()==null){
            mainFrame.showErrorMessage("You must select one of the beat change type!");
            return;
        }
        selectedNote.setBeatChange(BeatChange.valueOf(bg.getSelection().getActionCommand()));
    }

    private JComponent getBeatChangeComponent(final BeatChange beatChange){
        return new JComponent() {
            Dimension size = new Dimension(50, 30);

            public Dimension getPreferredSize() {
                return size;
            }

            public Dimension getSize() {
                return size;
            }

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                mainFrame.getMusicSheet().getBestDrawer().drawBeatChange(g2, beatChange, 2, 27);
            }
        };
    }
}
