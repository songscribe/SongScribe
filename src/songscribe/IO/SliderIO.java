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

import songscribe.ui.SlideFrame;
import songscribe.data.RelativePath;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;
import java.util.Vector;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.swing.*;

/**
 * @author Csaba Kávai
 */
public class SliderIO {
    public static final int IOMAJORVERSION = 1;
    public static final int IOMINORVERSION = 0;

    private static final String XMLSLIDER = "slider";
    private static final String XMLVERSION = "version";
    private static final String XMLSONGS = "songs";
    private static final String XMLSONG = "song";
    private static final String XMLRELATIVEFILE = "relativefile";
    private static final String XMLABSOLUTEFILE = "absolutefile";

    public static void writeSlider(ListIterator<File> songs, File file) throws IOException {
        PrintWriter pw = new PrintWriter(file, "UTF-8");
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<"+ XMLSLIDER +" "+ XMLVERSION +"=\""+IOMAJORVERSION+"."+IOMINORVERSION+"\">");
        XML.indent = 2;XML.writeBeginTag(pw, XMLSONGS);
        while(songs.hasNext()) {
            XML.indent = 4;XML.writeBeginTag(pw, XMLSONG);
            XML.indent = 6;
            File songFile = songs.next();
            XML.writeValue(pw, XMLRELATIVEFILE, RelativePath.getRelativePath(file.getParentFile(), songFile));
            XML.writeValue(pw, XMLABSOLUTEFILE, songFile.getAbsolutePath());
            XML.indent = 4;XML.writeEndTag(pw, XMLSONG);
        }
        XML.indent = 2;XML.writeEndTag(pw, XMLSONGS);
        XML.indent = 0;XML.writeEndTag(pw, XMLSLIDER);
        pw.close();
    }

    public static class DocumentReader extends DefaultHandler {
        private SlideFrame slideFrame;

        private File relativeFile, absoluteFile;
        private File readFile;
        private String lastTag;
        private StringBuilder value = new StringBuilder(200);

        private Vector<File> files = new Vector<File>();

        private int majorVersion, minorVersion;

        public DocumentReader(SlideFrame slideFrame, File readFile) {
            this.slideFrame = slideFrame;
            this.readFile = readFile;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals(XMLSLIDER)){
                try{
                    String version = attributes.getValue(XMLVERSION);
                    int dotIndex = version.indexOf('.');
                    majorVersion = Integer.parseInt(version.substring(0,dotIndex));
                    minorVersion = Integer.parseInt(version.substring(dotIndex+1));
                }catch(NumberFormatException e){
                    throw new SAXException("SongScribe version is not a number.", e);
                }
            }else{
                if(majorVersion==1 && minorVersion==0){
                    startElement10(qName);
                }else{
                    throw new SAXException("Unsupported version number.");
                }
            }
            value.delete(0, value.length());
        }

        private void startElement10(String qName) {
            lastTag = qName;
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(majorVersion==1 && minorVersion==0){
                endElement10(qName);
            }
            value.delete(0, value.length());
            lastTag = null;
        }

        public void endElement10(String qName){
            if(qName.equals(XMLSONG)) {
                if(absoluteFile==null)absoluteFile = new File("");
                if(relativeFile==null)relativeFile = new File("");
                File songFile = getAnyFile();
                if(songFile!=null){
                    files.add(songFile);
                }
                absoluteFile = relativeFile = null;
            }else if(qName.equals(lastTag)){
                String str = value.toString();
                if(lastTag.equals(XMLRELATIVEFILE)){
                    relativeFile = new File(readFile.getParentFile(), str);
                }else if(lastTag.equals(XMLABSOLUTEFILE)){
                    absoluteFile = new File(str);
                }
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if(lastTag!=null){
                value.append(ch, start, length);
            }
        }

        private File getAnyFile(){
            if(!absoluteFile.exists() && !relativeFile.exists()){
                JOptionPane.showMessageDialog(slideFrame, absoluteFile.getAbsolutePath()+" could not be found");
                //todo the ability to found
                return null;
            }else if(absoluteFile.exists()){
                return absoluteFile;
            }else{
                return relativeFile;
            }
        }

        public Vector<File> getFiles() {
            return files;
        }
    }
}
