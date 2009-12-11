package songscribe.uiconverter;

import songscribe.music.Composition;
import songscribe.ui.ProcessDialog;
import songscribe.ui.MusicSheet;
import songscribe.ui.Constants;
import songscribe.ui.Utilities;
import songscribe.IO.CompositionIO;
import songscribe.data.MyBorder;

import javax.swing.*;
import javax.sound.midi.MidiSystem;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.util.zip.ZipOutputStream;

public class ConvertAction extends AbstractAction {
    private UIConverter uiConverter;
    private JTextField songsDirectory;

    public static final int[] IMAGE_WIDTH = {/*640, */2240};
    public static final int[] LEFT_RIGHT_MARGIN = {/*13, */39};
    public static final String[] IMAGE_NAME_POSSIX = {/*"-s", */"-l"};

    public ConvertAction(UIConverter uiConverter, JTextField songsDirectory) {
        this.uiConverter = uiConverter;
        this.songsDirectory = songsDirectory;
        putValue(NAME, "Convert");
        putValue(Action.SMALL_ICON, new ImageIcon(UIConverter.getImage("ok.png")));
        assert IMAGE_WIDTH.length == IMAGE_NAME_POSSIX.length;
        assert IMAGE_WIDTH.length == LEFT_RIGHT_MARGIN.length;
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
        ProcessDialog pd = new ProcessDialog(uiConverter, "Converting...", songFiles.length * (3 + IMAGE_WIDTH.length * 2));
        pd.packAndPos();
        new ConvertThread(songDirectoryFile, songFiles, pd).start();
        pd.setVisible(true);
    }

    private class ConvertThread extends Thread {
        private File songDirectory;
        private File[] songFiles;
        private ProcessDialog processDialog;
        private static final String IMAGETYPE = "PNG";

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

            try {
                File zipFile = new File(songDirectory.getParentFile(), songDirectory.getName() + ".zip");
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                byte[] buf = new byte[1024];

                for (File songFile : songFiles) {
                    //loading file
                    uiConverter.getMusicSheet().setComposition(null);
                    uiConverter.openMusicSheet(songFile, false);
                    MusicSheet musicSheet = uiConverter.getMusicSheet();
                    Composition composition = musicSheet.getComposition();
                    
                    // ensuring of having the latest format by writing the mssw file again
                    File tempMsswSong = File.createTempFile("mssw_uiconvert", ".mssw");
                    PrintWriter tempMsswSongPrintWriter = new PrintWriter(new FileWriter(tempMsswSong));
                    CompositionIO.writeComposition(composition, tempMsswSongPrintWriter);
                    tempMsswSongPrintWriter.close();
                    Utilities.zipFile(zos, tempMsswSong, songFile.getName(), buf);
                    processDialog.nextValue();

                    // producing images                    
                    composition.setUnderLyrics("");
                    composition.setTranslatedLyrics("");
                    composition.setSongTitle("");
                    composition.setRightInfo("");

                    String fileName = songFile.getName();
                    int dotPos = fileName.lastIndexOf('.');
                    if (dotPos > 0) fileName = fileName.substring(0, dotPos);

                    for(int i = 0; i < IMAGE_WIDTH.length; i++) {
                        double scale = (double)(IMAGE_WIDTH[i] - 2 * LEFT_RIGHT_MARGIN[i]) / musicSheet.getSheetWidth();
                        BufferedImage image = musicSheet.createMusicSheetImageForExport(Color.WHITE, scale, myBorders[i]);
                        File imageFile = new File(songDirectory, fileName + IMAGE_NAME_POSSIX[i] + "." + IMAGETYPE.toLowerCase());
                        try {
                            Utilities.writeImage(image, IMAGETYPE, imageFile);
                        } catch (Exception e) {
                            imageFile = null;
                            uiConverter.showErrorMessage("Could not convert image for " + songFile.getName());
                        } finally{
                            processDialog.nextValue();
                        }
                        if (imageFile != null) {
                            Utilities.zipFile(zos, imageFile, null, buf);
                        }
                        processDialog.nextValue();
                    }

                    // producing MIDI
                    composition.musicChanged(props);
                    File midiFile = new File(songDirectory, fileName + ".mid");
                    try {
                        MidiSystem.write(composition.getSequence(), 1, midiFile);
                    } catch (IOException e) {
                        midiFile = null;
                        uiConverter.showErrorMessage("Could not convert MIDI for " + songFile.getName());
                    } finally {
                        processDialog.nextValue();
                    }

                    if (midiFile != null) {
                        Utilities.zipFile(zos, midiFile, null, buf);
                    }
                    processDialog.nextValue();
                }
                zos.close();

                JOptionPane.showMessageDialog(processDialog, "Conversion complete!");

                Utilities.openWebPage(uiConverter, uiConverter.getProperties().getProperty(Constants.BOOKUPLOADURL));
            } catch (IOException e) {
                uiConverter.showErrorMessage("Error while producing ZIP file.");
            } finally {
                processDialog.setVisible(false);
            }
        }
    }
}
