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
import songscribe.music.*;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Csaba Kávai
 */
public class NoteIO {
    // version 1.0
    private static final String XML_NOTE = "note";
    private static final String XML_TYPE = "type";
    private static final String XML_YPOS = "ypos";
    private static final String XML_DOTTED = "dotted";
    private static final String XML_PREFIX = "prefix";
    private static final String XML_VOLUME = "volume";
    private static final String XML_GLISSANDO = "glissando";

    // version 1.1
    private static final String XML_XPOS = "xpos";
    private static final String XML_PREFIX_IN_PARENTHESIS = "prefixinparenthesis";
    private static final String XML_UPPER = "upper";
    private static final String XML_FORCE_ARTICULATION = "forcearticulation";
    private static final String XML_DURATION_ARTICULATION = "durationarticulation";
    private static final String XML_SYLLABLE_MOVEMENT = "syllablemovement";
    private static final String XML_SYLLABLE_RELATION_MOVEMENT = "syllablerelationmovement";
    private static final String XML_TRILL = "trill";
    private static final String XML_FERMATA = "fermata";
    private static final String XML_FORCE_SYLLABLE = "forcesyllable";
    private static final String XML_BEAT_CHANGE = "beatchange";
    private static final String XML_GLISSANDO_X1_TRANSLATE = "glissandox1translate";
    private static final String XML_GLISSANDO_X2_TRANSLATE = "glissandox2translate";
    private static final String XML_GRACE_SEMIQUAVER_Y0_POS = "y0pos";
    private static final String XML_GRACE_SEMIQUAVER_X2_DIFFPOS = "x2diffpos";
    private static final String XML_INVERT_FRACTION_BEAM_ORIENTATION = "invertfractionbeamorientation";

    public static void writeNote(Note n, PrintWriter pw) throws IOException {
        pw.println("          <" + XML_NOTE + " " + XML_TYPE + "=\"" + n.getNoteType().name() + "\">");
        XML.indent = 12;
        XML.writeValue(pw, XML_XPOS, Integer.toString(n.getXPos()));
        XML.writeValue(pw, XML_YPOS, Integer.toString(n.getYPos()));

        if (n.getDotted() != 0) {
            XML.writeValue(pw, XML_DOTTED, Integer.toString(n.getDotted()));
        }

        if (n.getAccidental() != Note.Accidental.NONE) {
            XML.writeValue(pw, XML_PREFIX, n.getAccidental().name());
        }

        if (n.isAccidentalInParenthesis()) {
            XML.writeEmptyTag(pw, XML_PREFIX_IN_PARENTHESIS);
        }

        if (n.getForceArticulation() != null) {
            XML.writeValue(pw, XML_FORCE_ARTICULATION, n.getForceArticulation().name());
        }

        if (n.getDurationArticulation() != null) {
            XML.writeValue(pw, XML_DURATION_ARTICULATION, n.getDurationArticulation().name());
        }

        if (n.getGlissando() != Note.NO_GLISSANDO) {
            XML.writeValue(pw, XML_GLISSANDO, Integer.toString(n.getGlissando().pitch));

            if (n.getGlissando().x1Translate != 0) {
                XML.writeValue(pw, XML_GLISSANDO_X1_TRANSLATE, Integer.toString(n.getGlissando().x1Translate));
            }

            if (n.getGlissando().x2Translate != 0) {
                XML.writeValue(pw, XML_GLISSANDO_X2_TRANSLATE, Integer.toString(n.getGlissando().x2Translate));
            }
        }

        if (n.isUpper()) {
            XML.writeEmptyTag(pw, XML_UPPER);
        }

        if (n.getSyllableMovement() != 0) {
            XML.writeValue(pw, XML_SYLLABLE_MOVEMENT, Integer.toString(n.getSyllableMovement()));
        }

        if (n.getSyllableRelationMovement() != 0) {
            XML.writeValue(pw, XML_SYLLABLE_RELATION_MOVEMENT, Integer.toString(n.getSyllableRelationMovement()));
        }

        if (n.getTempoChange() != null) {
            TempoIO.writeTempo(n.getTempoChange(), pw, 12);
        }

        if (n.getAnnotation() != null) {
            AnnotationIO.writeAnnotation(n.getAnnotation(), pw, 12);
        }

        if (n.isTrill()) {
            XML.writeEmptyTag(pw, XML_TRILL);
        }

        if (n.isFermata()) {
            XML.writeEmptyTag(pw, XML_FERMATA);
        }

        if (n.isForceSyllable()) {
            XML.writeEmptyTag(pw, XML_FORCE_SYLLABLE);
        }

        if (n.isInvertFractionBeamOrientation()) {
            XML.writeEmptyTag(pw, XML_INVERT_FRACTION_BEAM_ORIENTATION);
        }

        if (n.getBeatChange() != null) {
            XML.writeValue(pw, XML_BEAT_CHANGE, n.getBeatChange().name());
        }

        if (n.getNoteType() == NoteType.GRACE_SEMIQUAVER) {
            XML.writeValue(pw, XML_GRACE_SEMIQUAVER_Y0_POS, Integer.toString(((GraceSemiQuaver) n).getY0Pos()));
            XML.writeValue(pw, XML_GRACE_SEMIQUAVER_X2_DIFFPOS, Integer.toString(((GraceSemiQuaver) n).getX2DiffPos()));
        }

        pw.println("          </" + XML_NOTE + ">");
    }

    public static class NoteReader {
        private Note note;
        private String lastTag;
        private TempoIO.TempoReader tempoReader;
        private AnnotationIO.AnnotationReader annotationReader;
        private StringBuilder value = new StringBuilder(20);
        private Where where;

