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

Created on Jul 23, 2006
*/
package songscribe.ui;

import songscribe.music.Note;
import songscribe.music.Annotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class AnnotationDialog extends MyDialog{
    Note selectedNote;
    JComboBox annotationCombo;
    JButton removeButton;
    JRadioButton aboveButton, belowButton;
    ButtonGroup alignmentGroup = new ButtonGroup();

    private enum Alignment{
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
        Utilities.readComboValuesFromFile(annotationCombo, new File("conf/annotations"));

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

        GroupLayout xPanelLayout = new GroupLayout(xPanel);
        xPanel.setLayout(xPanelLayout);
        xPanelLayout.setHorizontalGroup(
            xPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(xPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(xPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(Alignment.left.button)
                    .addComponent(Alignment.center.button)
                    .addComponent(Alignment.right.button))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        xPanelLayout.setVerticalGroup(
            xPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(xPanelLayout.createSequentialGroup()
                .addComponent(Alignment.left.button)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Alignment.center.button)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Alignment.right.button))
        );

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

        GroupLayout verticalPanelLayout = new GroupLayout(verticalPanel);
        verticalPanel.setLayout(verticalPanelLayout);
        verticalPanelLayout.setHorizontalGroup(
            verticalPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(verticalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(verticalPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(belowButton)
                    .addComponent(aboveButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        verticalPanelLayout.setVerticalGroup(
            verticalPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, verticalPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(aboveButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(belowButton)
                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(centerPanel);
        centerPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(xPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(verticalPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(annotationLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(annotationCombo, 0, 190, Short.MAX_VALUE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(annotationLabel)
                    .addComponent(annotationCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(verticalPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xPanel, GroupLayout.PREFERRED_SIZE, 88, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );       
        dialogPanel.add(centerPanel);

        //----------------------south------------------------
        JPanel south = new JPanel();
        removeButton = new JButton("Remove", REMOVEICON);
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

    private JRadioButton oldVerticalButton;
    protected void getData() {
        MusicSheet ms = mainFrame.getMusicSheet();
        selectedNote = ms.getSingleSelectedNote();
        Annotation tc = selectedNote.getAnnotation();
        boolean tcnull = tc==null;
        if(tcnull){
            tc = new Annotation("fine");
        }
        annotationCombo.setSelectedItem(tc.getAnnotation());

        for(Alignment a:Alignment.values()){
            if(tc.getXalignment()==a.value)a.button.setSelected(true);
        }

        oldVerticalButton = tc.getyPos()<0 ? aboveButton : belowButton;
        oldVerticalButton.setSelected(true);

        removeButton.setEnabled(!tcnull);
        if(tcnull){
            okButton.setText("Add");
            applyButton.setText("Apply addition");
        }else{
            okButton.setText("Modify");
            applyButton.setText("Apply modification");
        }
    }

    protected void setData() {
        for(Alignment a: Alignment.values()){
            if(a.button.isSelected()){
                if(selectedNote.getAnnotation()==null)selectedNote.setAnnotation(new Annotation(""));
                selectedNote.getAnnotation().setAnnotation((String)annotationCombo.getSelectedItem());
                selectedNote.getAnnotation().setXalignment(a.value);
            }
        }
        if(!oldVerticalButton.isSelected()){
            if(aboveButton.isSelected())selectedNote.getAnnotation().setyPos(Annotation.ABOVE);
            else selectedNote.getAnnotation().setyPos(Annotation.BELOW);
        }
        mainFrame.modifiedDocument();
    }
}
