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

import songscribe.data.MyJTextArea;
import songscribe.music.Composition;
import songscribe.music.NoteType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Csaba Kávai
 */
public class LyricsDialog extends MyDialog{
    protected MyJTextArea lyricsArea;
    protected MyJTextArea underSongArea;
    protected MyJTextArea translatedArea;
    protected JPanel centerPanel;
    protected JPanel charsPanel;
    protected JButton moreButton;
    protected JButton takeButton;
    protected JPanel morePanel;
    protected JButton nonBreakingHyphenButton;
    protected JPanel syllabifiedLyricsPanel;
    protected JPanel underLyricsPanel;
    protected JPanel translatedLyricsPanel;

    final static char[][] specChars = {{'\u0103', '\u0101', '\u00f1', '\u00e2', '\u0169', '\u00e3'},{'\u0102', '\u0100', '\u00d1', '\u00c2', '\u0168', '\u00c3'}};
    final static char[][] specCharsMap = {{'a', 'a', 'n', 'a', 'u', 'a'}, {'A', 'A', 'N', 'A', 'U', 'A'}};
    static final int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    final KeyStroke[][] specCharStroke = {{KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_SHORTCUT_KEY_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_SHORTCUT_KEY_MASK), null, null, null},
        {KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_SHORTCUT_KEY_MASK |InputEvent.SHIFT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK|InputEvent.SHIFT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_SHORTCUT_KEY_MASK|InputEvent.SHIFT_MASK), null, null, null}};

    public LyricsDialog(MainFrame mainFrame) {
        super(mainFrame, "Lyrics");

        charsPanel.setLayout(new GridLayout(2, specChars[0].length, 5, 5));
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
        }
        nonBreakingHyphenButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                lyricsArea.insert(Constants.NON_BREAKING_HYPHEN, lyricsArea.getCaretPosition());
                lyricsArea.requestFocusInWindow();
            }
        });
        takeButton.addActionListener(new TakeUnderSongLyricsFromSyllabifiedLyricsAction());
        morePanel.setVisible(false);
        moreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                morePanel.setVisible(!morePanel.isVisible());
                moreButton.setText(morePanel.isVisible() ? "<< Less" : "More >>");
                pack();
            }
        });

        dialogPanel.add(BorderLayout.CENTER, centerPanel);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData(){
        Composition composition = mainFrame.getMusicSheet().getComposition();
        lyricsArea.setText(composition.getLyrics());
        underSongArea.setText(composition.getUnderLyrics());
        translatedArea.setText(composition.getTranslatedLyrics());
    }

    protected void setData(){
        Composition composition = mainFrame.getMusicSheet().getComposition();
        composition.setLyrics(lyricsArea.getText());
        composition.setUnderLyrics(underSongArea.getText());
        composition.setTranslatedLyrics(translatedArea.getText());
        mainFrame.getMusicSheet().spellLyrics();
        mainFrame.modifiedDocument();
        mainFrame.getLyricsModePanel().getData();
    }

    private class TakeUnderSongLyricsFromSyllabifiedLyricsAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String underLyrics = Utilities.removeSyllablifyMarkings(lyricsArea.getText());
            underSongArea.setText(underLyrics);
        }
    }
}
