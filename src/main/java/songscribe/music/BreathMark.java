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

Created on Oct 1, 2006
*/
package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class BreathMark extends NotNote{
    public static final Image IMAGE = MainFrame.getImage("breathmark.png");
    public static final Rectangle REALNOTERECT = new Rectangle(1, 24, 6, 11);

    public BreathMark() {
        super();
    }

    private BreathMark(Note note) {
        super(note);
    }

    public BreathMark clone() {
        return new BreathMark(this);
    }

    public int getYPos() {
        return -7;
    }

    public Image getUpImage() {
        return IMAGE;
    }

    public Image getDownImage() {
        return IMAGE;
    }

    public NoteType getNoteType() {
        return NoteType.BREATHMARK;
    }

    public Rectangle getRealUpNoteRect() {
        return REALNOTERECT;
    }

    public Rectangle getRealDownNoteRect() {
        return REALNOTERECT;
    }
}
