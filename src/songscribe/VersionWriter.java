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

Created on Jan 30, 2007
*/
package songscribe;

import songscribe.ui.Utilities;

import java.io.FileNotFoundException;

/**
 * @author Csaba Kávai
 */
public class VersionWriter {
    public static void main(String[] args) throws FileNotFoundException {
        if(args[0].equals("full")){
            System.out.println(Utilities.getFileVersion());
        }else if(args[0].equals("normal")){
            System.out.println(Utilities.getVersion());
        }
    }
}
