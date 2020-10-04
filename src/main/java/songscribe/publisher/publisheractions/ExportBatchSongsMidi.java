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
*/
package songscribe.publisher.publisheractions;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.log4j.Logger;
import songscribe.data.DoNotShowException;
import songscribe.data.FileExtensions;
import songscribe.data.PlatformFileDialog;
import songscribe.music.Composition;
import songscribe.publisher.Page;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.Song;
import songscribe.ui.Constants;
import songscribe.ui.MyDialog;
import songscribe.ui.ProcessDialog;
import songscribe.ui.playsubmenu.InstrumentDialog;

import javax.sound.midi.MidiSystem;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;

public class ExportBatchSongsMidi extends AbstractAction {
    private static Logger logger = Logger.getLogger(ExportBatchSongsMidi.class);
    private Publisher publisher;
    private ExportBatchSongsMidiDialog exportBatchSongsImagesDialog;
    private ProcessDialog processDialog;
    private ArrayList<Song> songs;

    public ExportBatchSongsMidi(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Create MIDI of All Songs...");
        putValue(SMALL_ICON, publisher.blankIcon);
    }

    public void actionPerformed(ActionEvent e) {
        if (exportBatchSongsImagesDialog == null) {
            exportBatchSongsImagesDialog = new ExportBatchSongsMidiDialog();
        }

        exportBatchSongsImagesDialog.setVisible(true);
    }

    private class ExportThread extends Thread {
        public void run() {
            int successFul = 0;
            processDialog.packAndPos();
            Properties props = new Properties(publisher.getProperties());
            props.setProperty(Constants.WITH_REPEAT_PROP, exportBatchSongsImagesDialog.exportWithRepeatsCheckBox.isSelected() ? Constants.TRUE_VALUE : Constants.FALSE_VALUE);
            props.setProperty(Constants.INSTRUMENT_PROP, Integer.toString(exportBatchSongsImagesDialog.instrumentCombo.getSelectedIndex()));
            props.setProperty(Constants.TEMPO_CHANGE_PROP, "100");

            for (Song song : songs) {
                try {
                    Composition composition = song.getMusicSheet().getComposition();
                    composition.musicChanged(props);
                    String fileName = song.getSongFile().getName();

                    if (fileName.endsWith(FileExtensions.SONGWRITER)) {
                        fileName = fileName.substring(0, fileName.length() - FileExtensions.SONGWRITER.length());
                    }

                    fileName += ".mid";
                    MidiSystem.write(composition.getSequence(), 1, new File(exportBatchSongsImagesDialog.directory, fileName));
                    successFul++;
                }
                catch (IOException e) {
                    publisher.showErrorMessage(Publisher.COULD_NOT_SAVE_MESSAGE + "\n" + song.getSongFile().getName());
                    logger.error("Saving midi", e);
                }

                processDialog.nextValue();
            }
            processDialog.setVisible(false);

            if (successFul == songs.size()) {
                JOptionPane.showMessageDialog(publisher, "Successfully exported all songs.", publisher.PROG_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
            else if (successFul > 0) {
                JOptionPane.showMessageDialog(publisher,
                        "Successfully exported " + successFul + " songs,\nbut could not export " +
                        (songs.size() - successFul) + " songs.", publisher.PROG_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                publisher.showErrorMessage("No songs were exported!");
            }
        }
    }

    private class ExportBatchSongsMidiDialog extends MyDialog {
        private JComboBox instrumentCombo;
        private JCheckBox exportWithRepeatsCheckBox;
        private JPanel centerPanel;
        private JLabel destinationFolder;
        private JButton chooseFolderButton;
        private File directory;
        private PlatformFileDialog directoryChooser;

        private ExportBatchSongsMidiDialog() {
            super(publisher, "Create MIDI of All Songs");
            dialogPanel.add(BorderLayout.CENTER, centerPanel);
            southPanel.remove(applyButton);
            dialogPanel.add(BorderLayout.SOUTH, southPanel);
            instrumentCombo.setModel(new DefaultComboBoxModel(InstrumentDialog.INSTRUMENT_STRING));
            directory = publisher.getPreviousDirectory();
            destinationFolder.setText(directory.getAbsolutePath());
            chooseFolderButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (directoryChooser == null) {
                        directoryChooser = new PlatformFileDialog(publisher, "Select the destination folder for MIDI", true, null, true);
                    }

                    if (directoryChooser.showDialog()) {
                        directory = directoryChooser.getFile();
                        destinationFolder.setText(directory.getAbsolutePath());
                    }
                }
            });
        }

        protected void getData() throws DoNotShowException {
            if (publisher.isBookNull()) {
                throw new DoNotShowException();
            }
        }

        protected void setData() {
            // find songs
            songs = new ArrayList<Song>();

            for (ListIterator<Page> pages = publisher.getBook().pageIterator(); pages.hasNext(); ) {
                for (ListIterator<PageComponent> comps = pages.next().getPageComponentIterator(); comps.hasNext(); ) {
                    PageComponent c = comps.next();

                    if (c instanceof Song) {
                        songs.add((Song) c);
                    }
                }
            }

            if (songs.size() == 0) {
                publisher.showErrorMessage("There are no songs to export.");
            } else {
                processDialog = new ProcessDialog(publisher, "Creating MIDI...", songs.size());
                new ExportThread().start();
                processDialog.packAndPos();
                processDialog.setVisible(true);
            }
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
            centerPanel = new JPanel();
            centerPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
            final JPanel panel1 = new JPanel();
            panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            final JLabel label1 = new JLabel();
            label1.setText("Select Instrument:");
            panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final Spacer spacer1 = new Spacer();
            panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
            instrumentCombo = new JComboBox();
            panel1.add(instrumentCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final Spacer spacer2 = new Spacer();
            centerPanel.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
            exportWithRepeatsCheckBox = new JCheckBox();
            exportWithRepeatsCheckBox.setText("Export with repeats");
            centerPanel.add(exportWithRepeatsCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            final JLabel label2 = new JLabel();
            label2.setText("Destination folder:");
            panel2.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            destinationFolder = new JLabel();
            destinationFolder.setText("");
            panel2.add(destinationFolder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            chooseFolderButton = new JButton();
            chooseFolderButton.setText("Choose folder");
            panel2.add(chooseFolderButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final Spacer spacer3 = new Spacer();
            panel2.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        }

        /**
         * @noinspection ALL
         */
        public JComponent $$$getRootComponent$$$() {
            return centerPanel;
        }
    }
}
