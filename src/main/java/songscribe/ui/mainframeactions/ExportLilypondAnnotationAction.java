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

import songscribe.data.Interval;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.data.TupletIntervalData;

import songscribe.music.Composition;
import songscribe.music.GraceSemiQuaver;
import songscribe.music.KeyType;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.music.Tempo;

import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import java.awt.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

public class ExportLilypondAnnotationAction extends AbstractAction
{

    // left http://www.lilypond.org/doc/v2.18/Documentation/learning-big-page#fundamental-concepts
    private static final String[] PITCH_TYPES = { "c", "des", "d", "ees", "e", "f", "ges", "g", "aes", "a", "bes", "b" };
    private Logger logger = Logger.getLogger(ExportLilypondAnnotationAction.class);
    private MainFrame mainFrame;
    private PlatformFileDialog pfd;

    public ExportLilypondAnnotationAction(MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as LilyPond Notation...");
        pfd = new PlatformFileDialog(mainFrame, "Export as LilyPond Notation", false, new MyAcceptFilter("LilyPond Files", "ly"));
    }

    public void actionPerformed(ActionEvent e)
    {
        pfd.setFile(Utilities.getSongTitleFileNameForFileChooser(mainFrame.getMusicSheet()));
        if (pfd.showDialog())
        {
            File saveFile = pfd.getFile();

            if (!saveFile.getName().toLowerCase().endsWith(".ly"))
            {
                saveFile = new File(saveFile.getAbsolutePath() + ".ly");
            }

            if (saveFile.exists())
            {
                int answ = JOptionPane.showConfirmDialog(mainFrame,
                        "The file " + saveFile.getName() + " already exists. Do you want to overwrite it?", mainFrame.PROGNAME,
                        JOptionPane.YES_NO_OPTION);

                if (answ == JOptionPane.NO_OPTION)
                {
                    return;
                }
            }

            PrintWriter writer = null;

            try
            {
                writer = new PrintWriter(saveFile);
                writeLilypond(writer);
            }
            catch (IOException e1)
            {
                mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                logger.error("Saving LilyPond", e1);
            }
            finally
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
        }
    }

    public void writeLilypond(PrintWriter writer)
    {
        Composition composition = mainFrame.getMusicSheet().getComposition();

        writer.println("\\relative c' {");
        writer.println("\\autoBeamOff");
        writer.println("\\clef \"treble\" ");
        writer.print(translateTempo(composition.getTempo()));
        writer.println();
        writer.println("}");
    }

    /**
     * @return 0 for B, 1 for C, 2 for D, ..., 6 for A
     */
    int getPitchType(int yPos)
    {
        return (yPos <= 0) ? (-yPos % 7) : ((7 - (yPos % 7)) % 7);
    }

    String translateArticulation(Note note)
    {
        if (note.getDurationArticulation() != null)
        {
            switch (note.getDurationArticulation())
            {

                case STACCATO:
                    return "-.";

                case TENUTO:
                    return "--";

                default:
                    throw new RuntimeException("New articulation introduced without implementing lilypond conversion");
            }
        }

        if (note.getForceArticulation() != null)
        {
            switch (note.getForceArticulation())
            {

                case ACCENT:
                    return "->";

                default:
                    throw new RuntimeException("New articulation introduced without implementing lilypond conversion");
            }
        }

        return "";
    }

    String translateBeams(Note note)
    {
        int noteIndex = note.getLine().getNoteIndex(note);

        if (note.getLine().getBeamings().isStartOfAnyInterval(noteIndex))
        {
            return "[";
        }

        if (note.getLine().getBeamings().isEndOfAnyInterval(noteIndex))
        {
            return "]";
        }

        return "";
    }

