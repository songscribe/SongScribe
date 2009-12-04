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

Created on 2009.10.25.
*/

package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;

public class GraceSemiQuaver extends Note{
    public static final Image UPIMAGE = MainFrame.getImage("graceCrotchetUp.gif");
    public static final Image DOWNIMAGE = MainFrame.getImage("graceCrotchetDown.gif");

    public static final Rectangle REALUPNOTERECT = new Rectangle(2, 11, 13, 20);
    public static final Rectangle REALDOWNNOTERECT = new Rectangle(0, 24, 9, 20);

    private int y0Pos, x2DiffPos;

    public GraceSemiQuaver() {
        super();
    }

    private GraceSemiQuaver(Note note) {
        super(note);
    }

    public GraceSemiQuaver clone() {
        return new GraceSemiQuaver(this);
    }

    public Image getUpImage() {
        return UPIMAGE;
    }

    public Image getDownImage() {
        return DOWNIMAGE;
    }

    public NoteType getNoteType() {
        return NoteType.GRACESEMIQUAVER;
    }

    public Rectangle getRealUpNoteRect() {
        return REALUPNOTERECT;
    }

    public Rectangle getRealDownNoteRect() {
        return REALDOWNNOTERECT;
    }

    public int getDotted() {
        return 0;
    }

    public int getDefaultDuration() {
        return 0;
    }

    public int getY0Pos() {
        return y0Pos;
    }

    public int getX2DiffPos() {
        return x2DiffPos;
    }

    public void setY0Pos(int y0Pos) {
        this.y0Pos = y0Pos;
    }

    public void setX2DiffPos(int x2DiffPos) {
        this.x2DiffPos = x2DiffPos;
    }
}
