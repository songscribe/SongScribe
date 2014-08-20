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
    private static final String XMLTITLEFONT = "titlefont";
    private static final String XMLTITLEFONTSIZE = "titlefontsize";
    private static final String XMLTITLEFONTSTYLE = "titlefontstyle";
    private static final String XMLLYRICSFONT = "lyricsfont";
    private static final String XMLLYRICSFONTSIZE = "lyricsfontsize";
    private static final String XMLLYRICSFONTSTYLE = "lyricsfontstyle";
    private static final String XMLGENERALFONT = "generalfont";
    private static final String XMLGENERALFONTSIZE = "generalfontsize";

    public static void writeView(Composition c, PrintWriter pw) throws IOException {
        XML.indent = 4;
        XML.writeValue(pw, XMLTITLEFONT, c.getSongTitleFont().getFamily());
        XML.writeValue(pw, XMLTITLEFONTSIZE, Integer.toString(c.getSongTitleFont().getSize()));
        XML.writeValue(pw, XMLTITLEFONTSTYLE, ProfileManager.stringFontStyle(Utilities.isBold(c.getSongTitleFont()), Utilities.isItalic(c.getSongTitleFont())));
        XML.writeValue(pw, XMLLYRICSFONT, c.getLyricsFont().getFamily());
        XML.writeValue(pw, XMLLYRICSFONTSIZE, Integer.toString(c.getLyricsFont().getSize()));
        XML.writeValue(pw, XMLLYRICSFONTSTYLE, ProfileManager.stringFontStyle(Utilities.isBold(c.getLyricsFont()), Utilities.isItalic(c.getLyricsFont())));
        XML.writeValue(pw, XMLGENERALFONT, c.getGeneralFont().getFamily());
        XML.writeValue(pw, XMLGENERALFONTSIZE, Integer.toString(c.getGeneralFont().getSize()));
    }

    public static class ViewReader{
        private String lastTag;
        private StringBuilder value = new StringBuilder(40);

        private class StringFont{
            String name, style, size;

            public StringFont(String name, String style, String size) {
                this.name = name;
                this.style = style;
                this.size = size;
            }

            Font getFont(){
                return Utilities.createFont(name, ProfileManager.intFontStyle(style), Integer.parseInt(size));
            }
        }
        private StringFont title;
        private StringFont lyrics;
        private StringFont general;

        public ViewReader(ProfileManager pm) {
            title = new StringFont(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONT), pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONTSTYLE), pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONTSIZE));
            lyrics = new StringFont(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONT), pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONTSTYLE), pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONTSIZE));
            general = new StringFont(pm.getDefaultProperty(ProfileManager.ProfileKey.GENERALFONT), ProfileManager.stringFontStyle(false, false), pm.getDefaultProperty(ProfileManager.ProfileKey.GENERALFONTSIZE));
        }

        public void startElement11(String qName, Attributes attributes){
            lastTag = qName;
            value.delete(0, value.length());
        }

        public void endElement11(String qName){
            if(qName.equals(lastTag)){
                String str = value.toString();
                if(lastTag.equals(XMLTITLEFONT)){
                    title.name = str;
                }else if(lastTag.equals(XMLTITLEFONTSIZE)){
                    title.size = str;
                }else if(lastTag.equals(XMLTITLEFONTSTYLE)){
                    title.style = str;
                }else if(lastTag.equals(XMLLYRICSFONT)){
                    lyrics.name = str;
                }else if(lastTag.equals(XMLLYRICSFONTSIZE)){
                    lyrics.size = str;
                }else if(lastTag.equals(XMLLYRICSFONTSTYLE)){
                    lyrics.style = str;
                }else if(lastTag.equals(XMLGENERALFONT)){
                    general.name = str;
                }else if(lastTag.equals(XMLGENERALFONTSIZE)){
                    general.size = str;
                }
            }

            value.delete(0, value.length());
            lastTag = null;
        }

        public void characters(char[] ch, int start, int lenght){
            if(lastTag!=null){
                value.append(ch, start, lenght);
            }
        }

        public void setAttributes(Composition c){
            c.setSongTitleFont(title.getFont());
            c.setLyricsFont(lyrics.getFont());
            c.setGeneralFont(general.getFont());
        }
    }
}
