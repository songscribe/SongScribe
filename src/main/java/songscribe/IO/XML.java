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

    Created on Feb 18, 2006
*/
package songscribe.IO;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Csaba Kávai
 */
public class XML {
    public static int indent;

    public static void writeEmptyTag(PrintWriter pw, String tag) throws IOException {
        for (int i = 0; i < indent; i++) {
            pw.print(" ");
        }

        pw.print("<");
        pw.print(tag);
        pw.println(" />");
    }

    public static void writeBeginTag(PrintWriter pw, String tag) throws IOException {
        for (int i = 0; i < indent; i++) {
            pw.print(" ");
        }

        pw.print("<");
        pw.print(tag);
        pw.println(">");
    }

    public static void writeEndTag(PrintWriter pw, String tag) throws IOException {
        for (int i = 0; i < indent; i++) {
            pw.print(" ");
        }

        pw.print("</");
        pw.print(tag);
        pw.println(">");
    }

    public static void writeValue(PrintWriter pw, String tag, String value) {
        for (int i = 0; i < indent; i++) {
            pw.print(" ");
        }

        pw.print("<");
        pw.print(tag);
        pw.print(">");
        pw.print(escapeXML(value));
        pw.print("</");
        pw.print(tag);
        pw.println(">");
    }

    /**
     * Replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>
     *
     * @param string The string to be escaped.
     * @return The escaped string.
     */
    public static String escapeXML(String string) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);

            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;

                case '<':
                    sb.append("&lt;");
                    break;

                case '>':
                    sb.append("&gt;");
                    break;

                case '"':
                    sb.append("&quot;");
                    break;

                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }
}
