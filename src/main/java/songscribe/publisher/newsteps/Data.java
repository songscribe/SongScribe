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
package songscribe.publisher.newsteps;

import songscribe.ui.MainFrame;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Csaba Kávai
 */
public class Data {
    public ArrayList<File> files;
    public int paperWidth, paperHeight, leftInnerMargin, rightOuterMargin, topMargin, bottomMargin;
    public boolean mirrored;
    public int songsPerPage = 2;
    public MainFrame mainFrame;
}
