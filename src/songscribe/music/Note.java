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

Created on 2005.01.06., 21:49:39
*/

package songscribe.music;

import songscribe.ui.MainFrame;
import songscribe.data.Interval;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.HashMap;

/**
 * @author Csaba Kávai
 */
public abstract class Note implements Cloneable {
    public static final Point HOTSPOT = new Point(5, 27);
    public static final int NORMALIMAGEWIDTH = 18;
    public static final Dimension IMAGEDIM = new Dimension(19, 56);
    private static final int[] PITCHES = {71, 72, 74, 76, 77, 79, 81};
    private static final float[] DOTTEDLONGITUDE = {1.0f, 1.5f, 1.75f};
    private static final HashMap<Integer, Float> TUPLETMODIFIER = new HashMap<Integer, Float>();
    static{
        TUPLETMODIFIER.put(2, 3f/4f);
        TUPLETMODIFIER.put(3, 2f/3f);
        TUPLETMODIFIER.put(4, 3f/4f);
        TUPLETMODIFIER.put(5, 4f/5f);
        TUPLETMODIFIER.put(6, 2f/3f);
        TUPLETMODIFIER.put(7, 6f/7f);
    }

    public static final Rectangle[] REALNATURALFLATSHARPRECT = {
        new Rectangle(0, 17, 6, 22), new Rectangle(0, 15, 7, 19), new Rectangle(0, 17, 8, 22), new Rectangle(0, 23, 9, 10)
    };
    public static final Image[] NATURALFLATSHARPIMAGE = {MainFrame.getImage("natural.gif"), MainFrame.getImage("flat.gif"), MainFrame.getImage("sharp.gif"), MainFrame.getImage("doublesharp.gif")};
    public static final Image DOTIMAGE = MainFrame.getImage("dot.gif");

    public static final Note GLISSANDONOTE = new GlissandoNote();
    public static final Note PASTENOTE = new PasteNote();

    /**
     * Strores the vertical position of the note in the line.
     */
    protected int xPos;

    /**
     * Stores the y position of the note in the sheet.
     * <table border="1">
     * <tr><th>Pitch<th>Value
     * <tr><td>...<td>...
     * <tr><td>D5<td>-2
     * <tr><td>C5<td>-1
     * <tr><td>B4<td>0
     * <tr><td>A4<td>1
     * <tr><td>G4<td>2
     * <tr><td>...<td>...
     * </table>
     */
    protected int yPos;

    /**
     * Stores how many dots the note has. Possible values are 0, 1, 2.
     */
    protected int dotted;

    //prefixes
    public enum Accidental {
        NONE(0, -1, -1),
        NATURAL(1, 0, -1), FLAT(1, 1, -1), SHARP(1, 2, -1),
        DOUBLENATURAL(2, 0, 0), DOUBLEFLAT(2, 1, 1), DOUBLESHARP(1, 3, -1),
        NATURALFLAT(2, 0, 1), NATURALSHARP(2, 0, 2);

        private int nb;
        private int components[] = new int[2];

        Accidental(int nb, int firstComponent, int secondComponent) {
            this.nb = nb;
            components[0] = firstComponent;
            components[1] = secondComponent;
        }

        public int getNb() {
            return nb;
        }

        public int getComponent(int i) {
            return components[i];
        }
    }

    private static final int[] PREFIXMODIFIER = {0, 0, -1, 1, 0, -2, 2, -1, 1};

    protected Accidental accidental = Accidental.NONE;
    protected boolean isAccidentalInParenthesis;

    //glissando
    public static class Glissando{
        public int pitch, x1Translate, x2Translate;

        public Glissando(int pitch) {
            this.pitch = pitch;
        }
    }
    public static final Glissando NOGLISSANDO = new Glissando(Integer.MAX_VALUE);
    protected Glissando glissando = NOGLISSANDO;

    //tempochange
    protected Tempo tempoChange;

    //beatchange
    protected BeatChange beatChange;

    //annotation
    protected Annotation annotation;

    //whether the note is upper or not
    protected boolean upper;

    //articulations
    protected ForceArticulation forceArticulation;
    protected DurationArticulation durationArticulation;

    //trill
    protected boolean trill;

    protected int syllableMovement;

    public enum SyllableRelation{NO, EXTENDER, DASH, ONEDASH}

    public class Acceleration{
        //lenghening for beaming
        public int lengthening;
        //lyrics
        public String syllable;
        public SyllableRelation syllableRelation;
    }

