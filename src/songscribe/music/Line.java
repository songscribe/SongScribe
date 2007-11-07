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

Created on Feb 11, 2006
*/
package songscribe.music;

import songscribe.data.IntervalSet;
import songscribe.data.Interval;

import java.util.Vector;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class Line {
    private static final int[][] FLATSHARPORDEAL = {{0, 3, 6, 2, 5, 1, 4},{4, 1, 5, 2, 6, 3, 0}};

    private Composition composition;

    private int keys;
    private KeyType keyType;

    private Vector<Note> notes = new Vector<Note>(25, 10);

    private final IntervalSet beamings = new IntervalSet();
    private final IntervalSet ties = new IntervalSet();
    private final IntervalSet tuplets = new IntervalSet();
    private final IntervalSet fsEndings = new IntervalSet();

    private final IntervalSet[] intervalSets = {beamings, ties, tuplets, fsEndings};

    //view properties
    private int tempoChangeYPos;
    private int beatChangeYPos = -24;
    private int lyricsYPos = 48;
    private int fsEndingYPos = -25;
    private int trillYPos = -27;
    private float noteDistChangeRatio = 1f;

    //acceleration
    public Note.SyllableRelation beginRelation = Note.SyllableRelation.NO;

    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    public int getKeys() {
        return keys;
    }

    public void setKeys(int keys) {
        modifiedComposition();
        this.keys = keys;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        modifiedComposition();
        this.keyType = keyType;
    }

    public void addNote(Note note) {
        modifiedComposition();
        note.setLine(this);
        notes.add(note);
    }

    public void addNote(int index, Note note) throws ArrayIndexOutOfBoundsException {
        modifiedComposition();
        note.setLine(this);
        notes.add(index, note);
        shiftIntervals(intervalSets, index, 1);
    }

    public Note setNote(int index, Note note) throws ArrayIndexOutOfBoundsException {
        modifiedComposition();
        note.setLine(this);
        return notes.set(index, note);
    }

    public Note getNote(int index) throws ArrayIndexOutOfBoundsException {
        return notes.get(index);
    }

    public void removeNote(int index) throws ArrayIndexOutOfBoundsException {
        modifiedComposition();
        notes.remove(index);
        shiftIntervals(intervalSets, index, -1);
    }

    private void modifiedComposition() {
        if(composition!=null)composition.modifiedComposition();
    }

    public int noteCount() {
        return notes.size();
    }

    public int getNoteIndex(Note note){
        return notes.indexOf(note);
    }

    /**
     *
     * @param pitchType 0 for B, 1 for C, 2 for D, ..., 6 for A
     * @return true if there is a leading key for that pitchtype
     */
    public boolean existsKey(int pitchType){
        for(int i=0;i<keys;i++){
            if(FLATSHARPORDEAL[keyType.ordinal()-1][i]==pitchType){
                return true;
            }
        }
        return false;
    }

    public int getTempoChangeYPos() {
        return tempoChangeYPos;
    }

    public void setTempoChangeYPos(int tempoChangeYPos) {
        this.tempoChangeYPos = tempoChangeYPos;
        modifiedComposition();
    }

    public int getBeatChangeYPos() {
        return beatChangeYPos;
    }

    public void setBeatChangeYPos(int beatChangeYPos) {
        this.beatChangeYPos = beatChangeYPos;
        modifiedComposition();
    }

    public int getLyricsYPos() {
        return lyricsYPos;
    }

    public void setLyricsYPos(int lyricsYPos) {
        this.lyricsYPos = lyricsYPos;
        modifiedComposition();
    }

    public int getFsEndingYPos() {
        return fsEndingYPos;
    }

    public void setFsEndingYPos(int fsEndingYPos) {
        this.fsEndingYPos = fsEndingYPos;
        modifiedComposition();
    }

    public int getTrillYPos() {
        return trillYPos;
    }

    public void setTrillYPos(int trillYPos) {
        this.trillYPos = trillYPos;
        modifiedComposition();
    }

    public void mulNoteDistChange(float ratio){
        noteDistChangeRatio*=ratio;
        modifiedComposition();
    }

    public float getNoteDistChangeRatio() {
        return noteDistChangeRatio;
    }

    public IntervalSet getBeamings() {
        return beamings;
    }

    public IntervalSet getTies() {
        return ties;
    }

    public IntervalSet getTuplets() {
        return tuplets;
    }

    public IntervalSet getFsEndings() {
        return fsEndings;
    }

    public void removeInterval(int a, int b){
        for(IntervalSet is:intervalSets){
            is.removeInterval(a, b);
        }
    }

    public IntervalSet[] copyIntervals(int a, int b){
        IntervalSet[] retIs = new IntervalSet[intervalSets.length];
        for(int i=0;i<intervalSets.length;i++){
            retIs[i] = intervalSets[i].copyInterval(a, b);
        }
        shiftIntervals(retIs, 0, -a);
        return retIs;
    }

    public void pasteIntervals(IntervalSet[] copyIntervalSets, int xIndex){
        shiftIntervals(copyIntervalSets, 0, xIndex);
        for(int i=0;i<intervalSets.length;i++){
            for(ListIterator<Interval> li=copyIntervalSets[i].listIterator();li.hasNext();){
                Interval iv = li.next();
                intervalSets[i].addInterval(iv.getA(), iv.getB(), iv.getData());
            }
        }
        shiftIntervals(copyIntervalSets, 0, -xIndex);
    }

    private void shiftIntervals(IntervalSet[] iss, int from, int shift){
        for(IntervalSet is:iss){
            is.shiftValues(from, shift);
            is.removeInterval(Integer.MIN_VALUE, 0);
            is.removeInterval(notes.size()-1, Integer.MAX_VALUE);
        }
    }

    public int getFirstTempoChange(){
        if(composition.indexOfLine(this)==0)return 0;
        for(int n=0;n<noteCount();n++){
            if(getNote(n).getTempoChange()!=null){
                return n;
            }
        }
        return -1;
    }

    public boolean isAnnotation(){
        for(int n=0;n<noteCount();n++){
            if(getNote(n).getAnnotation()!=null){
                return true;
            }
        }
        return false;
    }

    public int getFirstTrill(){
        for(int n=0;n<noteCount();n++){
            if(getNote(n).isTrill()){
                return n;
            }
        }
        return -1;
    }

    public int getFirstBeatChange(){
        for(int n=0;n<noteCount();n++){
            if(getNote(n).getBeatChange()!=null){
                return n;
            }
        }
        return -1;
    }
}
