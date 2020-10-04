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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import songscribe.SongScribe;
import songscribe.data.FileExtensions;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
        setupDesktopHandlers(false, null);
        pack();
        setLocation(CENTER_POINT.x - getWidth() / 2, CENTER_POINT.y - getHeight() / 2);
        setVisible(true);
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure(SongScribe.basePath + "/conf/logger.properties");
        openMidi();
        new UIConverter();
    }

    public boolean isLegalFileName(String fileName) {
        return fileName.length() >= 10 &&
            fileName.endsWith(FileExtensions.SONGWRITER) &&
            Character.isDigit(fileName.charAt(0)) &&
            Character.isDigit(fileName.charAt(1)) &&
            Character.isDigit(fileName.charAt(2)) &&
            (fileName.charAt(3) == ' ' || fileName.charAt(3) == '-');
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
        mainPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Songs Folder:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        songsFolder = new JTextField();
        songsFolder.setEditable(false);
        panel1.add(songsFolder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(400, -1), null, 0, false));
        chooseButton = new JButton();
        chooseButton.setText("Choose");
        panel1.add(chooseButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        convertButton = new JButton();
        convertButton.setText("Convert");
        panel2.add(convertButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        songsSummaryPanel = new JPanel();
        songsSummaryPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(songsSummaryPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSplitPane splitPane1 = new JSplitPane();
        songsSummaryPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel3);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        acceptedTable = new JTable();
        acceptedTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
        scrollPane1.setViewportView(acceptedTable);
        final JLabel label2 = new JLabel();
        label2.setText("Accepted songs:");
        panel3.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel4);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel4.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        rejectList = new JList();
        Font rejectListFont = this.$$$getFont$$$(null, Font.PLAIN, -1, rejectList.getFont());
        if (rejectListFont != null) rejectList.setFont(rejectListFont);
        scrollPane2.setViewportView(rejectList);
        final JLabel label3 = new JLabel();
        label3.setText("Rejected songs:");
        panel4.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        numberSongButton = new JButton();
        numberSongButton.setText("Number a Song and Accept");
        panel5.add(numberSongButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel5.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel5.add(spacer5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, Font.ITALIC, -1, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Note: these songs have been rejected because the file name does not ");
        panel4.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, Font.ITALIC, -1, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("start with a three digit order number followed by a space or dash");
        panel4.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
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
                        acceptedTableModel.addRow(new Object[]{fileName.substring(0, 3), fileName.substring(4,
                            fileName.length() - FileExtensions.SONGWRITER.length())
                        });
                    } else if (fileName.endsWith(FileExtensions.SONGWRITER)) {
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
            } catch (NumberFormatException nfe) {
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
            acceptedTableModel.addRow(new Object[]{numberStr, selectedSong});
        }
    }
}
