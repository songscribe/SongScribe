/* 
SongScribe song notation program
Copyright (C) 2006-2007 Csaba Kavai

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

import songscribe.music.Tempo;
import songscribe.music.KeyType;

import java.util.Properties;
import java.util.Vector;
import java.io.*;
import java.awt.*;

import org.apache.log4j.Logger;

/**
 * @author Csaba Kávai
 */
public class ProfileManager {
    private static Logger logger = Logger.getLogger(ProfileManager.class);
    public static final String PROFILE = "Profile";
    private static final String PROFILEVERSION = "1.0";

    private static final String PLAIN = "Plain";
    private static final String BOLD = "Bold";
    private static final String ITALIC = "Italic";

    public enum ProfileKey{
        RIGHTINFORMATION("RightInformation"),
        TEMPOTYPE("TempoType"),
        TEMPO("Tempo"),
        TEMPODESCRIPTION("TempoDescription"),
        KEYS("Keys"),
        KEYTYPE("KeyType"),
        TITLEFONT("TitleFont"),
        TITLEFONTSIZE("TitleFontSize"),
        TITLEFONTSTYLE("TitleFontStyle"),
        LYRICSFONT("LyricsFont"),
        LYRICSFONTSIZE("LyricsFontSize"),
        LYRICSFONTSTYLE("LyricsFontStyle"),
        GENERALFONT("GeneralFont"),
        GENERALFONTSIZE("GeneralFontSize");

        private String key;

        private ProfileKey(String key){
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static final File DEFAULTDIR = new File("profiles");

    private MainFrame mainFrame;
    private String defaultProfileName;
    private Properties defaultProfile;

    public ProfileManager(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setDefaultProfile(mainFrame.getProperties().getProperty(Constants.DEFAULTPROFILEPROP));
    }

    public Object[] enumerateProfiles(){
        Vector<String> profiles = new Vector<String>(10, 10);
        for(File file:DEFAULTDIR.listFiles()){
            if(file.isFile() && getProfile(file.getName())!=null){
                profiles.add(file.getName());
            }
        }
        return profiles.toArray();
    }

    public Properties getProfile(String name){
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File(DEFAULTDIR, name)));
            if(!PROFILEVERSION.equals(props.getProperty(PROFILE))){
                return null;
            }
            return props;
        } catch (IOException e) {
            mainFrame.showErrorMessage("Could not load the profile "+name);
            logger.error("Profile load", e);
        }
        return null;
    }

    public void saveProfile(String name) throws IOException {
        CompositionSettingsDialog csd = mainFrame.getCompositionSettingsDialog();
        Properties props = new Properties();
        props.setProperty(PROFILE, PROFILEVERSION);
        props.setProperty(ProfileKey.RIGHTINFORMATION.getKey(), csd.rightInfoArea.getText());
        props.setProperty(ProfileKey.TEMPOTYPE.getKey(), ((Tempo.Type)csd.tempoTypeCombo.getSelectedItem()).name());
        props.setProperty(ProfileKey.TEMPO.getKey(), csd.tempoSpinner.getValue().toString());
        props.setProperty(ProfileKey.TEMPODESCRIPTION.getKey(), csd.tempoDescriptionCombo.getSelectedItem().toString());
        props.setProperty(ProfileKey.KEYTYPE.getKey(), ((KeyType)csd.keysCombo.getSelectedItem()).name());
        props.setProperty(ProfileKey.KEYS.getKey(), csd.keysSpinner.getValue().toString());
        props.setProperty(ProfileKey.TITLEFONT.getKey(), csd.titleFontCombo.getSelectedItem().toString());
        props.setProperty(ProfileKey.TITLEFONTSIZE.getKey(), csd.titleFontSizeSpinner.getValue().toString());
        props.setProperty(ProfileKey.TITLEFONTSTYLE.getKey(), stringFontStyle(csd.titleBoldToggle.isSelected(), csd.titleItalicToggle.isSelected()));
        props.setProperty(ProfileKey.LYRICSFONT.getKey(), csd.lyricsFontCombo.getSelectedItem().toString());
        props.setProperty(ProfileKey.LYRICSFONTSIZE.getKey(), csd.lyricsFontSizeSpinner.getValue().toString());
        props.setProperty(ProfileKey.LYRICSFONTSTYLE.getKey(), stringFontStyle(csd.lyricsBoldToggle.isSelected(), csd.lyricsItalicToggle.isSelected()));
        props.store(new FileOutputStream(new File(DEFAULTDIR, name)), null);
    }

    public void setDefaultProfile(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
        defaultProfile = getProfile(defaultProfileName);
        mainFrame.getProperties().setProperty(Constants.DEFAULTPROFILEPROP, defaultProfileName);
    }

    public String getDefaultProfileName() {
        return defaultProfileName;
    }

    public String getDefaultProperty(ProfileKey pk) {
        return defaultProfile.getProperty(pk.getKey());
    }

    public static String stringFontStyle(boolean isBold, boolean isItalic){
        if(!isBold && !isItalic){
            return PLAIN;
        }else{
            return (isBold?BOLD:"")+(isItalic?ITALIC:"");
        }
    }

    public static int intFontStyle(String stringFontStyle){
        return stringFontStyle==null ? Font.PLAIN : ((stringFontStyle.indexOf(BOLD)!=-1?Font.BOLD:Font.PLAIN)|(stringFontStyle.indexOf(ITALIC)!=-1?Font.ITALIC:Font.PLAIN));
    }
}
