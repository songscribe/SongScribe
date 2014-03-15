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
import songscribe.data.*;
import songscribe.music.*;
import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/** The following features are not supported in abc 2.1
 * 1. Accidentals in parenthesis. I opened a forum for this topic (with no answer yet): http://abcnotation.com/forums/viewtopic.php?f=7&t=260
 * 2. Beat change.
 * 3. Glissandos. I substituted them with slurs for now.
 * 4. On syllabified lyrics no long hyphen between compound words (like God-Realisation)
 * 5. Syllables under grace notes. Solution: put together with the syllable of next note with \
 * 6. Forcing syllable under a rest (new SongScribe feature)
 * @author Csaba Kávai
 */
public class ExportABCAnnotationAction extends AbstractAction {
    private Logger logger = Logger.getLogger(ExportABCAnnotationAction.class);
    private PlatformFileDialog pfd;
    private MainFrame mainFrame;
    
    int compositionUnitLength;

    public ExportABCAnnotationAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as ABC Notation...");
        pfd = new PlatformFileDialog(mainFrame, "Export as ABC Notation", false, new MyAcceptFilter("ABC Files", "abc"));
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
    
    int determineCompositionUnitLength(Composition composition) {
        Map<Integer, Integer> unitLengths = new HashMap<Integer, Integer>();
        for (int l = 0; l < composition.lineCount(); l++) {
            Line line = composition.getLine(l);
            for (int n = 0; n < line.noteCount(); n++) {
                Note note = line.getNote(n);
                if (note.getNoteType().isRealNote()) {
                    Integer defaultDuration = note.getDefaultDuration();
                    Integer count = unitLengths.get(defaultDuration);
                    if (count == null) {
                        unitLengths.put(defaultDuration, 1);
                    } else {
                        unitLengths.put(defaultDuration, count + 1);
                    }
                }
            }
        }
        Map.Entry<Integer, Integer> maxValueEntry = new AbstractMap.SimpleEntry<Integer, Integer>(0, Integer.MIN_VALUE);
        for (Map.Entry<Integer, Integer> entry: unitLengths.entrySet()) {
            if (entry.getValue() > maxValueEntry.getValue()) {
                maxValueEntry = entry;
            }
        }
        if (maxValueEntry.getValue() == Integer.MIN_VALUE) {
            return Composition.PPQ * 4;
        } else {
            return maxValueEntry.getKey();
        }
    }

