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
*/

package songscribe.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * this class provides functions used to generate a relative path
 * from two absolute paths
 *
 * @author David M. Howard
 */
public class RelativePath {
    /**
     * break a path down into individual elements and add to a list.
     * example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
     *
     * @param f input file
     * @return a List collection with the individual elements of the path in reverse order
     */
    private static List getPathList(File f) {
        List list = new ArrayList();
        File r;

        try {
            r = f.getCanonicalFile();

            while (r != null) {
                list.add(r.getName());
                r = r.getParentFile();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            list = null;
        }

        return list;
    }

    /**
     * figure out a string representing the relative path of
     * 'f' with respect to 'r'
     *
     * @param r home path
     * @param f path of file
     */
    private static String matchPathLists(List r, List f) {
        int i;
        int j;
        String s;
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size() - 1;
        j = f.size() - 1;

        // first eliminate common root
        while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for (; i >= 0; i--) {
            s += ".." + File.separator;
        }

        // for each level in the file path, add the path
        for (; j >= 1; j--) {
            s += f.get(j) + File.separator;
        }

        // file name
        s += f.get(j);
        return s;
    }

    /**
     * get relative path of File 'f' with respect to 'home' directory
     * example : home = /a/b/c
     * f    = /a/d/e/x.txt
     * s = getRelativePath(home,f) = ../../d/e/x.txt
     *
     * @param home base path, should be a directory, not a file, or it doesn't make sense
     * @param f    file to generate path for
     * @return path from home to f as a string
     */
    public static String getRelativePath(File home, File f) {
        File r;
        List homeList;
        List fileList;
        String s;

        homeList = getPathList(new File(home.getAbsolutePath()));
        fileList = getPathList(new File(f.getAbsolutePath()));
        s = matchPathLists(homeList, fileList);

        return s;
    }

    /**
     * test the function
     */
    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("RelativePath <home> <file>");
            return;
        }

        System.out.println("home = " + args[0]);
        System.out.println("file = " + args[1]);
        System.out.println("path = " + getRelativePath(new File(args[0]), new File(args[1])));
    }
}
