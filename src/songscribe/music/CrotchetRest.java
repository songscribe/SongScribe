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

Created on Jun 2, 2005
*/
package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class CrotchetRest extends Note {
    public static final Image IMAGE = MainFrame.getImage("crotchetRest.gif");
    public static final Rectangle REALNOTERECT = new Rectangle(0, 15, 8, 25);

    public CrotchetRest() {
        super();
    }

    private CrotchetRest(Note note) {
        super(note);
    }

    public CrotchetRest clone() {
        return new CrotchetRest(this);
    }

    public Image getUpImage() {
        return IMAGE;
    }

    public Image getDownImage() {
        return IMAGE;
    }

    public NoteType getNoteType() {
        return NoteType.CROTCHETREST;
    }

    public Rectangle getRealUpNoteRect() {
        return REALNOTERECT;
    }

    public Rectangle getRealDownNoteRect() {
        return REALNOTERECT;
    }

    public int getYPos() {
        return 0;
    }

    public Accidental getAccidental() {
        return Accidental.NONE;
    }

    public int getDefaultDuration() {
        return Composition.PPQ;
    }
}
