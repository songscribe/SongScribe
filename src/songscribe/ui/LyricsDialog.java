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

Created on Sep 24, 2005
*/
package songscribe.ui;

import songscribe.music.NoteType;
import songscribe.music.Composition;
import songscribe.data.MyJTextArea;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class LyricsDialog extends MyDialog{
    JTextArea lyricsArea = new MyJTextArea(6, 30);
    JTextArea underSongArea = new MyJTextArea(6, 30);
    JTextArea translatedArea = new MyJTextArea(6, 30);

    final static char[][] specChars = {{'\u0103', '\u0101', '\u00f1', '\u00e2'},{'\u0102', '\u0100', '\u00d1', '\u00c2'}};
    final static char[][] specCharsMap = {{'a', 'a', 'n', 'a'}, {'A', 'A', 'N', 'A'}};
    static final int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    final KeyStroke[][] specCharStroke = {{KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_SHORTCUT_KEY_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_SHORTCUT_KEY_MASK), null},
        {KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_SHORTCUT_KEY_MASK |InputEvent.SHIFT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK|InputEvent.SHIFT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_SHORTCUT_KEY_MASK|InputEvent.SHIFT_MASK), null}};

    public LyricsDialog(MainFrame mainFrame) {
        super(mainFrame, "Lyrics");

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel syllabifiedLyricsPanel = new JPanel();

        Border etchedEmpty = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5 ));
        syllabifiedLyricsPanel.setBorder(BorderFactory.createTitledBorder(etchedEmpty, "Syllabified lyrics"));
        syllabifiedLyricsPanel.setLayout(new BoxLayout(syllabifiedLyricsPanel, BoxLayout.Y_AXIS));
        lyricsArea.setToolTipText("This text is shown under the notes");
        JScrollPane lyricsScrollPane = new JScrollPane(lyricsArea);
        lyricsScrollPane.setAlignmentX(0.0f);
        syllabifiedLyricsPanel.add(lyricsScrollPane);
        syllabifiedLyricsPanel.add(Box.createVerticalStrut(10));

        JPanel charsPanel = new JPanel(new GridLayout(2, specChars[0].length, 5, 5));
        for(int i=0;i<2;i++){
            for(int j=0;j<specChars[i].length;j++){
                JButton button = new JButton(Character.toString(specChars[i][j]));

                final int k = i;
                final int l = j;
                AbstractAction action = new AbstractAction(){
                                    public void actionPerformed(ActionEvent e) {
                                        lyricsArea.insert(Character.toString(specChars[k][l]), lyricsArea.getCaretPosition());
                                        lyricsArea.requestFocusInWindow();
                                    }
                                };
                button.addActionListener(action);
                if(specCharStroke[i][j]!=null){
                    button.setToolTipText(NoteType.getCompoundName("", specCharStroke[i][j]));
                    lyricsArea.getInputMap().put(specCharStroke[i][j], specCharStroke[i][j]);
                    lyricsArea.getActionMap().put(specCharStroke[i][j], action);
                }
                charsPanel.add(button);
            }
            JPanel charsPanelHelper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            charsPanelHelper.setAlignmentX(0.0f);
            charsPanelHelper.add(charsPanel);
            syllabifiedLyricsPanel.add(charsPanelHelper);
        }
        syllabifiedLyricsPanel.setAlignmentX((0f));
        center.add(syllabifiedLyricsPanel);

        JPanel underSongLyricsPanel = new JPanel();
        underSongLyricsPanel.setBorder(BorderFactory.createTitledBorder(etchedEmpty, "Lyrics under the song"));
        underSongLyricsPanel.setLayout(new BoxLayout(underSongLyricsPanel, BoxLayout.Y_AXIS));
        underSongArea.setToolTipText("This text is shown under the song");
        JScrollPane underSongLyricsScrollPane = new JScrollPane(underSongArea);
        underSongLyricsScrollPane.setAlignmentX(0f);
        underSongLyricsPanel.add(underSongLyricsScrollPane);
        underSongLyricsPanel.add(Box.createVerticalStrut(10));
        JPanel takePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton takeButton = new JButton("Take from syllabified lyrics");
        takeButton.addActionListener(new TakeUnderSongLyricsFromSyllabifiedLyricsAction());
        takePanel.add(takeButton);
        takePanel.setAlignmentX(0f);
        underSongLyricsPanel.add(takePanel);
        underSongLyricsPanel.setAlignmentX(0f);
        center.add(Box.createVerticalStrut(15));
        center.add(underSongLyricsPanel);

        JPanel translatedLyricsPanel = new JPanel();
        translatedLyricsPanel.setBorder(BorderFactory.createTitledBorder(etchedEmpty, "Translated lyrics"));
        translatedLyricsPanel.setLayout(new BoxLayout(translatedLyricsPanel, BoxLayout.Y_AXIS));
        translatedArea.setToolTipText("This text is shown under the song as translation");
        JScrollPane translatedScrollPane = new JScrollPane(translatedArea);
        translatedScrollPane.setAlignmentX(0f);
        translatedLyricsPanel.add(translatedScrollPane);
        translatedLyricsPanel.setAlignmentX(0f);
        center.add(Box.createVerticalStrut(15));
        center.add(translatedLyricsPanel);

        dialogPanel.add(BorderLayout.CENTER, center);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData(){
        Composition c = mainFrame.getMusicSheet().getComposition();
        lyricsArea.setText(c.getLyrics());
        underSongArea.setText(c.getUnderLyrics());
        translatedArea.setText(c.getTranslatedLyrics());
    }

    protected void setData(){
        Composition c = mainFrame.getMusicSheet().getComposition();
        c.setLyrics(lyricsArea.getText());
        c.setUnderLyrics(underSongArea.getText());
        c.setTranslatedLyrics(translatedArea.getText());
        mainFrame.getMusicSheet().spellLyrics();
        mainFrame.modifiedDocument();
    }

    private class TakeUnderSongLyricsFromSyllabifiedLyricsAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            char[] lyricsChars = lyricsArea.getText().toCharArray();
            StringBuilder sb = new StringBuilder(lyricsArea.getText().length());
            for(int i=0;i<lyricsChars.length;i++){
                char c = lyricsChars[i];
                if(c!='-' && c!='_'){
                    sb.append(c);
                }else if(c=='-' && i<lyricsChars.length-1 && lyricsChars[i+1]=='-'){
                    sb.append('-');
                }
            }
            underSongArea.setText(sb.toString());
        }
    }
}
