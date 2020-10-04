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

    Created on Jun 7, 2006
*/
package songscribe.ui;

import org.apache.log4j.Logger;
import songscribe.SongScribe;
import songscribe.music.KeyType;
import songscribe.music.Tempo;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Csaba Kávai
 */
public class ProfileManager {
    public static final String PROFILE = "Profile";
    private static final String PROFILE_VERSION = "1.0";

    private static final String PLAIN = "Plain";
    private static final String BOLD = "Bold";
    private static final String ITALIC = "Italic";
    private static Logger logger = Logger.getLogger(ProfileManager.class);
    private MainFrame mainFrame;
    private String defaultProfileName;
    private Properties defaultProfile;

    public ProfileManager(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        ArrayList<String> existingProfiles = enumerateProfiles();

        if (existingProfiles.contains(mainFrame.getProperties().getProperty(Constants.DEFAULT_PROFILE_PROP))) {
            setDefaultProfile(mainFrame.getProperties().getProperty(Constants.DEFAULT_PROFILE_PROP));
        }
        else if (existingProfiles.contains(mainFrame.getDefaultProps().getProperty(Constants.DEFAULT_PROFILE_PROP))) {
            setDefaultProfile(mainFrame.getDefaultProps().getProperty(Constants.DEFAULT_PROFILE_PROP));
        }
        else if (!existingProfiles.isEmpty()) {
            setDefaultProfile(existingProfiles.get(0));
        }
        else {
            mainFrame.showErrorMessage(
                    "There is not a single profile in your installation. Try reinstalling " + Constants.PACKAGE_NAME);
            logger.fatal("There is not a single profile among profiles!", new IllegalStateException());
            System.exit(-1);
        }
    }

    public static String stringFontStyle(boolean isBold, boolean isItalic) {
        if (!isBold && !isItalic) {
            return PLAIN;
        }
        else {
            return (isBold ? BOLD : "") + (isItalic ? ITALIC : "");
        }
    }

    public static int intFontStyle(String stringFontStyle) {
        return stringFontStyle == null ? Font.PLAIN : ((stringFontStyle.contains(BOLD) ? Font.BOLD : Font.PLAIN) |
                                                       (stringFontStyle.contains(ITALIC) ? Font.ITALIC : Font.PLAIN
                                                       )
        );
    }

    public ArrayList<String> enumerateProfiles() {
        ArrayList<String> profiles = new ArrayList<String>();
        File[] files = new File(SongScribe.basePath + "/profiles").listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && getProfile(file.getName()) != null) {
                    profiles.add(file.getName());
                }
            }
        }

        return profiles;
    }

    public Properties getProfile(String name) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File(SongScribe.basePath + "/profiles", name)));

            if (!PROFILE_VERSION.equals(props.getProperty(PROFILE))) {
                return null;
            }

            return props;
        }
        catch (IOException e) {
            mainFrame.showErrorMessage("Could not load the profile " + name);
            logger.error("Profile load", e);
        }

        return null;
    }

    public void saveProfile(String name) throws IOException {
        CompositionSettingsDialog csd = mainFrame.getCompositionSettingsDialog();
        Properties props = new Properties();
        props.setProperty(PROFILE, PROFILE_VERSION);
        props.setProperty(ProfileKey.RIGHT_INFORMATION.getKey(), csd.rightInfoArea.getText());
        props.setProperty(ProfileKey.TEMPO_TYPE.getKey(), ((Tempo.Type) csd.tempoTypeCombo.getSelectedItem()).name());
        props.setProperty(ProfileKey.TEMPO.getKey(), csd.tempoSpinner.getValue().toString());
        props.setProperty(ProfileKey.TEMPO_DESCRIPTION.getKey(), csd.tempoDescriptionCombo.getSelectedItem().toString());
        props.setProperty(ProfileKey.KEY_TYPE.getKey(), ((KeyType) csd.keysCombo.getSelectedItem()).name());
        props.setProperty(ProfileKey.KEYS.getKey(), csd.keysSpinner.getValue().toString());
        props.setProperty(ProfileKey.TITLE_FONT.getKey(), csd.titleFontCombo.getSelectedItem().toString());
        props.setProperty(ProfileKey.TITLE_FONT_SIZE.getKey(), csd.titleFontSizeSpinner.getValue().toString());
        props.setProperty(ProfileKey.TITLE_FONT_STYLE.getKey(), stringFontStyle(csd.titleBoldToggle.isSelected(), csd.titleItalicToggle.isSelected()));
        props.setProperty(ProfileKey.LYRICS_FONT.getKey(), csd.lyricsFontCombo.getSelectedItem().toString());
        props.setProperty(ProfileKey.LYRICS_FONT_SIZE.getKey(), csd.lyricsFontSizeSpinner.getValue().toString());
        props.setProperty(ProfileKey.LYRICS_FONT_STYLE.getKey(), stringFontStyle(csd.lyricsBoldToggle.isSelected(), csd.lyricsItalicToggle.isSelected()));
        props.store(new FileOutputStream(new File(SongScribe.basePath + "/profiles", name)), null);
    }

    public void setDefaultProfile(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
        defaultProfile = getProfile(defaultProfileName);
        mainFrame.getProperties().setProperty(Constants.DEFAULT_PROFILE_PROP, defaultProfileName);
    }

    public String getDefaultProfileName() {
        return defaultProfileName;
    }

    public String getDefaultProperty(ProfileKey pk) {
        return defaultProfile.getProperty(pk.getKey());
    }

    public enum ProfileKey {
        RIGHT_INFORMATION("RightInformation"),
        TEMPO_TYPE("TempoType"),
        TEMPO("Tempo"),
        TEMPO_DESCRIPTION("TempoDescription"),
        KEYS("Keys"),
        KEY_TYPE("KeyType"),
        TITLE_FONT("TitleFont"),
        TITLE_FONT_SIZE("TitleFontSize"),
        TITLE_FONT_STYLE("TitleFontStyle"),
        LYRICS_FONT("LyricsFont"),
        LYRICS_FONT_SIZE("LyricsFontSize"),
        LYRICS_FONT_STYLE("LyricsFontStyle"),
        GENERAL_FONT("GeneralFont"),
        GENERAL_FONT_SIZE("GeneralFontSize");

        private String key;

        private ProfileKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
