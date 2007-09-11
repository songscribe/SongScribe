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

import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.music.KeyType;
import songscribe.data.Interval;
import songscribe.data.IntervalSet;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.ListIterator;

import org.xml.sax.Attributes;

/**
 * @author Csaba Kávai
 */
public class LineIO {
    private static final String XMLLINE = "line";
    private static final String XMLKEYS = "keys";
    private static final String XMLKEYTYPE = "keytype";
    private static final String XMLNOTEDISTCHANGE = "notedistchange";
    private static final String XMLLYRICSYPOS = "lyricsypos";
    private static final String XMLFSENDINGYPOS = "fsendingypos";
    private static final String XMLTEMPOCHANGEYPOS = "tempochangeypos";
    private static final String XMLBEAMINGS = "beamings";
    private static final String XMLTIES = "ties";
    private static final String XMLTRIPLETS = "triplets";
    private static final String XMLFSENDINGS = "fsendings";
    private static final String XMLNOTES = "notes";


    public static void writeLine(Line l, PrintWriter pw) throws IOException {
        pw.println("    <"+XMLLINE+">");
        XML.indent = 6;
        if(l.getKeys()!=l.getComposition().getDefaultKeys() || l.getKeyType()!=l.getComposition().getDefaultKeyType()){
            XML.writeValue(pw, XMLKEYS, Integer.toString(l.getKeys()));
            XML.writeValue(pw, XMLKEYTYPE, l.getKeyType().name());
        }
        if(l.getNoteDistChangeRatio()!=1f)XML.writeValue(pw, XMLNOTEDISTCHANGE, Float.toString(l.getNoteDistChangeRatio()));
        XML.writeValue(pw, XMLTEMPOCHANGEYPOS, Integer.toString(l.getTempoChangeYPos()));
        XML.writeValue(pw, XMLLYRICSYPOS, Integer.toString(l.getLyricsYPos()));
        XML.writeValue(pw, XMLFSENDINGYPOS , Integer.toString(l.getFsEndingYPos()));
        if(!l.getBeamings().isEmpty())XML.writeValue(pw, XMLBEAMINGS, intervalToString(l.getBeamings()));
        if(!l.getTies().isEmpty())XML.writeValue(pw, XMLTIES, intervalToString(l.getTies()));
        if(!l.getTriplets().isEmpty())XML.writeValue(pw, XMLTRIPLETS, intervalToString(l.getTriplets()));
        if(!l.getFsEndings().isEmpty())XML.writeValue(pw, XMLFSENDINGS, intervalToString(l.getFsEndings()));
        pw.println("      <"+XMLNOTES+">");
        for(int i=0;i<l.noteCount();i++){
            NoteIO.writeNote(l.getNote(i), pw);
        }
        pw.println("      </"+XMLNOTES+">");
        pw.println("    </"+XMLLINE+">");
    }

    private static String intervalToString(IntervalSet is){
        StringBuilder sb = new StringBuilder();
        for(ListIterator<Interval> li = is.listIterator();li.hasNext();){
            Interval i = li.next();
            sb.append(i.getA());
            sb.append(',');
            sb.append(i.getB());
            sb.append(';');
        }
        return sb.toString();
    }

    public static class LineReader{
        private Line line;
        private String lastTag;
        private NoteIO.NoteReader noteReader;
        private StringBuilder value = new StringBuilder(20);        

        private enum Where{LINE, NOTES}
        private Where where;

        private void stringToIntervalSet(IntervalSet is, String str){
            int begin = 0;
            while(str.indexOf(';', begin)!=-1){
                is.addInterval(Integer.parseInt(str.substring(begin, str.indexOf(',', begin))),
                        Integer.parseInt(str.substring(str.indexOf(',', begin)+1, str.indexOf(';', begin))));
                begin = str.indexOf(';', begin)+1;
            }
        }

        public void startElement11(String qName, Attributes attributes){
            if(where==null){
                if(qName.equals(XMLLINE)){
                    where = Where.LINE;
                    line = new Line();
                    lastTag = null;
                    noteReader = new NoteIO.NoteReader();
                }
            }else if(where==Where.NOTES){
                try{noteReader.startElement11(qName, attributes);}catch(NewLineException e){}
            }else{
                if(qName.equals(XMLNOTES)){
                    where = Where.NOTES;
                }else{
                    lastTag = qName;
                }
            }
            value.delete(0, value.length());
        }

        public Line endElement11(String qName){
            if(qName.equals(XMLNOTES)){
                where = Where.LINE;
            }else if(where==Where.NOTES){
                Note n = noteReader.endElement11(qName);
                if(n!=null){
                    line.addNote(n);
                }
            }else if(where==Where.LINE){
                if(qName.equals(XMLLINE)){
                    where=null;
                    return line;
                }else if(qName.equals(lastTag)){
                    String str = value.toString();
                    if(lastTag.equals(XMLKEYS)){
                        line.setKeys(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLKEYTYPE)){
                        line.setKeyType(KeyType.valueOf(str));
                    }else if(lastTag.equals(XMLNOTEDISTCHANGE)){
                        line.mulNoteDistChange(Float.parseFloat(str));
                    }else if(lastTag.equals(XMLTEMPOCHANGEYPOS)){
                        line.setTempoChangeYPos(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLLYRICSYPOS)){
                        line.setLyricsYPos(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLFSENDINGYPOS)){
                        line.setFsEndingYPos(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLBEAMINGS)){
                        stringToIntervalSet(line.getBeamings(), str);
                    }else if(lastTag.equals(XMLTIES)){
                        stringToIntervalSet(line.getTies(), str);
                    }else if(lastTag.equals(XMLTRIPLETS)){
                        stringToIntervalSet(line.getTriplets(), str);
                    }else if(lastTag.equals(XMLFSENDINGS)){
                        stringToIntervalSet(line.getFsEndings(), str);
                    }
                }
            }

            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public void characters(char[] ch, int start, int lenght){
            if(where==Where.NOTES){
                noteReader.characters(ch, start, lenght);
            }else if(lastTag!=null){
                value.append(ch, start, lenght);
            }
        }
    }
}
