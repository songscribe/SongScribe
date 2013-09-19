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

Created on Jan 3, 2007
*/
package songscribe.ui;

import songscribe.music.KeyType;
import songscribe.music.Line;

import javax.swing.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class KeySignatureChangeDialog extends MyDialog{
    Line selectedLine;
    JLabel indexOfSelectedNoteLabel = new JLabel();
    JComboBox keysCombo;
    SpinnerModel keysSpinner = new SpinnerNumberModel(4, 0, 7, 1);

    public KeySignatureChangeDialog(MainFrame mainFrame) {
        super(mainFrame, "Key Signature Change");

        //----------------------center------------------------
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Dimension large = new Dimension(0, 15);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        infoPanel.add(new JLabel("Index of selected line:"));
        infoPanel.add(indexOfSelectedNoteLabel);
        infoPanel.setAlignmentX(0f);
        center.add(infoPanel);
        center.add(Box.createRigidArea(large));


        JPanel keysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        keysCombo = new JComboBox(new KeyType[]{KeyType.FLATS, KeyType.SHARPS});
        keysCombo.setRenderer(new CompositionSettingsDialog.KeysImageListCellRenderer());
        keysPanel.add(keysCombo);
        JSpinner ts = new JSpinner(keysSpinner);
        keysPanel.add(ts);
        keysPanel.setAlignmentX(0f);
        center.add(keysPanel);
        dialogPanel.add(center);

        //----------------------south------------------------
        JPanel south = new JPanel();
        south.add(okButton);
        south.add(applyButton);
        south.add(cancelButton);
        dialogPanel.add(BorderLayout.SOUTH, south);
    }

    protected void getData() {
        MusicSheet ms = mainFrame.getMusicSheet();
        Line l = ms.getComposition().getLine(ms.getSelectedLine());
        indexOfSelectedNoteLabel.setText(Integer.toString(ms.getComposition().indexOfLine(l)+1));
        keysCombo.setSelectedItem(l.getKeyType());
        keysSpinner.setValue(l.getKeys());
    }

    protected void setData() {
        MusicSheet ms = mainFrame.getMusicSheet();
        Line l = ms.getComposition().getLine(ms.getSelectedLine());
        l.setKeyType((KeyType) keysCombo.getSelectedItem());
        l.setKeys((Integer) keysSpinner.getValue());
    }
}
