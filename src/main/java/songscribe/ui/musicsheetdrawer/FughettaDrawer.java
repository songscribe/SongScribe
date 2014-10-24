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

    Created on Sep 3, 2006
*/
package songscribe.ui.musicsheetdrawer;

import org.apache.log4j.Logger;
import songscribe.data.FileGeneralPath;
import songscribe.music.*;
import songscribe.ui.MusicSheet;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Csaba Kávai
 */
public class FughettaDrawer extends BaseMsDrawer {
    private static final Logger logger = Logger.getLogger(FughettaDrawer.class);

    private static final Map<NoteType, String> noteHead = new HashMap<NoteType, String>(10);

    static {
        noteHead.put(NoteType.SEMIBREVE, "\uf077");
        noteHead.put(NoteType.MINIM, "\uf0cd");
        noteHead.put(NoteType.CROTCHET, "\uf0cf");
        noteHead.put(NoteType.QUAVER, "\uf0cf");
        noteHead.put(NoteType.SEMIQUAVER, "\uf0cf");
        noteHead.put(NoteType.DEMI_SEMIQUAVER, "\uf0cf");
        noteHead.put(NoteType.SEMIBREVE_REST, "\uf0ee");
        noteHead.put(NoteType.MINIM_REST, "\uf0ee");
        noteHead.put(NoteType.CROTCHET_REST, "\uf0ce");
        noteHead.put(NoteType.QUAVER_REST, "\uf0e4");
        noteHead.put(NoteType.SEMIQUAVER_REST, "\uf0c5");
        noteHead.put(NoteType.DEMI_SEMIQUAVER_REST, "\uf0a8");
    }

