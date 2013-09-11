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
import songscribe.data.Fraction;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.music.Composition;
import songscribe.music.KeyType;
import songscribe.music.Note;
import songscribe.music.Tempo;
import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Csaba Kávai
 */
public class ExportABCAnnotationAction extends AbstractAction {
    private Logger logger = Logger.getLogger(ExportABCAnnotationAction.class);
    private PlatformFileDialog pfd;
    private MainFrame mainFrame;

    public ExportABCAnnotationAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as MIDI...");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("midiexport.png")));
        pfd = new PlatformFileDialog(mainFrame, "Export as ABC Annotation", false, new MyAcceptFilter("ABC Files", "abc"));
    }

    public void actionPerformed(ActionEvent e) {
        pfd.setFile(Utilities.getSongTitleFileNameForFileChooser(mainFrame.getMusicSheet()));
        if(pfd.showDialog()){
            File saveFile = pfd.getFile();
            if(!saveFile.getName().toLowerCase().endsWith(".abc")){
                saveFile = new File(saveFile.getAbsolutePath()+".abc");
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(mainFrame, "The file "+saveFile.getName()+" already exists. Do you want to overwrite it?",
                        mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(saveFile);
                writeABC(writer);
            } catch (IOException e1) {
                mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                logger.error("Saving ABC", e1);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    void writeABC(PrintWriter writer) {
        Composition composition = mainFrame.getMusicSheet().getComposition();
        writer.println("%abc-2.1");
        writer.println();

        // tune header
        writer.println("X:1");
        writer.println("T:"+composition.getSongTitle().replace('\n', ' '));
        writer.println("w:"+composition.getLyrics().replace('\n', ' '));
        writer.println("W:"+composition.getUnderLyrics().replace('\n', ' '));
        writer.println("C:"+composition.getRightInfo().replace('\n', ' '));
        writer.println("Q:" + translateTempo(composition.getTempo()));
        writer.println("L:1");
        writer.println("K:" + translateKey(composition.getDefaultKeyType(), composition.getDefaultKeys())); //last


    }

    String translateKey(KeyType keyType, int number) {
        String[] sharpKeys = {"C", "G", "D", "A", "E", "B", "F#", "C#"};
        String[] flatKeys = {"C", "F", "Bb", "Eb", "Ab", "Db", "Gb", "Cb"};
        String key;
        if (keyType == KeyType.SHARPS) {
            key = sharpKeys[number];
        } else {
            key = flatKeys[number];
        }
        return key + " major";
    }

    String translateTempo(Tempo tempo) {
        if (!tempo.isShowTempo()) {
            return "\"" + tempo.getTempoDescription() + "\"";
        } else {
            Fraction fraction = translateUnitLength(tempo.getTempoType().getNote().getDuration());
            return fraction.getNumerator() + "/" + fraction.getDenominator() + "=" + tempo.getVisibleTempo() + " \"" + tempo.getTempoDescription() + "\"";
        }
    }

    Fraction translateUnitLength(int duration) {
        int upper = duration;
        int lower = Composition.PPQ * 4;
        for (int i = 2; i <= upper; i++) {
            while (upper % i == 0 && lower % i == 0) {
                upper /= i;
                lower /= i;
            }
        }
        return new Fraction(upper, lower);
    }

    String translatePitch(Note note) {
        StringBuilder sb = new StringBuilder();
        char letter = (char)((note.getPitchType() + 1 ) % 7 + (note.getYPos() >= 0 ? 'A' : 'a'));
        sb.append(letter);
        for (int yPos = note.getYPos();yPos >= 7; yPos-=7) {
            sb.append(',');
        }
        for (int yPos = note.getYPos();yPos < -7; yPos+=7) {
            sb.append('\'');
        }
        return sb.toString();
    }

    String translateAccidental(Note.Accidental accidental) {
        String[] accidentalMap = {"=", "_", "^", "^^"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < accidental.getNb(); i++) {
            sb.append(accidentalMap[accidental.getComponent(i)]);
        }
        return sb.toString();
    }

    String translateNoteLength(int duration) {
        Fraction fraction = translateUnitLength(duration);
        if (fraction.getNumerator() == 1 && fraction.getDenominator() == 1) {
            return "";
        }
        if (fraction.getNumerator() == 1) {
            return "/" + fraction.getDenominator();
        }
        if (fraction.getDenominator() == 1) {
            return Integer.toString(fraction.getNumerator());
        }
        return fraction.getNumerator() + "/" + fraction.getDenominator();
    }

}
