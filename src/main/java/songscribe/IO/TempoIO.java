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

import songscribe.music.Tempo;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Csaba Kávai
 */
public class TempoIO {
    // version 1.1
    public static final String XML_TEMPO = "tempo";
    // version 1.0
    private static final String XML_TEMPO_CHANGE = "tempochange";
    private static final String XML_POS = "position";
    private static final String XML_VISIBLE_TEMPO = "visibletempo";
    private static final String XML_TEMPO_TYPE = "tempotype";
    private static final String XML_TEMPO_DESCRIPTION = "tempodescription";
    private static final String XML_DONT_SHOW_TEMPO = "dontshowtempo";

    public static void writeTempo(Tempo t, PrintWriter pw, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            pw.print(' ');
        }

        pw.println("<" + XML_TEMPO + ">");
        XML.indent = indent + 2;
        XML.writeValue(pw, XML_VISIBLE_TEMPO, Integer.toString(t.getVisibleTempo()));
        XML.writeValue(pw, XML_TEMPO_TYPE, t.getTempoType().name());
        XML.writeValue(pw, XML_TEMPO_DESCRIPTION, t.getTempoDescription());

        if (!t.isShowTempo()) {
            XML.writeEmptyTag(pw, XML_DONT_SHOW_TEMPO);
        }

        for (int i = 0; i < indent; i++) {
            pw.print(' ');
        }

        pw.println("</" + XML_TEMPO + ">");
    }

    public static class TempoReader {
        private Tempo tempo;
        private int pos10;
        private String lastTag;
        private StringBuilder value = new StringBuilder(20);

        public void startElement10(String qName) {
            if (qName.equals(XML_TEMPO_CHANGE)) {
                tempo = new Tempo();
                lastTag = null;
            }
            else {
                lastTag = qName;
            }

            value.delete(0, value.length());
        }

        public void startElement11(String qName) {
            if (qName.equals(XML_TEMPO)) {
                tempo = new Tempo();
                lastTag = null;
            }
            else {
                lastTag = qName;
            }

            value.delete(0, value.length());
        }

        public Tempo endElement10(String qName) {
            if (qName.equals(XML_TEMPO_CHANGE)) {
                return tempo;
            }
            else if (qName.equals(lastTag)) {
                String str = value.toString();

                if (lastTag.equals(XML_POS)) {
                    pos10 = Integer.valueOf(str);
                }
                else if (lastTag.equals(XML_VISIBLE_TEMPO)) {
                    tempo.setVisibleTempo(Integer.valueOf(str));
                }
                else if (lastTag.equals(XML_TEMPO_TYPE)) {
                    Tempo.Type type;

                    try {
                        type = Tempo.Type.valueOf(str.toUpperCase());
                    }
                    catch (IllegalArgumentException e) {
                        type = Tempo.Type.CROTCHET_DOTTED;
                    }

                    tempo.setTempoType(type);
                }
                else if (lastTag.equals(XML_TEMPO_DESCRIPTION)) {
                    tempo.setTempoDescription(str);
                }
                else if (lastTag.equals(XML_DONT_SHOW_TEMPO)) {
                    tempo.setShowTempo(false);
                }
            }

            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public Tempo endElement11(String qName) {
            if (qName.equals(XML_TEMPO)) {
                return tempo;
            }
            else if (qName.equals(lastTag)) {
                String str = value.toString();

                if (lastTag.equals(XML_VISIBLE_TEMPO)) {
                    tempo.setVisibleTempo(Integer.parseInt(str));
                }
                else if (lastTag.equals(XML_TEMPO_TYPE)) {
                    tempo.setTempoType(Tempo.Type.valueOf(str));
                }
                else if (lastTag.equals(XML_TEMPO_DESCRIPTION)) {
                    tempo.setTempoDescription(str);
                }
                else if (lastTag.equals(XML_DONT_SHOW_TEMPO)) {
                    tempo.setShowTempo(false);
                }
            }

            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public void characters(char[] ch, int start, int lenght) {
            if (lastTag != null) {
                value.append(ch, start, lenght);
            }
        }

        public int getPos10() {
            return pos10;
        }
    }
}
