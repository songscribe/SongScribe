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
package songscribe.uiconverter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import songscribe.data.FileExtensions;
import songscribe.ui.MacAdapter;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.Utilities;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class UIConverter extends MainFrame {
    private static Logger LOGGER = Logger.getLogger(UIConverter.class);

    private JPanel mainPanel;
    private JTextField songsFolder;
    private JButton chooseButton;
    private JButton convertButton;
    private JPanel songsSummaryPanel;
    private JTable acceptedTable;
    private JButton numberSongButton;
    private JList rejectList;
    private DefaultTableModel acceptedTableModel = new DefaultTableModel();
    private DefaultListModel rejectListModel = new DefaultListModel();
    private File currentDir;

    public UIConverter() {
        PROG_NAME = "Song Converter";
        lastWordForDoYouWannaSaveDialog = null;

        setTitle(PROG_NAME);
        setIconImage(getImage("swicon.png"));

        exitAction = new ExitAction() {
            public void actionPerformed(ActionEvent e) {
                closeMidi();
                System.exit(0);
            }
        };

        musicSheet = new MusicSheet(this);

        ChooseDirectoryAction chooseDirectoryAction = new ChooseDirectoryAction(this);
        chooseDirectoryAction.addPropertyChangeListener(new DirectorySelectionChangeListener());
        chooseButton.setAction(chooseDirectoryAction);
        convertButton.setAction(new ConvertAction(this, songsFolder));
        songsSummaryPanel.setVisible(false);
        convertButton.setEnabled(false);
        acceptedTableModel.addColumn("Number");
        acceptedTableModel.addColumn("Title");
        acceptedTable.setModel(acceptedTableModel);
        acceptedTable.getColumnModel().getColumn(0).setMaxWidth(60);
        acceptedTableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                convertButton.setEnabled(acceptedTableModel.getRowCount() > 0);
            }
        });
        rejectList.setModel(rejectListModel);
        numberSongButton.addActionListener(new NumberSongAction());
        getContentPane().add(mainPanel);
        pack();
        setLocation(CENTER_POINT.x - getWidth() / 2, CENTER_POINT.y - getHeight() / 2);
        setVisible(true);
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure("conf/logger.properties");
        openMidi();
        UIConverter ui = new UIConverter();

        if (Utilities.isMac()) {
            new MacAdapter(ui, true);
        }
    }

    public boolean isLegalFileName(String fileName) {
        return fileName.length() >= 10 &&
               fileName.endsWith(FileExtensions.SONGWRITER) &&
               Character.isDigit(fileName.charAt(0)) &&
               Character.isDigit(fileName.charAt(1)) &&
               Character.isDigit(fileName.charAt(2)) &&
               (fileName.charAt(3) == ' ' || fileName.charAt(3) == '-');
    }

    private class DirectorySelectionChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("directorychange")) {
                File newDir = (File) evt.getNewValue();
                currentDir = newDir;
                songsFolder.setText(newDir.getAbsolutePath());
                acceptedTableModel.setRowCount(0);
                rejectListModel.clear();

                for (File file : newDir.listFiles()) {
                    String fileName = file.getName();

                    if (isLegalFileName(fileName)) {
                        acceptedTableModel.addRow(new Object[] { fileName.substring(0, 3), fileName.substring(4,
                                fileName.length() - FileExtensions.SONGWRITER.length())
                        });
                    }
                    else if (fileName.endsWith(FileExtensions.SONGWRITER)) {
                        rejectListModel.addElement(fileName.substring(0,
                                fileName.length() - FileExtensions.SONGWRITER.length()));
                    }
                }

                songsSummaryPanel.setVisible(true);
                pack();
                setLocation(CENTER_POINT.x - getWidth() / 2, CENTER_POINT.y - getHeight() / 2);
            }
        }
    }

    private class NumberSongAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String selectedSong = (String) rejectList.getSelectedValue();

            if (selectedSong == null) {
                showErrorMessage("No rejected song is selected.");
                return;
            }

            String numberStr = JOptionPane.showInputDialog(UIConverter.this, "Enter the number of song:");

            if (numberStr == null) {
                return;
            }

            int number;

            try {
                number = Integer.parseInt(numberStr);
            }
            catch (NumberFormatException nfe) {
                showErrorMessage("The value could not be recognized as a number");
                return;
            }

            if (number < 1 || number > 999) {
                showErrorMessage("The number must be between 1 and 999");
                return;
            }

            numberStr = String.format("%03d", number);
            File originalSongFile = new File(currentDir, selectedSong + FileExtensions.SONGWRITER);
            File renamedSongFile = new File(currentDir, numberStr + " " + selectedSong + FileExtensions.SONGWRITER);

            if (!isLegalFileName(renamedSongFile.getName())) {
                showErrorMessage("An inner problem occured. Numbering failed. Contact the developer!");
                LOGGER.error("The renamed file did not pass the isLegalFileName method: " + renamedSongFile.getName());
                return;
            }

            boolean renameSuccessful = originalSongFile.renameTo(renamedSongFile);

            if (!renameSuccessful) {
                showErrorMessage("Numbering the song was not successful, because the file-rename failed.\nTry to rename the corresponding file manually.");
                return;
            }

            rejectListModel.removeElement(selectedSong);
            acceptedTableModel.addRow(new Object[] { numberStr, selectedSong });
        }
    }
}
