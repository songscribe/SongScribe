package songscribe.publisher.publisheractions;

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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;

public class ExportBatchSongsMidi extends AbstractAction{
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
        if(exportBatchSongsImagesDialog==null){
            exportBatchSongsImagesDialog = new ExportBatchSongsMidiDialog();
        }
        exportBatchSongsImagesDialog.setVisible(true);
    }

    private class ExportThread extends Thread{
        public void run() {
            int successFul = 0;
            processDialog.packAndPos();
            Properties props = new Properties(publisher.getProperties());
            props.setProperty(Constants.WITHREPEATPROP, exportBatchSongsImagesDialog.exportWithRepeatsCheckBox.isSelected() ? Constants.TRUEVALUE : Constants.FALSEVALUE);
            props.setProperty(Constants.INSTRUMENTPROP, Integer.toString(exportBatchSongsImagesDialog.instrumentCombo.getSelectedIndex()));
            props.setProperty(Constants.TEMPOCHANGEPROP, "100");
            for(Song song:songs){
                try {
                    Composition composition = song.getMusicSheet().getComposition();
                    composition.musicChanged(props);
                    String fileName = song.getSongFile().getName();
                    if(fileName.endsWith(FileExtensions.SONGWRITER))fileName = fileName.substring(0, fileName.length()-FileExtensions.SONGWRITER.length());
                    fileName+=".mid";
                    MidiSystem.write(composition.getSequence(), 1, new File(exportBatchSongsImagesDialog.directory, fileName));
                    successFul++;
                } catch (IOException e) {
                    publisher.showErrorMessage(Publisher.COULDNOTSAVEMESSAGE+"\n"+song.getSongFile().getName());
                    logger.error("Saving midi", e);
                }
                processDialog.nextValue();
            }
            processDialog.setVisible(false);
            if(successFul==songs.size()){
                JOptionPane.showMessageDialog(publisher, "Successfully exported all songs.", publisher.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            }else if(successFul>0){
                JOptionPane.showMessageDialog(publisher, "Successfully exported "+successFul+" songs,\nbut could not export "+(songs.size()-successFul)+" songs.", publisher.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            }else{
                publisher.showErrorMessage("No songs were exported!");
            }
        }
    }

    private class ExportBatchSongsMidiDialog extends MyDialog{
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
            instrumentCombo.setModel(new DefaultComboBoxModel(InstrumentDialog.INSTRUMENTSTRING));
            directory = publisher.getPreviousDirectory();
            destinationFolder.setText(directory.getAbsolutePath());
            chooseFolderButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(directoryChooser==null)directoryChooser = new PlatformFileDialog(publisher, "Select the destination folder for MIDI", true, null, true);
                    if(directoryChooser.showDialog()){
                        directory = directoryChooser.getFile();
                        destinationFolder.setText(directory.getAbsolutePath());
                    }
                }
            });
        }

        protected void getData() throws DoNotShowException {
            if(publisher.isBookNull())throw new DoNotShowException();
        }

        protected void setData() {
            //finding songs
            songs = new ArrayList<Song>();
            for(ListIterator<Page> pages = publisher.getBook().pageIterator();pages.hasNext();){
                for(ListIterator<PageComponent> comps = pages.next().getPageComponentIterator();comps.hasNext();){
                    PageComponent c = comps.next();
                    if(c instanceof Song){
                        songs.add((Song)c);
                    }
                }
            }
            if(songs.size()==0){
                publisher.showErrorMessage("There are no songs to export.");
            }else{
                processDialog = new ProcessDialog(publisher, "Creating MIDI...", songs.size());
                new ExportThread().start();
                processDialog.packAndPos();
                processDialog.setVisible(true);
            }
        }
    }
}
