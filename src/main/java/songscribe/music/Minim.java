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

    Created on 2005.01.14., 0:28:27
*/

package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class Minim extends Note {
    public static final Image UP_IMAGE = MainFrame.getImage("minimUp.gif");
    public static final Image DOWN_IMAGE = MainFrame.getImage("minimDown.gif");
    public static final Rectangle REAL_UP_NOTE_RECT = new Rectangle(0, 0, 11, 32);
    public static final Rectangle REAL_DOWN_NOTE_RECT = new Rectangle(0, 24, 11, 32);

    public Minim() {
        super();
    }

    private Minim(Note note) {
        super(note);
    }

    public Minim clone() {
        return new Minim(this);
    }

    public Image getUpImage() {
        return UP_IMAGE;
    }

    public Image getDownImage() {
        return DOWN_IMAGE;
    }

    public NoteType getNoteType() {
        return NoteType.MINIM;
    }

    public Rectangle getRealUpNoteRect() {
        return REAL_UP_NOTE_RECT;
    }

    public Rectangle getRealDownNoteRect() {
        return REAL_DOWN_NOTE_RECT;
    }

    public int getDefaultDuration() {
        return Composition.PPQ * 2;
    }
}
