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
import songscribe.publisher.Page;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.Song;
import songscribe.ui.BorderPanel;
import songscribe.ui.MusicSheet;
import songscribe.ui.MyDialog;
import songscribe.ui.ProcessDialog;
import songscribe.ui.Utilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class ExportBatchSongsImages extends AbstractAction {
    private static Logger logger = Logger.getLogger(ExportBatchSongsImages.class);
    private Publisher publisher;
    private ExportBatchSongsImagesDialog exportBatchSongsImagesDialog;
    private ProcessDialog processDialog;
    private ArrayList<Song> songs;

    public ExportBatchSongsImages(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Create Image of All Songs...");
        putValue(SMALL_ICON, publisher.blankIcon);
    }

    public void actionPerformed(ActionEvent e) {
        if (exportBatchSongsImagesDialog == null) {
            exportBatchSongsImagesDialog = new ExportBatchSongsImagesDialog();
        }

        exportBatchSongsImagesDialog.setVisible(true);
    }

    private class ExportThread extends Thread {
        public void run() {
            int successFul = 0;
            processDialog.packAndPos();

            for (Song song : songs) {
                MusicSheet ms = song.getMusicSheet();
                String lyrics = ms.getComposition().getUnderLyrics();
                String translatedLyrics = ms.getComposition().getTranslatedLyrics();
                String songTitle = ms.getComposition().getSongTitle();

                try {
                    if (exportBatchSongsImagesDialog.exportWithoutLyricsCheckBox.isSelected()) {
                        ms.getComposition().setUnderLyrics("");
                        ms.getComposition().setTranslatedLyrics("");
                    }

                    if (exportBatchSongsImagesDialog.exportWithoutSongTitlesCheckBox.isSelected()) {
                        ms.getComposition().setSongTitle("");
                    }

                    BufferedImage bi = ms.createMusicSheetImageForExport(Color.white,
                            ((Number) exportBatchSongsImagesDialog.resolutionSpinner.getValue()).doubleValue() /
                            (double) MusicSheet.RESOLUTION, exportBatchSongsImagesDialog.borderPanel.getMyBorder());
                    String fileName = song.getSongFile().getName();

                    if (fileName.endsWith(FileExtensions.SONGWRITER)) {
                        fileName = fileName.substring(0, fileName.length() - FileExtensions.SONGWRITER.length());
                    }

                    String extension = exportBatchSongsImagesDialog.formatCombo.getSelectedItem().toString().toLowerCase();
                    fileName += "." + extension;
                    Utilities.writeImage(bi, extension, new File(exportBatchSongsImagesDialog.directory, fileName));
                    successFul++;
                }
                catch (IOException e) {
                    publisher.showErrorMessage(Publisher.COULD_NOT_SAVE_MESSAGE + "\n" + song.getSongFile().getName());
                    logger.error("Saving image", e);
                }
                catch (AWTException e) {
                    publisher.showErrorMessage(Publisher.COULD_NOT_SAVE_MESSAGE + "\n" + song.getSongFile().getName());
                    logger.error("Saving image", e);
                }
                catch (OutOfMemoryError e) {
                    publisher.showErrorMessage(
                            "There is not enough memory for this resolution." + "\nCould not export: " +
                            song.getSongFile().getName());
                }
                finally {
                    if (exportBatchSongsImagesDialog.exportWithoutLyricsCheckBox.isSelected()) {
                        ms.getComposition().setUnderLyrics(lyrics);
                        ms.getComposition().setTranslatedLyrics(translatedLyrics);
                    }

                    if (exportBatchSongsImagesDialog.exportWithoutSongTitlesCheckBox.isSelected()) {
                        ms.getComposition().setSongTitle(songTitle);
                    }
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
                publisher.showErrorMessage("No song was exported!");
            }
        }
    }

    private class ExportBatchSongsImagesDialog extends MyDialog {
        private JPanel centerPanel;
        private JSpinner resolutionSpinner;
        private JCheckBox exportWithoutLyricsCheckBox;
        private JLabel destinationFolder;
        private JButton chooseFolderButton;
        private JComboBox formatCombo;
        private BorderPanel borderPanel;
        private JCheckBox exportWithoutSongTitlesCheckBox;
        private PlatformFileDialog directoryChooser;
        private File directory;

        private ExportBatchSongsImagesDialog() {
            super(publisher, "Create Image of All Songs");
            resolutionSpinner.setModel(new SpinnerNumberModel(100, 30, 1200, 1));
            dialogPanel.add(BorderLayout.CENTER, centerPanel);
            southPanel.remove(applyButton);
            dialogPanel.add(BorderLayout.SOUTH, southPanel);
            directory = publisher.getPreviousDirectory();
            destinationFolder.setText(directory.getAbsolutePath());
            chooseFolderButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (directoryChooser == null) {
                        directoryChooser = new PlatformFileDialog(publisher, "Select the destination folder for images", true, null, true);
                    }

                    if (directoryChooser.showDialog()) {
                        directory = directoryChooser.getFile();
                        destinationFolder.setText(directory.getAbsolutePath());
                        pack();
                    }
                }
            });

            borderPanel.setPackListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pack();
                }
            });
        }

        protected void getData() throws DoNotShowException {
            if (publisher.isBookNull()) {
                throw new DoNotShowException();
            }

            borderPanel.setExpertBorder(false);
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
                processDialog = new ProcessDialog(publisher, "Creating images...", songs.size());
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
            centerPanel.setLayout(new GridLayoutManager(7, 1, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
            final JPanel panel1 = new JPanel();
            panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            final JLabel label1 = new JLabel();
            label1.setText("Resolution:");
            panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            resolutionSpinner = new JSpinner();
            panel1.add(resolutionSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 22), null, 0, false));
            final JLabel label2 = new JLabel();
            label2.setText("dpi");
            panel1.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final Spacer spacer1 = new Spacer();
            panel1.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
            final Spacer spacer2 = new Spacer();
            centerPanel.add(spacer2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
            exportWithoutLyricsCheckBox = new JCheckBox();
            exportWithoutLyricsCheckBox.setText("Export without lyrics under the songs");
            centerPanel.add(exportWithoutLyricsCheckBox, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            final JLabel label3 = new JLabel();
            label3.setText("Destination folder:");
            panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            destinationFolder = new JLabel();
            destinationFolder.setText("");
            panel2.add(destinationFolder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            chooseFolderButton = new JButton();
            chooseFolderButton.setText("Choose folder");
            panel2.add(chooseFolderButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final Spacer spacer3 = new Spacer();
            panel2.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
            final JPanel panel3 = new JPanel();
            panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.add(panel3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            final JLabel label4 = new JLabel();
            label4.setText("Image format:");
            panel3.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final Spacer spacer4 = new Spacer();
            panel3.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
            formatCombo = new JComboBox();
            final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
            defaultComboBoxModel1.addElement("GIF");
            defaultComboBoxModel1.addElement("JPG");
            defaultComboBoxModel1.addElement("PNG");
            formatCombo.setModel(defaultComboBoxModel1);
            panel3.add(formatCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            final JPanel panel4 = new JPanel();
            panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
            centerPanel.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            final Spacer spacer5 = new Spacer();
            panel4.add(spacer5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
            borderPanel = new BorderPanel();
            panel4.add(borderPanel.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            exportWithoutSongTitlesCheckBox = new JCheckBox();
            exportWithoutSongTitlesCheckBox.setText("Export without song titles");
            centerPanel.add(exportWithoutSongTitlesCheckBox, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        }

        /**
         * @noinspection ALL
         */
        public JComponent $$$getRootComponent$$$() {
            return centerPanel;
        }
    }
}
