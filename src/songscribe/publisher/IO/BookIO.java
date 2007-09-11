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

Created on May 12, 2007
*/
package songscribe.publisher.IO;

import songscribe.publisher.Book;
import songscribe.publisher.Page;
import songscribe.publisher.Publisher;
import songscribe.IO.XML;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.util.ListIterator;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Csaba Kávai
 */
public class BookIO {
    public static final int IOMAJORVERSION = 1;
    public static final int IOMINORVERSION = 0;

    private static final String XMLPUBLISHER = "publisher";
    private static final String XMLVERSION = "version";
    private static final String XMLBOOKATTRIBUTES = "bookattributes";
    private static final String XMLPAGEWIDTH = "pagewidth";
    private static final String XMLPAGEHEIGHT = "pageheight";
    private static final String XMLLEFTINNERMARGIN = "leftinnermargin";
    private static final String XMLRIGHTOUTERMARGIN = "rightoutermargin";
    private static final String XMLTOPMARGIN = "topmargin";
    private static final String XMLBOTTOMMARGIN = "bottommargin";
    private static final String XMLMIRROREDMARGIN = "mirroredmargin";
    private static final String XMLPAGE = "page";

    static File file;

    public static void writeBook(Book b, File file, boolean writeAbsolute) throws IOException {
        BookIO.file = file;
        PrintWriter pw = new PrintWriter(file, "UTF-8");
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<"+ XMLPUBLISHER +" "+XMLVERSION+"=\""+IOMAJORVERSION+"."+IOMINORVERSION+"\">");
        XML.indent = 2;
        XML.writeBeginTag(pw, XMLBOOKATTRIBUTES);
        XML.indent = 4;
        XML.writeValue(pw, XMLPAGEWIDTH, Integer.toString(b.getPageSize().width));
        XML.writeValue(pw, XMLPAGEHEIGHT, Integer.toString(b.getPageSize().height));
        XML.writeValue(pw, XMLLEFTINNERMARGIN, Integer.toString(b.getLeftInnerMargin()));
        XML.writeValue(pw, XMLRIGHTOUTERMARGIN, Integer.toString(b.getRightOuterMargin()));
        XML.writeValue(pw, XMLTOPMARGIN, Integer.toString(b.getTopMargin()));
        XML.writeValue(pw, XMLBOTTOMMARGIN, Integer.toString(b.getBottomMargin()));
        XML.indent = 2;
        XML.writeEndTag(pw, XMLBOOKATTRIBUTES);
        if(b.isMirroredMargin())XML.writeEmptyTag(pw, XMLMIRROREDMARGIN);
        for(ListIterator<Page> li = b.pageIterator();li.hasNext();){
            XML.writeBeginTag(pw, XMLPAGE);
            PageIO.writePage(li.next(), pw, writeAbsolute);
            XML.writeEndTag(pw, XMLPAGE);
        }
        pw.println("</"+XMLPUBLISHER+">");
        pw.close();
    }

    public static class DocumentReader extends DefaultHandler {
        private enum Where {BOOK, PAGE}
        private Where where;
        private String lastTag;
        private StringBuilder value = new StringBuilder(200);
        int pageWidth, pageHeight, leftInnerMargin, rightOuterMargin, topMargin, bottomMargin;
        boolean mirroredMargin;

        private PageIO.PageReader pageReader;

        private Book book;
        private int majorVersion, minorVersion;
        private Publisher publisher;

        public DocumentReader(Publisher publisher, File file) {
            this.publisher = publisher;
            BookIO.file = file.getParentFile();
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(where==null){
                if(qName.equals(XMLPUBLISHER)){
                    try{
                        String version = attributes.getValue(XMLVERSION);
                        int dotIndex = version.indexOf('.');
                        majorVersion = Integer.parseInt(version.substring(0,dotIndex));
                        minorVersion = Integer.parseInt(version.substring(dotIndex+1));
                        where = Where.BOOK;
                    }catch(NumberFormatException e){
                        throw new SAXException("SongScribe version is not a number.", e);
                    }
                }
            }else{
                if(majorVersion==1 && minorVersion==0){
                    startElement10(uri, localName, qName, attributes);
                }else{
                    throw new SAXException("Unsupported version number.");
                }
            }
            value.delete(0, value.length());
        }

        public void startElement10(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(where==Where.PAGE){
                pageReader.startElement10(qName, attributes);
            }else if(where==Where.BOOK){
                if(qName.equals(XMLPAGE)){
                    pageReader = new PageIO.PageReader(book);
                    where = Where.PAGE;
                }else{
                    lastTag = qName;
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(majorVersion==1 && minorVersion==0){
                endElement10(qName);
            }
        }

        public void endElement10(String qName){
            if(qName.equals(XMLPAGE)){
                where = Where.BOOK;
            }else if(where==Where.PAGE){
                pageReader.endElement10(qName);
            }else if(where==Where.BOOK){
               if(qName.equals(lastTag)){
                    String str = value.toString();
                    if(lastTag.equals(XMLPAGEWIDTH)){
                        pageWidth = Integer.parseInt(str);
                    }else if(lastTag.equals(XMLPAGEHEIGHT)){
                        pageHeight = Integer.parseInt(str);
                    }else if(lastTag.equals(XMLLEFTINNERMARGIN)){
                        leftInnerMargin = Integer.parseInt(str);
                    }else if(lastTag.equals(XMLRIGHTOUTERMARGIN)){
                        rightOuterMargin = Integer.parseInt(str);
                    }else if(lastTag.equals(XMLTOPMARGIN)){
                        topMargin = Integer.parseInt(str);
                    }else if(lastTag.equals(XMLBOTTOMMARGIN)){
                        bottomMargin = Integer.parseInt(str);
                    }else if(lastTag.equals(XMLMIRROREDMARGIN)){
                        mirroredMargin = true;
                    }
                }else if(qName.equals(XMLBOOKATTRIBUTES)){
                    book = new Book(publisher, pageWidth, pageHeight, leftInnerMargin, rightOuterMargin, topMargin, bottomMargin, mirroredMargin);
                }
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if(where==Where.PAGE){
                pageReader.characters(ch, start, length);
            }else if(where==Where.BOOK && lastTag!=null){
                value.append(ch, start, length);
            }
        }

        public Book getBook() {
            return book;
        }
    }
}
