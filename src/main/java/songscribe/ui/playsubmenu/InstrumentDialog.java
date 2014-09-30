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

    Created on Oct 8, 2006
*/
package songscribe.ui.playsubmenu;

import org.apache.log4j.Logger;
import songscribe.music.Composition;
import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.MyDialog;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author Csaba Kávai
 */
public class InstrumentDialog extends MyDialog {
    public static final String[] INSTRUMENT_STRING = instrumentComboFactory();
    private JList instrumentList = new JList(INSTRUMENT_STRING);
    private static final Logger logger = Logger.getLogger(InstrumentDialog.class);

    public InstrumentDialog(MainFrame mainFrame) {
        super(mainFrame, "Instrument select");
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel center = new JPanel();
        center.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        instrumentList.setAlignmentY(0.5f);
        instrumentList.setVisibleRowCount(10);
        instrumentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        center.add(new JScrollPane(instrumentList));
        center.add(Box.createHorizontalStrut(20));
        JButton scaleButton = new JButton(new ScaleAction());
        scaleButton.setAlignmentY(0.5f);
        center.add(scaleButton);
        dialogPanel.add(BorderLayout.CENTER, center);
        southPanel.remove(applyButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }


    private static String[] instrumentComboFactory() {
        ArrayList<String> vs = new ArrayList<String>(128);
        if (MainFrame.synthesizer != null) {
            int i = 1;

            for (Instrument instrument : MainFrame.synthesizer.getLoadedInstruments()) {
                vs.add(i + " - " + instrument.getName());

                if (i++ == 128) {
                    break;
                }
            }
        }

        return vs.toArray(new String[vs.size()]);
    }

    protected void getData() {
        instrumentList.setSelectedIndex(Integer.parseInt(mainFrame.getProperties().getProperty(Constants.INSTRUMENT_PROP)));
        instrumentList.ensureIndexIsVisible(instrumentList.getSelectedIndex());
    }

    protected void setData() {
        mainFrame.getProperties().setProperty(Constants.INSTRUMENT_PROP, Integer.toString(instrumentList.getSelectedIndex()));
        mainFrame.fireMusicChanged(this);
    }

    private class ScaleAction extends AbstractAction {
        private final int[] SCALE = { 60, 62, 64, 65, 67, 69, 71, 72 };

        public ScaleAction() {
            putValue(NAME, "Play the scales");
            setEnabled(MainFrame.sequencer != null);
        }

        public void actionPerformed(ActionEvent e) {
            if (MainFrame.sequencer == null) {
                return;
            }

            try {
                Sequence sequence = new Sequence(Sequence.PPQ, Composition.PPQ, 0);
                Track track = sequence.createTrack();
                ShortMessage programChange = new ShortMessage();
                programChange.setMessage(0xc0, instrumentList.getSelectedIndex(), 0);
                track.add(new MidiEvent(programChange, 0));
                MetaMessage tempoMessage = new MetaMessage();
                int midiTempo = 60000000 / 120;
                tempoMessage.setMessage(0x51, new byte[] {
                        (byte) (midiTempo >> 16),
                        (byte) (midiTempo >> 8),
                        (byte) (midiTempo)
                }, 3);
                track.add(new MidiEvent(tempoMessage, 0));

                int ticks = 0;

                for (int pitch : SCALE) {
                    ShortMessage down = new ShortMessage();
                    down.setMessage(0x90, pitch, Composition.VELOCITY[0]);
                    track.add(new MidiEvent(down, ticks));

                    ticks += Composition.PPQ / 2;
                    ShortMessage up = new ShortMessage();
                    up.setMessage(0x80, pitch, Composition.VELOCITY[0]);
                    track.add(new MidiEvent(up, ticks));
                }

                MainFrame.sequencer.setSequence(sequence);
                MainFrame.sequencer.setTickPosition(0);
                MainFrame.sequencer.start();
            }
            catch (InvalidMidiDataException ex) {
                mainFrame.showErrorMessage("Could not play the scale because of an unexpected error.");
                logger.error("Creating MIDI sequence", ex);
            }
        }
    }
}
