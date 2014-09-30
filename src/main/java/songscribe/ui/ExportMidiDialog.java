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

    Created on Oct 29, 2006
*/
package songscribe.ui;

import org.apache.log4j.Logger;
import songscribe.ui.playsubmenu.InstrumentDialog;

import javax.sound.midi.MidiSystem;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Csaba Kávai
 */
public class ExportMidiDialog extends MyDialog {
    private Logger logger = Logger.getLogger(ExportMidiDialog.class);
    private JComboBox instrumentCombo;
    private JCheckBox withRepeatCheck;
    private File saveFile;

    public ExportMidiDialog(MainFrame mainFrame) {
        super(mainFrame, "MIDI properties");
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        MyDialog.addLabelToBox(center, "Instrument:", 5);
        instrumentCombo = new JComboBox(InstrumentDialog.INSTRUMENT_STRING);
        instrumentCombo.setAlignmentX(0f);
        center.add(instrumentCombo);
        center.add(Box.createVerticalStrut(15));
        withRepeatCheck = new JCheckBox("Export with repeats");
        withRepeatCheck.setAlignmentX(0f);
        center.add(withRepeatCheck);
        dialogPanel.add(BorderLayout.CENTER, center);
        southPanel.remove(applyButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    public void setSaveFile(File saveFile) {
        this.saveFile = saveFile;
    }

    protected void getData() {
        instrumentCombo.setSelectedIndex(Integer.parseInt(mainFrame.getProperties().getProperty(Constants.INSTRUMENT_PROP)));
        withRepeatCheck.setSelected(mainFrame.getProperties().getProperty(Constants.WITH_REPEAT_PROP).equals(Constants.TRUE_VALUE));
    }

    protected void setData() {
        try {
            Properties props = new Properties(mainFrame.getProperties());
            props.setProperty(Constants.WITH_REPEAT_PROP, withRepeatCheck.isSelected() ? Constants.TRUE_VALUE : Constants.FALSE_VALUE);
            props.setProperty(Constants.INSTRUMENT_PROP, Integer.toString(instrumentCombo.getSelectedIndex()));
            props.setProperty(Constants.TEMPO_CHANGE_PROP, "100");
            mainFrame.getMusicSheet().getComposition().musicChanged(props);
            MidiSystem.write(mainFrame.getMusicSheet().getComposition().getSequence(), 1, saveFile);
            mainFrame.getMusicSheet().getComposition().musicChanged(mainFrame.getProperties());
            Utilities.openExportFile(mainFrame, saveFile);
        }
        catch (IOException e1) {
            mainFrame.showErrorMessage(MainFrame.COULD_NOT_SAVE_MESSAGE);
            logger.error("Saving MIDI", e1);
        }
    }
}
