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
    //version 1.0
    private static final String XMLNOTE = "note";
    private static final String XMLTYPE = "type";
    private static final String XMLYPOS = "ypos";
    private static final String XMLDOTTED = "dotted";
    private static final String XMLPREFIX = "prefix";
    private static final String XMLVOLUME = "volume";
    private static final String XMLGLISSANDO = "glissando";

    //version 1.1
    private static final String XMLXPOS = "xpos";
    private static final String XMLPREFIXINPARENTHESIS = "prefixinparenthesis";
    private static final String XMLUPPER = "upper";
    private static final String XMLFORCEARTICULATION = "forcearticulation";
    private static final String XMLDURATIONARTICULATION = "durationarticulation";
    private static final String XMLSYLLABLEMOVEMENT = "syllablemovement";
    private static final String XMLTRILL = "trill";
    private static final String XMLFERMATA = "fermata";
    private static final String XMLFORCESYLLABLE = "forcesyllable";
    private static final String XMLBEATCHANGE = "beatchange";
    private static final String XMLGLISSANDOX1TRANSLATE = "glissandox1translate";
    private static final String XMLGLISSANDOX2TRANSLATE = "glissandox2translate";
    private static final String XMLGRACESEMIQUAVERY0POS = "y0pos";
    private static final String XMLGRACESEMIQUAVERX2DIFFPOS = "x2diffpos";

    public static void writeNote(Note n, PrintWriter pw) throws IOException {
        pw.println("          <"+XMLNOTE+" "+XMLTYPE+"=\""+n.getNoteType().name()+"\">");
        XML.indent = 12;
        XML.writeValue(pw, XMLXPOS, Integer.toString(n.getXPos()));
        XML.writeValue(pw, XMLYPOS, Integer.toString(n.getYPos()));
        if(n.getDotted()!=0)XML.writeValue(pw, XMLDOTTED, Integer.toString(n.getDotted()));
        if(n.getAccidental()!=Note.Accidental.NONE)XML.writeValue(pw, XMLPREFIX, n.getAccidental().name());
        if(n.isAccidentalInParenthesis())XML.writeEmptyTag(pw, XMLPREFIXINPARENTHESIS);
        if(n.getForceArticulation()!=null)XML.writeValue(pw, XMLFORCEARTICULATION, n.getForceArticulation().name());
        if(n.getDurationArticulation()!=null)XML.writeValue(pw, XMLDURATIONARTICULATION, n.getDurationArticulation().name());
        if(n.getGlissando()!=Note.NOGLISSANDO){
            XML.writeValue(pw, XMLGLISSANDO, Integer.toString(n.getGlissando().pitch));
            if(n.getGlissando().x1Translate!=0)XML.writeValue(pw, XMLGLISSANDOX1TRANSLATE, Integer.toString(n.getGlissando().x1Translate));
            if(n.getGlissando().x2Translate!=0)XML.writeValue(pw, XMLGLISSANDOX2TRANSLATE, Integer.toString(n.getGlissando().x2Translate));
        }
        if(n.isUpper())XML.writeEmptyTag(pw, XMLUPPER);
        if(n.getSyllableMovement()!=0)XML.writeValue(pw, XMLSYLLABLEMOVEMENT, Integer.toString(n.getSyllableMovement()));
        if(n.getTempoChange()!=null)TempoIO.writeTempo(n.getTempoChange(), pw, 12);
        if(n.getAnnotation()!=null)AnnotationIO.writeAnnotation(n.getAnnotation(), pw, 12);
        if(n.isTrill())XML.writeEmptyTag(pw, XMLTRILL);
        if(n.isFermata())XML.writeEmptyTag(pw, XMLFERMATA);
        if(n.isForceSyllable())XML.writeEmptyTag(pw, XMLFORCESYLLABLE);
        if(n.getBeatChange()!=null)XML.writeValue(pw, XMLBEATCHANGE, n.getBeatChange().name());
        if(n.getNoteType() == NoteType.GRACESEMIQUAVER) {
            XML.writeValue(pw, XMLGRACESEMIQUAVERY0POS, Integer.toString(((GraceSemiQuaver) n).getY0Pos()));
            XML.writeValue(pw, XMLGRACESEMIQUAVERX2DIFFPOS, Integer.toString(((GraceSemiQuaver) n).getX2DiffPos()));
        }
        pw.println("          </"+XMLNOTE+">");
    }

    public static class NoteReader{
        private Note note;
        private String lastTag;
        private TempoIO.TempoReader tempoReader;
        private AnnotationIO.AnnotationReader annotationReader;
        private StringBuilder value = new StringBuilder(20);

        private enum Where{NOTE, TEMPOCHANGE, ANNOTATION}
        private Where where;

        public void startElement10(String qName, Attributes attributes) throws NewLineException{
            if(qName.equals(XMLNOTE)){
                lastTag = null;
                where = Where.NOTE;
                String type = attributes.getValue(XMLTYPE);
                if(type.equals("NEWLINE")){
                    where = null;
                    throw new NewLineException();
                }else if(type.equals("LINE")){
                    type = NoteType.SINGLEBARLINE.name();
                }
                note = NoteType.valueOf(type).newInstance();
            }else {
                lastTag = qName;
            }
            value.delete(0, value.length());
        }

        public void startElement11(String qName, Attributes attributes) throws NewLineException{
            if(where==Where.TEMPOCHANGE){
                tempoReader.startElement11(qName);
            }else if(where==Where.ANNOTATION){
                annotationReader.startElement11(qName);
            }else if(qName.equals(XMLNOTE)){
                lastTag = null;
                where = Where.NOTE;
                if(attributes.getValue(XMLTYPE).equals("VERTICALLINE")){
                    note = NoteType.SINGLEBARLINE.newInstance();
                }else{
                    note = NoteType.valueOf(attributes.getValue(XMLTYPE)).newInstance();
                }
            }else if(qName.equals(TempoIO.XMLTEMPO)){
                where = Where.TEMPOCHANGE;
                tempoReader = new TempoIO.TempoReader();
                tempoReader.startElement11(qName);
            }else if(qName.equals(AnnotationIO.XMLANNOTATION)){
                where = Where.ANNOTATION;
                annotationReader = new AnnotationIO.AnnotationReader();
                annotationReader.startElement11(qName);
            }else if(where==Where.NOTE){
                lastTag = qName;
            }
            value.delete(0, value.length());
        }

        public Note endElement10(String qName){
            return endElement11(qName);
        }

        public Note endElement11(String qName){
            if(where == Where.TEMPOCHANGE){
                Tempo t = tempoReader.endElement11(qName);
                if(t!=null){
                    note.setTempoChange(t);
                    where = Where.NOTE;
                }
            }else if(where == Where.ANNOTATION){
                Annotation a = annotationReader.endElement11(qName);
                if(a!=null){
                    note.setAnnotation(a);
                    where = Where.NOTE;
                }
            }else if(where==Where.NOTE){
                if(qName.equals(XMLNOTE)){
                    return note;
                }else if(qName.equals(lastTag)){
                    String str = value.toString();
                    if(lastTag.equals(XMLXPOS)){
                        note.setXPos(Integer.valueOf(str));
                    }else if(lastTag.equals(XMLYPOS)){
                        note.setYPos(Integer.valueOf(str));
                    }else if(lastTag.equals(XMLDOTTED)){
                        note.setDotted(Integer.valueOf(str));
                    }else if(lastTag.equals(XMLPREFIX)){
                        note.setAccidental(Note.Accidental.valueOf(str));
                    }else if(lastTag.equals(XMLPREFIXINPARENTHESIS)){
                        note.setAccidentalInParenthesis(true);
                    }else if(lastTag.equals(XMLVOLUME) && str.equals("LOUDER")){//old
                        note.setForceArticulation(ForceArticulation.ACCENT);
                    }else if(lastTag.equals(XMLFORCEARTICULATION)){
                        note.setForceArticulation(ForceArticulation.valueOf(str));
                    }else if(lastTag.equals(XMLDURATIONARTICULATION)){
                        note.setDurationArticulation(DurationArticulation.valueOf(str));
                    }else if(lastTag.equals(XMLGLISSANDO)){
                        note.setGlissando(Integer.valueOf(str));
                    }else if(lastTag.equals(XMLGLISSANDOX1TRANSLATE)){
                        note.getGlissando().x1Translate=Integer.valueOf(str);
                    }else if(lastTag.equals(XMLGLISSANDOX2TRANSLATE)){
                        note.getGlissando().x2Translate=Integer.valueOf(str);
                    }else if(lastTag.equals(XMLUPPER)){
                        note.setUpper(true);
                    }else if(lastTag.equals(XMLSYLLABLEMOVEMENT)){
                        note.setSyllableMovement(Integer.valueOf(str));
                    }else if(lastTag.equals(XMLTRILL)){
                        note.setTrill(true);
                    }else if(lastTag.equals(XMLFERMATA)){
                        note.setFermata(true);
                    }else if(lastTag.equals(XMLFORCESYLLABLE)){
                        note.setForceSyllable(true);
                    }else if(lastTag.equals(XMLBEATCHANGE)){
                        note.setBeatChange(BeatChange.valueOf(str));
                    }else if(lastTag.equals(XMLGRACESEMIQUAVERY0POS)){
                        ((GraceSemiQuaver) note).setY0Pos(Integer.valueOf(str));
                    }else if(lastTag.equals(XMLGRACESEMIQUAVERX2DIFFPOS)){
                        ((GraceSemiQuaver) note).setX2DiffPos(Integer.valueOf(str));
                    }
                }
            }
            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public void characters(char[] ch, int start, int lenght){
            if(where==Where.TEMPOCHANGE){
                tempoReader.characters(ch, start, lenght);
            }else if(where==Where.ANNOTATION){
                annotationReader.characters(ch, start, lenght);
            }else if(where==Where.NOTE && lastTag!=null){
                value.append(ch, start, lenght);
            }
        }
    }
}
