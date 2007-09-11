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

import songscribe.music.Tempo;

import java.io.PrintWriter;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class TempoIO {
    //version 1.0
    private static final String XMLTEMPOCHANGE = "tempochange";
    private static final String XMLPOS = "position";
    private static final String XMLVISIBLETEMPO = "visibletempo";
    private static final String XMLTEMPOTYPE = "tempotype";
    private static final String XMLTEMPODESCRIPTION = "tempodescription";
    private static final String XMLDONTSHOWTEMPO = "dontshowtempo";

    //version 1.1
    public static final String XMLTEMPO = "tempo";

    public static void writeTempo(Tempo t, PrintWriter pw, int indent) throws IOException {
        for(int i=0;i<indent;i++)pw.print(' ');
        pw.println("<"+XMLTEMPO+">");
        XML.indent = indent+2;
        XML.writeValue(pw, XMLVISIBLETEMPO, Integer.toString(t.getVisibleTempo()));
        XML.writeValue(pw, XMLTEMPOTYPE, t.getTempoType().name());
        XML.writeValue(pw, XMLTEMPODESCRIPTION, t.getTempoDescription());
        if(!t.isShowTempo())XML.writeEmptyTag(pw, XMLDONTSHOWTEMPO);
        for(int i=0;i<indent;i++)pw.print(' ');
        pw.println("</"+XMLTEMPO+">");
    }

    public static class TempoReader{
        private Tempo tempo;
        private int pos10;
        private String lastTag;
        private StringBuilder value = new StringBuilder(20);

        public void startElement10(String qName){
            if(qName.equals(XMLTEMPOCHANGE)){
                tempo = new Tempo();
                lastTag = null;
            }else{
                lastTag = qName;
            }
            value.delete(0, value.length());
        }

        public void startElement11(String qName){
            if(qName.equals(XMLTEMPO)){
                tempo = new Tempo();
                lastTag = null;
            }else{
                lastTag = qName;
            }
            value.delete(0, value.length());
        }

        public Tempo endElement10(String qName){
            if(qName.equals(XMLTEMPOCHANGE)){
                return tempo;
            }else if(qName.equals(lastTag)){
                String str = value.toString();
                if(lastTag.equals(XMLPOS)){
                    pos10 = Integer.valueOf(str);
                }else if(lastTag.equals(XMLVISIBLETEMPO)){
                    tempo.setVisibleTempo(Integer.valueOf(str));
                }else if(lastTag.equals(XMLTEMPOTYPE)){
                    Tempo.Type type;
                    try{
                        type = Tempo.Type.valueOf(str.toUpperCase());
                    }catch(IllegalArgumentException e){
                        type = Tempo.Type.CROTCHETDOTTED;
                    }
                    tempo.setTempoType(type);
                }else if(lastTag.equals(XMLTEMPODESCRIPTION)){
                    tempo.setTempoDescription(str);
                }else if(lastTag.equals(XMLDONTSHOWTEMPO)){
                    tempo.setShowTempo(false);
                }
            }
            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public Tempo endElement11(String qName){
            if(qName.equals(XMLTEMPO)){
                return tempo;
            }else if(qName.equals(lastTag)){
                String str = value.toString();
                if(lastTag.equals(XMLVISIBLETEMPO)){
                    tempo.setVisibleTempo(Integer.parseInt(str));
                }else if(lastTag.equals(XMLTEMPOTYPE)){
                    tempo.setTempoType(Tempo.Type.valueOf(str));
                }else if(lastTag.equals(XMLTEMPODESCRIPTION)){
                    tempo.setTempoDescription(str);
                }else if(lastTag.equals(XMLDONTSHOWTEMPO)){
                    tempo.setShowTempo(false);
                }
            }
            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public void characters(char[] ch, int start, int lenght){
            if(lastTag!=null){
                value.append(ch, start, lenght);
            }
        }

        public int getPos10() {
            return pos10;
        }
    }
}
