/*
    SongScribe song notation program
    Copyright (C) 2014 Csaba Kavai

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
*/
package songscribe.converter;

import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import javax.sound.midi.MidiSystem;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class MidiConverter {
    @ArgumentDescribe("MIDI Instrument (0-127)")
    public int instrument = 0;

    @ArgumentDescribe("Export with repeats")
    public boolean withRepeat = false;

    @ArgumentDescribe("Tempo change (in percentage)")
    public int tempoChange = 100;

    @FileArgument
    public File[] files;

    public static void main(String[] args) {
        ArgumentReader am = new ArgumentReader(args, MidiConverter.class);
        ((MidiConverter) am.getObj()).convert();
    }

    public void convert() {
        if (instrument < 0 || instrument > 127) {
            System.out.println("The instrument must be in range of 0-127");
            return;
        }

        if (tempoChange <= 0 || tempoChange > 200) {
            System.out.println("The tempo change must be in range of 1-200");
            return;
        }

        MainFrame mf = new MainFrame() {
            @Override
            public void showErrorMessage(String message) {
                System.out.println(message);
            }
        };
        mf.setMusicSheet(new MusicSheet(mf));

        Properties props = new Properties(mf.getProperties());
        props.setProperty(Constants.WITH_REPEAT_PROP, withRepeat ? Constants.TRUE_VALUE : Constants.FALSE_VALUE);
        props.setProperty(Constants.INSTRUMENT_PROP, Integer.toString(instrument));
        props.setProperty(Constants.TEMPO_CHANGE_PROP, Integer.toString(tempoChange));

        for (File file : files) {
            try {
                mf.getMusicSheet().setComposition(null);
                mf.openMusicSheet(file, false);
                mf.getMusicSheet().getComposition().musicChanged(props);
                String path = file.getCanonicalPath();
                int dotPos = path.lastIndexOf('.');

                if (dotPos > 0) {
                    path = path.substring(0, dotPos);
                }

                MidiSystem.write(mf.getMusicSheet().getComposition().getSequence(), 1, new File(path + ".midi"));
            }
            catch (IOException e) {
                System.out.println("Could not convert " + file.getName());
            }
        }
    }
}
