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

    Created on Jun 16, 2006
*/
package songscribe.IO;

import org.xml.sax.Attributes;
import songscribe.music.Composition;
import songscribe.ui.ProfileManager;
import songscribe.ui.Utilities;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Csaba Kávai
 */
public class ViewIO {
    private static final String XML_TITLE_FONT = "titlefont";
    private static final String XML_TITLE_FONT_SIZE = "titlefontsize";
    private static final String XML_TITLE_FONT_STYLE = "titlefontstyle";
    private static final String XML_LYRICS_FONT = "lyricsfont";
    private static final String XML_LYRICS_FONT_SIZE = "lyricsfontsize";
    private static final String XML_LYRICS_FONT_STYLE = "lyricsfontstyle";
    private static final String XML_GENERAL_FONT = "generalfont";
    private static final String XML_GENERAL_FONT_SIZE = "generalfontsize";

    public static void writeView(Composition c, PrintWriter pw) throws IOException {
        XML.indent = 4;
        XML.writeValue(pw, XML_TITLE_FONT, c.getSongTitleFont().getFamily());
        XML.writeValue(pw, XML_TITLE_FONT_SIZE, Integer.toString(c.getSongTitleFont().getSize()));
        XML.writeValue(pw, XML_TITLE_FONT_STYLE, ProfileManager.stringFontStyle(Utilities.isBold(c.getSongTitleFont()), Utilities.isItalic(c.getSongTitleFont())));
        XML.writeValue(pw, XML_LYRICS_FONT, c.getLyricsFont().getFamily());
        XML.writeValue(pw, XML_LYRICS_FONT_SIZE, Integer.toString(c.getLyricsFont().getSize()));
        XML.writeValue(pw, XML_LYRICS_FONT_STYLE, ProfileManager.stringFontStyle(Utilities.isBold(c.getLyricsFont()), Utilities.isItalic(c.getLyricsFont())));
        XML.writeValue(pw, XML_GENERAL_FONT, c.getGeneralFont().getFamily());
        XML.writeValue(pw, XML_GENERAL_FONT_SIZE, Integer.toString(c.getGeneralFont().getSize()));
    }

    public static class ViewReader {
        private String lastTag;
        private StringBuilder value = new StringBuilder(40);
        private StringFont title;
        private StringFont lyrics;
        private StringFont general;

        public ViewReader(ProfileManager pm) {
            title = new StringFont(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLE_FONT), pm.getDefaultProperty(ProfileManager.ProfileKey.TITLE_FONT_STYLE), pm.getDefaultProperty(ProfileManager.ProfileKey.TITLE_FONT_SIZE));
            lyrics = new StringFont(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICS_FONT), pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICS_FONT_STYLE), pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICS_FONT_SIZE));
            general = new StringFont(pm.getDefaultProperty(ProfileManager.ProfileKey.GENERAL_FONT), ProfileManager.stringFontStyle(false, false), pm.getDefaultProperty(ProfileManager.ProfileKey.GENERAL_FONT_SIZE));
        }

        public void startElement11(String qName, Attributes attributes) {
            lastTag = qName;
            value.delete(0, value.length());
        }

        public void endElement11(String qName) {
            if (qName.equals(lastTag)) {
                String str = value.toString();

                if (lastTag.equals(XML_TITLE_FONT)) {
                    title.name = str;
                }
                else if (lastTag.equals(XML_TITLE_FONT_SIZE)) {
                    title.size = str;
                }
                else if (lastTag.equals(XML_TITLE_FONT_STYLE)) {
                    title.style = str;
                }
                else if (lastTag.equals(XML_LYRICS_FONT)) {
                    lyrics.name = str;
                }
                else if (lastTag.equals(XML_LYRICS_FONT_SIZE)) {
                    lyrics.size = str;
                }
                else if (lastTag.equals(XML_LYRICS_FONT_STYLE)) {
                    lyrics.style = str;
                }
                else if (lastTag.equals(XML_GENERAL_FONT)) {
                    general.name = str;
                }
                else if (lastTag.equals(XML_GENERAL_FONT_SIZE)) {
                    general.size = str;
                }
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        public void characters(char[] ch, int start, int lenght) {
            if (lastTag != null) {
                value.append(ch, start, lenght);
            }
        }

        public void setAttributes(Composition c) {
            c.setSongTitleFont(title.getFont());
            c.setLyricsFont(lyrics.getFont());
            c.setGeneralFont(general.getFont());
        }

        private class StringFont {
            String name, style, size;

            public StringFont(String name, String style, String size) {
                this.name = name;
                this.style = style;
                this.size = size;
            }

            Font getFont() {
                return Utilities.createFont(name, ProfileManager.intFontStyle(style), Integer.parseInt(size));
            }
        }
    }
}
