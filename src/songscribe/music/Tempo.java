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

Created on 2005.10.08.
*/
package songscribe.music;

/**
 * @author Csaba Kávai
 */
public class Tempo {
    public static enum Type{
        SEMIBREVE(new Semibreve()),
        MINIM(new Minim()),
        CROTCHETDOTTED(new Crotchet()),
        CROTCHET(new Crotchet()),
        QUAVER(new Quaver());

        static{
            CROTCHETDOTTED.getNote().setDotted(1);
            CROTCHETDOTTED.getNote().setYPos(1);//beacuse of the dot to be in the right place
        }

        private Note note;

        private Type(Note note) {
            this.note = note;
        }

        public Note getNote() {
            return note;
        }
    }

    private int visibleTempo;
    private Type tempoType;
    private String tempoDescription;
    private boolean showTempo;

    public Tempo(){
        this(120, Type.CROTCHET, "Moderate", true);
    }

    public Tempo(int tempo, Type tempoType, String tempoDescription, boolean showTempo) {
        this.visibleTempo = tempo;
        this.tempoType = tempoType;
        this.tempoDescription = tempoDescription;
        this.showTempo = showTempo;
    }

    public int getVisibleTempo() {
        return visibleTempo;
    }

    public void setVisibleTempo(int visibleTempo) {
        this.visibleTempo = visibleTempo;
    }

    public Type getTempoType() {
        return tempoType;
    }

    public void setTempoType(Type tempoType) {
        this.tempoType = tempoType;
    }

    public String getTempoDescription() {
        return tempoDescription;
    }

    public void setTempoDescription(String tempoDescription) {
        this.tempoDescription = tempoDescription;
    }

    public int getRealTempo() {
        return visibleTempo*tempoType.getNote().getDuration()/Composition.PPQ;
    }

    public boolean isShowTempo() {
        return showTempo;
    }

    public void setShowTempo(boolean showTempo) {
        this.showTempo = showTempo;
    }
}
