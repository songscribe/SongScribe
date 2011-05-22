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

Created on Nov 2, 2007
*/
package songscribe.music;

/**
 * @author Csaba Kávai
 */
public enum BeatChange {
    QUAVEREQUALSQUAVER(new Quaver(), new Quaver(), 1f),
    DOTTEDCROCHETEQUALSMINIM(createDottedVersion(new Crotchet()), new Minim(), 3f/4f),
    MINIMEQUALSDOTTEDCROCHET(new Minim(), createDottedVersion(new Crotchet()), 4f/3f),
    CROTCHETQUALSDOTTEDCROCHET(new Crotchet(), createDottedVersion(new Crotchet()), 2f/3f),
    DOTTEDCROCHETQUALSCROCHET(createDottedVersion(new Crotchet()), new Crotchet(), 3f/2f);

    private Note firstNote, secondNote;
    private float tempoChange;

    private BeatChange(Note firstNote, Note secondNote, float tempoChange) {
        this.firstNote = firstNote;
        this.secondNote = secondNote;
        this.tempoChange = tempoChange;
    }

    private static Note createDottedVersion(Note note) {
        note.setDotted(1);
        note.setYPos(1);
        return note;
    }

    public Note getFirstNote() {
        return firstNote;
    }

    public Note getSecondNote() {
        return secondNote;
    }

    public float getTempoChange() {
        return tempoChange;
    }
}
