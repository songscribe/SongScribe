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

    Created on Aug 4, 2006
*/
package songscribe.data;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Csaba Kávai
 */
public class MyAcceptFilter extends FileFilter implements FilenameFilter {
    private String description;
    private String[] extensions;

    public MyAcceptFilter(String description, String... extensions) {
        this.extensions = new String[extensions.length];

        for (int i = 0; i < extensions.length; i++) {
            this.extensions[i] = extensions[i].toLowerCase();
        }

        StringBuilder sb = new StringBuilder(description.length() + 10 + extensions.length * 3);
        sb.append(description);
        sb.append(" (");

        for (String ex : this.extensions) {
            sb.append(ex.toUpperCase());
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());
        sb.append(")");
        this.description = sb.toString();
    }

    public boolean accept(File f) {
        return f.isDirectory() || accept(f.getName());
    }

    public boolean accept(File dir, String name) {
        return accept(name);
    }

    private boolean accept(String fileName) {
        int i = fileName.lastIndexOf('.');
        String ext;

        if (i != -1 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        else {
            return false;
        }

        for (String s : extensions) {
            if (ext.equals(s)) {
                return true;
            }
        }

        return false;
    }

    public String getExtension(int index) {
        return extensions[index];
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return getDescription();
    }
}
