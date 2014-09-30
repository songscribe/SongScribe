/*
    SongScribe song notation program
    Copyright (C) 2006 Csaba Kavai

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

    Created on Feb 19, 2006
*/
package songscribe.IO;

import org.xml.sax.Attributes;
import songscribe.data.Interval;
import songscribe.data.IntervalSet;
import songscribe.music.KeyType;
import songscribe.music.Line;
import songscribe.music.Note;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class LineIO {
    private static final String XML_LINE = "line";
    private static final String XML_KEYS = "keys";
    private static final String XML_KEYTYPE = "keytype";
    private static final String XML_NOTE_DIST_CHANGE = "notedistchange";
    private static final String XML_LYRICS_YPOS = "lyricsypos";
    private static final String XML_FSENDING_YPOS = "fsendingypos";
    private static final String XML_TEMPO_CHANGE_YPOS = "tempochangeypos";
    private static final String XML_BEAT_CHANGE_YPOS = "beatchangeypos";
    private static final String XML_TRILL_YPOS = "trillypos";
    private static final String XML_BEAMINGS = "beamings";
    private static final String XML_TIES = "ties";
    private static final String XML_SLURS = "slurs";
    private static final String XML_TRIPLETS = "triplets";// the old version of triplets
    private static final String XML_TUPLETS = "tuplets";
    private static final String XML_FSENDINGS = "fsendings";
    private static final String XML_NOTES = "notes";
    private static final String XML_CRESCENDO = "crescendo";
    private static final String XML_DIMINUENDO = "diminuendo";


    public static void writeLine(Line l, PrintWriter pw) throws IOException {
        pw.println("    <" + XML_LINE + ">");
        XML.indent = 6;

        if (l.getKeys() != l.getComposition().getDefaultKeys() ||
            l.getKeyType() != l.getComposition().getDefaultKeyType()) {
            XML.writeValue(pw, XML_KEYS, Integer.toString(l.getKeys()));
            XML.writeValue(pw, XML_KEYTYPE, l.getKeyType().name());
        }

        if (l.getNoteDistChangeRatio() != 1f) {
            XML.writeValue(pw, XML_NOTE_DIST_CHANGE, Float.toString(l.getNoteDistChangeRatio()));
        }

        if (l.getFirstTempoChange() > -1) {
            XML.writeValue(pw, XML_TEMPO_CHANGE_YPOS, Integer.toString(l.getTempoChangeYPos()));
        }

        if (l.getFirstBeatChange() > -1) {
            XML.writeValue(pw, XML_BEAT_CHANGE_YPOS, Integer.toString(l.getBeatChangeYPos()));
        }

        XML.writeValue(pw, XML_LYRICS_YPOS, Integer.toString(l.getLyricsYPos()));

        if (!l.getFsEndings().isEmpty()) {
            XML.writeValue(pw, XML_FSENDING_YPOS, Integer.toString(l.getFsEndingYPos()));
        }

        if (l.getFirstTrill() > -1) {
            XML.writeValue(pw, XML_TRILL_YPOS, Integer.toString(l.getTrillYPos()));
        }

        if (!l.getBeamings().isEmpty()) {
            XML.writeValue(pw, XML_BEAMINGS, intervalToString(l.getBeamings()));
        }

        if (!l.getTies().isEmpty()) {
            XML.writeValue(pw, XML_TIES, intervalToString(l.getTies()));
        }

        if (!l.getSlurs().isEmpty()) {
            XML.writeValue(pw, XML_SLURS, intervalToString(l.getSlurs()));
        }

        if (!l.getTuplets().isEmpty()) {
            XML.writeValue(pw, XML_TUPLETS, intervalToString(l.getTuplets()));
        }

        if (!l.getFsEndings().isEmpty()) {
            XML.writeValue(pw, XML_FSENDINGS, intervalToString(l.getFsEndings()));
        }

        if (!l.getCrescendo().isEmpty()) {
            XML.writeValue(pw, XML_CRESCENDO, intervalToString(l.getCrescendo()));
        }

        if (!l.getDiminuendo().isEmpty()) {
            XML.writeValue(pw, XML_DIMINUENDO, intervalToString(l.getDiminuendo()));
        }

        pw.println("      <" + XML_NOTES + ">");

        for (int i = 0; i < l.noteCount(); i++) {
            NoteIO.writeNote(l.getNote(i), pw);
        }

        pw.println("      </" + XML_NOTES + ">");
        pw.println("    </" + XML_LINE + ">");
    }

    private static String intervalToString(IntervalSet is) {
        StringBuilder sb = new StringBuilder();

        for (ListIterator<Interval> li = is.listIterator(); li.hasNext(); ) {
            Interval i = li.next();
            sb.append(i.getA());
            sb.append(',');
            sb.append(i.getB());

            if (i.getData() != null) {
                sb.append(',');
                sb.append(i.getData());
            }

            sb.append(';');
        }

        return sb.toString();
    }

    public static class LineReader {
        private Line line;
        private String lastTag;
        private NoteIO.NoteReader noteReader;
        private StringBuilder value = new StringBuilder(20);
        private Where where;

        private void stringToIntervalSet(IntervalSet is, String str) {
            int begin = 0;
            int end;

            while ((end = str.indexOf(';', begin)) != -1) {
                int firstComma = str.indexOf(',', begin);
                int secondComma = str.indexOf(',', firstComma + 1);

                if (secondComma > end) {
                    secondComma = -1;
                }

                int a = Integer.parseInt(str.substring(begin, firstComma));
                int b = Integer.parseInt(str.substring(firstComma + 1, secondComma == -1 ? end : secondComma));
                String data = secondComma == -1 ? null : str.substring(secondComma + 1, end);
                is.addInterval(a, b, data);
                begin = str.indexOf(';', begin) + 1;
            }
        }

        public void startElement11(String qName, Attributes attributes) {
            if (where == null) {
                if (qName.equals(XML_LINE)) {
                    where = Where.LINE;
                    line = new Line();
                    lastTag = null;
                    noteReader = new NoteIO.NoteReader();
                }
            }
            else if (where == Where.NOTES) {
                try {
                    noteReader.startElement11(qName, attributes);
                }
                catch (NewLineException e) {
                    // pass
                }
            }
            else {
                if (qName.equals(XML_NOTES)) {
                    where = Where.NOTES;
                }
                else {
                    lastTag = qName;
                }
            }

            value.delete(0, value.length());
        }

        public Line endElement11(String qName) {
            if (qName.equals(XML_NOTES)) {
                where = Where.LINE;
            }
            else if (where == Where.NOTES) {
                Note n = noteReader.endElement11(qName);

                if (n != null) {
                    line.addNote(n);
                }
            }
            else if (where == Where.LINE) {
                if (qName.equals(XML_LINE)) {
                    where = null;
                    return line;
                }
                else if (qName.equals(lastTag)) {
                    String str = value.toString();

                    if (lastTag.equals(XML_KEYS)) {
                        line.setKeys(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_KEYTYPE)) {
                        line.setKeyType(KeyType.valueOf(str));
                    }
                    else if (lastTag.equals(XML_NOTE_DIST_CHANGE)) {
                        line.mulNoteDistChange(Float.parseFloat(str));
                    }
                    else if (lastTag.equals(XML_TEMPO_CHANGE_YPOS)) {
                        line.setTempoChangeYPos(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_BEAT_CHANGE_YPOS)) {
                        line.setBeatChangeYPos(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_LYRICS_YPOS)) {
                        line.setLyricsYPos(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_FSENDING_YPOS)) {
                        line.setFsEndingYPos(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_TRILL_YPOS)) {
                        line.setTrillYPos(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_BEAMINGS)) {
                        stringToIntervalSet(line.getBeamings(), str);
                    }
                    else if (lastTag.equals(XML_TIES)) {
                        stringToIntervalSet(line.getTies(), str);
                    }
                    else if (lastTag.equals(XML_SLURS)) {
                        stringToIntervalSet(line.getSlurs(), str);
                    }
                    else if (lastTag.equals(XML_CRESCENDO)) {
                        stringToIntervalSet(line.getCrescendo(), str);
                    }
                    else if (lastTag.equals(XML_DIMINUENDO)) {
                        stringToIntervalSet(line.getDiminuendo(), str);
                    }
                    else if (lastTag.equals(XML_TUPLETS) || lastTag.equals(XML_TRIPLETS)) {
                        stringToIntervalSet(line.getTuplets(), str);

                        for (ListIterator<Interval> li = line.getTuplets().listIterator(); li.hasNext(); ) {
                            Interval interval = li.next();

                            if (interval.getData() == null) {
                                interval.setData("3");
                            }
                        }
                    }
                    else if (lastTag.equals(XML_FSENDINGS)) {
                        stringToIntervalSet(line.getFsEndings(), str);
                    }
                }
            }

            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public void characters(char[] ch, int start, int lenght) {
            if (where == Where.NOTES) {
                noteReader.characters(ch, start, lenght);
            }
            else if (lastTag != null) {
                value.append(ch, start, lenght);
            }
        }

        private enum Where { LINE, NOTES }
    }
}