    public final Acceleration a = new Acceleration();

    /**
     * Strores the line owned by the note.
     */
    protected Line line;

    protected Note(){}

    protected Note(Note note) {
        xPos = note.xPos;
        yPos = note.yPos;
        dotted = note.dotted;
        accidental = note.accidental;
        isAccidentalInParenthesis = note.isAccidentalInParenthesis;
        line = note.line;
        tempoChange = note.tempoChange;
        beatChange = note.beatChange;
        upper = note.upper;
        glissando = note.glissando;
        forceArticulation = note.forceArticulation;
        durationArticulation = note.durationArticulation;
        annotation = note.annotation;
        trill = note.trill;
        syllableMovement = note.syllableMovement;
    }

    public abstract Image getUpImage();

    public abstract Image getDownImage();

    public abstract NoteType getNoteType();

    public abstract Note clone();

    public abstract Rectangle getRealUpNoteRect();

    public abstract Rectangle getRealDownNoteRect();

    public abstract int getDefaultDuration();

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public int getDotted() {
        return dotted;
    }

    public void setDotted(int dotted) {
        this.dotted = dotted;
    }

    public Accidental getAccidental() {
        return accidental;
    }

    public void setAccidental(Accidental accidental) {
        this.accidental = accidental;
        isAccidentalInParenthesis&=getAccidental()!=Accidental.NONE;
    }

    public boolean isAccidentalInParenthesis() {
        return isAccidentalInParenthesis;
    }

    public void setAccidentalInParenthesis(boolean accidentalInParenthesis) {
        isAccidentalInParenthesis = getAccidental()!=Accidental.NONE && accidentalInParenthesis;
    }

    public Glissando getGlissando() {
        return glissando;
    }

    public void setGlissando(int pitch) {
        if(glissando==NOGLISSANDO)glissando = new Glissando(pitch);
        else glissando.pitch=pitch;
    }

    public Tempo getTempoChange() {
        return tempoChange;
    }

    public void setTempoChange(Tempo tempoChange) {
        this.tempoChange = tempoChange;
    }

    public BeatChange getBeatChange() {
        return beatChange;
    }

    public void setBeatChange(BeatChange beatChange) {
        this.beatChange = beatChange;
    }

    public boolean isUpper() {
        return upper;
    }

