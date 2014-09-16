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

Created on 2005.01.14., 17:47:05
*/
package songscribe.music;

import org.apache.log4j.Logger;
import songscribe.data.Interval;
import songscribe.ui.*;

import javax.sound.midi.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Csaba Kávai
 */
public final class Composition{
    private static Logger logger = Logger.getLogger(Composition.class);

    public static final int PPQ = 96;

    public static final int[] VELOCITY = {98, 127};

    public static final int GRACEQUAVER_DURATION = PPQ/8;
    public static final int TOP_LINE_OFFSET = 0;

    //music data
    private Tempo tempo;
    private String number;
    private String songTitle;
    private String place;
    private int month, day;
    private String year;
    private String lyrics;
    private String underLyrics;
    private String translatedLyrics;
    private String rightInfo;
    private int defaultKeys;
    private KeyType defaultKeyType;

    //view data
    private Font songTitleFont;
    private Font lyricsFont;
    private Font generalFont;
    private int topSpace;
    private int rightInfoStartY;
    private int rowHeight = 4;
    private int lineWidth;

    private ArrayList<Line> lines = new ArrayList<Line>();

    //sequence data
    private Sequence sequence;
    private boolean modifiedComposition = true;

    //non-writable global attributes
    private int durationShortitude;
    private boolean withRepeat;
    private int instrument;
    private int manualTempoChange;
    private boolean colorizeNote;
    private boolean userSetTopSpace;

    private MainFrame mainFrame;
    private static final double LOG2 = Math.log(2);

