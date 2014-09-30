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

    Created on Feb 18, 2006
*/
package songscribe.IO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import songscribe.music.*;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Csaba Kávai
 */
public class CompositionIO {
    public static final int IO_MAJOR_VERSION = 1;
    public static final int IO_MINOR_VERSION = 2;

    // version 1.0
    private static final String XML_COMPOSITION = "composition";
    private static final String XML_VERSION = "version";
    private static final String XML_KEYS = "keys";
    private static final String XML_KEYTYPE = "keytype";
    private static final String XML_NUMBER = "number";
    private static final String XML_SONGTITLE = "songtitle";
    private static final String XML_LYRICS = "lyrics";
    private static final String XML_RIGHT_INFO = "rightinfo";
    private static final String XML_NOTES = "notes";
    private static final String XML_TEMPO_CHANGES = "tempochanges";

    // version 1.1
    private static final String XML_LINES = "lines";
    private static final String XML_VIEW = "view";
    private static final String XML_UNDERLYRICS = "underlyrics";
    private static final String XML_TRANSLATED_LYRICS = "translatedlyrics";
    private static final String XML_TOP_SPACE = "topspace";
    private static final String XML_LINE_WIDTH = "linewidth";
    private static final String XML_ROW_HEIGHT = "rowheight";
    private static final String XML_PLACE = "place";
    private static final String XML_YEAR = "year";
    private static final String XML_MONTH = "month";
    private static final String XML_DAY = "day";
    private static final String XML_RIGHT_INFO_STARTY = "rightinfostarty";
    private static final String XML_PARSONS_SIMPLE = "parsonssimple";
    private static final String XML_PARSONS_IMPROVED = "parsonsimproved";


    public static void writeComposition(Composition c, PrintWriter pw) throws IOException {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<" + XML_COMPOSITION + " " + XML_VERSION + "=\"" + IO_MAJOR_VERSION + "." + IO_MINOR_VERSION + "\">");
        XML.indent = 2;
        XML.writeValue(pw, XML_KEYS, Integer.toString(c.getDefaultKeys()));
        XML.writeValue(pw, XML_KEYTYPE, c.getDefaultKeyType().name());
        TempoIO.writeTempo(c.getTempo(), pw, 2);
        XML.indent = 2;

        if (c.getNumber().length() > 0) {
            XML.writeValue(pw, XML_NUMBER, c.getNumber());
        }

        if (c.getSongTitle().length() > 0) {
            XML.writeValue(pw, XML_SONGTITLE, c.getSongTitle());
        }

        if (c.getPlace().length() > 0) {
            XML.writeValue(pw, XML_PLACE, c.getPlace());
        }

        if (c.getYear().length() > 0) {
            XML.writeValue(pw, XML_YEAR, c.getYear());
        }

        if (c.getMonth() > 0) {
            XML.writeValue(pw, XML_MONTH, Integer.toString(c.getMonth()));
        }

        if (c.getDay() > 0) {
            XML.writeValue(pw, XML_DAY, Integer.toString(c.getDay()));
        }

        if (c.getLyrics().length() > 0) {
            XML.writeValue(pw, XML_LYRICS, c.getLyrics());
        }

        if (c.getUnderLyrics().length() > 0) {
            XML.writeValue(pw, XML_UNDERLYRICS, c.getUnderLyrics());
        }

        if (c.getTranslatedLyrics().length() > 0) {
            XML.writeValue(pw, XML_TRANSLATED_LYRICS, c.getTranslatedLyrics());
        }

        if (c.getRightInfo().length() > 0) {
            XML.writeValue(pw, XML_RIGHT_INFO, c.getRightInfo());
        }

        if (c.isUserSetTopSpace()) {
            XML.writeValue(pw, XML_TOP_SPACE, Integer.toString(c.getTopSpace()));
        }

        XML.writeValue(pw, XML_RIGHT_INFO_STARTY, Integer.toString(c.getRightInfoStartY()));

        if (c.getRowHeight() != 0) {
            XML.writeValue(pw, XML_ROW_HEIGHT, Integer.toString(c.getRowHeight()));
        }

        XML.writeValue(pw, XML_LINE_WIDTH, Integer.toString(c.getLineWidth()));
        XML.writeValue(pw, XML_PARSONS_SIMPLE, ParsonsCodeGenerator.getParsonsCode(c, false));
        XML.writeValue(pw, XML_PARSONS_IMPROVED, ParsonsCodeGenerator.getParsonsCode(c, true));
        pw.println("  <" + XML_LINES + ">");

        for (int l = 0; l < c.lineCount(); l++) {
            LineIO.writeLine(c.getLine(l), pw);
        }

