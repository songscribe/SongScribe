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

Created on Oct 29, 2006
*/
package songscribe.ui;

import songscribe.ui.playsubmenu.InstrumentDialog;

import javax.swing.*;
import javax.sound.midi.MidiSystem;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Csaba Kávai
 */
public class ExportMidiDialog extends MyDialog{
    private Logger logger = Logger.getLogger(ExportMidiDialog.class);
    private JComboBox instrumentCombo;
    private JCheckBox withRepeatCheck;
    private File saveFile;

    public ExportMidiDialog(MainFrame mainFrame) {
        super(mainFrame, "MIDI properies");
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        MyDialog.addLabelToBox(center, "Instrument:", 5);
        instrumentCombo = new JComboBox(InstrumentDialog.INSTRUMENTSTRING);
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
        instrumentCombo.setSelectedIndex(Integer.parseInt(mainFrame.getProperties().getProperty(Constants.INSTRUMENTPROP)));
        withRepeatCheck.setSelected(mainFrame.getProperties().getProperty(Constants.WITHREPEATPROP).equals(Constants.TRUEVALUE));
    }

    protected void setData() {
        try {
            Properties props = new Properties(mainFrame.getProperties());
            props.setProperty(Constants.WITHREPEATPROP, withRepeatCheck.isSelected() ? Constants.TRUEVALUE : Constants.FALSEVALUE);
            props.setProperty(Constants.INSTRUMENTPROP, Integer.toString(instrumentCombo.getSelectedIndex()));
            props.setProperty(Constants.TEMPOCHANGEPROP, "100");
            mainFrame.getMusicSheet().getComposition().musicChanged(props);
            MidiSystem.write(mainFrame.getMusicSheet().getComposition().getSequence(), 1, saveFile);
            mainFrame.getMusicSheet().getComposition().musicChanged(mainFrame.getProperties());
        } catch (IOException e1) {
            mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
            logger.error("Saving MIDI", e1);
        }
    }
}