        public void startElement10(String qName, Attributes attributes) throws NewLineException {
            if (qName.equals(XML_NOTE)) {
                lastTag = null;
                where = Where.NOTE;
                String type = attributes.getValue(XML_TYPE);

                if (type.equals("NEWLINE")) {
                    where = null;
                    throw new NewLineException();
                }
                else if (type.equals("LINE")) {
                    type = NoteType.SINGLE_BARLINE.name();
                }

                note = NoteType.valueOf(type).newInstance();
            }
            else {
                lastTag = qName;
            }

            value.delete(0, value.length());
        }

        public void startElement11(String qName, Attributes attributes) throws NewLineException {
            if (where == Where.TEMPO_CHANGE) {
                tempoReader.startElement11(qName);
            }
            else if (where == Where.ANNOTATION) {
                annotationReader.startElement11(qName);
            }
            else if (qName.equals(XML_NOTE)) {
                lastTag = null;
                where = Where.NOTE;

                if (attributes.getValue(XML_TYPE).equals("VERTICALLINE")) {
                    note = NoteType.SINGLE_BARLINE.newInstance();
                }
                else {
                    note = NoteType.valueOf(attributes.getValue(XML_TYPE)).newInstance();
                }
            }
            else if (qName.equals(TempoIO.XML_TEMPO)) {
                where = Where.TEMPO_CHANGE;
                tempoReader = new TempoIO.TempoReader();
                tempoReader.startElement11(qName);
            }
            else if (qName.equals(AnnotationIO.XML_ANNOTATION)) {
                where = Where.ANNOTATION;
                annotationReader = new AnnotationIO.AnnotationReader();
                annotationReader.startElement11(qName);
            }
            else if (where == Where.NOTE) {
                lastTag = qName;
            }

            value.delete(0, value.length());
        }

        public Note endElement10(String qName) {
            return endElement11(qName);
        }

        public Note endElement11(String qName) {
            if (where == Where.TEMPO_CHANGE) {
                Tempo t = tempoReader.endElement11(qName);

                if (t != null) {
                    note.setTempoChange(t);
                    where = Where.NOTE;
                }
            }
            else if (where == Where.ANNOTATION) {
                Annotation a = annotationReader.endElement11(qName);

                if (a != null) {
                    note.setAnnotation(a);
                    where = Where.NOTE;
                }
            }
            else if (where == Where.NOTE) {
                if (qName.equals(XML_NOTE)) {
                    return note;
                }
                else if (qName.equals(lastTag)) {
                    String str = value.toString();

                    if (lastTag.equals(XML_XPOS)) {
                        note.setXPos(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_YPOS)) {
                        note.setYPos(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_DOTTED)) {
                        note.setDotted(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_PREFIX)) {
                        note.setAccidental(Note.Accidental.valueOf(str));
                    }
                    else if (lastTag.equals(XML_PREFIX_IN_PARENTHESIS)) {
                        note.setAccidentalInParenthesis(true);
                    }
                    else if (lastTag.equals(XML_VOLUME) && str.equals("LOUDER")) {//old
                        note.setForceArticulation(ForceArticulation.ACCENT);
                    }
                    else if (lastTag.equals(XML_FORCE_ARTICULATION)) {
                        note.setForceArticulation(ForceArticulation.valueOf(str));
                    }
                    else if (lastTag.equals(XML_DURATION_ARTICULATION)) {
                        note.setDurationArticulation(DurationArticulation.valueOf(str));
                    }
                    else if (lastTag.equals(XML_GLISSANDO)) {
                        note.setGlissando(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_GLISSANDO_X1_TRANSLATE)) {
                        note.getGlissando().x1Translate = Integer.valueOf(str);
                    }
                    else if (lastTag.equals(XML_GLISSANDO_X2_TRANSLATE)) {
                        note.getGlissando().x2Translate = Integer.valueOf(str);
                    }
                    else if (lastTag.equals(XML_UPPER)) {
                        note.setUpper(true);
                    }
                    else if (lastTag.equals(XML_SYLLABLE_MOVEMENT)) {
                        note.setSyllableMovement(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_SYLLABLE_RELATION_MOVEMENT)) {
                        note.setSyllableRelationMovement(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_TRILL)) {
                        note.setTrill(true);
                    }
                    else if (lastTag.equals(XML_FERMATA)) {
                        note.setFermata(true);
                    }
                    else if (lastTag.equals(XML_FORCE_SYLLABLE)) {
                        note.setForceSyllable(true);
                    }
                    else if (lastTag.equals(XML_INVERT_FRACTION_BEAM_ORIENTATION)) {
                        note.setInvertFractionBeamOrientation(true);
                    }
                    else if (lastTag.equals(XML_BEAT_CHANGE)) {
                        note.setBeatChange(BeatChange.valueOf(str));
                    }
                    else if (lastTag.equals(XML_GRACE_SEMIQUAVER_Y0_POS)) {
                        ((GraceSemiQuaver) note).setY0Pos(Integer.valueOf(str));
                    }
                    else if (lastTag.equals(XML_GRACE_SEMIQUAVER_X2_DIFFPOS)) {
                        ((GraceSemiQuaver) note).setX2DiffPos(Integer.valueOf(str));
                    }
                }
            }

            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public void characters(char[] ch, int start, int lenght) {
            if (where == Where.TEMPO_CHANGE) {
                tempoReader.characters(ch, start, lenght);
            }
            else if (where == Where.ANNOTATION) {
                annotationReader.characters(ch, start, lenght);
            }
            else if (where == Where.NOTE && lastTag != null) {
                value.append(ch, start, lenght);
            }
        }

        private enum Where { NOTE, TEMPO_CHANGE, ANNOTATION}
    }
}
