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

    Created on May 12, 2007
*/
package songscribe.publisher.IO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import songscribe.IO.XML;
import songscribe.publisher.Book;
import songscribe.publisher.Page;
import songscribe.publisher.PageNumber;
import songscribe.publisher.Publisher;
import songscribe.ui.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class BookIO {
    public static final int IO_MAJOR_VERSION = 1;
    public static final int IO_MINOR_VERSION = 0;

    private static final String XML_PUBLISHER = "publisher";
    private static final String XML_VERSION = "version";
    private static final String XML_BOOK_ATTRIBUTES = "bookattributes";
    private static final String XML_PAGE_WIDTH = "pagewidth";
    private static final String XML_PAGE_HEIGHT = "pageheight";
    private static final String XML_LEFT_INNER_MARGIN = "leftinnermargin";
    private static final String XML_RIGHT_OUTER_MARGIN = "rightoutermargin";
    private static final String XML_TOP_MARGIN = "topmargin";
    private static final String XML_BOTTOM_MARGIN = "bottommargin";
    private static final String XML_MIRRORED_MARGIN = "mirroredmargin";
    private static final String XML_PAGE = "page";

    // page number properties
    private static final String XML_PAGE_NUMBER = "pagenumber";
    private static final String XML_FONT_NAME = "fontname";
    private static final String XML_FONT_STYLE = "fontstyle";
    private static final String XML_FONT_SIZE = "fontsize";
    private static final String XML_ALIGNMENT = "alignment";
    private static final String XML_PLACEMENT = "placement";
    private static final String XML_FROM_PAGE = "frompage";
    private static final String XML_SPACE_FROM_MARGIN = "spacefrommargin";

    static File file;

    public static void writeBook(Book b, File file, boolean writeAbsolute) throws IOException {
        BookIO.file = file;
        PrintWriter pw = new PrintWriter(file, "UTF-8");
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<" + XML_PUBLISHER + " " + XML_VERSION + "=\"" + IO_MAJOR_VERSION + "." + IO_MINOR_VERSION + "\">");
        XML.indent = 2;

        // book attributes
        XML.writeBeginTag(pw, XML_BOOK_ATTRIBUTES);
        XML.indent = 4;
        XML.writeValue(pw, XML_PAGE_WIDTH, Integer.toString(b.getPageSize().width));
        XML.writeValue(pw, XML_PAGE_HEIGHT, Integer.toString(b.getPageSize().height));
        XML.writeValue(pw, XML_LEFT_INNER_MARGIN, Integer.toString(b.getLeftInnerMargin()));
        XML.writeValue(pw, XML_RIGHT_OUTER_MARGIN, Integer.toString(b.getRightOuterMargin()));
        XML.writeValue(pw, XML_TOP_MARGIN, Integer.toString(b.getTopMargin()));
        XML.writeValue(pw, XML_BOTTOM_MARGIN, Integer.toString(b.getBottomMargin()));

        if (b.isMirroredMargin()) {
            XML.writeEmptyTag(pw, XML_MIRRORED_MARGIN);
        }

        XML.indent = 2;
        XML.writeEndTag(pw, XML_BOOK_ATTRIBUTES);

        // page number
        PageNumber pn = b.getPageNumber();

        if (pn != null) {
            XML.writeBeginTag(pw, XML_PAGE_NUMBER);
            XML.indent = 4;
            XML.writeValue(pw, XML_FONT_NAME, pn.getFont().getName());
            XML.writeValue(pw, XML_FONT_STYLE, Integer.toString(pn.getFont().getStyle()));
            XML.writeValue(pw, XML_FONT_SIZE, Integer.toString(pn.getFont().getSize()));
            XML.writeValue(pw, XML_ALIGNMENT, pn.getAlignment().name());
            XML.writeValue(pw, XML_PLACEMENT, pn.getPlacement().name());
            XML.writeValue(pw, XML_FROM_PAGE, Integer.toString(pn.getFromPage()));
            XML.writeValue(pw, XML_SPACE_FROM_MARGIN, Integer.toString(pn.getSpaceFromMargin()));
            XML.indent = 2;
            XML.writeEndTag(pw, XML_PAGE_NUMBER);
        }

        for (ListIterator<Page> li = b.pageIterator(); li.hasNext(); ) {
            XML.writeBeginTag(pw, XML_PAGE);
            PageIO.writePage(li.next(), pw, writeAbsolute);
            XML.writeEndTag(pw, XML_PAGE);
        }

        pw.println("</" + XML_PUBLISHER + ">");
        pw.close();
    }

    public static class DocumentReader extends DefaultHandler {
        int pageWidth, pageHeight, leftInnerMargin, rightOuterMargin, topMargin, bottomMargin;
        boolean mirroredMargin;
        private Where where;
        private String lastTag;
        private StringBuilder value = new StringBuilder(200);
        private PageIO.PageReader pageReader;
        private Book book;
        private int majorVersion, minorVersion;
        private Publisher publisher;
        private PageNumber pageNumber = new PageNumber();
        private String fontName;
        private int fontStyle, fontSize;

        public DocumentReader(Publisher publisher, File file) {
            this.publisher = publisher;
            BookIO.file = file.getParentFile();
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (where == null) {
                if (qName.equals(XML_PUBLISHER)) {
                    try {
                        String version = attributes.getValue(XML_VERSION);
                        int dotIndex = version.indexOf('.');
                        majorVersion = Integer.parseInt(version.substring(0, dotIndex));
                        minorVersion = Integer.parseInt(version.substring(dotIndex + 1));
                        where = Where.BOOK;
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
                else {
                    throw new SAXException("Unsupported version number.");
                }
            }

            value.delete(0, value.length());
        }

        public void startElement10(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (where == Where.PAGE) {
                pageReader.startElement10(qName, attributes);
            }
            else if (where == Where.BOOK) {
                if (qName.equals(XML_PAGE)) {
                    pageReader = new PageIO.PageReader(book);
                    where = Where.PAGE;
                }
                else {
                    lastTag = qName;
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (majorVersion == 1 && minorVersion == 0) {
                endElement10(qName);
            }
        }

        public void endElement10(String qName) {
            if (qName.equals(XML_PAGE)) {
                where = Where.BOOK;
            }
            else if (where == Where.PAGE) {
                pageReader.endElement10(qName);
            }
            else if (where == Where.BOOK) {
                if (qName.equals(lastTag)) {
                    String str = value.toString();

                    if (lastTag.equals(XML_PAGE_WIDTH)) {
                        pageWidth = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_PAGE_HEIGHT)) {
                        pageHeight = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_LEFT_INNER_MARGIN)) {
                        leftInnerMargin = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_RIGHT_OUTER_MARGIN)) {
                        rightOuterMargin = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_TOP_MARGIN)) {
                        topMargin = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_BOTTOM_MARGIN)) {
                        bottomMargin = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_MIRRORED_MARGIN)) {
                        mirroredMargin = true;
                    }
                    else if (lastTag.equals(XML_FONT_NAME)) {
                        fontName = str;
                    }
                    else if (lastTag.equals(XML_FONT_SIZE)) {
                        fontSize = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_FONT_STYLE)) {
                        fontStyle = Integer.parseInt(str);
                    }
                    else if (lastTag.equals(XML_ALIGNMENT)) {
                        pageNumber.setAlignment(PageNumber.Alignment.valueOf(str));
                    }
                    else if (lastTag.equals(XML_PLACEMENT)) {
                        pageNumber.setPlacement(PageNumber.Placement.valueOf(str));
                    }
                    else if (lastTag.equals(XML_FROM_PAGE)) {
                        pageNumber.setFromPage(Integer.parseInt(str));
                    }
                    else if (lastTag.equals(XML_SPACE_FROM_MARGIN)) {
                        pageNumber.setSpaceFromMargin(Integer.parseInt(str));
                    }
                }
                else if (qName.equals(XML_BOOK_ATTRIBUTES)) {
                    book = new Book(publisher, pageWidth, pageHeight, leftInnerMargin, rightOuterMargin, topMargin, bottomMargin, mirroredMargin);
                }
                else if (qName.equals(XML_PAGE_NUMBER)) {
                    pageNumber.setFont(Utilities.createFont(fontName, fontStyle, fontSize));
                    book.setPageNumber(pageNumber);
                }
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if (where == Where.PAGE) {
                pageReader.characters(ch, start, length);
            }
            else if (where == Where.BOOK && lastTag != null) {
                value.append(ch, start, length);
            }
        }

        public Book getBook() {
            return book;
        }

        private enum Where { BOOK, PAGE }
    }
}
