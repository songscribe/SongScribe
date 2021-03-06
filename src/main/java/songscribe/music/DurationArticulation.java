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

    Created on Sep 30, 2006
*/
package songscribe.music;

/**
 * @author Csaba Kávai
 */
public enum DurationArticulation {
    STACCATO(33), TENUTO(100);

    private int duration;

    DurationArticulation(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }
}
