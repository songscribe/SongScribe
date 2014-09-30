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
import songscribe.IO.XML;
import songscribe.data.RelativePath;
import songscribe.publisher.Book;
import songscribe.publisher.Page;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.PImage;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.Song;
import songscribe.publisher.pagecomponents.Text;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class PageIO {
    private static final String XML_SONG = "song";
    private static final String XML_IMAGE = "image";
    private static final String XML_TEXT = "text";
    // page component properties
    private static final String XML_POS = "position";
    private static final String XML_RESOLUTION = "resolution";
    // song and image properties
    private static final String XML_RELATIVE_FILE = "relativefile";
    private static final String XML_ABSOLUTE_FILE = "absolutefile";
    // text properties
    private static final String XML_FONT_NAME = "fontname";
    private static final String XML_FONT_STYLE = "fontstyle";
    private static final String XML_FONT_SIZE = "fontsize";
    private static final String XML_ALIGNMENT = "alignment";
    private static final String XML_STRING = "string";
    // page number properties
    private static final String XML_PAGE_NUMBER = "pagenumber";
    private static final String XML_PLACEMENT = "placement";
    private static final String XML_FROM_PAGE = "frompage";
    private static final String XML_SPACE_FROM_MARGIN = "spacefrommargin";

    public static void writePage(Page p, PrintWriter pw, boolean writeAbsolute) throws IOException {
        for (ListIterator<PageComponent> li = p.getPageComponentIterator(); li.hasNext(); ) {
            PageComponent pc = li.next();
            XML.indent += 2;

            if (pc instanceof Song) {
                XML.writeBeginTag(pw, XML_SONG);
                writePageComponent(pc, pw);
                XML.indent += 2;

                if (writeAbsolute) {
                    XML.writeValue(pw, XML_ABSOLUTE_FILE, ((Song) pc).getSongFile().getAbsolutePath());
                }

                XML.writeValue(pw, XML_RELATIVE_FILE, RelativePath.getRelativePath(BookIO.file.getParentFile(), ((Song) pc).getSongFile()));
                XML.indent -= 2;
                XML.writeEndTag(pw, XML_SONG);
            }
            else if (pc instanceof PImage) {
                XML.writeBeginTag(pw, XML_IMAGE);
                writePageComponent(pc, pw);
                XML.indent += 2;
                XML.writeValue(pw, XML_ABSOLUTE_FILE, ((PImage) pc).getImageFile().getAbsolutePath());
                XML.writeValue(pw, XML_RELATIVE_FILE, RelativePath.getRelativePath(BookIO.file.getParentFile(), ((PImage) pc).getImageFile()));
                XML.indent -= 2;
                XML.writeEndTag(pw, XML_IMAGE);
            }
            else if (pc instanceof Text) {
                Text text = (Text) pc;
                XML.writeBeginTag(pw, XML_TEXT);
                writePageComponent(pc, pw);
                XML.indent += 2;
                XML.writeValue(pw, XML_FONT_NAME, text.getFont().getName());
                XML.writeValue(pw, XML_FONT_STYLE, Integer.toString(text.getFont().getStyle()));
                XML.writeValue(pw, XML_FONT_SIZE, Integer.toString(text.getFont().getSize()));
                XML.writeValue(pw, XML_ALIGNMENT, text.getAlignment().name());
                XML.writeValue(pw, XML_STRING, text.getText());
                XML.indent -= 2;
                XML.writeEndTag(pw, XML_TEXT);
            }

            XML.indent -= 2;
        }
    }

    private static void writePageComponent(PageComponent pc, PrintWriter pw) throws IOException {
        XML.indent += 2;
        XML.writeValue(pw, XML_POS,
                pc.getPos().x + "," + pc.getPos().y + "," + pc.getPos().width + "," + pc.getPos().height);
        XML.writeValue(pw, XML_RESOLUTION, Double.toString(pc.getResolution()));
        XML.indent -= 2;
    }

    public static class PageReader {
        private Book book;
        private Page page;
        private String lastTag;
        private StringBuilder value = new StringBuilder(20);

        private Rectangle pos;
        private double resolution;
        private File relativeFile, absoluteFile;
        private String fontName;
        private int fontStyle, fontSize;
        private Text.Alignment alignment;
        private String string;

        public PageReader(Book book) {
            this.book = book;
            page = book.addPage();
        }

        public void startElement10(String qName, Attributes attributes) {
            lastTag = qName;
            value.delete(0, value.length());
        }

        public void endElement10(String qName) {
            if (qName.equals(lastTag)) {
                String str = value.toString();

                if (lastTag.equals(XML_POS)) {
                    String[] p = str.split(",");
                    pos = new Rectangle(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                }
                else if (lastTag.equals(XML_RESOLUTION)) {
                    resolution = Double.parseDouble(str);
                }
                else if (lastTag.equals(XML_RELATIVE_FILE)) {
                    relativeFile = new File(BookIO.file, str);
                }
                else if (lastTag.equals(XML_ABSOLUTE_FILE)) {
                    absoluteFile = new File(str);
                }
                else if (lastTag.equals(XML_FONT_NAME)) {
                    fontName = str;
                }
                else if (lastTag.equals(XML_FONT_STYLE)) {
                    fontStyle = Integer.parseInt(str);
                }
                else if (lastTag.equals(XML_FONT_SIZE)) {
                    fontSize = Integer.parseInt(str);
                }
                else if (lastTag.equals(XML_ALIGNMENT)) {
                    alignment = Text.Alignment.valueOf(str);
                }
                else if (lastTag.equals(XML_STRING)) {
                    string = str;
                }
            }
            else if (qName.equals(XML_SONG)) {
                File openFile = getAnyFile();

                if (openFile != null) {
                    page.addPageComponent(new Song(book.getPublisher().openMusicSheet(openFile), pos.x, pos.y, resolution, openFile));
                }
            }
            else if (qName.equals(XML_IMAGE)) {
                File openFile = getAnyFile();

                if (openFile != null) {
                    page.addPageComponent(new PImage(Publisher.getImage(openFile), pos.x, pos.y, resolution, openFile));
                }

                absoluteFile = null;
            }
            else if (qName.equals(XML_TEXT)) {
                page.addPageComponent(new Text(string, Utilities.createFont(fontName, fontStyle, fontSize), alignment, pos.x, pos.y));
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        private File getAnyFile() {
            if ((absoluteFile == null || !absoluteFile.exists()) && !relativeFile.exists()) {
                JOptionPane.showMessageDialog(book.getPublisher(),
                        absoluteFile.getAbsolutePath() + " could not be found");
                return null;
            }
            else if (absoluteFile != null && absoluteFile.exists()) {
                return absoluteFile;
            }
            else {
                return relativeFile;
            }
        }

        public void characters(char[] ch, int start, int lenght) {
            if (lastTag != null) {
                value.append(ch, start, lenght);
            }
        }
    }
}
