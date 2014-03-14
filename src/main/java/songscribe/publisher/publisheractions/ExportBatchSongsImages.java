package songscribe.publisher.publisheractions;

import org.apache.log4j.Logger;
import songscribe.data.DoNotShowException;
import songscribe.data.FileExtensions;
import songscribe.data.PlatformFileDialog;
import songscribe.publisher.Page;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.Song;
import songscribe.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.ArrayList;

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
        if(exportBatchSongsImagesDialog==null){
            exportBatchSongsImagesDialog = new ExportBatchSongsImagesDialog();
        }
        exportBatchSongsImagesDialog.setVisible(true);
    }

    private class ExportThread extends Thread{
        public void run() {
            int successFul = 0;
            processDialog.packAndPos();
            for(Song song:songs){
                MusicSheet ms = song.getMusicSheet();
                String lyrics = ms.getComposition().getUnderLyrics();
                String translatedLyrics = ms.getComposition().getTranslatedLyrics();
                String songTitle = ms.getComposition().getSongTitle();
                try {
                    if(exportBatchSongsImagesDialog.exportWithoutLyricsCheckBox.isSelected()){
                        ms.getComposition().setUnderLyrics("");
                        ms.getComposition().setTranslatedLyrics("");
                    }
                    if(exportBatchSongsImagesDialog.exportWithoutSongTitlesCheckBox.isSelected()){
                        ms.getComposition().setSongTitle("");
                    }
                    BufferedImage bi = ms.createMusicSheetImageForExport(Color.white, ((Number)exportBatchSongsImagesDialog.resolutionSpinner.getValue()).doubleValue()/(double) MusicSheet.RESOLUTION, exportBatchSongsImagesDialog.borderPanel.getMyBorder());
                    String fileName = song.getSongFile().getName();
                    if(fileName.endsWith(FileExtensions.SONGWRITER))fileName = fileName.substring(0, fileName.length()-FileExtensions.SONGWRITER.length());
                    String extension = exportBatchSongsImagesDialog.formatCombo.getSelectedItem().toString().toLowerCase();
                    fileName+="."+extension;
                    Utilities.writeImage(bi, extension, new File(exportBatchSongsImagesDialog.directory, fileName));
                    successFul++;
                } catch (IOException e) {
                    publisher.showErrorMessage(Publisher.COULDNOTSAVEMESSAGE+"\n"+song.getSongFile().getName());
                    logger.error("Saving image", e);
                } catch (AWTException e) {
                    publisher.showErrorMessage(Publisher.COULDNOTSAVEMESSAGE+"\n"+song.getSongFile().getName());
                    logger.error("Saving image", e);
                } catch (OutOfMemoryError e){
                    publisher.showErrorMessage("There is not enough memory for this resolution."+"\nCould not export: "+song.getSongFile().getName());
                } finally{
                    if(exportBatchSongsImagesDialog.exportWithoutLyricsCheckBox.isSelected()){
                        ms.getComposition().setUnderLyrics(lyrics);
                        ms.getComposition().setTranslatedLyrics(translatedLyrics);
                    }
                    if(exportBatchSongsImagesDialog.exportWithoutSongTitlesCheckBox.isSelected()){
                        ms.getComposition().setSongTitle(songTitle);
                    }
                }
                processDialog.nextValue();
            }
            processDialog.setVisible(false);
            if(successFul==songs.size()){
                JOptionPane.showMessageDialog(publisher, "Successfully exported all songs.", publisher.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            }else if(successFul>0){
                JOptionPane.showMessageDialog(publisher, "Successfully exported "+successFul+" songs,\nbut could not export "+(songs.size()-successFul)+" songs.", publisher.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            }else{
                publisher.showErrorMessage("No song was exported!");
            }
        }
    }

    private class ExportBatchSongsImagesDialog extends MyDialog{
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
                    if(directoryChooser==null)directoryChooser = new PlatformFileDialog(publisher, "Select the destination folder for images", true, null, true);
                    if(directoryChooser.showDialog()){
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
            if(publisher.isBookNull())throw new DoNotShowException();
            borderPanel.setExpertBorder(false);
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
                processDialog = new ProcessDialog(publisher, "Creating images...", songs.size());
                new ExportThread().start();
                processDialog.packAndPos();
                processDialog.setVisible(true);
            }
        }
    }
}
