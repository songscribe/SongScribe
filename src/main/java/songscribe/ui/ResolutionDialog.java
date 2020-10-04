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

    Created on Jun 26, 2006
*/
package songscribe.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import songscribe.data.MyBorder;
import songscribe.music.Composition;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public class ResolutionDialog extends MyDialog implements ChangeListener {
    private boolean approved;
    private MainFrame mainFrame;
    private JPanel mainPanel;
    private JTextField widthField;
    private JTextField heightField;
    private JCheckBox withoutLyricsCheck;
    private JSpinner resolutionSpinner;
    private BorderPanel borderPanel;
    private JCheckBox exportWithoutTitleCheckBox;
    private int msWidth, msHeight, msHeightWL, msHeightWT;

    public ResolutionDialog(MainFrame mainFrame) {
        super(mainFrame, "Image properties");
        this.mainFrame = mainFrame;
        borderPanel.setPackListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pack();
            }
        });
        borderPanel.addChangeListener(this);
        resolutionSpinner.addChangeListener(this);
        withoutLyricsCheck.addChangeListener(this);
        exportWithoutTitleCheckBox.addChangeListener(this);
        resolutionSpinner.setModel(new SpinnerNumberModel(300, 30, 1200, 1));
        dialogPanel.add(BorderLayout.CENTER, mainPanel);
        southPanel = new JPanel();
        southPanel.add(okButton);
        southPanel.add(cancelButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData() {
        approved = false;
        resolutionSpinner.setValue(Integer.parseInt(mainFrame.getProperties().getProperty(Constants.DPI_PROP)));
        MusicSheet musicSheet = mainFrame.getMusicSheet();
        Composition composition = musicSheet.getComposition();
        msWidth = musicSheet.getSheetWidth();
        msHeight = musicSheet.getSheetHeight();

        String underLyrics = composition.getUnderLyrics();
        String translatedLyrics = composition.getTranslatedLyrics();
        composition.setUnderLyrics("");
        composition.setTranslatedLyrics("");
        msHeightWL = msHeight - musicSheet.getSheetHeight();
        composition.setUnderLyrics(underLyrics);
        composition.setTranslatedLyrics(translatedLyrics);

        String songTitle = composition.getSongTitle();
        composition.setSongTitle("");
        msHeightWT = msHeight - musicSheet.getSheetHeight();
        composition.setSongTitle(songTitle);

        if (underLyrics.length() == 0 && translatedLyrics.length() == 0) {
            withoutLyricsCheck.setSelected(false);
            withoutLyricsCheck.setEnabled(false);
        } else {
            withoutLyricsCheck.setEnabled(true);
        }

        if (composition.getSongTitle().length() == 0) {
            exportWithoutTitleCheckBox.setSelected(false);
            exportWithoutTitleCheckBox.setEnabled(false);
        } else {
            exportWithoutTitleCheckBox.setEnabled(true);
        }

        borderPanel.setExpertBorder(false);
        stateChanged(null);
    }

    protected void setData() {
        approved = true;
        mainFrame.getProperties().setProperty(Constants.DPI_PROP, resolutionSpinner.getValue().toString());
    }

    public boolean isApproved() {
        return approved;
    }

    public int getResolution() {
        return (Integer) resolutionSpinner.getValue();
    }

    public boolean isWithoutLyrics() {
        return withoutLyricsCheck.isSelected();
    }

    public boolean isWithoutTitle() {
        return exportWithoutTitleCheckBox.isSelected();
    }

    public MyBorder getBorder() {
        return borderPanel.getMyBorder();
    }

    public void stateChanged(ChangeEvent e) {
        float scale = (float) getResolution() / (float) MusicSheet.RESOLUTION;
        MyBorder myBorder = borderPanel.getMyBorder();
        widthField.setText(Integer.toString(Math.round(scale * msWidth) + myBorder.getWidth()));
        int height = msHeight;

        if (withoutLyricsCheck.isSelected()) {
            height -= msHeightWL;
        }

        if (exportWithoutTitleCheckBox.isSelected()) {
            height -= msHeightWT;
        }

        heightField.setText(Integer.toString(Math.round(scale * height) + myBorder.getHeight()));
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Image resolution:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        resolutionSpinner = new JSpinner();
        panel1.add(resolutionSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 22), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("DPI");
        panel1.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        withoutLyricsCheck = new JCheckBox();
        withoutLyricsCheck.setText("Export without lyrics under the song");
        mainPanel.add(withoutLyricsCheck, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        borderPanel = new BorderPanel();
        panel2.add(borderPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Image size", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("Width:");
        panel4.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Height:");
        panel4.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        widthField = new JTextField();
        widthField.setEditable(false);
        panel4.add(widthField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        heightField = new JTextField();
        heightField.setEditable(false);
        panel4.add(heightField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("px");
        panel4.add(label5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("px");
        panel4.add(label6, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel3.add(spacer4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel3.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        exportWithoutTitleCheckBox = new JCheckBox();
        exportWithoutTitleCheckBox.setText("Export without title");
        mainPanel.add(exportWithoutTitleCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
