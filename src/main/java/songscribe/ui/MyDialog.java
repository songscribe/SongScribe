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

    Created on 2005.10.08.
*/
package songscribe.ui;

import songscribe.data.DoNotShowException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Csaba Kávai
 */
public abstract class MyDialog {
    public static final Icon OK_ICON = new ImageIcon(MainFrame.getImage("ok.png"));
    public static final Icon APPLY_ICON = new ImageIcon(MainFrame.getImage("add.png"));
    public static final Icon REMOVE_ICON = new ImageIcon(MainFrame.getImage("remove.png"));
    public static final Icon CANCEL_ICON = new ImageIcon(MainFrame.getImage("cancel.png"));
    protected MainFrame mainFrame;
    protected String dialogTitle;
    protected boolean modal;
    protected JPanel dialogPanel = new JPanel(new BorderLayout());
    protected JPanel southPanel = new JPanel();
    protected JButton okButton, applyButton, cancelButton;
    private Point prevLocation;
    private JDialog dialog;

    protected MyDialog(MainFrame mainFrame, String dialogTitle) {
        this(mainFrame, dialogTitle, true);
    }

    protected MyDialog(MainFrame mainFrame, String dialogTitle, boolean modal) {
        this.mainFrame = mainFrame;
        this.dialogTitle = dialogTitle;
        this.modal = modal;
        okButton = new JButton("OK", OK_ICON);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setData();

                if (MyDialog.this.mainFrame.getMusicSheet() != null) {
                    MyDialog.this.mainFrame.getMusicSheet().setRepaintImage(true);
                    MyDialog.this.mainFrame.getMusicSheet().repaint();
                }

                setVisible(false);
            }
        });
        applyButton = new JButton("Apply", APPLY_ICON);
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setData();

                if (MyDialog.this.mainFrame.getMusicSheet() != null) {
                    MyDialog.this.mainFrame.getMusicSheet().setRepaintImage(true);
                    MyDialog.this.mainFrame.getMusicSheet().repaint();
                }
            }
        });
        cancelButton = new JButton("Cancel", CANCEL_ICON);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        southPanel.add(okButton);
        southPanel.add(applyButton);
        southPanel.add(cancelButton);
    }

    public static void addLabelToBox(JPanel box, String text, int gapHeight) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(0f);
        box.add(label);

        if (gapHeight > 0) {
            box.add(Box.createVerticalStrut(gapHeight));
        }
    }

    public void setVisible(boolean visible) {
        if (visible) {
            dialog = new JDialog(mainFrame, dialogTitle, modal);
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                }
            });
            dialog.setContentPane(dialogPanel);
            dialog.getRootPane().setDefaultButton(okButton);

            try {
                getData();
                dialog.pack();

                if (prevLocation == null) {
                    prevLocation = new Point(
                            MainFrame.CENTER_POINT.x - dialog.getWidth() / 2,
                            MainFrame.CENTER_POINT.y - dialog.getHeight() / 2);
                }

                dialog.setLocation(prevLocation);
                dialog.setVisible(true);
            }
            catch (DoNotShowException e) {
                // pass
            }
        }
        else {
            prevLocation = new Point(dialog.getLocation());
            dialog.dispose();
        }
    }

    protected void pack() {
        dialog.pack();
    }

    protected abstract void getData() throws DoNotShowException;

    protected abstract void setData();
}
