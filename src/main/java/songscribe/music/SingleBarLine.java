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

    Created on 2006.01.16.
*/
package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class SingleBarLine extends NotNote {
    public static final Image IMAGE = MainFrame.getImage("singlebarline.gif");
    public static final Rectangle REAL_NOTE_RECT = new Rectangle(17, 12, 1, 32);

    public SingleBarLine() {
        super();
    }

    private SingleBarLine(Note note) {
        super(note);
    }

    public SingleBarLine clone() {
        return new SingleBarLine(this);
    }

    public Image getUpImage() {
        return IMAGE;
    }

    public Image getDownImage() {
        return IMAGE;
    }

    public NoteType getNoteType() {
        return NoteType.SINGLE_BARLINE;
    }

    public Rectangle getRealUpNoteRect() {
        return REAL_NOTE_RECT;
    }

    public Rectangle getRealDownNoteRect() {
        return REAL_NOTE_RECT;
    }
}
