/*
SongScribe song notation program
Copyright (C) 2006-2010 Csaba Kavai

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

Created on Jan 23, 2010
*/
package songscribe.ui;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Csaba Kávai
 */
public class LyricsModePanel extends LyricsDialog {
    private JPanel lyricsModePanel;
    private JSplitPane lyricsSplitPane;
    private JSplitPane subSplitPane;

    public LyricsModePanel(MainFrame mainFrame) {
        super(mainFrame);
        lyricsSplitPane.setLeftComponent(syllabifiedLyricsPanel);
        subSplitPane.setTopComponent(underLyricsPanel);
        subSplitPane.setBottomComponent(translatedLyricsPanel);
        MusicSheet musicSheet = mainFrame.getMusicSheet();
        musicSheet.addFocusLostExceptions(lyricsArea);
        musicSheet.addFocusLostExceptions(underSongArea);
        musicSheet.addFocusLostExceptions(translatedArea);
        lyricsArea.addKeyListener(new LyricsKeyListener(true));
        underSongArea.addKeyListener(new LyricsKeyListener(false));
        translatedArea.addKeyListener(new LyricsKeyListener(false));
    }

    public JPanel getLyricsModePanel() {
        return lyricsModePanel;
    }

    private class LyricsKeyListener extends KeyAdapter {
        private boolean spellLyricsNecessary;

        private LyricsKeyListener(boolean spellLyricsNecessary) {
            this.spellLyricsNecessary = spellLyricsNecessary;
        }

        public void keyTyped(KeyEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MusicSheet sheet = mainFrame.getMusicSheet();
                    sheet.getComposition().setLyrics(lyricsArea.getText());
                    sheet.getComposition().setUnderLyrics(underSongArea.getText());
                    sheet.getComposition().setTranslatedLyrics(translatedArea.getText());
                    if (spellLyricsNecessary) {
                        sheet.spellLyrics();
                    }
                    mainFrame.modifiedDocument();
                    sheet.setRepaintImage(true);
                    sheet.repaint();
                }
            });
        }
    }
}
