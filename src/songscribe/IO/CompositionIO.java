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

Created on Feb 18, 2006
*/
package songscribe.IO;

import songscribe.music.*;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import java.io.PrintWriter;
import java.io.IOException;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Csaba Kávai
 */
public class CompositionIO {
    public static final int IOMAJORVERSION = 1;
    public static final int IOMINORVERSION = 1;

    //version 1.0
    private static final String XMLCOMPOSITION = "composition";
    private static final String XMLVERSION = "version";
    private static final String XMLKEYS = "keys";
    private static final String XMLKEYTYPE = "keytype";
    private static final String XMLNUMBER = "number";
    private static final String XMLSONGTITLE = "songtitle";
    private static final String XMLLYRICS = "lyrics";
    private static final String XMLRIGHTINFO = "rightinfo";
    private static final String XMLNOTES = "notes";
    private static final String XMLTEMPOCHANGES = "tempochanges";

    //version 1.1
    private static final String XMLLINES = "lines";
    private static final String XMLVIEW = "view";
    private static final String XMLUNDERLYRICS = "underlyrics";
    private static final String XMLTRANSLATEDLYRICS = "translatedlyrics";
    private static final String XMLTOPSPACE = "topspace";
    private static final String XMLLINEWIDTH = "linewidth";
    private static final String XMLROWHEIGHT = "rowheight";
    private static final String XMLPLACE = "place";
    private static final String XMLYEAR = "year";
    private static final String XMLMONTH = "month";
    private static final String XMLDAY = "day";
    private static final String XMLRIGHTINFOSTARTY = "rightinfostarty";


    public static void writeComposition(Composition c, PrintWriter pw) throws IOException {
        pw.println("<"+XMLCOMPOSITION+" "+XMLVERSION+"=\""+IOMAJORVERSION+"."+IOMINORVERSION+"\">");
        XML.indent = 2;
        XML.writeValue(pw, XMLKEYS, Integer.toString(c.getDefaultKeys()));
        XML.writeValue(pw, XMLKEYTYPE, c.getDefaultKeyType().name());
        TempoIO.writeTempo(c.getTempo(), pw, 2);
        XML.indent = 2;
        if(c.getNumber().length()>0)XML.writeValue(pw, XMLNUMBER, c.getNumber());
        if(c.getSongTitle().length()>0)XML.writeValue(pw, XMLSONGTITLE, c.getSongTitle());
        if(c.getPlace().length()>0)XML.writeValue(pw, XMLPLACE, c.getPlace());
        if(c.getYear().length()>0)XML.writeValue(pw, XMLYEAR, c.getYear());
        if(c.getMonth()>0)XML.writeValue(pw, XMLMONTH, Integer.toString(c.getMonth()));
        if(c.getDay()>0)XML.writeValue(pw, XMLDAY, Integer.toString(c.getDay()));
        if(c.getLyrics().length()>0)XML.writeValue(pw, XMLLYRICS, c.getLyrics());
        if(c.getUnderLyrics().length()>0)XML.writeValue(pw, XMLUNDERLYRICS, c.getUnderLyrics());
        if(c.getTranslatedLyrics().length()>0)XML.writeValue(pw, XMLTRANSLATEDLYRICS, c.getTranslatedLyrics());
        if(c.getRightInfo().length()>0)XML.writeValue(pw, XMLRIGHTINFO, c.getRightInfo());
        if(c.isUserSetTopSpace())XML.writeValue(pw, XMLTOPSPACE, Integer.toString(c.getTopSpace()));
        XML.writeValue(pw, XMLRIGHTINFOSTARTY, Integer.toString(c.getRightInfoStartY()));
        if(c.getRowHeight()!=0)XML.writeValue(pw, XMLROWHEIGHT, Integer.toString(c.getRowHeight()));
        XML.writeValue(pw, XMLLINEWIDTH, Integer.toString(c.getLineWidth()));
        pw.println("  <"+XMLLINES+">");
        for(int l=0;l<c.lineCount();l++){
            LineIO.writeLine(c.getLine(l), pw);
        }
        pw.println("  </"+XMLLINES+">");        
        pw.println("  <"+XMLVIEW+">");
        ViewIO.writeView(c, pw);
        pw.println("  </"+XMLVIEW+">");
        pw.println("</"+XMLCOMPOSITION+">");
    }