    public Composition(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        ProfileManager pm = mainFrame.getProfileManager();
        number = "1";
        songTitle = "A New Song";
        place = "";
        year = "";
        lyrics = "";
        underLyrics = "";
        translatedLyrics = "";
        rightInfo = pm.getDefaultProperty(ProfileManager.ProfileKey.RIGHTINFORMATION);
        tempo = new Tempo(Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.TEMPO)),
                Tempo.Type.valueOf(pm.getDefaultProperty(ProfileManager.ProfileKey.TEMPOTYPE)),
                pm.getDefaultProperty(ProfileManager.ProfileKey.TEMPODESCRIPTION),
                true);
        defaultKeys = Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.KEYS));
        defaultKeyType = KeyType.valueOf(pm.getDefaultProperty(ProfileManager.ProfileKey.KEYTYPE));
        songTitleFont = Utilities.createFont(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONT),
                ProfileManager.intFontStyle(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONTSTYLE)),
                Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.TITLEFONTSIZE)));
        lyricsFont = Utilities.createFont(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONT),
                ProfileManager.intFontStyle(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONTSTYLE)),
                Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.LYRICSFONTSIZE)));
        generalFont = Utilities.createFont(pm.getDefaultProperty(ProfileManager.ProfileKey.GENERALFONT),
                Font.PLAIN, Integer.parseInt(pm.getDefaultProperty(ProfileManager.ProfileKey.GENERALFONTSIZE)));
        recalcTopSpace();
        lineWidth = MusicSheet.PAGESIZE.width;
        rightInfoStartY = (int) (songTitleFont.getSize() * 1.5);
        addLine(new Line());
    }

    public void musicChanged(Properties props) {
        withRepeat = props.getProperty(Constants.WITHREPEATPROP).equals(Constants.TRUEVALUE);
        instrument = Integer.parseInt(props.getProperty(Constants.INSTRUMENTPROP));
        manualTempoChange = Integer.parseInt(props.getProperty(Constants.TEMPOCHANGEPROP));
        durationShortitude = Integer.parseInt(props.getProperty(Constants.DURATIONSHORTITUDEPROP));
        colorizeNote = props.getProperty(Constants.COLORIZENOTE).equals(Constants.TRUEVALUE);
        modifiedComposition = true;
    }

    public Tempo getTempo() {
        return tempo;
    }

    public void setTempo(Tempo tempo) {
        this.tempo = tempo;
    }

    public Sequence getSequence(){
        if(!modifiedComposition){
            return sequence;
        }
        modifiedComposition = false;

        int ticks = 0;
        boolean repeating = false;
        try{
            sequence = new Sequence(Sequence.PPQ, PPQ, 0);
            Track track = sequence.createTrack();
            ShortMessage programChange = new ShortMessage();
            programChange.setMessage(0xc0, instrument, 0);
            track.add(new MidiEvent(programChange, 0));
            track.add(new MidiEvent(getMidiTempoMessage(tempo.getRealTempo()), 0));
            for(int l=0;l<lines.size();l++){
                Line line = lines.get(l);
                noteLabel:
                for(int n=0;n<line.noteCount();n++){
                    Note note = line.getNote(n);
                    if(withRepeat &&
                            (note.getNoteType()==NoteType.REPEATRIGHT || note.getNoteType()==NoteType.REPEATLEFTRIGHT)
                            && !repeating){
                        repeating = true;
                        for(;l>=0;l--){
                            for(--n;n>=0;n--){
                                if(line.getNote(n).getNoteType().isRepeat()){
                                    continue noteLabel;
                                }
                            }
                            if(l>0){
                                line=lines.get(l-1);
                                n=line.noteCount();
                            }
                        }
                        l=0;
                        continue;
                    }else if(withRepeat && (note.getNoteType()==NoteType.REPEATRIGHT || note.getNoteType()==NoteType.REPEATLEFTRIGHT) && repeating){
                        repeating = false;
                    }

                    //first-second endings
                    Interval fsInterval = line.getFsEndings().findInterval(n);
                    if(repeating && fsInterval!=null){
                        for(;n<=fsInterval.getB();n++){
                            if(line.getNote(n).getNoteType()==NoteType.REPEATRIGHT){
                                n--;
                                continue noteLabel;
                            }
                        }
                    }
                    if(!withRepeat && fsInterval!=null && note.getNoteType()==NoteType.REPEATRIGHT){
                        n = fsInterval.getB();
                        continue;
                    }

                    //tempo change
                    if(note.getTempoChange()!=null){
                        track.add(new MidiEvent(getMidiTempoMessage(note.getTempoChange().getRealTempo()), ticks));
                    }

                    //colorize notes
                    if(colorizeNote){
                        MetaMessage playNoteMessage = new MetaMessage();
                        playNoteMessage.setMessage(0, new byte[]{(byte)(l>>8), (byte)l, (byte)(n>>8), (byte)n}, 4);
                        track.add(new MidiEvent(playNoteMessage, ticks));
                    }

                    //sounds
                    ticks = addUpDownMessagesToTrack(track, line, n, ticks);
                }
            }
            MetaMessage finalMessage = new MetaMessage();
            finalMessage.setMessage(0, new byte[]{(byte)(-1>>8), (byte)-1, (byte)(-1>>8), (byte)-1}, 4);
            track.add(new MidiEvent(finalMessage, ticks));
        } catch (InvalidMidiDataException e) {
            mainFrame.showErrorMessage("Could not get the MIDI sequence because of an unexpected error.");
            logger.error("Creating MIDI sequence", e);
        }
        return sequence;
    }

    public Sequence getSelectedSequence(Line line, int begin, int end){
        Sequence selectedSequence = null;
        int ticks = 0;
        try{
            selectedSequence = new Sequence(Sequence.PPQ, PPQ, 0);
            Track track = selectedSequence.createTrack();
            ShortMessage programChange = new ShortMessage();
            programChange.setMessage(0xc0, instrument, 0);
            track.add(new MidiEvent(programChange, 0));
            track.add(new MidiEvent(getMidiTempoMessage(tempo.getRealTempo()), 0));
            for(int n=begin;n<=end;n++){
                Note note = line.getNote(n);

                //handling first-second endings
                Interval fsInterval = line.getFsEndings().findInterval(n);
                if(fsInterval!=null && note.getNoteType()==NoteType.REPEATRIGHT){
                    n = fsInterval.getB();
                    continue;
                }

                //tempo change
                if(note.getTempoChange()!=null){
                    track.add(new MidiEvent(getMidiTempoMessage(note.getTempoChange().getRealTempo()), ticks));
                }

                //colorize notes
                if(colorizeNote){
                    int l = lines.indexOf(line);
                    MetaMessage playNoteMessage = new MetaMessage();
                    playNoteMessage.setMessage(0, new byte[]{(byte)(l>>8), (byte)l, (byte)(n>>8), (byte)n}, 4);
                    track.add(new MidiEvent(playNoteMessage, ticks));
                }

                //sounds
                ticks = addUpDownMessagesToTrack(track, line, n, ticks);
            }
        } catch (InvalidMidiDataException e) {
            mainFrame.showErrorMessage("Could not get the MIDI sequence because of an unexpected error.");
            logger.error("Creating MIDI sequence", e);
        }
        return selectedSequence;
    }

    private int addUpDownMessagesToTrack(Track track, Line line, int n, int ticks) throws InvalidMidiDataException {
        Note note = line.getNote(n);
        NoteType type = note.getNoteType();
        if(type.isGraceNote()) {
            switch (type) {
                case GRACESEMIQUAVER:
                    GraceSemiQuaver graceSemiQuaver = (GraceSemiQuaver) note;
                    int yPos = note.getYPos();
                    note.setYPos(graceSemiQuaver.getY0Pos());
                    addDownMessageToTrack(track, ticks, note);
                    ticks += GRACEQUAVER_DURATION;
                    addUpMessageToTrack(track, ticks, note);
                    note.setYPos(yPos);
                case GRACEQUAVER:
                    addDownMessageToTrack(track, ticks, note);
                    ticks += GRACEQUAVER_DURATION;
                    addUpMessageToTrack(track, ticks, note);
                    break;
                default:
                    mainFrame.showErrorMessage("Programmer's error: no such NoteType in MIDI generation: " + type);
            }
        }
        else if(type.isNote() || type.isRest()) {
            int noteDuration = getNoteDurationWithTuplet(line, note, n);
            if(type.isNote()){
                Interval interval = line.getTies().findInterval(n);
                if(interval==null || interval.getA()==n){
                    addDownMessageToTrack(track, ticks, note);
                }

                if(interval==null || interval.getB()==n){
                    int currDuration = note.getDurationArticulation() == null ? durationShortitude : note.getDurationArticulation().getDuration();
                    addUpMessageToTrack(track, (int)(ticks + noteDuration * currDuration / 100f), note);
                }
            }
            ticks += noteDuration;
        }
        return ticks;
    }

    private void addDownMessageToTrack(Track track, int ticks, Note note) throws InvalidMidiDataException {
        ShortMessage down = new ShortMessage();
        down.setMessage(0x90, note.getPitch(), VELOCITY[note.getForceArticulation()== ForceArticulation.ACCENT ? 1 : 0]);
        track.add(new MidiEvent(down, ticks));
        // System.out.print("Pitch: " +note.getPitch()+"    Duration: "+ticks);
    }

    private void addUpMessageToTrack(Track track, int ticks, Note note) throws InvalidMidiDataException {
        ShortMessage up = new ShortMessage();
        up.setMessage(0x80, note.getPitch(), VELOCITY[note.getForceArticulation()== ForceArticulation.ACCENT ? 1 : 0]);
        track.add(new MidiEvent(up, ticks));
        // System.out.println("-" +(int)(ticks+getNoteDurationWithTuplet(line, note, n)*currDuration/100f)+", "+note.getPitch());
    }

    private int getNoteDurationWithTuplet(Line line, Note note, int n){
        return Math.round(note.getDuration()*getTupletFactor(line, n));
    }

    private MetaMessage getMidiTempoMessage(int realTempo) throws InvalidMidiDataException {
        MetaMessage tempoMessage = new MetaMessage();
        int midiTempo = 60000000/(realTempo*manualTempoChange/100);
        tempoMessage.setMessage(0x51, new byte[]{(byte)(midiTempo>>16), (byte)(midiTempo>>8), (byte) midiTempo}, 3);
        return tempoMessage;
    }

    public Tempo getLastTempo(Line line, int noteIndex){
        //finding the last tempochange
        boolean lastLine = true;
        for(int lineIndex = lines.indexOf(line);lineIndex>=0;lineIndex--){
            Line currentLine = lines.get(lineIndex);
            for(int n = lastLine?noteIndex:currentLine.noteCount()-1;n>=0;n--) {
                if(currentLine.getNote(n).getTempoChange() != null) {
                    return currentLine.getNote(n).getTempoChange();
                }
            }
            lastLine = false;
        }
        return getTempo();
    }

    private float getTupletFactor(Line line, int noteIndex){
        Interval tupletInt = line.getTuplets().findInterval(noteIndex);
        if(tupletInt!=null){
            Tempo lastTempo = getLastTempo(line, noteIndex);
            float duration = 0f;
            for (int i = tupletInt.getA();i<=tupletInt.getB();i++){
               duration += line.getNote(i).getDuration();
            }
            duration/=lastTempo.getTempoType().getNote().getDuration();
            float newDuration;
            if(duration>=1){
                newDuration = (float) Math.floor(duration);
                if(newDuration==duration && newDuration>1)newDuration--;
            }else{
                newDuration = (float)Math.pow(2, Math.floor(Math.log(duration)/ LOG2));
            }

            return newDuration / duration;
        }else{
            return 1;
        }
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getUnderLyrics() {
        return underLyrics;
    }

    public void setUnderLyrics(String underLyrics) {
        this.underLyrics = underLyrics;
    }

    public String getTranslatedLyrics() {
        return translatedLyrics;
    }

    public void setTranslatedLyrics(String translatedLyrics) {
        this.translatedLyrics = translatedLyrics;
    }

    public String getRightInfo() {
        return rightInfo;
    }

    public void setRightInfo(String rightInfo) {
        this.rightInfo = rightInfo;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getDefaultKeys() {
        return defaultKeys;
    }

    public void setDefaultKeys(int defaultKeys) {
        this.defaultKeys = defaultKeys;
    }

    public KeyType getDefaultKeyType() {
        return defaultKeyType;
    }

    public void setDefaultKeyType(KeyType defaultKeyType) {
        this.defaultKeyType = defaultKeyType;
    }

    public void modifiedComposition() {
        modifiedComposition = true;
        mainFrame.modifiedDocument();
    }

    public void addLine(Line line){
        addLine(lines.size(), line);
    }

    public void addLine(int index, Line line){
        lines.add(index, line);
        line.setComposition(this);
        if(line.getKeys()==0 && line.getKeyType()==null){
            line.setKeys(defaultKeys);
            line.setKeyType(defaultKeyType);
        }
        if(line.getTempoChangeYPos()==0){
            line.setTempoChangeYPos((index==0?-5:-3)*MusicSheet.LINEDIST);
        }
        modifiedComposition();
    }

    public void removeLine(int index){
        lines.remove(index);
        modifiedComposition();
    }

    public Line getLine(int index) {
        return lines.get(index);
    }

    public int lineCount(){
        return lines.size();
    }

    public int indexOfLine(Line line){
        return lines.indexOf(line);
    }

    public Font getSongTitleFont() {
        return songTitleFont;
    }

    public void setSongTitleFont(Font songTitleFont) {
        this.songTitleFont = songTitleFont;
        modifiedComposition();
    }

    public Font getLyricsFont() {
        return lyricsFont;
    }

    public void setLyricsFont(Font lyricsFont) {
        this.lyricsFont = lyricsFont;
        modifiedComposition();
    }

    public Font getGeneralFont() {
        return generalFont;
    }

    public void setGeneralFont(Font generalFont) {
        this.generalFont = generalFont;
        modifiedComposition();
    }

    public void setTopSpace(int topSpace, boolean user) {
        this.topSpace = topSpace;
        userSetTopSpace = userSetTopSpace|user;
        modifiedComposition();
    }

    public int getTopSpace() {
        return topSpace;
    }

    public int getRightInfoStartY() {
        return rightInfoStartY;
    }

    public void setRightInfoStartY(int rightInfoStartY) {
        this.rightInfoStartY = rightInfoStartY;
    }

    public void recalcTopSpace(){
        if(!userSetTopSpace){
            topSpace = (2 * songTitleFont.getSize()) + (Utilities.lineCount(rightInfo) * generalFont.getSize()) - (2 * MusicSheet.LINEDIST) + TOP_LINE_OFFSET;
        }
    }

    public boolean isUserSetTopSpace() {
        return userSetTopSpace;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
        modifiedComposition();
    }


    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * Do not call this directly unless you know what you are doing. Instead use musicSheet.setLineWidth
     */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        modifiedComposition();
    }
}
