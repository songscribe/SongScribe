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

import songscribe.SongScribe;
import songscribe.music.Note;
import songscribe.music.Tempo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class TempoChangeDialog extends MyDialog {
    Note selectedNote;
    JLabel indexOfSelectedNoteLabel = new JLabel();
    JComboBox tempoTypeCombo;
    SpinnerModel tempoSpinner = new SpinnerNumberModel(120, 40, 220, 1);
    JComboBox tempoDescriptionCombo = new JComboBox();
    JCheckBox showOnlyDescriptionCheckBox = new JCheckBox("Show only the tempo description");
    JButton removeButton;

    public TempoChangeDialog(MainFrame mainFrame) {
        super(mainFrame, "Tempo change");

        //----------------------center------------------------
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Dimension large = new Dimension(0, 15);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        infoPanel.add(new JLabel("Index of selected note:"));
        infoPanel.add(indexOfSelectedNoteLabel);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(infoPanel);
        center.add(Box.createRigidArea(large));

        JPanel tempoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tempoTypeCombo = new JComboBox(Tempo.Type.values());
        tempoTypeCombo.setRenderer(new CompositionSettingsDialog.NoteImageListCellRenderer());
        tempoPanel.add(tempoTypeCombo);
        tempoPanel.add(new JLabel("="));
        JSpinner ts = new JSpinner(tempoSpinner);
        tempoPanel.add(ts);
        tempoPanel.add(new JLabel("Description:"));
        tempoDescriptionCombo.setEditable(true);
        Utilities.readComboValuesFromFile(tempoDescriptionCombo, new File(SongScribe.basePath + "/conf/tempochanges"));
        Utilities.readComboValuesFromFile(tempoDescriptionCombo, new File(SongScribe.basePath + "/conf/tempos"));
        tempoPanel.add(tempoDescriptionCombo);
        tempoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(tempoPanel);
        center.add(Box.createRigidArea(large));
        center.add(showOnlyDescriptionCheckBox);
        dialogPanel.add(center);

        //----------------------south------------------------
        JPanel south = new JPanel();
        removeButton = new JButton("Remove", REMOVE_ICON);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MusicSheet ms = TempoChangeDialog.this.mainFrame.getMusicSheet();
                selectedNote.setTempoChange(null);
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

    protected void getData() {
        MusicSheet ms = mainFrame.getMusicSheet();
        selectedNote = ms.getSingleSelectedNote();
        Tempo tc = selectedNote.getTempoChange();
        boolean tcnull = tc == null;

        if (tcnull) {
            tc = new Tempo(144, Tempo.Type.CROTCHET, "Slower", true);
        }

        indexOfSelectedNoteLabel.setText((ms.getComposition().indexOfLine(selectedNote.getLine()) + 1) + ". line " +
                                         (selectedNote.getLine().getNoteIndex(selectedNote) + 1) + ". note");
        tempoTypeCombo.setSelectedIndex(tc.getTempoType().ordinal());
        tempoSpinner.setValue(tc.getVisibleTempo());
        tempoDescriptionCombo.setSelectedItem(tc.getTempoDescription());
        showOnlyDescriptionCheckBox.setSelected(!tc.isShowTempo());

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
        selectedNote.setTempoChange(new Tempo(((Integer) tempoSpinner.getValue()), (Tempo.Type) tempoTypeCombo.getSelectedItem(), (String) tempoDescriptionCombo.getSelectedItem(), !showOnlyDescriptionCheckBox.isSelected()));
        mainFrame.getMusicSheet().getComposition().modifiedComposition();
    }
}