    public static class DocumentReader extends DefaultHandler {
        private enum Where {COMPOSITION, LINES, VIEW, NOTES, TEMPO, TEMPOCHANGE}
        private Where where;
        private String lastTag;
        private StringBuilder value = new StringBuilder(200);

        private NoteIO.NoteReader noteReader;
        private TempoIO.TempoReader tempoReader;
        private LineIO.LineReader lineReader;
        private ViewIO.ViewReader viewReader;

        private Composition composion;
        private int majorVersion, minorVersion;
        private MainFrame mainFrame;

        public DocumentReader(MainFrame mainFrame) {
            this.mainFrame = mainFrame;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(where==null){
                if(qName.equals(XMLCOMPOSITION)){
                    try{
                        String version = attributes.getValue(XMLVERSION);
                        int dotIndex = version.indexOf('.');
                        majorVersion = Integer.parseInt(version.substring(0,dotIndex));
                        minorVersion = Integer.parseInt(version.substring(dotIndex+1));
                        composion = new Composition(mainFrame);
                        composion.setTopSpace(0, false);
                        composion.removeLine(0);
                        where = Where.COMPOSITION;
                        if(majorVersion==1 && minorVersion==0){
                            noteReader = new NoteIO.NoteReader();
                            tempoReader = new TempoIO.TempoReader();
                        }else if(majorVersion==1 && minorVersion==1){
                            lineReader = new LineIO.LineReader();
                            viewReader = new ViewIO.ViewReader(mainFrame.getProfileManager());
                        }else {
                            throw new SAXException("Unsupported version number.");
                        }
                    }catch(NumberFormatException e){
                        throw new SAXException("SongScribe version is not a number.", e);
                    }
                }
            }else{
                if(majorVersion==1 && minorVersion==0){
                    startElement10(uri, localName, qName, attributes);
                }else if(majorVersion==1 && minorVersion==1){
                    startElement11(uri, localName, qName, attributes);
                }
            }
            value.delete(0, value.length());
        }

        public void startElement10(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(where==Where.NOTES){
                try{
                    noteReader.startElement10(qName, attributes);
                }catch(NewLineException e){
                    mainFrame.getMusicSheet().drawWidthIfWiderLine(composion.getLine(composion.lineCount()-1), true);
                    composion.addLine(new Line());
                }
            }else if(where==Where.TEMPOCHANGE){
                tempoReader.startElement10(qName);
            }else if(where==Where.COMPOSITION){
                if(qName.equals(XMLNOTES)){
                    where = Where.NOTES;
                }else if(qName.equals(XMLTEMPOCHANGES)){
                    where = Where.TEMPOCHANGE;
                }else{
                    lastTag = qName;
                }
            }
        }

