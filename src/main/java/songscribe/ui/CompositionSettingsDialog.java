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

Created on Sep 4, 2005
*/
package songscribe.ui;

import org.apache.log4j.Logger;
import songscribe.data.MyJTextArea;
import songscribe.music.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Csaba Kávai
 */
public class CompositionSettingsDialog extends MyDialog{
    private static Logger logger = Logger.getLogger(CompositionSettingsDialog.class);
    JTextField numberField = new JTextField(3);
    JTextArea titleField = new JTextArea(2, 25);
    SpinnerModel takeFirstWordsSpinner = new SpinnerNumberModel(4, 1, 10, 1);
    JTextField placeField = new JTextField(10);
    JComboBox monthCombo = new JComboBox(new String[]{"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
    JComboBox dayCombo;
    JTextField yearField = new JTextField(5);
    JTextArea rightInfoArea = new MyJTextArea(4, 20);
    JComboBox tempoTypeCombo;
    SpinnerModel tempoSpinner = new SpinnerNumberModel(120, 40, 220, 1);
    JComboBox tempoDescriptionCombo = new JComboBox();
    JCheckBox showOnlyDescriptionCheckBox = new JCheckBox("Show only the tempo description");
    JComboBox keysCombo;
    SpinnerModel keysSpinner = new SpinnerNumberModel(4, 0, 7, 1);

    JComboBox titleFontCombo, lyricsFontCombo, generalFontCombo;
    SpinnerModel titleFontSizeSpinner, lyricsFontSizeSpinner, generalFontSizeSpinner;
    JToggleButton titleBoldToggle, titleItalicToggle, lyricsBoldToggle, lyricsItalicToggle;
    JButton setDefaultButton = new JButton("Reset to default");

    JComboBox profileCombo;
    JLabel profileInfoLabel = new JLabel();
    JButton setAsDefaultButton = new JButton("Set as default");


    public CompositionSettingsDialog(MainFrame owner) {
        super(owner, "Composition settings");

        //----------------------basic center------------------------
        JPanel basicCenter = new JPanel();
        basicCenter.setLayout(new BoxLayout(basicCenter, BoxLayout.Y_AXIS));
        basicCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Dimension small = new Dimension(0, 5);
        Dimension large = new Dimension(0, 15);

        //title of the song section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Title of song"));
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(new JLabel("Number:"));
        tempPanel.add(new JLabel("    "));
        tempPanel.add(numberField);
        tempPanel.add(new JLabel("."));
        tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(tempPanel);
        titlePanel.add(Box.createRigidArea(small));
        MyDialog.addLabelToBox(titlePanel, "Title:", 2);
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField.setFont(numberField.getFont());
        titlePanel.add(new JScrollPane(titleField));
        tempPanel = new JPanel();
        tempPanel.add(new JLabel("Take the first"));
        tempPanel.add(new JSpinner(takeFirstWordsSpinner));
        tempPanel.add(new JLabel("words from lyrics"));
        JButton takeButton = new JButton("Take");
        takeButton.addActionListener(new TakeFirstLyricsWordAction());
        tempPanel.add(takeButton);
        tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(tempPanel);
        basicCenter.add(titlePanel);
        basicCenter.add(Box.createRigidArea(large));

        //place and date
        monthCombo.setEditable(false);
        String[] days = new String[32];
        days[0] = "";
        for(int i=1;i<=31;i++)days[i] = Integer.toString(i);
        dayCombo = new JComboBox(days);
        dayCombo.setEditable(false);
        JPanel placeAndDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        placeAndDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Date and place"));
        placeAndDatePanel.add(createTitleAndInputPanel("Place:", placeField));
        placeAndDatePanel.add(createTitleAndInputPanel("Month:", monthCombo));
        placeAndDatePanel.add(createTitleAndInputPanel("Day:", dayCombo));
        placeAndDatePanel.add(createTitleAndInputPanel("Year:", yearField));
        placeAndDatePanel.setAlignmentX(0f);
        basicCenter.add(placeAndDatePanel);
        basicCenter.add(Box.createRigidArea(large));

        //information on right side
        JPanel informationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        informationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Information on right side"));
        rightInfoArea.setFont(numberField.getFont());
        informationPanel.add(new JScrollPane(rightInfoArea));
        JPanel addPlaceAndOrDatePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JButton addDateButton = new JButton("Append date");
        addDateButton.addActionListener(new AddPlaceAndDateAction(false));
        addPlaceAndOrDatePanel.add(addDateButton, gbc);
        JButton addPlaceAndDateButton = new JButton("<html><center>Append place<br>and date</center><html>");
        addPlaceAndDateButton.addActionListener(new AddPlaceAndDateAction(true));
        gbc.gridy=1;gbc.insets=new Insets(5, 0, 0, 0);
        addPlaceAndOrDatePanel.add(addPlaceAndDateButton, gbc);
        informationPanel.add(addPlaceAndOrDatePanel);
        informationPanel.setAlignmentX(0f);
        basicCenter.add(informationPanel);
        basicCenter.add(Box.createRigidArea(large));

        //tempo section
        JPanel upperTempoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tempoTypeCombo = new JComboBox(Tempo.Type.values());
        tempoTypeCombo.setRenderer(new NoteImageListCellRenderer());
        upperTempoPanel.add(tempoTypeCombo);
        upperTempoPanel.add(new JLabel("="));
        JSpinner ts = new JSpinner(tempoSpinner);
        upperTempoPanel.add(ts);
        upperTempoPanel.add(new JLabel("Description:"));
        tempoDescriptionCombo.setEditable(true);
        Utilities.readComboValuesFromFile(tempoDescriptionCombo, new File("conf/tempos"));
        upperTempoPanel.add(tempoDescriptionCombo);
        upperTempoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        showOnlyDescriptionCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel tempoPanel = new JPanel();
        tempoPanel.setLayout(new BoxLayout(tempoPanel, BoxLayout.Y_AXIS));
        tempoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Tempo"));
        tempoPanel.add(upperTempoPanel);
        tempoPanel.add(Box.createRigidArea(small));
        tempoPanel.add(showOnlyDescriptionCheckBox);
        basicCenter.add(tempoPanel);
        basicCenter.add(Box.createRigidArea(large));

        //leading keys section
        JPanel keysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        keysCombo = new JComboBox(new KeyType[]{KeyType.FLATS, KeyType.SHARPS});
        keysCombo.setRenderer(new KeysImageListCellRenderer());
        keysPanel.add(keysCombo);
        JSpinner ks = new JSpinner(keysSpinner);
        ks.setPreferredSize(ts.getPreferredSize());
        keysPanel.add(ks);
        keysPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Key Signature"));
        keysPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        basicCenter.add(keysPanel);

        //----------------------font center------------------------
        JPanel fontCenter = new JPanel();
        fontCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fontCenter.setLayout(new BoxLayout(fontCenter, BoxLayout.Y_AXIS));

        titleFontCombo = new JComboBox(MainFrame.FONTFAMILIES);
        lyricsFontCombo = new JComboBox(MainFrame.FONTFAMILIES);
        generalFontCombo = new JComboBox(MainFrame.FONTFAMILIES);
        titleFontSizeSpinner = new SpinnerNumberModel(1, 1, 256, 1);
        lyricsFontSizeSpinner = new SpinnerNumberModel(1, 1, 256, 1);
        generalFontSizeSpinner = new SpinnerNumberModel(1, 1, 256, 1);
        titleBoldToggle = new JToggleButton("<html><b>B</b></html>");
        titleItalicToggle = new JToggleButton("<html><i>I</i></html>");
        lyricsBoldToggle = new JToggleButton("<html><b>B</b></html>");
        lyricsItalicToggle = new JToggleButton("<html><i>I</i></html>");
        MyDialog.addLabelToBox(fontCenter, "Title font:", 5);
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        tempPanel.add(titleFontCombo);
        tempPanel.add(new JSpinner(titleFontSizeSpinner));
        tempPanel.add(titleBoldToggle);
        tempPanel.add(titleItalicToggle);
        tempPanel.setAlignmentX(0f);
        fontCenter.add(tempPanel);
        fontCenter.add(Box.createRigidArea(large));
        MyDialog.addLabelToBox(fontCenter, "Lyrics font:", 5);
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        tempPanel.add(lyricsFontCombo);
        tempPanel.add(new JSpinner(lyricsFontSizeSpinner));
        tempPanel.add(lyricsBoldToggle);
        tempPanel.add(lyricsItalicToggle);
        tempPanel.setAlignmentX(0f);
        fontCenter.add(tempPanel);
        fontCenter.add(Box.createRigidArea(large));
        MyDialog.addLabelToBox(fontCenter, "General font (for tempo, annotations):", 5);
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        tempPanel.add(generalFontCombo);
        tempPanel.add(new JSpinner(generalFontSizeSpinner));
        tempPanel.setAlignmentX(0f);
        fontCenter.add(tempPanel);

        JPanel fontCenterHelper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fontCenterHelper.add(fontCenter);
        setDefaultButton.addActionListener(new SetDefaultFontsAction());
        JPanel setDefaultButtonHelper = new JPanel();
        setDefaultButtonHelper.add(setDefaultButton);
        setDefaultButtonHelper.setAlignmentX(0f);
        fontCenter.add(Box.createRigidArea(large));
        fontCenter.add(setDefaultButtonHelper);

        //----------------------profile center------------------------
        JPanel profileCenter = new JPanel(new BorderLayout());
        profileCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel northProfilePanel = new JPanel();
        northProfilePanel.setLayout(new BoxLayout(northProfilePanel, BoxLayout.Y_AXIS));
        northProfilePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        MyDialog.addLabelToBox(northProfilePanel, "Available profiles:", 5);
        profileCombo = new JComboBox(mainFrame.getProfileManager().enumerateProfiles().toArray());
        profileCombo.addActionListener(new ProfileComboAction());
        profileCombo.setSelectedItem(mainFrame.getProfileManager().getDefaultProfileName());
        JPanel profileComboHelper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        profileComboHelper.add(profileCombo);
        profileComboHelper.setAlignmentX(0f);
        northProfilePanel.add(profileComboHelper);
        profileCenter.add(BorderLayout.NORTH, northProfilePanel);
        profileInfoLabel.setVerticalAlignment(SwingConstants.TOP);
        profileInfoLabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Profile properties"));
        profileCenter.add(BorderLayout.CENTER, profileInfoLabel);
        JPanel southProfilePanel = new JPanel();
        setAsDefaultButton.addActionListener(new SetAsDefaultProfileAction());
        southProfilePanel.add(setAsDefaultButton);
        JButton newProfileButton = new JButton("New profile");
        newProfileButton.addActionListener(new NewProfileAction());
        southProfilePanel.add(newProfileButton);
        JButton deleteProfileButton = new JButton("Delete profile");
        deleteProfileButton.addActionListener(new DeleteProfileAction());
        southProfilePanel.add(deleteProfileButton);
        profileCenter.add(BorderLayout.SOUTH, southProfilePanel);

        //tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Basic", basicCenter);
        tabbedPane.addTab("Fonts", fontCenterHelper);
        tabbedPane.addTab("Profile", profileCenter);

        dialogPanel.add(BorderLayout.CENTER, tabbedPane);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData(){
        MusicSheet ms = mainFrame.getMusicSheet();
        Composition c = ms.getComposition();
        numberField.setText(c.getNumber());
        titleField.setText(c.getSongTitle());
        placeField.setText(c.getPlace());
        monthCombo.setSelectedIndex(c.getMonth());
        dayCombo.setSelectedIndex(c.getDay());
        yearField.setText(c.getYear());
        rightInfoArea.setText(c.getRightInfo());
        tempoTypeCombo.setSelectedItem(c.getTempo().getTempoType());
        tempoSpinner.setValue(c.getTempo().getVisibleTempo());
        tempoDescriptionCombo.setSelectedItem(c.getTempo().getTempoDescription());
        showOnlyDescriptionCheckBox.setSelected(!c.getTempo().isShowTempo());
        keysCombo.setSelectedItem(c.getDefaultKeyType());
        keysSpinner.setValue(c.getDefaultKeys());
        titleFontCombo.setSelectedItem(c.getSongTitleFont().getFamily());
        lyricsFontCombo.setSelectedItem(c.getLyricsFont().getFamily());
        generalFontCombo.setSelectedItem(c.getGeneralFont().getFamily());
        titleFontSizeSpinner.setValue(c.getSongTitleFont().getSize());
        lyricsFontSizeSpinner.setValue(c.getLyricsFont().getSize());
        generalFontSizeSpinner.setValue(c.getGeneralFont().getSize());
        titleBoldToggle.setSelected(c.getSongTitleFont().isBold());
        titleItalicToggle.setSelected(c.getSongTitleFont().isItalic());
        lyricsBoldToggle.setSelected(c.getLyricsFont().isBold());
        lyricsItalicToggle.setSelected(c.getLyricsFont().isItalic());
    }

    protected void setData(){
        MusicSheet ms = mainFrame.getMusicSheet();
        Composition c = ms.getComposition();
        try{
            if(numberField.getText().length()>0){
                Integer.parseInt(numberField.getText());
            }
            c.setNumber(numberField.getText());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(dialogPanel, "The number of the song is not a number", mainFrame.PROGNAME, JOptionPane.ERROR_MESSAGE);
        }
        c.setSongTitle(titleField.getText());
        c.setPlace(placeField.getText());
        c.setMonth(monthCombo.getSelectedIndex());
        c.setDay(dayCombo.getSelectedIndex());
        try{
            if(yearField.getText().length()>0){
                Integer.parseInt(yearField.getText());
            }
            c.setYear(yearField.getText());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(dialogPanel, "The year of the song is not a number", mainFrame.PROGNAME, JOptionPane.ERROR_MESSAGE);
        }
        c.setRightInfo(rightInfoArea.getText());
        c.getTempo().setTempoType((Tempo.Type) tempoTypeCombo.getSelectedItem());
        c.getTempo().setVisibleTempo((Integer) tempoSpinner.getValue());
        c.getTempo().setTempoDescription((String)tempoDescriptionCombo.getSelectedItem());
        c.getTempo().setShowTempo(!showOnlyDescriptionCheckBox.isSelected());
        int oldDefKeys = c.getDefaultKeys();
        KeyType oldDefKeyType = c.getDefaultKeyType();
        c.setDefaultKeys((Integer) keysSpinner.getValue());
        c.setDefaultKeyType((KeyType)keysCombo.getSelectedItem());
        for(int i=0;i<c.lineCount();i++){
            Line l = c.getLine(i);
            if(l.getKeys()==oldDefKeys && l.getKeyType()==oldDefKeyType){
                l.setKeys(c.getDefaultKeys());
                l.setKeyType(c.getDefaultKeyType());
            }
        }
        c.setSongTitleFont(Utilities.createFont((String)titleFontCombo.getSelectedItem(),
                (titleBoldToggle.isSelected() ? Font.BOLD : 0) | (titleItalicToggle.isSelected() ? Font.ITALIC : 0),
                (Integer)titleFontSizeSpinner.getValue()));
        c.setLyricsFont(Utilities.createFont((String)lyricsFontCombo.getSelectedItem(),
                (lyricsBoldToggle.isSelected() ? Font.BOLD : 0) | (lyricsItalicToggle.isSelected() ? Font.ITALIC : 0),
                (Integer)lyricsFontSizeSpinner.getValue()));
        c.setGeneralFont(Utilities.createFont((String)generalFontCombo.getSelectedItem(),
                Font.PLAIN,
                (Integer)generalFontSizeSpinner.getValue()));
        c.recalcTopSpace();
        ms.viewChanged();
    }

    private JPanel createTitleAndInputPanel(String title, JComponent input){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        MyDialog.addLabelToBox(panel, title, 5);
        input.setAlignmentX(0f);
        panel.add(input);
        return panel;
    }

    static class NoteImageListCellRenderer implements ListCellRenderer{
        JLabel[] labels = new JLabel[Tempo.Type.values().length];

        public NoteImageListCellRenderer() {
            Dimension imgDim = new Dimension(35, 35);
            Dimension prefDim = new Dimension(35, 37);
            int i=0;
            for(Tempo.Type t:Tempo.Type.values()){
                Rectangle noteRect = t.getNote().getRealUpNoteRect();
                BufferedImage noteImg = (BufferedImage)Note.clipNoteImage(t.getNote().getUpImage(),
                        noteRect, Color.blue, imgDim);
                if(t.getNote().getDotted()>0){
                    Graphics2D g2 = noteImg.createGraphics();
                    for(int j=0;j<t.getNote().getDotted();j++){
                        g2.drawImage(Note.DOTIMAGE, j*4+(imgDim.width-noteRect.width)/2-noteRect.x,
                                (imgDim.height-noteRect.height)/2-noteRect.y, null);
                    }
                    g2.dispose();
                }
                labels[i] = new JLabel(new ImageIcon(noteImg));
                labels[i].setPreferredSize(prefDim);
                i++;
            }
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return labels[value==null ? 0 : ((Tempo.Type)value).ordinal()];
        }
    }

    static class KeysImageListCellRenderer implements ListCellRenderer{
        JLabel[] labels = new JLabel[2];

        public KeysImageListCellRenderer() {
            Dimension imgDim = new Dimension(35, 35);
            Dimension prefDim = new Dimension(35, 37);
            for(int i=0;i<2;i++){
                labels[i] = new JLabel(new ImageIcon(Note.clipNoteImage(Note.NATURALFLATSHARPIMAGE[i+1],
                            Note.REALNATURALFLATSHARPRECT[i+1], Color.blue, imgDim)));
                labels[i].setPreferredSize(prefDim);
            }
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return labels[value ==KeyType.FLATS ? 0 : 1];
        }
    }

    private class TakeFirstLyricsWordAction implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            String lyrics = mainFrame.getMusicSheet().getComposition().getLyrics();
            if(lyrics.length()==0){
                JOptionPane.showMessageDialog(dialogPanel, "You have not entered lyrics yet!", mainFrame.PROGNAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            StringBuilder words = new StringBuilder(50);
            int wordsCount=0;
            boolean firstLetter = false;
            boolean lastHyphen = false;
            goThruString:
            for(int i=0;i<lyrics.length();i++){
                switch(lyrics.charAt(i)){
                    case ' ':
                    case '\n':
                        wordsCount++;
                        if(wordsCount>=((Number)takeFirstWordsSpinner.getValue()).intValue()){
                           break goThruString;
                        }
                        words.append(' ');
                        firstLetter = true;
                        break;
                    case '-':
                        if(lastHyphen){
                            words.append('-');
                            wordsCount++;
                            firstLetter=true;
                        }
                        lastHyphen = !lastHyphen;
                    case '_':
                        break;
                    default:
                        if(firstLetter){
                            words.append(new String(new char[]{lyrics.charAt(i)}).toUpperCase());
                            firstLetter = false;
                        }else{
                            words.append(lyrics.charAt(i));
                        }
                        lastHyphen = false;
                }
            }
            if(!Character.isLetter(words.charAt(words.length()-1))){
                words.deleteCharAt(words.length()-1);
            }
            titleField.setText(words.toString());
        }
    }

    private class AddPlaceAndDateAction implements ActionListener {
        private boolean withPlace;

        public AddPlaceAndDateAction(boolean withPlace) {
            this.withPlace = withPlace;
        }

        public void actionPerformed(ActionEvent e) {
            StringBuilder sb = new StringBuilder(30);
            String rightInfo = rightInfoArea.getText();
            if(rightInfo.charAt(rightInfo.length()-1)!='\n')sb.append('\n');
            if(monthCombo.getSelectedIndex()>0){
                sb.append(monthCombo.getSelectedItem());
                if(dayCombo.getSelectedIndex()>0){
                    sb.append(' ');
                    sb.append(dayCombo.getSelectedItem());
                }
                sb.append(", ");
            }
            try {
                sb.append(Integer.toString(Integer.parseInt(yearField.getText())));
            } catch (NumberFormatException e1) {
                JOptionPane.showMessageDialog(dialogPanel, "The year of the song is not a number", mainFrame.PROGNAME, JOptionPane.ERROR_MESSAGE);
            }
            if(withPlace){
                sb.append('\n');
                sb.append(placeField.getText());
            }
            rightInfoArea.append(sb.toString());
        }
    }

    private class SetDefaultFontsAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ProfileManager pm = mainFrame.getProfileManager();
            titleFontCombo.setSelectedItem(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONT));
            lyricsFontCombo.setSelectedItem(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONT));
            generalFontCombo.setSelectedItem(pm.getDefaultProperty(ProfileManager.ProfileKey.GENERALFONT));
            titleFontSizeSpinner.setValue(Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONTSIZE)));
            lyricsFontSizeSpinner.setValue(Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONTSIZE)));
            generalFontSizeSpinner.setValue(Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.GENERALFONTSIZE)));

            int titleStyle = ProfileManager.intFontStyle(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONTSTYLE));
            titleBoldToggle.setSelected((titleStyle&Font.BOLD)!=0);
            titleItalicToggle.setSelected((titleStyle&Font.ITALIC)!=0);
            int lyricsStyle = ProfileManager.intFontStyle(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONTSTYLE));
            lyricsBoldToggle.setSelected((lyricsStyle&Font.BOLD)!=0);
            lyricsItalicToggle.setSelected((lyricsStyle&Font.ITALIC)!=0);
        }
    }

    private class SetAsDefaultProfileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            mainFrame.getProfileManager().setDefaultProfile((String)profileCombo.getSelectedItem());
            setAsDefaultButton.setEnabled(false);
        }
    }

    private class NewProfileAction implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(dialogPanel, "Enter the name of the new profile:", mainFrame.PROGNAME, JOptionPane.QUESTION_MESSAGE);
            if(name==null)return;
            try {
                mainFrame.getProfileManager().saveProfile(name);
                profileCombo.addItem(name);
                profileCombo.setSelectedItem(name);
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(dialogPanel, "Could not create a new profile.", mainFrame.PROGNAME, JOptionPane.ERROR_MESSAGE);
                logger.error("Profile creating", e1);
            }
        }
    }

    private class DeleteProfileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(profileCombo.getItemCount()<=1){
                JOptionPane.showMessageDialog(dialogPanel, "You cannot delete the last profile", mainFrame.PROGNAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            int answer = JOptionPane.showConfirmDialog(dialogPanel, "Are you sure to delete the selected profile?", mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION);
            if(answer==JOptionPane.YES_OPTION){
                String selected = (String)profileCombo.getSelectedItem();
                if(new File(ProfileManager.DEFAULTDIR, selected).delete()){
                    profileCombo.removeItem(selected);
                    if(selected.equals(mainFrame.getProfileManager().getDefaultProfileName())){
                        setAsDefaultButton.doClick();
                    }
                }else{
                    JOptionPane.showMessageDialog(dialogPanel, "Could not delete the profile", mainFrame.PROGNAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class ProfileComboAction implements ActionListener{
        StringBuilder sb = new StringBuilder(200);

        public void actionPerformed(ActionEvent e) {
            String selected = (String)profileCombo.getSelectedItem();
            Properties props = mainFrame.getProfileManager().getProfile(selected);
            sb.delete(0, sb.length());
            sb.append("<html><table border=0>");
            for(ProfileManager.ProfileKey pk : ProfileManager.ProfileKey.values()){
                sb.append("<tr><td>");
                sb.append(pk.getKey());
                sb.append(":</td><td>");
                sb.append(props.getProperty(pk.getKey()));
                sb.append("</td></tr>");
            }
            sb.append("</table></html>");
            profileInfoLabel.setText(sb.toString());
            setAsDefaultButton.setEnabled(!selected.equals(mainFrame.getProfileManager().getDefaultProfileName()));
        }
    }
}
