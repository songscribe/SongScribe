package songscribe.uiconverter;

import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import javax.swing.*;

import com.apple.mrj.MRJApplicationUtils;
import org.apache.log4j.PropertyConfigurator;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;

public class UIConverter extends MainFrame {
    private JPanel mainPanel;
    private JTextField songsFolder;
    private JButton chooseButton;
    private JButton convertButton;
    private JPanel countPanel;
    private JTextField countField;
    private JButton listOfSongsButton;
    private JButton helpForSongFileButton;

    public UIConverter() {
        PROGNAME = "Song Converter";
        lastWordForDoYouWannaSaveDialog = null;
        MRJApplicationUtils.registerPrefsHandler(null);
        MRJApplicationUtils.registerOpenDocumentHandler(null);
        MRJApplicationUtils.registerOpenApplicationHandler(null);
        MRJApplicationUtils.registerPrintDocumentHandler(null);

        setTitle(PROGNAME);
        setIconImage(getImage("swicon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeMidi();
                System.exit(0);
            }
        });

        musicSheet = new MusicSheet(this);

        ChooseDirectoryAction chooseDirectoryAction = new ChooseDirectoryAction(this);
        chooseDirectoryAction.addPropertyChangeListener(new DirectorySelectionChangeListener());
        chooseButton.setAction(chooseDirectoryAction);
        convertButton.setAction(new ConvertAction(this, songsFolder));
        countPanel.setVisible(false);
        convertButton.setEnabled(false);
        helpForSongFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLegalNamingMessage();
            }
        });
        getContentPane().add(mainPanel);
        pack();
        setLocation(CENTERPOINT.x-getWidth()/2, CENTERPOINT.y-getHeight()/2);
        setVisible(true);
    }

    private class DirectorySelectionChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals("directorychange")){
                File newDir = (File) evt.getNewValue();
                songsFolder.setText(newDir.getAbsolutePath());
                int legalSongsCount = getLegalSongs(newDir);
                countField.setText(Integer.toString(legalSongsCount));
                listOfSongsButton.setEnabled(legalSongsCount > 0);
                convertButton.setEnabled(legalSongsCount > 0);
                countPanel.setVisible(true);
                pack();
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
                fileName.endsWith(".mssw") &&
                Character.isDigit(fileName.charAt(0)) &&
                Character.isDigit(fileName.charAt(1)) &&
                Character.isDigit(fileName.charAt(2)) &&
                (fileName.charAt(3)==' ' || fileName.charAt(3)=='-');
    }

    private void showLegalNamingMessage() {
        JOptionPane.showMessageDialog(this,
                "The songs files are accepted for batch coversion that meets the following requirements:\n" +
                "1. The extension of file must be '.mssw'\n" +
                "2. The first three character must be a digit indicating the number of song in the songbook.\n" +
                "3. The first three digits must be followed by a space or dash\n" +
                "E.g. a good name is '008 Minati Janabo.mssw'");
    }

    private int getLegalSongs(File dir) {
        int count = 0;
        for(File file: dir.listFiles()) {
            String fileName = file.getName();
            if(isLegalFileName(fileName)) {
                count++;
            }
        }
        return count;
    }

}
