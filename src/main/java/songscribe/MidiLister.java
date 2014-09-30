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

    Created on Jul 23, 2005
*/
package songscribe;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class MidiLister {
    static final JFrame frame = new JFrame("MidiLister");
    static JTextField inputFileField = new JTextField(60);
    static JButton openButton = new JButton("Megnyit");
    static JButton listButton = new JButton("Listáz");
    static JTextArea listArea = new JTextArea(30, 80);
    static File previousFile = new File(".");

    public static void main(String[] args) {
        JPanel north = new JPanel();
        north.add(inputFileField);
        north.add(openButton);
        JPanel center = new JPanel();
        center.add(listButton);
        JPanel south = new JPanel();
        south.add(new JScrollPane(listArea));
        frame.getContentPane().add(north, BorderLayout.NORTH);
        frame.getContentPane().add(center, BorderLayout.CENTER);
        frame.getContentPane().add(south, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        listArea.setEditable(false);
        listButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                list();
            }
        });
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //JFileChooser jfc = new JFileChooser(previousFile.isDirectory() ? previousFile : previousFile.getParentFile());
                JFileChooser jfc = new JFileChooser(previousFile);
                jfc.showOpenDialog(frame);
                if (jfc.getSelectedFile() != null) {
                    previousFile = jfc.getSelectedFile();
                    inputFileField.setText(previousFile.getAbsolutePath());
                }
            }
        });

        /*try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            System.exit(0);
        }*/
    }

    private static void list() {
        Sequence seq = null;

        try {
            seq = MidiSystem.getSequence(new FileInputStream(inputFileField.getText()));
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error opening the file");
            return;
        }
        catch (InvalidMidiDataException e) {
            JOptionPane.showMessageDialog(frame, "A MIDI file format error");
            return;
        }

        listArea.append("File name: " + inputFileField.getText() + "\n");
        listArea.append("Tick length: " + seq.getTickLength() + "\n");
        Track[] tracks = seq.getTracks();
        listArea.append("Number of tracks: " + tracks.length + "\n");

        for (int t = 0; t < tracks.length; t++) {
            Track activeTrack = tracks[t];
            listArea.append("Track " + Integer.toString(t) + "\n");

            for (int e = 0; e < activeTrack.size(); e++) {
                MidiEvent activeEvent = activeTrack.get(e);
                MidiMessage activeMessage = activeEvent.getMessage();
                listArea.append("Data: ");
                byte[] message = activeMessage.getMessage();

                for (byte b : message) {
                    listArea.append(Integer.toHexString(b & 0xFF) + " ");
                }

                listArea.append("   Tick: " + activeEvent.getTick() + "\n");
            }
        }

        listArea.append("\n");
    }
}
