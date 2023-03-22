package songscribe.converter;

import songscribe.SongScribe;
import songscribe.data.PropertyChangeListener;
import songscribe.ui.CompositionSettingsDialog;
import songscribe.ui.IMainFrame;
import songscribe.ui.InsertMenu;
import songscribe.ui.LyricsModePanel;
import songscribe.ui.MusicSheet;
import songscribe.ui.ProfileManager;
import songscribe.ui.StatusBar;
import songscribe.ui.playsubmenu.PlayMenu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public class ConverterMainFrame implements IMainFrame {
    private MusicSheet musicSheet;

    private final Properties defaultProps = new Properties();

    private final ProfileManager profileManager;

    public ConverterMainFrame() {
        try {
            defaultProps.load(new FileInputStream(SongScribe.basePath + "/conf/defprops"));
        }
        catch (IOException e) {
            showErrorMessage("The program could not start, because a necessary file is not available. Please reinstall the software.");
            throw new RuntimeException(e);
        }
        profileManager = new ProfileManager(this);
    }

    @Override
    public void showInfoMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void showErrorMessage(String message) {
        System.out.println(message);
    }

    @Override
    public int showConfirmDialog(String message, int optionType, int messageType) {
        System.out.println(message);
        return JOptionPane.YES_OPTION;
    }

    @Override
    public void setSaveFile(File saveFile) {
    }

    @Override
    public boolean isModifiedDocument() {
        return false;
    }

    @Override
    public void setModifiedDocument(boolean modifiedDocument) {
    }

    @Override
    public void setFrameSize() {
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public void addProperyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    @Override
    public InsertMenu getInsertMenu() {
        return mock(InsertMenu.class);
    }

    @Override
    public Properties getProperties() {
        return new Properties();
    }

    @Override
    public PlayMenu getPlayMenu() {
        return mock(PlayMenu.class);
    }

    @Override
    public LyricsModePanel getLyricsModePanel() {
        return mock(LyricsModePanel.class);
    }

    @Override
    public void setMode(MusicSheet.Mode noteEdit) {
    }

    @Override
    public void fireMusicChanged(Object o) {
    }

    @Override
    public StatusBar getStatusBar() {
        return mock(StatusBar.class);
    }

    @Override
    public Component getFocusOwner() {
        return mock(Component.class);
    }

    @Override
    public MusicSheet getMusicSheet() {
        return musicSheet;
    }

    public void setMusicSheet(MusicSheet musicSheet) {
        this.musicSheet = musicSheet;
    }

    @Override
    public Properties getDefaultProps() {
        return defaultProps;
    }

    @Override
    public CompositionSettingsDialog getCompositionSettingsDialog() {
        return mock(CompositionSettingsDialog.class);
    }
}
