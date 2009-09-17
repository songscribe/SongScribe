package songscribe.uiconverter;

import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.MacAdapter;
import songscribe.ui.Utilities;
import songscribe.data.FileExtensions;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
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
        PROGNAME = "Song Converter";
        lastWordForDoYouWannaSaveDialog = null;

        setTitle(PROGNAME);
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

        if (Utilities.isMac())
            MacAdapter.attachTo(this, false);
        
        pack();
        setLocation(CENTERPOINT.x-getWidth()/2, CENTERPOINT.y-getHeight()/2);
        setVisible(true);
    }

    private class DirectorySelectionChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals("directorychange")){
                File newDir = (File) evt.getNewValue();
                currentDir = newDir;
                songsFolder.setText(newDir.getAbsolutePath());
                acceptedTableModel.setRowCount(0);
                rejectListModel.clear();
                for(File file: newDir.listFiles()) {
                    String fileName = file.getName();
                    if(isLegalFileName(fileName)) {
                        acceptedTableModel.addRow(new Object[]{fileName.substring(0, 3), fileName.substring(4, fileName.length() - FileExtensions.SONGWRITER.length())});
                    } else if(fileName.endsWith(FileExtensions.SONGWRITER)){
                        rejectListModel.addElement(fileName.substring(0, fileName.length() - FileExtensions.SONGWRITER.length()));
                    }
                }

                songsSummaryPanel.setVisible(true);
                pack();
                setLocation(CENTERPOINT.x-getWidth()/2, CENTERPOINT.y-getHeight()/2);
            }
        }
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure("conf/logger.properties");
        openMidi();
        new UIConverter();
    }

    public boolean isLegalFileName(String fileName) {
        return fileName.length() >= 10 &&
                fileName.endsWith(FileExtensions.SONGWRITER) &&
                Character.isDigit(fileName.charAt(0)) &&
                Character.isDigit(fileName.charAt(1)) &&
                Character.isDigit(fileName.charAt(2)) &&
                (fileName.charAt(3)==' ' || fileName.charAt(3)=='-');
    }

    private class NumberSongAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String selectedSong = (String) rejectList.getSelectedValue();
            if (selectedSong ==null) {
                showErrorMessage("No rejected song is selected.");
                return;
            }
            String numberStr = JOptionPane.showInputDialog(UIConverter.this, "Enter the number of song:");
            if (numberStr ==null) {
                return;
            }
            int number;
            try{
                number = Integer.parseInt(numberStr);
            }catch(NumberFormatException nfe){
                showErrorMessage("The value could not be recognized as a number");
                return;
            }
            if (number<1 || number>999) {
                showErrorMessage("The number must be between 1 and 999");
                return;
            }
            numberStr = String.format("%03d", number);
            File originalSongFile = new File(currentDir, selectedSong + FileExtensions.SONGWRITER);
            File renamedSongFile = new File(currentDir, numberStr + " " + selectedSong + FileExtensions.SONGWRITER);

            if(!isLegalFileName(renamedSongFile.getName())){
                showErrorMessage("An inner problem occured. Numbering failed. Contact the developer!");
                LOGGER.error("The renamed file did not pass the isLegalFileName method: " + renamedSongFile.getName());
                return;
            }

            boolean renameSuccessful = originalSongFile.renameTo(renamedSongFile);
            if(!renameSuccessful) {
                showErrorMessage("Numbering the song was not successful, because the file-rename failed.\nTry to rename the corresponding file manually.");
                return;
            }

            rejectListModel.removeElement(selectedSong);
            acceptedTableModel.addRow(new Object[]{numberStr, selectedSong});
        }
    }
}
