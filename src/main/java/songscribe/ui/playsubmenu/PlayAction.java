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

Created on Oct 8, 2006
*/
package songscribe.ui.playsubmenu;

import org.apache.log4j.Logger;
import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
class PlayAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(PlayAction.class);
    private PlayMenu playMenu;

    public PlayAction(PlayMenu playMenu) {
        this.playMenu = playMenu;
        putValue(Action.NAME, "Play");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("player_play.png")));
        setEnabled(MainFrame.sequencer!=null);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if(MainFrame.sequencer==null)return;
            MusicSheet musicSheet = playMenu.getMainFrame().getMusicSheet();
            MusicSheet.NoteSelection noteSelection = musicSheet.getSelection();
            Sequence thisSequence = noteSelection==null ? musicSheet.getComposition().getSequence() : musicSheet.getComposition().getSelectedSequence(noteSelection.line, noteSelection.begin, noteSelection.end);
            if(MainFrame.sequencer.getTickPosition()>=MainFrame.sequencer.getTickLength() || thisSequence!=MainFrame.sequencer.getSequence()){
                MainFrame.sequencer.setTickPosition(0);
                if(thisSequence!=MainFrame.sequencer.getSequence()){
                    MainFrame.sequencer.setSequence(thisSequence);
                }
            }
            playMenu.enableAllComponents(false);
            boolean playContinuously = playMenu.getMainFrame().getProperties().getProperty(Constants.PLAYCONTINUOUSLYPROP).equals(Constants.TRUEVALUE);
            MainFrame.sequencer.setLoopCount(playContinuously ? Sequencer.LOOP_CONTINUOUSLY : 0);
            MainFrame.sequencer.start();
            if(!playContinuously){
                playMenu.disableComponentsThread = new DisableComponentsThread(playMenu, (MainFrame.sequencer.getMicrosecondLength()-MainFrame.sequencer.getMicrosecondPosition())/1000);
                playMenu.disableComponentsThread.start();
            }
        } catch (InvalidMidiDataException e1) {
            playMenu.getMainFrame().showErrorMessage("Could not play back the song because of an unexpected error.");
            logger.error("Playback", e1);
        }
    }
}
