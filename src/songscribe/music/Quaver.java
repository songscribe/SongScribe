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

Created on 2005.01.06., 22:08:19
*/

package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public final class Quaver extends Note {
    public static final Image UPIMAGE = MainFrame.getImage("quaverUp.gif");
    public static final Image DOWNIMAGE = MainFrame.getImage("quaverDown.gif");
    public static final Rectangle REALUPNOTERECT = new Rectangle(0, 0, 18, 32);
    public static final Rectangle REALDOWNNOTERECT = new Rectangle(0, 24, 11, 32);

    public Quaver() {
        super();
    }

    private Quaver(Note note) {
        super(note);
    }

    public Quaver clone() {
        return new Quaver(this);
    }

    public Image getUpImage() {
        return UPIMAGE;
    }

    public Image getDownImage() {
        return DOWNIMAGE;
    }

    public NoteType getNoteType() {
        return NoteType.QUAVER;
    }

    public Rectangle getRealUpNoteRect() {
        return REALUPNOTERECT;
    }

    public Rectangle getRealDownNoteRect() {
        return REALDOWNNOTERECT;
    }

    public int getDefaultDuration() {
        return Composition.PPQ/2;
    }
}
