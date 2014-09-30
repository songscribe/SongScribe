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

    Created on Jul 23, 2006
*/
package songscribe.IO;

import songscribe.music.Annotation;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Csaba Kávai
 */
public class AnnotationIO {
    // version 1.1
    public static final String XML_ANNOTATION = "annotation";
    public static final String XML_NAME = "name";
    public static final String XML_ALIGNMENT = "alignment";
    public static final String XML_YPOS = "ypos";

    public static void writeAnnotation(Annotation a, PrintWriter pw, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            pw.print(' ');
        }

        pw.println("<" + XML_ANNOTATION + ">");
        XML.indent = indent + 2;
        XML.writeValue(pw, XML_NAME, a.getAnnotation());
        XML.writeValue(pw, XML_ALIGNMENT, Float.toString(a.getXAlignment()));
        XML.writeValue(pw, XML_YPOS, Integer.toString(a.getYPos()));

        for (int i = 0; i < indent; i++) {
            pw.print(' ');
        }

        pw.println("</" + XML_ANNOTATION + ">");
    }

    public static class AnnotationReader {
        private Annotation annotation;
        private String lastTag;
        private StringBuilder value = new StringBuilder(20);

        public void startElement11(String qName) {
            if (qName.equals(XML_ANNOTATION)) {
                annotation = new Annotation("");
                lastTag = null;
            }
            else {
                lastTag = qName;
            }

            value.delete(0, value.length());
        }

        public Annotation endElement11(String qName) {
            if (qName.equals(XML_ANNOTATION)) {
                return annotation;
            }
            else if (qName.equals(lastTag)) {
                String str = value.toString();

                if (lastTag.equals(XML_NAME)) {
                    annotation.setAnnotation(str);
                }
                else if (lastTag.equals(XML_ALIGNMENT)) {
                    annotation.setXAlignment(Float.parseFloat(str));
                }
                else if (lastTag.equals(XML_YPOS)) {
                    annotation.setYPos(Integer.parseInt(str));
                }
            }

            value.delete(0, value.length());
            lastTag = null;
            return null;
        }

        public void characters(char[] ch, int start, int length) {
            if (lastTag != null) {
                value.append(ch, start, length);
            }
        }
    }
}