        public void startElement11(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(where==Where.LINES){
                lineReader.startElement11(qName, attributes);
            }else if(where==Where.VIEW){
                viewReader.startElement11(qName, attributes);
            }else if(where==Where.TEMPO){
                tempoReader.startElement11(qName);
            }else if(where==Where.COMPOSITION){
                if(qName.equals(XMLLINES)){
                    where = Where.LINES;
                }else if(qName.equals(XMLVIEW)){
                    where = Where.VIEW;
                }else if(qName.equals(TempoIO.XMLTEMPO)){
                    where = Where.TEMPO;
                    tempoReader = new TempoIO.TempoReader();
                    tempoReader.startElement11(qName);
                }else{
                    lastTag = qName;
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(majorVersion==1 && minorVersion==0){
                endElement10(qName);
            }else if(majorVersion==1 && minorVersion==1){
                endElement11(qName);
            }
        }

        public void endElement10(String qName){
            if(qName.equals(XMLNOTES)){
                where = Where.COMPOSITION;
            }else if(qName.equals(XMLTEMPOCHANGES)){
                where = Where.COMPOSITION;
            }else if(where==Where.NOTES){
                Note note = noteReader.endElement10(qName);
                if(note!=null){
                    if(composion.lineCount()==0){
                        composion.addLine(new Line());
                    }
                    Line line = composion.getLine(composion.lineCount()-1);
                    note.setXPos(MusicSheet.calculateLastNoteXPos(line, note));
                    note.setUpper(MusicSheet.defaultUpperNote(note));
                    line.addNote(note);
                }
            }else if(where==Where.TEMPOCHANGE){
                Tempo tc = tempoReader.endElement10(qName);
                if(tc!=null){
                    if(tempoReader.getPos10()==0){
                        composion.setTempo(tc);
                    }else{
                        int firstNoteInLine = 0;
                        for(int l=0;l<composion.lineCount();l++){
                            Line line = composion.getLine(l);
                            if(tempoReader.getPos10()<firstNoteInLine+line.noteCount()){
                                line.getNote(tempoReader.getPos10()-firstNoteInLine).setTempoChange(tc);
                                break;
                            }else{
                                firstNoteInLine+=line.noteCount()+1;
                            }
                        }
                    }
                }
            }else if(where==Where.COMPOSITION){
                if(qName.equals(XMLCOMPOSITION)){
                    composion.modifiedComposition();
                }else if(qName.equals(lastTag)){
                    String str = value.toString();
                    if(lastTag.equals(XMLKEYS)){
                        composion.setDefaultKeys(Integer.valueOf(str));
                    }else if(lastTag.equals(XMLKEYTYPE)){
                        composion.setDefaultKeyType(KeyType.valueOf(str));
                    }else if(lastTag.equals(XMLNUMBER)){
                        composion.setNumber(str);
                    }else if(lastTag.equals(XMLSONGTITLE)){
                        composion.setSongTitle(str);
                    }else if(lastTag.equals(XMLLYRICS)){
                        composion.setLyrics(str);
                    }else if(lastTag.equals(XMLRIGHTINFO)){
                        composion.setRightInfo(str);
                    }
                }
            }
            value.delete(0, value.length());
            lastTag = null;
        }

        public void endElement11(String qName){
            if(qName.equals(XMLLINES)){
                where = Where.COMPOSITION;
            }else if(qName.equals(XMLVIEW)){
                viewReader.setAttributes(composion);
                where = Where.COMPOSITION;
            }else if(where==Where.LINES){
                Line l = lineReader.endElement11(qName);
                if(l!=null){
                    composion.addLine(l);
                }
            }else if(where==Where.TEMPO){
                Tempo t = tempoReader.endElement11(qName);
                if(t!=null){
                    composion.setTempo(t);
                    where=Where.COMPOSITION;
                }
            }else if(where==Where.COMPOSITION){
               if(qName.equals(lastTag)){
                    String str = value.toString();
                    if(lastTag.equals(XMLKEYS)){
                        composion.setDefaultKeys(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLKEYTYPE)){
                        composion.setDefaultKeyType(KeyType.valueOf(str));
                    }else if(lastTag.equals(XMLNUMBER)){
                        composion.setNumber(str);
                    }else if(lastTag.equals(XMLSONGTITLE)){
                        composion.setSongTitle(str);
                    }else if(lastTag.equals(XMLPLACE)){
                        composion.setPlace(str);
                    }else if(lastTag.equals(XMLYEAR)){
                        composion.setYear(str);
                    }else if(lastTag.equals(XMLMONTH)){
                        composion.setMonth(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLDAY)){
                        composion.setDay(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLLYRICS)){
                        composion.setLyrics(str);
                    }else if(lastTag.equals(XMLUNDERLYRICS)){
                        composion.setUnderLyrics(str);
                    }else if(lastTag.equals(XMLTRANSLATEDLYRICS)){
                        composion.setTranslatedLyrics(str);
                    }else if(lastTag.equals(XMLRIGHTINFO)){
                        composion.setRightInfo(str);
                    }else if(lastTag.equals(XMLTOPSPACE)){
                        composion.setTopSpace(Integer.parseInt(str), false);
                    }else if(lastTag.equals(XMLRIGHTINFOSTARTY)){
                        composion.setRightInfoStartY(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLROWHEIGHT)){
                        composion.setRowHeight(Integer.parseInt(str));
                    }else if(lastTag.equals(XMLLINEWIDTH)){
                        composion.setLineWidth(Integer.parseInt(str));
                    }
                }
            }else if(where==Where.VIEW){
                viewReader.endElement11(qName);
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if(where==Where.LINES){
                lineReader.characters(ch, start, length);
            }else if(where==Where.VIEW){
                viewReader.characters(ch, start, length);
            }else if(where==Where.NOTES){
                noteReader.characters(ch, start, length);
            }else if(where==Where.TEMPOCHANGE){
                tempoReader.characters(ch, start, length);
            }else if(where==Where.TEMPO){
                tempoReader.characters(ch, start, length);
            }else if(where==Where.COMPOSITION && lastTag!=null){
                value.append(ch, start, length);
            }
        }

        public Composition getComposion() {
            if(composion.getTopSpace()==0)composion.recalcTopSpace();
            return composion;
        }
    }
}
