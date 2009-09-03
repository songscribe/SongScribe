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

    public static final int[] GIF_WIDTH = {640, 2240};
    public static final int[] LEFT_RIGHT_MARGIN = {13, 39};
    public static final String[] GIF_NAME_POSSIX = {"-normal", "-large"};

    public ConvertAction(UIConverter uiConverter, JTextField songsDirectory) {
        this.uiConverter = uiConverter;
        this.songsDirectory = songsDirectory;
        putValue(NAME, "Convert");
        putValue(Action.SMALL_ICON, new ImageIcon(UIConverter.getImage("ok.png")));
        assert GIF_WIDTH.length == GIF_NAME_POSSIX.length;
        assert GIF_WIDTH.length == LEFT_RIGHT_MARGIN.length;
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
        ProcessDialog pd = new ProcessDialog(uiConverter, "Converting...", songFiles.length * (2 + GIF_WIDTH.length));
        pd.packAndPos();
        new ConvertThread(songDirectoryFile, songFiles, pd).start();
        pd.setVisible(true);
        JOptionPane.showMessageDialog(uiConverter, "Successfully converted.");
    }

    private class ConvertThread extends Thread {
        private File songDirectory;
        private File[] songFiles;
        private ProcessDialog processDialog;
        private static final String IMAGETYPE = "GIF";

        private ConvertThread(File songDirectory, File[] songFiles, ProcessDialog processDialog) {
            this.songDirectory = songDirectory;
            this.songFiles = songFiles;
            this.processDialog = processDialog;
        }

        @Override
        public void run() {
            MyBorder[] myBorders = new MyBorder[LEFT_RIGHT_MARGIN.length];
            for (int i = 0; i < LEFT_RIGHT_MARGIN.length; i++) {
                myBorders[i] = new MyBorder();
                myBorders[i].setLeft(LEFT_RIGHT_MARGIN[i]);
                myBorders[i].setRight(LEFT_RIGHT_MARGIN[i]);
            }

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
                MusicSheet musicSheet = uiConverter.getMusicSheet();
                musicSheet.getComposition().setUnderLyrics("");
                musicSheet.getComposition().setTranslatedLyrics("");
                musicSheet.getComposition().setSongTitle("");
                musicSheet.getComposition().setRightInfo("");

                String fileName = songFile.getName();
                int dotPos = fileName.lastIndexOf('.');
                if (dotPos > 0) fileName = fileName.substring(0, dotPos);

                for(int i = 0; i < GIF_WIDTH.length; i++) {
                    double scale = (double)(GIF_WIDTH[i] - 2 * LEFT_RIGHT_MARGIN[i]) / musicSheet.getSheetWidth();
                    BufferedImage image = musicSheet.createMusicSheetImageForExport(Color.WHITE, scale, myBorders[i]);
                    try {
                        Utilities.writeImage(image, IMAGETYPE, new File(songDirectory, fileName + GIF_NAME_POSSIX[i] + "." + IMAGETYPE.toLowerCase()));
                    } catch (Exception e) {
                        uiConverter.showErrorMessage("Could not convert image for " + songFile.getName());
                    }
                    processDialog.nextValue();
                }

                // producing MIDI
                musicSheet.getComposition().musicChanged(props);
                try {
                    MidiSystem.write(musicSheet.getComposition().getSequence(), 1, new File(songDirectory, fileName + ".midi"));
                } catch (IOException e) {
                    uiConverter.showErrorMessage("Could not convert MIDI for " + songFile.getName());
                }
                processDialog.nextValue();
            }
            processDialog.setVisible(false);
        }
    }
}
