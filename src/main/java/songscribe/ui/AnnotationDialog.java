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

    Created on Jul 23, 2006
*/
package songscribe.ui;

import songscribe.SongScribe;
import songscribe.music.Annotation;
import songscribe.music.Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class AnnotationDialog extends MyDialog {
    Note selectedNote;
    JComboBox annotationCombo;
    JButton removeButton;
    JRadioButton aboveButton, belowButton;
    ButtonGroup alignmentGroup = new ButtonGroup();

    public AnnotationDialog(MainFrame mainFrame) {
        super(mainFrame, "Annotation");

        //----------------------centerPanel------------------------
        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel annotationLabel;
        JPanel verticalPanel;
        JPanel xPanel;

        annotationLabel = new JLabel();
        annotationCombo = new JComboBox();
        xPanel = new JPanel();
        verticalPanel = new JPanel();
        aboveButton = new JRadioButton();
        belowButton = new JRadioButton();

        annotationLabel.setText("Annotation:");

        annotationCombo.setEditable(true);
        Utilities.readComboValuesFromFile(annotationCombo, new File(SongScribe.basePath + "/conf/annotations"));

        xPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Alignment for note"));
        Alignment.left.button.setText("Left");
        Alignment.left.button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Alignment.left.button.setMargin(new java.awt.Insets(0, 0, 0, 0));

        Alignment.center.button.setText("Center");
        Alignment.center.button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Alignment.center.button.setMargin(new java.awt.Insets(0, 0, 0, 0));

        Alignment.right.button.setText("Right");
        Alignment.right.button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Alignment.right.button.setMargin(new java.awt.Insets(0, 0, 0, 0));

        alignmentGroup.add(Alignment.left.button);
        alignmentGroup.add(Alignment.center.button);
        alignmentGroup.add(Alignment.right.button);

        org.jdesktop.layout.GroupLayout xPanelLayout = new org.jdesktop.layout.GroupLayout(xPanel);
        xPanel.setLayout(xPanelLayout);
        xPanelLayout.setHorizontalGroup(xPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(xPanelLayout.createSequentialGroup().addContainerGap().add(xPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(Alignment.left.button).add(Alignment.center.button).add(Alignment.right.button)).addContainerGap(39, Short.MAX_VALUE)));
        xPanelLayout.setVerticalGroup(xPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(xPanelLayout.createSequentialGroup().add(Alignment.left.button).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(Alignment.center.button).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(Alignment.right.button)));

        verticalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Vertical position"));
        aboveButton.setText("Above the staff");
        aboveButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        aboveButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        belowButton.setText("Below the staff");
        belowButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        belowButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        ButtonGroup verticalGroup = new ButtonGroup();
        verticalGroup.add(aboveButton);
        verticalGroup.add(belowButton);

        org.jdesktop.layout.GroupLayout verticalPanelLayout = new org.jdesktop.layout.GroupLayout(verticalPanel);
        verticalPanel.setLayout(verticalPanelLayout);
        verticalPanelLayout.setHorizontalGroup(verticalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(verticalPanelLayout.createSequentialGroup().addContainerGap().add(verticalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(belowButton).add(aboveButton)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        verticalPanelLayout.setVerticalGroup(verticalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, verticalPanelLayout.createSequentialGroup().addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(aboveButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(belowButton).addContainerGap()));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(centerPanel);
        centerPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(xPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(verticalPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).add(layout.createSequentialGroup().add(annotationLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(annotationCombo, 0, 190, Short.MAX_VALUE))).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(annotationLabel).add(annotationCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(verticalPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(xPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, Short.MAX_VALUE)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        dialogPanel.add(centerPanel);

        //----------------------south------------------------
        JPanel south = new JPanel();
        removeButton = new JButton("Remove", REMOVE_ICON);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MusicSheet ms = AnnotationDialog.this.mainFrame.getMusicSheet();
                selectedNote.setAnnotation(null);
                setVisible(false);
                ms.setRepaintImage(true);
                ms.repaint();
                AnnotationDialog.this.mainFrame.modifiedDocument();
            }
        });
        south.add(okButton);
        south.add(applyButton);
        south.add(removeButton);
        south.add(cancelButton);
        dialogPanel.add(BorderLayout.SOUTH, south);
    }

    protected void getData() {
        MusicSheet ms = mainFrame.getMusicSheet();
        selectedNote = ms.getSingleSelectedNote();
        Annotation tc = selectedNote.getAnnotation();
        boolean tcnull = tc == null;

        if (tcnull) {
            tc = new Annotation("fine");
        }

        annotationCombo.setSelectedItem(tc.getAnnotation());

        for (Alignment a : Alignment.values()) {
            if (tc.getXAlignment() == a.value) {
                a.button.setSelected(true);
            }
        }

        JRadioButton oldVerticalButton = tc.getYPos() < 0 ? aboveButton : belowButton;
        oldVerticalButton.setSelected(true);

        removeButton.setEnabled(!tcnull);

        if (tcnull) {
            okButton.setText("Add");
            applyButton.setText("Apply addition");
        }
        else {
            okButton.setText("Modify");
            applyButton.setText("Apply modification");
        }
    }

    protected void setData() {
        Annotation annotation;
        String annotationText = (String) annotationCombo.getSelectedItem();

        if (annotationText == null || annotationText.length() == 0) {
            annotation = null;
        }
        else {
            // horizontal alignment
            Alignment horizontalAlignment = null;

            for (Alignment alignment : Alignment.values()) {
                if (alignment.button.isSelected()) {
                    horizontalAlignment = alignment;
                    break;
                }
            }

            if (horizontalAlignment == null) {
                String message = "Programmer's error: no such horizontal annotation.";
                mainFrame.showErrorMessage(message);
                throw new RuntimeException(message);
            }

            annotation = new Annotation(annotationText, horizontalAlignment.value);

            // vertical alignment
            int yPos;

            if (aboveButton.isSelected()) {
                yPos = Annotation.ABOVE;
            }
            else if (belowButton.isSelected()) {
                yPos = Annotation.BELOW;
            }
            else {
                String message = "Programmer's error: no such vertical annotation.";
                mainFrame.showErrorMessage(message);
                throw new RuntimeException(message);
            }

            annotation.setYPos(yPos);
        }

        selectedNote.setAnnotation(annotation);
        mainFrame.modifiedDocument();
    }

    private enum Alignment {
        left(Component.LEFT_ALIGNMENT),
        center(Component.CENTER_ALIGNMENT),
        right(Component.RIGHT_ALIGNMENT);

        JRadioButton button;
        float value;

        Alignment(float value) {
            this.button = new JRadioButton();
            this.value = value;
        }
    }
}
