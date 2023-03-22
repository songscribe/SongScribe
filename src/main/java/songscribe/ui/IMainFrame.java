package songscribe.ui;

import songscribe.data.PropertyChangeListener;
import songscribe.ui.playsubmenu.PlayMenu;

import java.awt.*;
import java.io.File;
import java.util.Properties;

public interface IMainFrame {
    void showInfoMessage(String message);
    void showErrorMessage(String message);

    int showConfirmDialog(String message, int optionType, int messageType);

    ProfileManager getProfileManager();

    void addProperyChangeListener(PropertyChangeListener propertyChangeListener);

    InsertMenu getInsertMenu();

    void setSaveFile(File saveFile);

    Properties getProperties();

    PlayMenu getPlayMenu();

    LyricsModePanel getLyricsModePanel();

    void setMode(MusicSheet.Mode noteEdit);

    void fireMusicChanged(Object o);

    StatusBar getStatusBar();

    Component getFocusOwner();

    boolean isModifiedDocument();

    void setModifiedDocument(boolean modifiedDocument);

    MusicSheet getMusicSheet();

    void setFrameSize();

    Properties getDefaultProps();

    CompositionSettingsDialog getCompositionSettingsDialog();
}