    private static final double upperCrotchetStemX = size / 3.6056337d;
    private static final double upperMinimStemX = size / 3.1411042f;
    private static final double tempoStemShortening = 2;
    private static final Line2D.Float upperStem = new Line2D.Float(0f, -size / 32f, 0f, -size / 1.1429f);
    private static final Line2D.Float lowerStem = new Line2D.Float(0f, size / 60f, 0f, size / 1.1429f);
    private static final Line2D.Float graceNoteUpperSlash = new Line2D.Float(
            size / 18.285715f, -size / 5.5652175f, size / 2.3703704f, -size / 2.6666667f);
    private static final Line2D.Float graceNoteLowerSlash = new Line2D.Float(
            -size / 11.906977f, size / 5.5652175f, size / 3.5310345f, size / 2.6666667f);
    private static final BasicStroke graceNoteSlashStroke = new BasicStroke(0.64f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final double upperFlagX = size / 3.6834533d;
    private static final double upperFlagY = -size / 1.6623377d;
    private static final double upperFlag2Y = -size / 1.1851852d;
    private static final double upperFlag3Y = -size / 0.9411765d;
    private static final double lowerFlagY = size / 1.6d;
    private static final double lowerFlag2Y = size / 1.1428572d;
    private static final double lowerFlag3Y = size / 0.9078014d;
    private static final double flagYLength = 7;
    private static final double graceNoteScale = 0.6;
    private static final float SEMIQUAVER_AND_DEMI_SEMIQUAVER_FLAG_COLLAPSE = 5f;
    private static final double LOWER_STEM_CROTCHET_Y1_OFFSET = 0.6f;

    private static final String trebleClef = "\uf026";
    //  NATURAL   FLAT      SHARP     DOUBLE-NATURAL  DOUBLE-FLAT     D-SHARP   NATURAL-FLAT    NATURAL-SHARP
    private static final String[] accidentals = {
            "", "\uf06e", "\uf062", "\uf023", "\uf06e\uf06e", "\uf0ba", "\uf0dc", "\uf06e\uf062", "\uf06e\uf023"
    };
    private static final String[] accidentalParenthesis = {
            "", "\uf04e", "\uf041", "\uf061", "\uf06e\uf06e", "\uf08c", "\uf081", "\uf06e\uf062", "\uf06e\uf023"
    };
    private static final double manualParenthesisY = size / 3.5068493d;
    private static final String beginParenthesis = "\uf028";
    private static final String endParenthesis = "\uf029";

    private static final String mainUpperFlag = "\uf06a";
    private static final String secondUpperFlag = "\uf0fb";
    private static final String mainLowerFlag = "\uf04a";
    private static final String secondLowerFlag = "\uf0f0";

    private static final Line2D.Float barLine = new Line2D.Float(Note.NORMAL_IMAGE_WIDTH,
            -size / 2f, Note.NORMAL_IMAGE_WIDTH, size / 2f);
    private static final Line2D.Float verticalLine = new Line2D.Float(0, -size / 2f, 0, size / 2f);
    private static final BasicStroke repeatHeavyStroke = new BasicStroke(4.167f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final BasicStroke repeatThinStroke = new BasicStroke(0.64f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final Ellipse2D.Float repeatCircle1 = new Ellipse2D.Float(0f,
            size / 2f - size / 2.3703704f, size / 8f, size / 8f);
    private static final Ellipse2D.Float repeatCircle2 = new Ellipse2D.Float(0f,
            size / 2f - size / 1.4545455f, size / 8f, size / 8f);
    private static final double repeatLeftThickX = 4.167d / 2d;
    private static final double repeatRightThickX = Note.NORMAL_IMAGE_WIDTH - repeatLeftThickX;
    private static final double repeatLeftRightThickX = Note.NORMAL_IMAGE_WIDTH / 2d;
    private static final double repeatThickThinDiff = size / 6.095238d;
    private static final double repeatThinCircleDiff = size / 6.918919d;
    private static final double barLineSpace = 4.167d;

    private static final BasicStroke heavyLineStroke = new BasicStroke(4.167f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke thinLineStroke = new BasicStroke(0.674f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    private static final double dotWidth = size / 9.142858d;
    private static final Ellipse2D.Double[] noteDots = {
            new Ellipse2D.Double(13.1d, -dotWidth / 2, dotWidth, dotWidth),
            new Ellipse2D.Double(15.878d + dotWidth, -dotWidth / 2, dotWidth, dotWidth)
    };
    private static float[] baseAccidentalWidths = null;
    private static float[] baseAccidentalParenthesisWidths = null;
    private static float beginParenthesisWidth, endParenthesisWidth;
    private GeneralPath breathMark, fermata;

    public FughettaDrawer(MusicSheet ms) throws IOException {
        super(ms);
        crotchetWidth = upperCrotchetStemX;
        beamX1Correction = 0.3d;
        beamX2Correction = 0.3d;
        breathMark = FileGeneralPath.readGeneralPath(new File("fonts/bm"));
        fermata = FileGeneralPath.readGeneralPath(new File("fonts/fm"));
    }

    public static void calculateAccidentalWidths(Graphics2D g2) {
        //firstcall
        if (baseAccidentalWidths == null) {
            FontMetrics fm = g2.getFontMetrics(fughetta);
            Note.Accidental[] accArray = Note.Accidental.values();
            baseAccidentalWidths = new float[accidentals.length];

            for (int i = 0; i < baseAccidentalWidths.length; i++) {
                baseAccidentalWidths[i] = accidentals[i].length() == 1 ? fm.stringWidth(accidentals[i]) : 0f;
            }

            for (int i = 0; i < baseAccidentalWidths.length; i++) {
                if (baseAccidentalWidths[i] == 0f && accidentals[i].length() == 2) {
                    baseAccidentalWidths[i] = baseAccidentalWidths[accArray[i].getComponent(0) + 1];
                    baseAccidentalWidths[i] += spaceBtwTwoAccidentals;
                    baseAccidentalWidths[i] += baseAccidentalWidths[accArray[i].getComponent(1) + 1];
                }
            }

            baseAccidentalParenthesisWidths = new float[accidentalParenthesis.length];
            beginParenthesisWidth = fm.stringWidth(beginParenthesis);
            endParenthesisWidth = fm.stringWidth(endParenthesis);

            for (int i = 0; i < baseAccidentalParenthesisWidths.length; i++) {
                baseAccidentalParenthesisWidths[i] = !accidentals[i].equals(accidentalParenthesis[i]) ? fm.stringWidth(accidentalParenthesis[i]) :
                        baseAccidentalWidths[i] + spaceBtwAccidentalAndParenthesis + beginParenthesisWidth +
                        endParenthesisWidth;
            }
        }
    }

    public static float getAccidentalWidth(Note note) {
        return note.isAccidentalInParenthesis() ? baseAccidentalParenthesisWidths[note.getAccidental().ordinal()] : baseAccidentalWidths[note.getAccidental().ordinal()];
    }

    public static float getAccidentalComponentWidth(Note note, int component) {
        if (baseAccidentalWidths == null) {
            getAccidentalWidth(note);
        }

        return baseAccidentalWidths[note.getAccidental().getComponent(component) + 1];
    }

    public void paintNote(Graphics2D g2, Note note, int line, boolean beamed, Color color) {
        AffineTransform origTransform = g2.getTransform();
        g2.translate(note.getXPos(), ms.getNoteYPos(note.getYPos(), line));
        AffineTransform translatedTransform = g2.getTransform();
        g2.setFont(fughetta);
        g2.setPaint(color);

        // draw the note
        NoteType nt = note.getNoteType();

        if (noteHead.containsKey(nt)) {
            paintSimpleNote(g2, note, beamed, note.isUpper(), false);

            // if the note is beamed, draw the lengthened stem
            if (beamed) {
                g2.setStroke(stemStroke);

                // decrease the length a little so the stem does not extend past an angled beam
                double offset = beamStroke.getLineWidth() * 0.1;

                if (note.isUpper()) {
                    note.a.stem.setLine(upperCrotchetStemX, upperStem.y1, upperCrotchetStemX,
                            upperStem.y2 - note.a.lengthening + offset);
                }
                else {
                    note.a.stem.setLine(lowerStem.x1, lowerStem.y1 + LOWER_STEM_CROTCHET_Y1_OFFSET, lowerStem.x2,
                            lowerStem.y2 - note.a.lengthening - offset);
                }

                g2.draw(note.a.stem);
            }
        }
        else {
            switch (nt) {
                case GRACE_QUAVER:
                    g2.scale(graceNoteScale, graceNoteScale);
                    g2.drawString(noteHead.get(NoteType.QUAVER), 0, 0);
                    g2.setStroke(stemStroke);

                    if (note.isUpper()) {
                        g2.translate(upperCrotchetStemX, 0);
                        g2.draw(upperStem);
                        g2.translate(-upperCrotchetStemX, 0);
                        g2.drawString(mainUpperFlag, (float) upperFlagX, (float) upperFlagY);
                        g2.setTransform(translatedTransform);
                        g2.setStroke(graceNoteSlashStroke);
                        g2.draw(graceNoteUpperSlash);
                    }
                    else {
                        g2.draw(lowerStem);
                        g2.drawString(mainLowerFlag, 0f, (float) lowerFlagY);
                        g2.setTransform(translatedTransform);
                        g2.setStroke(graceNoteSlashStroke);
                        g2.draw(graceNoteLowerSlash);
                    }
                    break;

                case GRACE_SEMIQUAVER:
                    g2.scale(graceNoteScale, graceNoteScale);
                    GraceSemiQuaver graceSemiQuaver = (GraceSemiQuaver) note;
                    g2.drawString(noteHead.get(NoteType.QUAVER), 0, (float) (
                            (graceSemiQuaver.getY0Pos() - graceSemiQuaver.getYPos()) * MusicSheet.HALF_LINE_DIST /
                            graceNoteScale));
                    g2.drawString(noteHead.get(NoteType.QUAVER), (float) (graceSemiQuaver.getX2DiffPos() / graceNoteScale), 0);
                    g2.setTransform(origTransform);
                    g2.translate(note.isUpper() ? -2.7 : -1.7, 0);
                    drawGraceSemiQuaverBeam(g2, note, line);
                    break;

                case REPEAT_LEFT:
                    drawRepeat(g2, repeatLeftThickX, 1d, true);
                    break;

                case REPEAT_RIGHT:
                    drawRepeat(g2, repeatRightThickX, -1f, true);
                    break;

                case REPEAT_LEFT_RIGHT:
                    drawRepeat(g2, repeatLeftRightThickX, 1f, true);
                    drawRepeat(g2, repeatLeftRightThickX, -1f, false);
                    break;

                case FINAL_DOUBLE_BARLINE:
                case DOUBLE_BARLINE:
                case SINGLE_BARLINE:
                    drawBarLine(g2, nt);
                    break;

                case BREATH_MARK:
                    g2.scale(0.0625, 0.0625);
                    g2.fill(breathMark);
                    break;
            }

            g2.setTransform(translatedTransform);
        }

        g2.setStroke(lineStroke);

        // draw the stave-longitude
        if (Math.abs(note.getYPos()) > 5 && note.getNoteType().drawStaveLongitude()) {
            for (int i = note.getYPos() + (note.getYPos() % 2 == 0 ? 0 : (note.getYPos() > 0 ? -1 : 1));
                 Math.abs(i) > 5;
                 i += note.getYPos() > 0 ? -2 : 2)
            {
                int y1 = (i - note.getYPos()) * (int) size / 8;
                float x2 = Note.HOT_SPOT.x + 8;

                if (note.getNoteType() == NoteType.SEMIBREVE) {
                    x2 += 3.4f;
                }
                else if (note.getNoteType() == NoteType.MINIM) {
                    x2 += 0.7f;
                }

                g2.draw(new Line2D.Float(Note.HOT_SPOT.x - 8, y1, x2, y1));
            }
        }

        // draw accidental
        int accidental = note.getAccidental().ordinal();

        if (accidental > 0) {
            float resizeFactor = 1;

            if (note.getNoteType().isGraceNote()) {
                g2.setFont(fughettaGrace);
                resizeFactor = graceAccidentalResizeFactor;
            }

            if (!note.isAccidentalInParenthesis() ||
                !accidentals[accidental].equals(accidentalParenthesis[accidental])) {
                drawSimpleAccidental(g2, note, -spaceBtwNoteAndAccidental - getAccidentalWidth(note), resizeFactor);
            }
            else {
                float xPos = -spaceBtwNoteAndAccidental - getAccidentalWidth(note);
                drawAntialiasedString(g2, beginParenthesis, xPos * resizeFactor, manualParenthesisY * resizeFactor);
                xPos += beginParenthesisWidth + spaceBtwAccidentalAndParenthesis;

                if (note.getAccidental().getComponent(1) == 1) {
                    xPos += 0.5f;
                }

                drawSimpleAccidental(g2, note, xPos, resizeFactor);
                drawAntialiasedString(g2, endParenthesis,
                        (-spaceBtwNoteAndAccidental - endParenthesisWidth) * resizeFactor,
                        manualParenthesisY * resizeFactor);
            }

            if (note.getNoteType().isGraceNote()) {
                g2.setFont(fughetta);
            }
        }

        g2.setTransform(origTransform);

        // draw the glissando
        if (note.getGlissando() != Note.NO_GLISSANDO) {
            drawGlissando(g2, ms.getComposition().getLine(line).getNoteIndex(note), note.getGlissando(), line);
        }

        // draw the articulations
        drawArticulation(g2, note, line);

        // draw the fermata
        if (note.isFermata()) {
            g2.translate(note.getXPos() - 5, ms.getNoteYPos(getFermataYPos(note), line) + 12);
            g2.scale(0.0625, 0.0625);
            g2.scale(0.9, 0.8);
            g2.fill(fermata);
            g2.setTransform(origTransform);
        }

        g2.setPaint(Color.black);
        g2.setTransform(origTransform);
    }

    private void drawSimpleAccidental(Graphics2D g2, Note note, float startX, float resizeFactor) {
        int accidental = note.getAccidental().ordinal();
        startX *= resizeFactor;
        String str = note.isAccidentalInParenthesis() ? accidentalParenthesis[accidental] : accidentals[accidental];

        if (str.length() == 1) {
            drawAntialiasedString(g2, str, startX, 0f);
        }
        else {
            drawAntialiasedString(g2, str.substring(0, 1), startX, 0f);
            drawAntialiasedString(g2, str.substring(1),
                    startX + (getAccidentalComponentWidth(note, 0) + spaceBtwTwoAccidentals) * resizeFactor, 0f);
        }
    }

    private void paintSimpleNote(Graphics2D g2, Note note, boolean beamed, boolean upper, boolean isTempoNote) {
        NoteType nt = note.getNoteType();

        // draw the note head
        String headStr = noteHead.get(nt);
        float noteHeadXPos = 0;

        if (nt.isNoteWithStem() && !upper) {
            noteHeadXPos -= stemStroke.getLineWidth() / 2;
        }

        g2.drawString(headStr, noteHeadXPos, 0f);

        // draw the stem
        if (!beamed) {
            if (nt.isNoteWithStem()) {
                double stemLengthOffset = 0;
                double stemYOffset = 0;

                if (isTempoNote) {
                    stemLengthOffset = -tempoStemShortening;
                }
                else {
                    if (nt == NoteType.SEMIQUAVER) {
                        stemLengthOffset = flagYLength - SEMIQUAVER_AND_DEMI_SEMIQUAVER_FLAG_COLLAPSE;
                    }
                    else if (nt == NoteType.DEMI_SEMIQUAVER) {
                        stemLengthOffset = 2 * flagYLength - SEMIQUAVER_AND_DEMI_SEMIQUAVER_FLAG_COLLAPSE;
                    }
                    else if (nt == NoteType.CROTCHET) {
                        stemYOffset = LOWER_STEM_CROTCHET_Y1_OFFSET;
                    }
                    else if (nt == NoteType.MINIM) {
                        stemYOffset = 0.1f;
                    }
                }

                if (upper) {
                    double stemX = nt == NoteType.MINIM ? upperMinimStemX : upperCrotchetStemX;
                    note.a.stem.setLine(stemX, upperStem.getY1(), stemX, upperStem.getY2() - stemLengthOffset);
                }
                else {
                    note.a.stem.setLine(0d, lowerStem.getY1() + stemYOffset, 0d, lowerStem.getY2() + stemLengthOffset);
                }

                g2.setStroke(stemStroke);
                g2.draw(note.a.stem);
            }

            // draw the flag(s)
            if (nt.isBeamable()) {
                float offset = nt == NoteType.QUAVER ? 0f : SEMIQUAVER_AND_DEMI_SEMIQUAVER_FLAG_COLLAPSE;

                if (upper) {
                    if (isTempoNote) {
                        g2.translate(0, tempoStemShortening);
                    }

                    g2.drawString(mainUpperFlag, (float) upperFlagX, (float) upperFlagY + offset);

                    if (nt != NoteType.QUAVER) {
                        g2.drawString(secondUpperFlag, (float) upperFlagX, (float) upperFlag2Y + offset);

                        if (nt != NoteType.SEMIQUAVER) {
                            g2.drawString(secondUpperFlag, (float) upperFlagX, (float) upperFlag3Y + offset);
                        }
                    }

                    if (isTempoNote) {
                        g2.translate(0, -tempoStemShortening);
                    }
                }
                else {
                    if (isTempoNote) {
                        g2.translate(0, -tempoStemShortening);
                    }

                    g2.drawString(mainLowerFlag, 0, (float) lowerFlagY - offset);

                    if (nt != NoteType.QUAVER) {
                        g2.drawString(secondLowerFlag, 0, (float) lowerFlag2Y - offset);

                        if (nt != NoteType.SEMIQUAVER) {
                            g2.drawString(secondLowerFlag, 0, (float) lowerFlag3Y - offset);
                        }
                    }

                    if (isTempoNote) {
                        g2.translate(0, tempoStemShortening);
                    }
                }
            }
        }

        // draw the dots
        AffineTransform at = g2.getTransform();

        if (note.getYPos() % 2 == 0) {
            g2.translate(0, -size / 8);
        }

        if (note.getNoteType() == NoteType.SEMIBREVE) {
            g2.translate(3.5, 0);
        }

        if (note.getNoteType() == NoteType.MINIM) {
            g2.translate(1.4, 0);
        }

        if (note.getNoteType().isBeamable() && !beamed && upper) {
            g2.translate(note.getNoteType() == NoteType.QUAVER ? 5 : 8, 0);
        }

        for (int i = 0; i < note.getDotted(); i++) {
            g2.fill(noteDots[i]);
        }

        g2.setTransform(at);
    }

    private void drawBarLine(Graphics2D g2, NoteType nt) {
        AffineTransform at = g2.getTransform();

        if (nt == NoteType.DOUBLE_BARLINE) {
            g2.setStroke(thinLineStroke);
            g2.draw(barLine);
            g2.translate(-barLineSpace - thinLineStroke.getLineWidth(), 0);
        }
        else if (nt == NoteType.FINAL_DOUBLE_BARLINE) {
            g2.setStroke(heavyLineStroke);
            g2.translate(-heavyLineStroke.getLineWidth() / 2f, 0);
            g2.draw(barLine);
            g2.translate(-barLineSpace - heavyLineStroke.getLineWidth() / 2f - thinLineStroke.getLineWidth() / 2f, 0);
        }

        g2.setStroke(thinLineStroke);
        g2.draw(barLine);
        g2.setTransform(at);
    }

    private void drawRepeat(Graphics2D g2, double thickStart, double direction, boolean drawThick) {
        AffineTransform at = g2.getTransform();
        g2.translate(thickStart, 0);

        // align repeat bottom with staff line
        double offset = lineStroke.getLineWidth() / 2;
        Line2D.Double line = new Line2D.Double(verticalLine.x1, verticalLine.y1, verticalLine.x2, verticalLine.y2 + offset);

        if (drawThick) {
            g2.setStroke(repeatHeavyStroke);
            g2.draw(line);
        }

        g2.setStroke(repeatThinStroke);
        g2.translate(repeatThickThinDiff * direction, 0);
        g2.draw(line);
        g2.translate(repeatThinCircleDiff * direction - repeatCircle1.width / 2, 0);
        g2.fill(repeatCircle1);
        g2.fill(repeatCircle2);
        g2.setTransform(at);
    }

    protected int drawLineBeginning(Graphics2D g2, Line line, int l) {
        g2.setFont(fughetta);

        // draw the treble clef
        int baseline = ms.getMiddleLine() + MusicSheet.LINE_DIST + l * ms.getRowHeight();
        drawAntialiasedString(g2, trebleClef, 5, baseline);
        FontMetrics metrics = g2.getFontMetrics();
        int maxY = baseline + metrics.getMaxDescent();

        // draw the leading sharps or flats
        if (line.getKeys() > 0) {
            int fsPos = ms.getLeadingKeysPos();
            int fs = line.getKeyType().ordinal();

            for (int i = 0; i < line.getKeys(); i++) {
                drawAntialiasedString(g2, accidentals[fs + 1], fsPos, ms.getNoteYPos(FLAT_SHARP_ORDER[fs][i % 7], l));
                fsPos += 8;
            }
        }

        return maxY;
    }

    protected void drawKeySignatureChange(Graphics2D g2, int l, KeyType[] keyTypes, int[] keys, int[] froms, boolean[] isNatural) {
        g2.setFont(fughetta);
        int fsPos = ms.getComposition().getLineWidth() - 5;

        for (int key : keys) {
            fsPos -= key * 8;
        }

        for (int kt = 0; kt < keyTypes.length; kt++) {
            if (keyTypes[kt] == null) {
                break;
            }

            int fs = keyTypes[kt].ordinal();

            for (int i = 0; i < keys[kt]; i++) {
                drawAntialiasedString(
                        g2,
                        accidentals[(isNatural[kt] ? 0 : fs) + 1],
                        fsPos,
                        ms.getNoteYPos(FLAT_SHARP_ORDER[fs][(i + froms[kt]) % 7], l)
                );

                fsPos += 8;
            }
        }
    }

    protected void drawTempoChangeNote(Graphics2D g2, Note tempoNote, int x, int y) {
        g2.setFont(fughetta);
        AffineTransform at = g2.getTransform();
        g2.translate(x, y - size * tempoChangeZoomY / 8.0);
        g2.scale(tempoChangeZoomX, tempoChangeZoomY);
        paintSimpleNote(g2, tempoNote, false, true, true);
        g2.setTransform(at);
    }
}
