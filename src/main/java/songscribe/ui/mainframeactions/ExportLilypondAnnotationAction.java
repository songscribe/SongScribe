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

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.log4j.Logger;
import songscribe.data.Interval;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.data.TupletIntervalData;
import songscribe.music.Composition;
import songscribe.music.Crotchet;
import songscribe.music.GraceSemiQuaver;
import songscribe.music.KeyType;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.music.Tempo;
import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ExportLilypondAnnotationAction extends AbstractAction
{

    // left http://www.lilypond.org/doc/v2.18/Documentation/learning-big-page#fundamental-concepts
    private static final String[] PITCH_TYPES = { "c", "des", "d", "es", "e", "f", "ges", "g", "aes", "a", "bes", "b" };
    private Logger logger = Logger.getLogger(ExportLilypondAnnotationAction.class);
    private MainFrame mainFrame;
    private PlatformFileDialog pfd;
    private Configuration cfg = new Configuration();

    public ExportLilypondAnnotationAction(MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as LilyPond Notation...");
        pfd = new PlatformFileDialog(mainFrame, "Export as LilyPond Notation", false, new MyAcceptFilter("LilyPond Files", "ly"));
        try {
            cfg.setDirectoryForTemplateLoading(new File("conf/lilypond-templates"));
        } catch (IOException e) {
            logger.error("Template directory does not exists", e);  
        }
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
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
            catch (TemplateException e1)
            {
                mainFrame.showErrorMessage("Wrong tempate: " + e1.getMessage());
                logger.error("Template exception", e1);
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

    public void writeLilypond(PrintWriter writer) throws IOException, TemplateException {
        
        Composition composition = mainFrame.getMusicSheet().getComposition();
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("rightinfo", "\"" + composition.getRightInfo().replace("\n", "\" \"")+ "\"");
        root.put("title", "\"" + composition.getSongTitle() + "\"");
        root.put("tempo", translateTempo(composition.getTempo()));
        root.put("key", translateKey(composition.getLine(0).getKeyType(), composition.getLine(0).getKeys()));
        root.put("score", translateScore(composition));
        root.put("lyrics", "");

        cfg.getTemplate("main.ftl").process(root, writer);        
    }

    String translateScore(Composition composition) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < composition.lineCount();i++){
            Line line = composition.getLine(i);
            if (i > 0) {
                Line previousLine = composition.getLine(i-1);
                if (previousLine.getKeys() != line.getKeys() || previousLine.getKeyType() != line.getKeyType()) {
                    sb.append(translateKey(line.getKeyType(), line.getKeys())).append(' ');
                }
            }
            sb.append(translateLine(line));
        }
        return sb.toString();
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
        String[] flatKeys = { "c", "f", "bes", "es", "aes", "des", "ges", "ces" };
        String key;

        if (keyType == KeyType.SHARPS)
        {
            key = sharpKeys[number];
        }
        else
        {
            key = flatKeys[number];
        }

        return "\\key " + key + " \\major";
    }

    String translateLine(Line line)
    {
        StringBuilder sb = new StringBuilder();        
        sb.append(translateImlicitRepeatLeftAtLineBeginning(line)).append(' ');
        for (int n = 0; n < line.noteCount(); n++) {
            sb.append(translateNote(line.getNote(n))).append(' ');
        }
        sb.append(translateImlicitRepeatRightAtLineEnd(line));
        sb.append(" \\bar \"\" \\break").append("\n");

        return sb.toString();
    }

    String translateNote(Note note)
    {
        StringBuilder sb = new StringBuilder();
        NoteType noteType = note.getNoteType();
        if (note.getTempoChange() != null) {
            sb.append(translateTempo(note.getTempoChange())).append(' ');
        }
        if (noteType.isGraceNote())
        {
            sb.append("\\acciaccatura ");
        }

        if (noteType == NoteType.GRACESEMIQUAVER)
        {
            sb.append("{");
            int origY = note.getYPos();

            note.setYPos(((GraceSemiQuaver) note).getY0Pos());
            sb.append(translatePitch(note.getPitch()));
            note.setYPos(origY);
            sb.append(translateDuration(note));
            sb.append(' ');
        }

        if (noteType.isNote())
        {
            sb.append(translatePitch(note.getPitch()));
        }

        if (noteType.isRest())
        {
            sb.append("r");
        }
        
        if (noteType.isRepeat() || noteType.isBarLine()) {
            sb.append(translateRepeatAndBarLine(noteType));
        }
        
        if (noteType == NoteType.BREATHMARK) {
            sb.append("\\breathe");
        }
        

        sb.append(translateDuration(note));
        sb.append(translateDotted(note));
        sb.append(translateTie(note));
        sb.append(translateSlur(note));
        sb.append(translateArticulation(note));
        sb.append(translateDynamics(note));
        sb.append(translateFermata(note));
        sb.append(translateTrill(note));
        sb.append(translateGlissando(note));
        sb.append(translateBeams(note));
        sb.append(translateText(note));
        sb.append(translateTuplet(note));
        
        if (noteType == NoteType.GRACESEMIQUAVER)
        {
            sb.append("}");
        }

        return sb.toString();
    }

    private String translateGlissando(Note note) {
        if (note.getGlissando() == Note.NOGLISSANDO) {
            return "";
        } else {
            Line line = note.getLine();
            int noteIndex = line.getNoteIndex(note);
            if (noteIndex < line.noteCount() - 1 && line.getNote(noteIndex + 1).getYPos() == note.getGlissando().pitch) {
                return "\\glissando";
            } else {
                Note hiddenNote = new Crotchet();
                hiddenNote.setYPos(note.getGlissando().pitch);
                hiddenNote.setLine(line);
                line.getNotes().add(noteIndex + 1, hiddenNote);
                String pitch = translatePitch(hiddenNote.getPitch());
                line.getNotes().remove(noteIndex + 1);
                return "\\glissando \\hideNotes " + pitch + "4 \\unHideNotes";
            }
        }
    }
    
    private String translateTrill(Note note) {
        if (!note.isTrill()) {
            return "";
        } else {
            Line line = note.getLine();
            int noteIndex = line.getNoteIndex(note);
            boolean firstTrill = noteIndex > 0 && !line.getNote(noteIndex - 1).isTrill();
            boolean lastTrill = noteIndex < line.noteCount() - 1 && !line.getNote(noteIndex + 1).isTrill();
            if (firstTrill && lastTrill) {
                return "\\trill";
            } else if (firstTrill) {
                return "\\startTrillSpan";
            } else if (lastTrill) {
                return "\\stopTrillSpan";
            } else {
                return "";
            }
        }
    }

    private String translateFermata(Note note) {
        return note.isFermata() ? "\\fermata" : "";
    }

    String translatePitch(int notePitch)
    {
        int pitch = notePitch - 60;
        int pitchTypeIndex = pitch % 12 < 0 ? 12 + pitch % 12 : pitch % 12;
        StringBuilder sb = new StringBuilder(PITCH_TYPES[pitchTypeIndex]);
        int type = pitch / 12 + 1;

        if (pitch < 0)
        {
            type--;
        }

        for (int i = 0; i < type; i++)
        {
            sb.append((pitch < 0) ? ',' : '\'');
        }

        return sb.toString();
    }

    String translateRepeatAndBarLine(NoteType noteType)
    {
        String repeatVisibility = "\\once \\revert Staff.BarLine #'break-visibility ";
        switch (noteType)
        {

            case REPEATLEFT:
                return repeatVisibility + "\\repeat volta 2 {";

            case REPEATRIGHT:
                return "}";

            case REPEATLEFTRIGHT:
                return "} " + repeatVisibility + "\\repeat volta 2 {";

            case SINGLEBARLINE:
                return repeatVisibility + "\\bar \"|\"";

            case DOUBLEBARLINE:
                return repeatVisibility + "\\bar \"||\"";

            case FINALDOUBLEBARLINE:
                return repeatVisibility + "\\bar \"|.\"";

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
        // \tempo \markup \medium { \note #"4." #1 = 144 Moderate-Fast}
        StringBuilder sb = new StringBuilder("\\tempo \\markup \\medium { ");
        if (tempo.isShowTempo()) {
            sb.append("\\note #\"");
            Note note = tempo.getTempoType().getNote();
            sb.append(translateDuration(note));
            for (int i = 0; i < note.getDotted(); i++) {
                sb.append('.');
            }
            sb.append("\" #1 = ").append(tempo.getVisibleTempo()).append(' ');
        }
        sb.append(tempo.getTempoDescription()).append(" }");
        return sb.toString();
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