    public void writeABC(PrintWriter writer) {
        Composition composition = mainFrame.getMusicSheet().getComposition();
        compositionUnitLength = determineCompositionUnitLength(composition);
        writer.println("%abc-2.1");
        writer.println("I:abc-creator " + MainFrame.PACKAGENAME + " " + Utilities.getPublicVersion());
        writer.println();

        // tune header
        writer.println("X:1");
        writer.println("T:"+composition.getSongTitle().replace('\n', ' '));
        writer.println("W:"+composition.getUnderLyrics().replace('\n', ' '));
        writer.println("C:"+composition.getRightInfo().replace('\n', ' '));
        writer.println("Q:" + translateTempo(composition.getTempo()));
        writer.println("L:" + translateUnitLength(compositionUnitLength, Composition.PPQ * 4).asAbcString());        
        writer.println("K:" + translateKey(composition.getDefaultKeyType(), composition.getDefaultKeys())); //last
        translateComposition(writer, composition);
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
            Fraction fraction = translateUnitLength(tempo.getTempoType().getNote().getDuration(), Composition.PPQ * 4);
            return fraction.asAbcString() + "=" + tempo.getVisibleTempo() + " \"" + tempo.getTempoDescription() + "\"";
        }
    }

    Fraction translateUnitLength(int duration, int unitLength) {
        int upper = duration;
        int lower = unitLength;
        for (int i = 2; i <= upper; i++) {
            while (upper % i == 0 && lower % i == 0) {
                upper /= i;
                lower /= i;
            }
        }
        return new Fraction(upper, lower);
    }

    String translatePitch(int yPos) {
        StringBuilder sb = new StringBuilder();
        char letter = (char)((getPitchType(yPos) + 1 ) % 7 + (yPos >= 0 ? 'A' : 'a'));
        sb.append(letter);
        for (int y = yPos;y >= 7; y-=7) {
            sb.append(',');
        }
        for (int y = yPos;y < -7; y+=7) {
            sb.append('\'');
        }
        return sb.toString();
    }

    /**
     * @return 0 for B, 1 for C, 2 for D, ..., 6 for A
     */
    int getPitchType(int yPos){
        return yPos<=0 ? -yPos % 7 : (7 - yPos % 7) % 7;
    }

    String translateAccidental(Note.Accidental accidental) {
        // TODO no accidental in parenthesis
        String[] accidentalMap = {"=", "_", "^", "^^"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < accidental.getNb(); i++) {
            sb.append(accidentalMap[accidental.getComponent(i)]);
        }
        return sb.toString();
    }

    String translateNoteLength(int duration) {
        Fraction fraction = translateUnitLength(duration, compositionUnitLength);
        if (fraction.getNumerator() == 1 && fraction.getDenominator() == 1) {
            return "";
        }
        if (fraction.getNumerator() == 1) {
            return "/" + fraction.getDenominator();
        }
        if (fraction.getDenominator() == 1) {
            return Integer.toString(fraction.getNumerator());
        }
        return fraction.asAbcString();
    }
    
    String translateRepeatAndBarLine(NoteType noteType) {
        switch (noteType) {
            case REPEATLEFT:
                return "|:";
            case REPEATRIGHT:
                return ":|";
            case REPEATLEFTRIGHT:
                return "::";
            case SINGLEBARLINE:
                return "|";
            case DOUBLEBARLINE:
                return "||";
            case FINALDOUBLEBARLINE:
                return "|]";
            default:
                return "";
        }
    }
    
    String translateDecorations(Note note) {
        StringBuilder sb = new StringBuilder();
        if (note.getForceArticulation() == ForceArticulation.ACCENT) {
            sb.append("!>!");
        }
        if (note.getDurationArticulation() == DurationArticulation.STACCATO) {
            sb.append(".");
        }
        if (note.getDurationArticulation() == DurationArticulation.TENUTO) {
            sb.append("!tenuto!");
        }
        if (note.isFermata()) {
            sb.append("!fermata!");
        }
        if (note.isTrill()) {
            sb.append("!trill!");
        }
        return sb.toString();
    }
    
    String translateAnnotation(Annotation annotation) {
        if (annotation != null) {
            int aboveDiff = Math.abs(annotation.getyPos() - Annotation.ABOVE);
            int belowDiff = Math.abs(annotation.getyPos() - Annotation.BELOW);
            return "\"" + (aboveDiff < belowDiff ? "^" : "_") + annotation.getAnnotation() + "\""; 
        }
        return "";
    }

    String translateNote(Note note) {
        StringBuilder sb = new StringBuilder();
        if (note.getTempoChange() != null) {
            sb.append("[").append("Q:").append(translateTempo(note.getTempoChange())).append("]");
        }
        sb.append(translateAnnotation(note.getAnnotation()));
        NoteType noteType = note.getNoteType();
        if (noteType.isNote()) {
            if (noteType.isGraceNote()) {
                sb.append("{/");
            }
            sb.append(translateDecorations(note));
            sb.append(translateAccidental(note.getAccidental()));
            
            if (note.getNoteType() == NoteType.GRACESEMIQUAVER) {
                sb.append(translatePitch(((GraceSemiQuaver)note).getY0Pos()));
                sb.append(translateNoteLength(new Semiquaver().getDefaultDuration()));
            }
            
            sb.append(translatePitch(note.getYPos()));
            int duration;
            switch (noteType) {
                case GRACEQUAVER:
                    duration = new Quaver().getDefaultDuration();
                    break;
                case GRACESEMIQUAVER:
                    duration = new Semiquaver().getDefaultDuration();
                    break;
                default:
                    duration = note.getDefaultDurationWithDots();
            }
            sb.append(translateNoteLength(duration));            
            if (noteType.isGraceNote()) {
                sb.append("}");
            } 
        }
        if (noteType.isRest()) {
            sb.append("z").append(translateNoteLength(note.getDefaultDurationWithDots()));
        }
        if (noteType.isRepeat() || noteType.isBarLine()) {
            sb.append(translateRepeatAndBarLine(noteType));
        }
        if (noteType == NoteType.BREATHMARK) {
            sb.append("!breath!");
        }
        return sb.toString();
    }
    
    String translateLine(Line line) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < line.noteCount(); n++) {
            if (line.getBeamings().isStartOfAnyInterval(n)) {
                sb.append(" ");
            }                                                         
            if (line.getFsEndings().isStartOfAnyInterval(n)) {
                sb.append("[1 ");
            }  
            if (line.getTuplets().isStartOfAnyInterval(n)) {
                Interval tupletInterval = line.getTuplets().findInterval(n);
                int numberOfNotes = tupletInterval.getB() - tupletInterval.getA() + 1;
                sb.append("(").append(TupletIntervalData.getGrade(tupletInterval)).append("::").append(numberOfNotes);
            }
            if (isSlurOrGlissandoBegin(line, n)) {
                sb.append("(");
            }
            
            sb.append(translateNote(line.getNote(n)));

            if (line.getNote(n).getNoteType() == NoteType.REPEATRIGHT && line.getFsEndings().isInsideAnyInterval(n)) {
                sb.append("[2 ");
            }
            if (line.getBeamings().isEndOfAnyInterval(n)) {
                sb.append(" ");
            }
            if (line.getFsEndings().isEndOfAnyInterval(n)) {
                sb.append("|] ");
            }
            Interval tieInterval = line.getTies().findInterval(n);
            if (tieInterval != null && n < tieInterval.getB()) {
                sb.append("-");
            }
            if (isSlurOrGlissandoEnd(line, n)) {
                sb.append(") ");
            }
        }
        return sb.toString().replace("  ", " ");
    }

    boolean isSlurOrGlissandoBegin(Line line, int n) {
        // TODO Glissandos are handled as slurs, but this is not ideal as abc 2.1 doesn't allow glissandos
        return line.getSlurs().isStartOfAnyInterval(n) || 
                line.getNote(n).getGlissando() != Note.NOGLISSANDO;
    }

    boolean isSlurOrGlissandoEnd(Line line, int n) {
        return line.getSlurs().isEndOfAnyInterval(n) || 
                (n > 0 && line.getNote(n - 1).getGlissando() != Note.NOGLISSANDO);
    }
    
    String translateLyrics(Line line) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < line.noteCount(); n++) {
            Note note = line.getNote(n);
            // TODO syllable forcing under rests is not supported in abc
            
            if (note.getNoteType().isNote()) {  
                sb.append(translateSyllable(note.a.syllable));
                // TODO syllables under gracenotes are not supported in abc therefore me must put together with the next note
                if (note.getNoteType().isGraceNote()) { 
                    sb.append('\\');
                }
                switch (note.a.syllableRelation) {
                    case NO:
                        sb.append(' ');
                        break;
                    case ONEDASH: //TODO long dash is not supported in abc
                    case DASH:
                        sb.append('-');
                        break;
                    case EXTENDER:
                        sb.append('_');
                        break;
                }
            }
        }
        return sb.toString();
    }
    
    String translateSyllable(String syllable) {
        if (Constants.UNDERSCORE.equals(syllable) || Constants.HYPHEN.equals(syllable)) {
            return "";
        }
        return syllable.replace(Constants.NON_BREAKING_HYPHEN, "\\-");
    }

    void translateComposition(PrintWriter writer, Composition composition) {
        for (int l = 0; l < composition.lineCount(); l++) {
            Line line = composition.getLine(l);
            writer.println(translateLine(line));
            writer.println("w:" + translateLyrics(line));
        }
    }

}