    String translateDotted(Note note)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < note.getDotted(); i++)
        {
            sb.append('.');
        }

        return sb.toString();
    }

    String translateDuration(Note note)
    {
        if (note.getNoteType() == NoteType.GRACEQUAVER)
        {
            return "8";
        }

        if (note.getNoteType() == NoteType.GRACESEMIQUAVER)
        {
            return "16";
        }

        if (note.getDefaultDuration() > 0)
        {
            return Integer.toString(Composition.PPQ * 4 / note.getDefaultDuration());
        }
        else
        {
            return "";
        }
    }

    String translateDynamics(Note note)
    {
        int noteIndex = note.getLine().getNoteIndex(note);

        if (note.getLine().getCrescendo().isEndOfAnyInterval(noteIndex) || note.getLine().getDiminuendo().isEndOfAnyInterval(noteIndex))
        {
            return "\\!";
        }

        if (note.getLine().getCrescendo().isStartOfAnyInterval(noteIndex))
        {
            return "\\<";
        }

        if (note.getLine().getDiminuendo().isStartOfAnyInterval(noteIndex))
        {
            return "\\>";
        }

        return "";
    }

    String translateImlicitRepeatLeftAtLineBeginning(Line line)
    {
        NoteType firstRepeatSign = null;

        for (int n = 0; (n < line.noteCount()) && (firstRepeatSign == null); n++)
        {
            if (line.getNote(n).getNoteType().isRepeat())
            {
                firstRepeatSign = line.getNote(n).getNoteType();
            }
        }

        if ((firstRepeatSign == NoteType.REPEATRIGHT) || (firstRepeatSign == NoteType.REPEATLEFTRIGHT))
        {
            return translateRepeatAndBarLine(NoteType.REPEATLEFT);
        }
        else
        {
            return "";
        }
    }

    String translateImlicitRepeatRightAtLineEnd(Line line)
    {
        NoteType lastRepeatSign = null;

        for (int n = line.noteCount() - 1; (n >= 0) && (lastRepeatSign == null); n--)
        {
            if (line.getNote(n).getNoteType().isRepeat())
            {
                lastRepeatSign = line.getNote(n).getNoteType();
            }
        }

        if ((lastRepeatSign == NoteType.REPEATLEFT) || (lastRepeatSign == NoteType.REPEATLEFTRIGHT))
        {
            return translateRepeatAndBarLine(NoteType.REPEATRIGHT);
        }
        else
        {
            return "";
        }
    }

    String translateKey(KeyType keyType, int number)
    {
        String[] sharpKeys = { "c", "g", "d", "a", "e", "b", "fis", "cis" };
        String[] flatKeys = { "c", "f", "bes", "ees", "aes", "des", "ges", "ces" };
        String key;

        if (keyType == KeyType.SHARPS)
        {
            key = sharpKeys[number];
        }
        else
        {
            key = flatKeys[number];
        }

        return "\\key" + key + " \\major";
    }

    String translateLine(Line line)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(translateKey(line.getKeyType(), line.getKeys()));
        sb.append(translateImlicitRepeatLeftAtLineBeginning(line));
        sb.append(translateImlicitRepeatRightAtLineEnd(line));

        return sb.toString();
    }

    String translateNote(Note note)
    {
        StringBuilder sb = new StringBuilder();
        NoteType noteType = note.getNoteType();

        if (noteType.isGraceNote())
        {
            sb.append("\\acciaccatura ");
        }

        if (noteType == NoteType.GRACESEMIQUAVER)
        {
            sb.append("{");
            int origY = note.getYPos();

            note.setYPos(((GraceSemiQuaver) note).getY0Pos());
            sb.append(translatePitch(note));
            note.setYPos(origY);
            sb.append(translateDuration(note));
            sb.append(' ');
        }

        if (noteType.isNote())
        {
            sb.append(translatePitch(note));
        }

        if (noteType.isRest())
        {
            sb.append("r");
        }

        sb.append(translateDuration(note));
        sb.append(translateDotted(note));
        sb.append(translateTie(note));
        sb.append(translateSlur(note));
        sb.append(translateArticulation(note));
        sb.append(translateDynamics(note));
        sb.append(translateBeams(note));
        sb.append(translateText(note));
        sb.append(translateTuplet(note));
        if (noteType == NoteType.GRACESEMIQUAVER)
        {
            sb.append("}");
        }

        return sb.toString();
    }

    String translatePitch(Note note)
    {
        int pitch = note.getPitch() - 60;
        StringBuilder sb = new StringBuilder(PITCH_TYPES[pitch % 12]);
        int type = pitch / 12;

        if (pitch < 0)
        {
            type--;
        }

        for (int i = 0; i < type; i++)
        {
            sb.insert(1, (pitch < 0) ? ',' : '\'');
        }

        return sb.toString();
    }

    String translateRepeatAndBarLine(NoteType noteType)
    {
        switch (noteType)
        {

            case REPEATLEFT:
                return "\\repeat volta 2 {";

            case REPEATRIGHT:
                return "}";

            case REPEATLEFTRIGHT:
                return "} \\repeat volta 2 {";

            case SINGLEBARLINE:
                return "\\bar \"|\"";

            case DOUBLEBARLINE:
                return "\\bar \"||\"";

            case FINALDOUBLEBARLINE:
                return "\\bar \"|.\"";

            default:
                return "";
        }
    }

    String translateSlur(Note note)
    {
        int noteIndex = note.getLine().getNoteIndex(note);

        if (note.getLine().getSlurs().isStartOfAnyInterval(noteIndex))
        {
            return "(";
        }

        if (note.getLine().getSlurs().isEndOfAnyInterval(noteIndex))
        {
            return ")";
        }

        return "";
    }

    String translateTempo(Tempo tempo)
    {
        return "\\tempo \"" + tempo.getTempoDescription() + "\" " + translateDuration(tempo.getTempoType().getNote()) + " = "
              + tempo.getVisibleTempo();
    }

    String translateText(Note note)
    {
        StringBuilder sb = new StringBuilder();

        if (note.getAnnotation() != null)
        {

            // TODO text alignments
            sb.append((note.getAnnotation().getyPos() < 0) ? "^" : "_").append("\\markup { ").append(note.getAnnotation().getAnnotation())
              .append("}");
        }

        return sb.toString();
    }

    String translateTie(Note note)
    {
        return note.getLine().getTies().isStartOfAnyInterval(note.getLine().getNoteIndex(note)) ? "~" : "";
    }

    String translateTuplet(Note note)
    {
        int noteIndex = note.getLine().getNoteIndex(note);

        if (note.getLine().getTuplets().isStartOfAnyInterval(noteIndex))
        {
            Interval interval = note.getLine().getTuplets().findInterval(noteIndex);
            int grade = TupletIntervalData.getGrade(interval);

            return "\\tuplet " + grade + "/" + (grade - 1) + " {";
        }

        if (note.getLine().getSlurs().isEndOfAnyInterval(noteIndex))
        {
            return "}";
        }

        return "";
    }
}
