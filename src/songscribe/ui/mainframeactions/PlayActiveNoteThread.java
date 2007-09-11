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

Created on Aug 6, 2006
*/
package songscribe.ui.mainframeactions;

import org.apache.log4j.Logger;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;

import songscribe.ui.MainFrame;

/**
 * @author Csaba Kávai
 */
public class PlayActiveNoteThread extends Thread{
    private static Logger logger = Logger.getLogger(PlayActiveNoteThread.class);
    private int pitch;

    public PlayActiveNoteThread(int pitch) {
        this.pitch = pitch;
    }

    public void run() {
        if(MainFrame.receiver==null)return;
        try {
            ShortMessage down = new ShortMessage();
            down.setMessage(0x90, pitch, 64);
            ShortMessage up = new ShortMessage();
            up.setMessage(0x80, pitch, 64);
            MainFrame.receiver.send(down, -1);
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {}
            MainFrame.receiver.send(up, -1);
        } catch (InvalidMidiDataException e) {
            logger.error("PlayActiveNoteThread", e);
        }
    }
}
