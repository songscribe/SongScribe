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
import java.awt.event.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class LyricsDialog extends MyDialog{
    private MyJTextArea lyricsArea;
    private MyJTextArea underSongArea;
    private MyJTextArea translatedArea;
    private JPanel centerPanel;
    private JPanel charsPanel;
    private JButton moreButton;
    private JButton takeButton;
    private JPanel morePanel;
    private JButton nonBreakingHyphenButton;

    final static char[][] specChars = {{'\u0103', '\u0101', '\u00f1', '\u00e2', '\u0169'},{'\u0102', '\u0100', '\u00d1', '\u00c2', '\u0168'}};
    final static char[][] specCharsMap = {{'a', 'a', 'n', 'a', 'u'}, {'A', 'A', 'N', 'A', 'U'}};
    static final int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    final KeyStroke[][] specCharStroke = {{KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_SHORTCUT_KEY_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_SHORTCUT_KEY_MASK), null, null},
        {KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_SHORTCUT_KEY_MASK |InputEvent.SHIFT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK|InputEvent.SHIFT_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_SHORTCUT_KEY_MASK|InputEvent.SHIFT_MASK), null, null}};

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
                lyricsArea.insert(Character.toString('\u00AD'), lyricsArea.getCaretPosition());
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
            boolean inParanthesis = false;
            StringBuilder sb = new StringBuilder(lyricsArea.getText().length());
            for(int i=0;i<lyricsChars.length;i++){
                char c = lyricsChars[i];
                if(c=='(')inParanthesis=true;
                if(!inParanthesis){
                    if(c!='-' && c!='_'){
                        sb.append(c);
                    }else if(c=='-' && i<lyricsChars.length-1 && lyricsChars[i+1]=='-'){
                        sb.append('-');
                    }
                }
                if(c==')')inParanthesis=false;
            }
            underSongArea.setText(sb.toString());
        }
    }
}