        pw.println("  </" + XML_LINES + ">");
        pw.println("  <" + XML_VIEW + ">");
        ViewIO.writeView(c, pw);
        pw.println("  </" + XML_VIEW + ">");
        pw.println("</" + XML_COMPOSITION + ">");
    }

    public static class DocumentReader extends DefaultHandler {
        private Where where;
        private String lastTag;
        private StringBuilder value = new StringBuilder(200);
        private NoteIO.NoteReader noteReader;
        private TempoIO.TempoReader tempoReader;
        private LineIO.LineReader lineReader;
        private ViewIO.ViewReader viewReader;
        private Composition composition;
        private int majorVersion, minorVersion;
        private MainFrame mainFrame;

        public DocumentReader(MainFrame mainFrame) {
            this.mainFrame = mainFrame;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (where == null) {
                if (qName.equals(XML_COMPOSITION)) {
                    try {
                        String version = attributes.getValue(XML_VERSION);
                        int dotIndex = version.indexOf('.');
                        majorVersion = Integer.parseInt(version.substring(0, dotIndex));
                        minorVersion = Integer.parseInt(version.substring(dotIndex + 1));
                        composition = new Composition(mainFrame);
                        composition.setTopSpace(0, false);
                        composition.removeLine(0);
                        where = Where.COMPOSITION;

                        if (majorVersion == 1 && minorVersion == 0) {
                            noteReader = new NoteIO.NoteReader();
                            tempoReader = new TempoIO.TempoReader();
                        }
                        else if (majorVersion == 1 && minorVersion == 1) {
                            lineReader = new LineIO.LineReader();
                            viewReader = new ViewIO.ViewReader(mainFrame.getProfileManager());
                        }
                        else if (majorVersion == 1 && minorVersion == 2) {
                            lineReader = new LineIO.LineReader();
                            viewReader = new ViewIO.ViewReader(mainFrame.getProfileManager());
                        }
                        else {
                            throw new SAXException("Unsupported version number.");
                        }
                    }
                    catch (NumberFormatException e) {
                        throw new SAXException("SongScribe version is not a number.", e);
                    }
                }
            }
            else {
                if (majorVersion == 1 && minorVersion == 0) {
                    startElement10(uri, localName, qName, attributes);
                }
                else if (majorVersion == 1 && minorVersion == 1) {
                    startElement11(uri, localName, qName, attributes);
                }
                else if (majorVersion == 1 && minorVersion == 2) {
                    startElement12(uri, localName, qName, attributes);
                }
            }

            value.delete(0, value.length());
        }

        public void startElement10(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (where == Where.NOTES) {
                try {
                    noteReader.startElement10(qName, attributes);
                }
                catch (NewLineException e) {
                    mainFrame.getMusicSheet().drawWidthIfWiderLine(composition.getLine(composition.lineCount() - 1), true);
                    composition.addLine(new Line());
                }
            }
            else if (where == Where.TEMPO_CHANGE) {
                tempoReader.startElement10(qName);
            }
            else if (where == Where.COMPOSITION) {
                if (qName.equals(XML_NOTES)) {
                    where = Where.NOTES;
                }
                else if (qName.equals(XML_TEMPO_CHANGES)) {
                    where = Where.TEMPO_CHANGE;
                }
                else {
                    lastTag = qName;
                }
            }
        }

        public void startElement11(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (where == Where.LINES) {
                lineReader.startElement11(qName, attributes);
            }
            else if (where == Where.VIEW) {
                viewReader.startElement11(qName, attributes);
            }
            else if (where == Where.TEMPO) {
                tempoReader.startElement11(qName);
            }
            else if (where == Where.COMPOSITION) {
                if (qName.equals(XML_LINES)) {
                    where = Where.LINES;
                }
                else if (qName.equals(XML_VIEW)) {
                    where = Where.VIEW;
                }
                else if (qName.equals(TempoIO.XML_TEMPO)) {
                    where = Where.TEMPO;
                    tempoReader = new TempoIO.TempoReader();
                    tempoReader.startElement11(qName);
                }
                else {
                    lastTag = qName;
                }
            }
        }

        public void startElement12(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // no changes
            startElement11(uri, localName, qName, attributes);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (majorVersion == 1 && minorVersion == 0) {
                endElement10(qName);
            }
            else if (majorVersion == 1 && minorVersion == 1) {
                endElement11(qName);
            }
            else if (majorVersion == 1 && minorVersion == 2) {
                endElement12(qName);
            }
        }

        public void endElement10(String qName) {
            if (qName.equals(XML_NOTES)) {
                where = Where.COMPOSITION;
            }
            else if (qName.equals(XML_TEMPO_CHANGES)) {
                where = Where.COMPOSITION;
            }
            else if (where == Where.NOTES) {
                Note note = noteReader.endElement10(qName);

                if (note != null) {
                    if (composition.lineCount() == 0) {
                        composition.addLine(new Line());
                    }

                    Line line = composition.getLine(composition.lineCount() - 1);
                    note.setXPos(MusicSheet.calculateLastNoteXPos(line, note));
                    note.setUpper(MusicSheet.defaultUpperNote(note));
                    line.addNote(note);
                }
            }
            else if (where == Where.TEMPO_CHANGE) {
                Tempo tc = tempoReader.endElement10(qName);

                if (tc != null) {
                    if (tempoReader.getPos10() == 0) {
                        composition.setTempo(tc);
                    }
                    else {
                        int firstNoteInLine = 0;

                        for (int l = 0; l < composition.lineCount(); l++) {
                            Line line = composition.getLine(l);

                            if (tempoReader.getPos10() < firstNoteInLine + line.noteCount()) {
                                line.getNote(tempoReader.getPos10() - firstNoteInLine).setTempoChange(tc);
                                break;
                            }
                            else {
                                firstNoteInLine += line.noteCount() + 1;
                            }
                        }
                    }
                }
            }
            else if (where == Where.COMPOSITION) {
                if (qName.equals(XML_COMPOSITION)) {
                    composition.modifiedComposition();
                }
                else if (qName.equals(lastTag)) {
                    String str = value.toString();

                    if (lastTag.equals(XML_KEYS)) {
                        composition.setDefaultKeys(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_KEYTYPE)) {
                        composition.setDefaultKeyType(KeyType.valueOf(str));
                    }
                    else if (lastTag.equals(XML_NUMBER)) {
                        composition.setNumber(str);
                    }
                    else if (lastTag.equals(XML_SONGTITLE)) {
                        composition.setSongTitle(str);
                    }
                    else if (lastTag.equals(XML_LYRICS)) {
                        composition.setLyrics(str);
                    }
                    else if (lastTag.equals(XML_RIGHT_INFO)) {
                        composition.setRightInfo(str);
                    }
                }
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        public void endElement11(String qName) {
            // no change except end the end of the line reading we set the quaver notes to upper position
            endElement12(qName);

            if (where == Where.LINES && composition.lineCount() > 0) {
                Line lastLine = composition.getLine(composition.lineCount() - 1);

                for (int i = 0; i < lastLine.noteCount(); i++) {
                    if (lastLine.getNote(i).getNoteType().isGraceNote()) {
                        lastLine.getNote(i).setUpper(true);
                    }
                }
            }
        }

        public void endElement12(String qName) {
            if (qName.equals(XML_LINES)) {
                where = Where.COMPOSITION;
            }
            else if (qName.equals(XML_VIEW)) {
                viewReader.setAttributes(composition);
                where = Where.COMPOSITION;
            }
            else if (where == Where.LINES) {
                Line l = lineReader.endElement11(qName);

                if (l != null) {
                    composition.addLine(l);
                }
            }
            else if (where == Where.TEMPO) {
                Tempo t = tempoReader.endElement11(qName);

                if (t != null) {
                    composition.setTempo(t);
                    where = Where.COMPOSITION;
                }
            }
            else if (where == Where.COMPOSITION) {
                if (qName.equals(lastTag)) {
                    String str = value.toString();

                    if (lastTag.equals(XML_KEYS)) {
                        composition.setDefaultKeys(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_KEYTYPE)) {
                        composition.setDefaultKeyType(KeyType.valueOf(str));
                    }
                    else if (lastTag.equals(XML_NUMBER)) {
                        composition.setNumber(str);
                    }
                    else if (lastTag.equals(XML_SONGTITLE)) {
                        composition.setSongTitle(str);
                    }
                    else if (lastTag.equals(XML_PLACE)) {
                        composition.setPlace(str);
                    }
                    else if (lastTag.equals(XML_YEAR)) {
                        composition.setYear(str);
                    }
                    else if (lastTag.equals(XML_MONTH)) {
                        composition.setMonth(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_DAY)) {
                        composition.setDay(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_LYRICS)) {
                        composition.setLyrics(str);
                    }
                    else if (lastTag.equals(XML_UNDERLYRICS)) {
                        composition.setUnderLyrics(str);
                    }
                    else if (lastTag.equals(XML_TRANSLATED_LYRICS)) {
                        composition.setTranslatedLyrics(str);
                    }
                    else if (lastTag.equals(XML_RIGHT_INFO)) {
                        composition.setRightInfo(str);
                    }
                    else if (lastTag.equals(XML_TOP_SPACE)) {
                        composition.setTopSpace(Integer.parseInt(str), false);
                    }
                    else if (lastTag.equals(XML_RIGHT_INFO_STARTY)) {
                        composition.setRightInfoStartY(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_ROW_HEIGHT)) {
                        composition.setRowHeight(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_LINE_WIDTH)) {
                        composition.setLineWidth(Integer.parseInt(str));
                    }
                }
            }
            else if (where == Where.VIEW) {
                viewReader.endElement11(qName);
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if (where == Where.LINES) {
                lineReader.characters(ch, start, length);
            }
            else if (where == Where.VIEW) {
                viewReader.characters(ch, start, length);
            }
            else if (where == Where.NOTES) {
                noteReader.characters(ch, start, length);
            }
            else if (where == Where.TEMPO_CHANGE) {
                tempoReader.characters(ch, start, length);
            }
            else if (where == Where.TEMPO) {
                tempoReader.characters(ch, start, length);
            }
            else if (where == Where.COMPOSITION && lastTag != null) {
                value.append(ch, start, length);
            }
        }

        public Composition getComposition() {
            if (composition.getTopSpace() == 0) {
                composition.recalcTopSpace();
            }

            return composition;
        }

        private enum Where { COMPOSITION, LINES, VIEW, NOTES, TEMPO, TEMPO_CHANGE }
    }
}
