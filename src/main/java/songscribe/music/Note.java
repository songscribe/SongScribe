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

    Created on 2005.01.06., 21:49:39
*/

package songscribe.music;

import songscribe.ui.MainFrame;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * @author Csaba Kávai
 */
public abstract class Note implements Cloneable {
    public static final Point HOT_SPOT = new Point(5, 27);
    public static final int NORMAL_IMAGE_WIDTH = 18;
    public static final Rectangle[] REAL_NATURAL_FLAT_SHARP_RECT = {
            new Rectangle(0, 17, 6, 22),
            new Rectangle(0, 15, 7, 19),
            new Rectangle(0, 17, 8, 22),
            new Rectangle(0, 23, 9, 10)
    };
    public static final Image[] NATURAL_FLAT_SHARP_IMAGE = {
            MainFrame.getImage("natural.gif"),
            MainFrame.getImage("flat.gif"),
            MainFrame.getImage("sharp.gif"),
            MainFrame.getImage("doublesharp.gif")
    };
    public static final Image DOT_IMAGE = MainFrame.getImage("dot.gif");
    public static final Note GLISSANDO_NOTE = new GlissandoNote();
    public static final Note PASTE_NOTE = new PasteNote();
    public static final Glissando NO_GLISSANDO = new Glissando(Integer.MAX_VALUE);
    protected Glissando glissando = NO_GLISSANDO;
    private static final int[] PITCHES = { 71, 72, 74, 76, 77, 79, 81 };
    private static final float[] DOTTED_DURATION = { 1.0f, 1.5f, 1.75f };
    private static final int[] PREFIX_MODIFIER = { 0, 0, -1, 1, 0, -2, 2, -1, 1 };
    private static ArrayList<ColoredNote> coloredNotes = new ArrayList<ColoredNote>();
    private static ArrayList<ColoredImage> coloredImages = new ArrayList<ColoredImage>();
    public final Acceleration a = new Acceleration();
    /**
     * The horizontal position of the note in the line.
     */
    protected int xPos;
    /**
     * The y position of the note in the sheet.
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
     * How many dots the note has. Possible values are 0, 1, 2.
     */
    protected int dotted;
    protected Accidental accidental = Accidental.NONE;
    protected boolean isAccidentalInParenthesis;
    protected Tempo tempoChange;
    protected BeatChange beatChange;
    protected Annotation annotation;
    protected boolean upper;
    protected ForceArticulation forceArticulation;
    protected DurationArticulation durationArticulation;
    protected boolean trill;
    protected boolean fermata;
    protected int syllableMovement;
    protected int syllableRelationMovement;
    protected boolean forceSyllable;
    protected boolean invertFractionBeamOrientation;
    /**
     * The line which owns this note.
     */
    protected Line line;
    private StringBuilder pitchStringBuffer = new StringBuilder(10);