    public void setUpper(boolean upper) {
        this.upper = upper;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public ForceArticulation getForceArticulation() {
        return forceArticulation;
    }

    public void setForceArticulation(ForceArticulation forceArticulation) {
        this.forceArticulation = forceArticulation;
    }

    public DurationArticulation getDurationArticulation() {
        return durationArticulation;
    }

    public void setDurationArticulation(DurationArticulation durationArticulation) {
        this.durationArticulation = durationArticulation;
    }

    public boolean isTrill() {
        return trill;
    }

    public void setTrill(boolean trill) {
        this.trill = trill;
    }

    public int getSyllableMovement() {
        return syllableMovement;
    }

    public void setSyllableMovement(int syllableMovement) {
        this.syllableMovement = syllableMovement;
    }

    public int getPitch(){
        return PITCHES[getPitchType()]+12*((yPos<=0 ? -yPos : -yPos-6)/7)+
                PREFIXMODIFIER[(accidental==Accidental.NONE?findLastPrefix():accidental).ordinal()];
    }

    public int getActiveNotePitch(Line line){
        return PITCHES[getPitchType()]+12*((yPos<=0 ? -yPos : -yPos-6)/7)+
                PREFIXMODIFIER[getActiveNotePrefix(line).ordinal()];
    }

    private Accidental getActiveNotePrefix(Line line){
        return accidental==Accidental.NONE?(!line.existsKey(getPitchType()) ? Accidental.NONE :
                    line.getKeyType()==KeyType.FLATS ? Accidental.FLAT : Accidental.SHARP):accidental;
    }

    private StringBuilder pitchStringBuffer = new StringBuilder(10);
    public String getActiveNotePitchString(Line line){
        pitchStringBuffer.delete(0, pitchStringBuffer.length());
        Accidental p = getActiveNotePrefix(line);
        if(p==Accidental.FLAT){
            pitchStringBuffer.append('b');
            pitchStringBuffer.append(' ');
        }else if(p==Accidental.SHARP){
            pitchStringBuffer.append('#');
            pitchStringBuffer.append(' ');
        }
        pitchStringBuffer.append((char)((getPitchType()+1)%7+'A'));
        if(yPos<0)pitchStringBuffer.append('\'');
        else if(yPos>6)pitchStringBuffer.append(',');
        return pitchStringBuffer.toString();
    }

    /**
     * @return 0 for B, 1 for C, 2 for D, ..., 6 for A
     */
    public int getPitchType(){
        return yPos<=0 ? -yPos % 7 : (7 - yPos % 7) % 7;
    }

    public int getDuration(){
        Interval tupletInt = line!=null ? line.getTuplets().findInterval(line.getNoteIndex(this)) : null;
        return Math.round(getDefaultDuration()*DOTTEDLONGITUDE[dotted]*(tupletInt!=null ? TUPLETMODIFIER.get(Integer.parseInt(tupletInt.getData())) : 1f));
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public Line getLine() {
        return line;
    }

    public Accidental findLastPrefix(){
        int thisPitchType = getPitchType();        
        for(int i=line.getNoteIndex(this)-1;i>=0;i--){
            Note note = line.getNote(i);
            if(note.getPitchType()==thisPitchType && note.getAccidental()!=Accidental.NONE){
                return line.getNote(i).getAccidental();
            }
        }
        return !line.existsKey(thisPitchType) ? Accidental.NONE :
                    line.getKeyType()==KeyType.FLATS ? Accidental.FLAT : Accidental.SHARP;
    }

    public static Image getImage(NoteType noteType, boolean upImage) {
        return upImage ? noteType.getInstance().getUpImage() : noteType.getInstance().getDownImage();
    }

    public static Image clipNoteImage(Image noteImage, Rectangle noteRect, Color borderColor, Dimension size) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2.drawImage(noteImage, (img.getWidth() - noteRect.width) / 2 - noteRect.x, (img.getHeight() - noteRect.height) / 2 - noteRect.y, null);
        g2.setPaint(borderColor);
        g2.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
        g2.dispose();
        return img;
    }

    private static class ColoredNote {
        //key
        NoteType noteType;
        Color color;
        boolean upper;

        //value
        Image image;

        public ColoredNote(NoteType noteType, Color color, boolean upper, Image image) {
            this.noteType = noteType;
            this.color = color;
            this.upper = upper;
            this.image = image;
        }
    }

    private static Vector<ColoredNote> coloredNotes = new Vector<ColoredNote>(50, 10);

    public static Image getColoredNote(NoteType noteType, Color color, boolean upper) {
        for (ColoredNote cn : coloredNotes) {
            if (noteType == cn.noteType && upper == cn.upper && color.equals(cn.color)) {
                return cn.image;
            }
        }

        //not found
        Image ret = colorizeImage(getImage(noteType, upper), color);
        coloredNotes.add(new ColoredNote(noteType, color, upper, ret));
        return ret;
    }

    private static class ColoredImage {
        //key
        Image originalImage;
        Color color;

        //value
        Image coloredImage;

        public ColoredImage(Image originalImage, Color color, Image coloredImage) {
            this.originalImage = originalImage;
            this.color = color;
            this.coloredImage = coloredImage;
        }
    }

    private static Vector<ColoredImage> coloredImages = new Vector<ColoredImage>(20, 10);

    public static Image getColoredImage(Image originalImage, Color color){
        for (ColoredImage ci : coloredImages) {
            if (originalImage == ci.originalImage && color == ci.color) {
                return ci.coloredImage;
            }
        }

        //not found
        Image ret = colorizeImage(originalImage, color);
        coloredImages.add(new ColoredImage(originalImage, color, ret));
        return ret;
    }

    private static Image colorizeImage(Image img, Color color){
        BufferedImage ret = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = ret.createGraphics();
        g2.drawImage(img, 0, 0, null);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        for (int y = 0; y < ret.getHeight(); y++) {
            for (int x = 0; x < ret.getWidth(); x++) {
                int rgb = ret.getRGB(x, y);
                int newRgb = rgb&0xFF000000|
                        ((255-red)*(rgb&0xFF0000>>16)+red*red)/255<<16|
                        ((255-green)*(rgb&0xFF00>>8)+green*green)/255<<8|
                        ((255-blue)*(rgb&0xFF)+blue*blue)/255;
                if ((rgb & 0xFF000000) != 0) {//not transparent
                    ret.setRGB(x, y, newRgb);
                }
            }
        }
        return ret;
    }
}
