package songscribe.uiconverter;

import songscribe.ui.ProcessDialog;
import songscribe.ui.MusicSheet;
import songscribe.ui.Constants;
import songscribe.ui.Utilities;
import songscribe.data.MyBorder;

import javax.swing.*;
import javax.sound.midi.MidiSystem;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

public class ConvertAction extends AbstractAction {
    private UIConverter uiConverter;
    private JTextField songsDirectory;

    public ConvertAction(UIConverter uiConverter, JTextField songsDirectory) {
        this.uiConverter = uiConverter;
        this.songsDirectory = songsDirectory;
        putValue(NAME, "Convert");
        putValue(Action.SMALL_ICON, new ImageIcon(UIConverter.getImage("ok.png")));
    }

    public void actionPerformed(ActionEvent e) {
        if (songsDirectory.getText().length() == 0) {
            uiConverter.showErrorMessage("You need to select a folder first.");
            return;
        }
        File songDirectoryFile = new File(songsDirectory.getText());
        if (!songDirectoryFile.exists()) {
            uiConverter.showErrorMessage("The selected folder does not exist.");
            return;
        }
        File[] songFiles = songDirectoryFile.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return uiConverter.isLegalFileName(name);
            }
        });
        if (songFiles.length == 0) {
            uiConverter.showErrorMessage("No files in this folder to convert.");
            return;
        }
        ProcessDialog pd = new ProcessDialog(uiConverter, "Converting...", songFiles.length * 3);
        pd.packAndPos();
        new ConvertThread(songDirectoryFile, songFiles, pd).start();
        pd.setVisible(true);
        JOptionPane.showMessageDialog(uiConverter, "Successfully converted.");
    }

    private class ConvertThread extends Thread {
        private File songDirectory;
        private File[] songFiles;
        private ProcessDialog processDialog;
        private static final int DPI = 100;
        private static final String IMAGETYPE = "GIF";

        private ConvertThread(File songDirectory, File[] songFiles, ProcessDialog processDialog) {
            this.songDirectory = songDirectory;
            this.songFiles = songFiles;
            this.processDialog = processDialog;
        }

        @Override
        public void run() {
            MyBorder myBorder = new MyBorder(10);

            Properties props = new Properties(uiConverter.getProperties());
            props.setProperty(Constants.WITHREPEATPROP, Constants.FALSEVALUE);
            props.setProperty(Constants.INSTRUMENTPROP, Integer.toString(0));
            props.setProperty(Constants.TEMPOCHANGEPROP, Integer.toString(100));

            for (File songFile : songFiles) {
                //loading file
                uiConverter.getMusicSheet().setComposition(null);
                uiConverter.openMusicSheet(songFile, false);
                processDialog.nextValue();

                // producing gif
                uiConverter.getMusicSheet().getComposition().setUnderLyrics("");
                uiConverter.getMusicSheet().getComposition().setTranslatedLyrics("");
                uiConverter.getMusicSheet().getComposition().setSongTitle("");
                BufferedImage image = uiConverter.getMusicSheet().createMusicSheetImageForExport(Color.WHITE, (double) DPI / MusicSheet.RESOLUTION, myBorder);
                String fileName = songFile.getName();
                int dotPos = fileName.lastIndexOf('.');
                if (dotPos > 0) fileName = fileName.substring(0, dotPos);
                try {
                    Utilities.writeImage(image, IMAGETYPE, new File(songDirectory, fileName + "." + IMAGETYPE.toLowerCase()));
                } catch (Exception e) {
                    uiConverter.showErrorMessage("Could not convert image for " + songFile.getName());
                }
                processDialog.nextValue();

                // producing MIDI
                uiConverter.getMusicSheet().getComposition().musicChanged(props);
                try {
                    MidiSystem.write(uiConverter.getMusicSheet().getComposition().getSequence(), 1, new File(songDirectory, fileName + ".midi"));
                } catch (IOException e) {
                    uiConverter.showErrorMessage("Could not convert MIDI for " + songFile.getName());
                }
                processDialog.nextValue();
            }
            processDialog.setVisible(false);
        }
    }
}
