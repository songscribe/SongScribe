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

    Created on 2005.12.16.
*/
package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class RepeatLeftRight extends NotNote {
    public static final Image IMAGE = MainFrame.getImage("repeatLeftRight.gif");
    public static final Rectangle REAL_NOTE_RECT = new Rectangle(0, 12, 22, 32);

    public RepeatLeftRight() {
        super();
    }

    private RepeatLeftRight(Note note) {
        super(note);
    }

    public RepeatLeftRight clone() {
        return new RepeatLeftRight(this);
    }

    public Image getUpImage() {
        return IMAGE;
    }

    public Image getDownImage() {
        return IMAGE;
    }

    public NoteType getNoteType() {
        return NoteType.REPEAT_LEFT_RIGHT;
    }

    public Rectangle getRealUpNoteRect() {
        return REAL_NOTE_RECT;
    }

    public Rectangle getRealDownNoteRect() {
        return REAL_NOTE_RECT;
    }
}