    protected Note() {
    }

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
        fermata = note.fermata;
        syllableMovement = note.syllableMovement;
        syllableRelationMovement = note.syllableRelationMovement;
        forceSyllable = note.forceSyllable;
        invertFractionBeamOrientation = note.invertFractionBeamOrientation;
    }

    public static Image getImage(NoteType noteType, boolean upImage) {
        return upImage ? noteType.getInstance().getUpImage() : noteType.getInstance().getDownImage();
    }

    public static Image clipNoteImage(Image noteImage, Rectangle noteRect, Color borderColor, Dimension size) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2.drawImage(noteImage,
                (img.getWidth() - noteRect.width) / 2 - noteRect.x,
                (img.getHeight() - noteRect.height) / 2 - noteRect.y, null);
        g2.setPaint(borderColor);
        g2.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
        g2.dispose();
        return img;
    }

    public static Image getColoredNote(NoteType noteType, Color color, boolean upper) {
        for (ColoredNote cn : coloredNotes) {
            if (noteType == cn.noteType && upper == cn.upper && color.equals(cn.color)) {
                return cn.image;
            }
        }

        // not found
        Image ret = colorizeImage(getImage(noteType, upper), color);
        coloredNotes.add(new ColoredNote(noteType, color, upper, ret));
        return ret;
    }

    public static Image getColoredImage(Image originalImage, Color color) {
        for (ColoredImage ci : coloredImages) {
            if (originalImage == ci.originalImage && color == ci.color) {
                return ci.coloredImage;
            }
        }

        // not found
        Image ret = colorizeImage(originalImage, color);
        coloredImages.add(new ColoredImage(originalImage, color, ret));
        return ret;
    }

    private static Image colorizeImage(Image img, Color color) {
        BufferedImage result = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(img, 0, 0, null);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                int rgb = result.getRGB(x, y);
                int newRgb = rgb & 0xFF000000 |
                             ((255 - red) * (rgb & 0xFF0000 >> 16) + red * red) / 255 << 16 |
                             ((255 - green) * (rgb & 0xFF00 >> 8) + green * green) / 255 << 8 |
                             ((255 - blue) * (rgb & 0xFF) + blue * blue) / 255;
                if ((rgb & 0xFF000000) != 0) {
                    // not transparent
                    result.setRGB(x, y, newRgb);
                }
            }
        }

        return result;
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
        isAccidentalInParenthesis &= getAccidental() != Accidental.NONE;
    }

    public boolean isAccidentalInParenthesis() {
        return isAccidentalInParenthesis;
    }

    public void setAccidentalInParenthesis(boolean accidentalInParenthesis) {
        isAccidentalInParenthesis = getAccidental() != Accidental.NONE && accidentalInParenthesis;
    }

    public Glissando getGlissando() {
        return glissando;
    }

    public void setGlissando(int pitch) {
        if (glissando == NO_GLISSANDO) {
            glissando = new Glissando(pitch);
        }
        else {
            glissando.pitch = pitch;
        }
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

    public boolean isFermata() {
        return fermata;
    }

    public void setFermata(boolean fermata) {
        this.fermata = fermata;
    }

    public int getSyllableMovement() {
        return syllableMovement;
    }

    public void setSyllableMovement(int syllableMovement) {
        this.syllableMovement = syllableMovement;
    }

    public int getSyllableRelationMovement() {
        return syllableRelationMovement;
    }

    public void setSyllableRelationMovement(int syllableRelationMovement) {
        this.syllableRelationMovement = syllableRelationMovement;
    }

    public boolean isForceSyllable() {
        return forceSyllable;
    }

    public void setForceSyllable(boolean forceSyllable) {
        this.forceSyllable = forceSyllable;
    }

    public boolean isInvertFractionBeamOrientation() {
        return invertFractionBeamOrientation;
    }

    public void setInvertFractionBeamOrientation(boolean invertFractionBeamOrientation) {
        this.invertFractionBeamOrientation = invertFractionBeamOrientation;
    }

    public int getPitch() {
        return PITCHES[getPitchType()] + 12 * ((yPos <= 0 ? -yPos : -yPos - 6) / 7) +
               PREFIX_MODIFIER[(accidental == Accidental.NONE ? findLastPrefix() : accidental).ordinal()];
    }

    public int getActiveNotePitch(Line line) {
        return PITCHES[getPitchType()] + 12 * ((yPos <= 0 ? -yPos : -yPos - 6) / 7) +
               PREFIX_MODIFIER[getActiveNotePrefix(line).ordinal()];
    }

    private Accidental getActiveNotePrefix(Line line) {
        return accidental == Accidental.NONE ? (!line.keyExists(getPitchType()) ? Accidental.NONE : line.getKeyType() ==
                                                                                                    KeyType.FLATS ? Accidental.FLAT : Accidental.SHARP
        ) : accidental;
    }

    public String getActiveNotePitchString(Line line) {
        pitchStringBuffer.delete(0, pitchStringBuffer.length());
        Accidental p = getActiveNotePrefix(line);

        if (p == Accidental.FLAT) {
            pitchStringBuffer.append('b');
            pitchStringBuffer.append(' ');
        }
        else if (p == Accidental.SHARP) {
            pitchStringBuffer.append('#');
            pitchStringBuffer.append(' ');
        }

        pitchStringBuffer.append((char) ((getPitchType() + 1) % 7 + 'A'));

        if (yPos < 0) {
            pitchStringBuffer.append('\'');
        }
        else if (yPos > 6) {
            pitchStringBuffer.append(',');
        }

        return pitchStringBuffer.toString();
    }

    /**
     * @return 0 for B, 1 for C, 2 for D, ..., 6 for A
     */
    public int getPitchType() {
        return yPos <= 0 ? -yPos % 7 : (7 - yPos % 7) % 7;
    }

    public int getDefaultDurationWithDots() {
        return (int) (getDefaultDuration() * DOTTED_DURATION[dotted]);
    }

    public int getDuration() {
        return (int) (getDefaultDurationWithDots() * (fermata ? 1.5f : 1.0f));
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public Accidental findLastPrefix() {
        for (int i = line.getNoteIndex(this) - 1; i >= 0; i--) {
            Note note = line.getNote(i);

            if (note.getYPos() == yPos && note.getAccidental() != Accidental.NONE) {
                return line.getNote(i).getAccidental();
            }
        }

        return !line.keyExists(getPitchType()) ? Accidental.NONE :
                line.getKeyType() == KeyType.FLATS ? Accidental.FLAT : Accidental.SHARP;
    }

    // prefixes
    public enum Accidental {
        NONE(0, -1, -1),
        NATURAL(1, 0, -1), FLAT(1, 1, -1), SHARP(1, 2, -1),
        DOUBLE_NATURAL(2, 0, 0), DOUBLE_FLAT(2, 1, 1), DOUBLE_SHARP(1, 3, -1),
        NATURAL_FLAT(2, 0, 1), NATURAL_SHARP(2, 0, 2);

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

    public enum SyllableRelation { NO, EXTENDER, DASH, ONE_DASH }

    // glissando
    public static class Glissando {
        public int pitch, x1Translate, x2Translate;

        public Glissando(int pitch) {
            this.pitch = pitch;
        }
    }

    private static class ColoredNote {
        // key
        NoteType noteType;
        Color color;
        boolean upper;

        // value
        Image image;

        public ColoredNote(NoteType noteType, Color color, boolean upper, Image image) {
            this.noteType = noteType;
            this.color = color;
            this.upper = upper;
            this.image = image;
        }
    }

    private static class ColoredImage {
        // key
        Image originalImage;
        Color color;

        // value
        Image coloredImage;

        public ColoredImage(Image originalImage, Color color, Image coloredImage) {
            this.originalImage = originalImage;
            this.color = color;
            this.coloredImage = coloredImage;
        }
    }

    public class Acceleration {
        // lengthening for beaming
        public int lengthening;
        // stem
        public Line2D.Double stem = new Line2D.Double();
        // lyrics
        public String syllable;
        public SyllableRelation syllableRelation;
        public float longDashPosition;
    }
}
