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

    Created on Jun 24, 2006
*/
package songscribe.ui.musicsheetdrawer;

import songscribe.SongScribe;
import songscribe.data.CrescendoDiminuendoIntervalData;
import songscribe.data.Interval;
import songscribe.data.IntervalSet;
import songscribe.data.SlurData;
import songscribe.data.TupletIntervalData;
import songscribe.music.Annotation;
import songscribe.music.BeatChange;
import songscribe.music.Composition;
import songscribe.music.DurationArticulation;
import songscribe.music.ForceArticulation;
import songscribe.music.GraceSemiQuaver;
import songscribe.music.KeyType;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.music.Tempo;
import songscribe.ui.Constants;
import songscribe.ui.MusicSheet;
import songscribe.ui.Utilities;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public abstract class BaseMsDrawer {
    protected static final float size = 32;
    private static final double glissandoLength = size / 2.6666667;
    protected static final double INNER_BEAM_LENGTH = 11d;
    protected static final double INNER_BEAM_OFFSET = 6d;
    protected static final BasicStroke beamStroke = new BasicStroke(4.04f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    protected static final BasicStroke lineStroke = new BasicStroke(0.63f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    protected static final BasicStroke stemStroke = new BasicStroke(0.836f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    protected static final BasicStroke tenutoStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    protected static final Font fughetta;
    protected static final Font fughettaGrace;
    protected static final double tempoChangeZoomX = 0.8;
    protected static final double tempoChangeZoomY = 0.6;
    protected static final float spaceBtwNoteAndAccidental = 2.7f;//1.139f;
    protected static final float spaceBtwTwoAccidentals = 1.3f;
    protected static final float spaceBtwAccidentalAndParenthesis = 0f;
    protected static final float graceAccidentalResizeFactor = 0.65f;
    private static final BasicStroke dashStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {
            3.937f, 5.9055f
    }, 0f);
    private static final BasicStroke longDashStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke graceSemiQuaverStemStroke = new BasicStroke(0.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke graceSemiQuaverBeamStroke = new BasicStroke(2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final BasicStroke graceSemiQuaverStrikeStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final BasicStroke underScoreStroke = new BasicStroke(0.836f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final Ellipse2D.Float staccatoEllipse = new Ellipse2D.Float(0f, 0f, 3.5f, 3.5f);
    private static final Color selectionColor = new Color(254, 45, 125);
    private static final Font tupletFont;
    private static final Font fsEndingFont;
    private static final String GLISSANDO = "\uf07e";
    private static final String TRILL = "\uf0d9";
    private static final float longDashWidth = 7f;
    private static final NoteType[] BEAM_LEVELS = {
            NoteType.DEMI_SEMIQUAVER,
            NoteType.SEMIQUAVER,
            NoteType.QUAVER
    };
    protected final int[][] FLAT_SHARP_ORDER = {
            { },
            { 0, -3, 1, -2, 2, -1, 3 },
            { -4, -1, -5, -2, 1, -3, 0 }
    };
    protected double crotchetWidth;
    protected double beamX1Correction, beamX2Correction;
    protected MusicSheet ms;

    static {
        String fontName = "Fughetta";

        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(SongScribe.basePath + "/fonts/" + fontName + ".ttf"));
            fughetta = font.deriveFont(size);
            fughettaGrace = font.deriveFont(size * graceAccidentalResizeFactor);

            fontName = "TupletNumbers";
            font = Font.createFont(Font.TRUETYPE_FONT, new File(SongScribe.basePath + "/fonts/" + fontName + ".ttf"));
            tupletFont = font.deriveFont(13f);
            fsEndingFont = tupletFont;
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot load a required font (" + fontName + ")", e);
        }
    }

    // fields for the key signature change
    private KeyType[] keyTypes = new KeyType[2];
    private int[] keys = new int[2];
    private int[] froms = new int[2];
    private boolean[] isNaturals = new boolean[2];
    private int height = 0;
    private Font oldGeneralFont, annotationFont;
    private int lyricsMaxY = 0;
    private int lyricsMaxDescent;

    private boolean drawStringWithShapes = true;

    public BaseMsDrawer(MusicSheet ms) {
        this.ms = ms;
    }

    public void drawMusicSheet(Graphics2D g2, boolean drawEditingComponents, double scale) {
        height = 0;
        FughettaDrawer.calculateAccidentalWidths(g2);
        Composition composition = ms.getComposition();

        if (scale != 1d) {
            g2.scale(scale, scale);
        }

        if (!drawEditingComponents) {
            g2.translate(0, -ms.getStartY());
        }

        g2.setPaint(Color.black);
        drawTitle(g2, composition);
        drawRightInfo(g2, composition);

        g2.setFont(composition.getLyricsFont());
        FontMetrics lyricsMetrics = g2.getFontMetrics(composition.getLyricsFont());
        lyricsMaxDescent = lyricsMetrics.getMaxDescent();

        drawUnderLyrics(g2, composition);
        drawTempo(g2, composition);
        drawComposition(g2, drawEditingComponents, composition);

        if (lyricsMaxY != 0) {
            height = lyricsMaxY;
        }
    }

    private void drawComposition(Graphics2D g2, boolean drawEditingComponents, Composition composition) {
        for (int lineIndex = 0; lineIndex < composition.lineCount(); lineIndex++) {
            Boolean lastLine = lineIndex == composition.lineCount() - 1;
            drawStaffLines(g2, composition, lineIndex);

            // drawing the treble clef and the leading keys
            Line line = composition.getLine(lineIndex);
            int maxY = drawLineBeginning(g2, line, lineIndex);

            if (lyricsMaxY == 0) {
                height = maxY;
            }

            // draw the notes
            int lyricsDrawn = 0;

            for (int noteIndex = 0; noteIndex < line.noteCount(); noteIndex++) {
                Note note = line.getNote(noteIndex);

                if (note.getTempoChange() != null) {
                    drawTempoChange(g2, note.getTempoChange(), lineIndex, noteIndex);
                }

                if (note.getBeatChange() != null) {
                    drawBeatChange(g2, lineIndex, note);
                }

                boolean beamed = line.getBeamings().findInterval(noteIndex) != null && note.getNoteType() != NoteType.GRACE_QUAVER;
                paintNote(g2, note, lineIndex, beamed, ms.isNoteSelected(noteIndex, lineIndex) && drawEditingComponents ? selectionColor : Color.black);
                drawTie(g2, lineIndex, line, noteIndex, note);
                lyricsDrawn = drawLyrics(g2, composition, lineIndex, lastLine, line, lyricsDrawn, noteIndex, note);
                drawAnnotation(g2, lineIndex, lastLine, note);
                drawTrill(g2, lineIndex, line, noteIndex, note);
            }

            drawSlur(g2, lineIndex, line);
            drawBeamsOnLine(g2, lineIndex, line);
            drawTuplets(g2, lineIndex, line);
            drawKeyChanges(g2, composition, lineIndex, line);
            drawEndings(g2, lineIndex, line);
            drawCrescendos(g2, lineIndex, line);
            drawDiminuendos(g2, lineIndex, line);
        }
    }

    private void drawDiminuendos(Graphics2D g2, int lineIndex, Line line) {
        g2.setStroke(lineStroke);

        for (ListIterator<Interval> li = line.getDiminuendo().listIterator(); li.hasNext(); ) {
            Interval interval = li.next();
            Note startNote = line.getNote(interval.getA());
            Note endNote = line.getNote(interval.getB());
            int x1 = startNote.getXPos() + CrescendoDiminuendoIntervalData.getX1Shift(interval);
            int yShift = CrescendoDiminuendoIntervalData.getYShift(interval);
            int x2 = endNote.getXPos() + (int) crotchetWidth + CrescendoDiminuendoIntervalData.getX2Shift(interval);

            g2.drawLine(
                    x1,
                    ms.getNoteYPos(5, lineIndex) + yShift,
                    x2,
                    ms.getNoteYPos(6, lineIndex) + yShift);
            g2.drawLine(
                    x1,
                    ms.getNoteYPos(7, lineIndex) + yShift,
                    x2,
                    ms.getNoteYPos(6, lineIndex) + yShift);
        }
    }

    private void drawCrescendos(Graphics2D g2, int lineIndex, Line line) {
        g2.setStroke(lineStroke);

        for (ListIterator<Interval> li = line.getCrescendo().listIterator(); li.hasNext(); ) {
            Interval interval = li.next();
            Note startNote = line.getNote(interval.getA());
            Note endNote = line.getNote(interval.getB());
            int x1 = startNote.getXPos() + CrescendoDiminuendoIntervalData.getX1Shift(interval);
            int yShift = CrescendoDiminuendoIntervalData.getYShift(interval);
            int x2 = endNote.getXPos() + (int) crotchetWidth + CrescendoDiminuendoIntervalData.getX2Shift(interval);

            g2.drawLine(
                    x1,
                    ms.getNoteYPos(6, lineIndex) + yShift,
                    x2,
                    ms.getNoteYPos(5, lineIndex) + yShift);
            g2.drawLine(
                    x1,
                    ms.getNoteYPos(6, lineIndex) + yShift,
                    x2,
                    ms.getNoteYPos(7, lineIndex) + yShift);
        }
    }

    abstract protected void drawEndings(Graphics2D g2, int lineIndex, Line line);

    private void drawKeyChanges(Graphics2D g2, Composition composition, int lineIndex, Line line) {
        if (lineIndex + 1 >= composition.lineCount()) {
            return;
        }

        Line nextLine = composition.getLine(lineIndex + 1);

        if (nextLine.getKeys() != line.getKeys() || nextLine.getKeyType() != line.getKeyType()) {
            if (nextLine.getKeyType() == line.getKeyType()) {
                keyTypes[0] = nextLine.getKeyType();
                keys[0] = nextLine.getKeys();
                froms[0] = 0;
                isNaturals[0] = false;

                if (nextLine.getKeys() > line.getKeys()) {
                    keyTypes[1] = null;
                    keys[1] = 0;
                    froms[1] = 0;
                    isNaturals[1] = false;
                }
                else {
                    keyTypes[1] = line.getKeyType();
                    keys[1] = line.getKeys() - nextLine.getKeys();
                    froms[1] = nextLine.getKeys();
                    isNaturals[1] = true;
                }
            }
            else {
                keyTypes[0] = line.getKeyType();
                keys[0] = line.getKeys();
                froms[0] = 0;
                isNaturals[0] = true;
                keyTypes[1] = nextLine.getKeyType();
                keys[1] = nextLine.getKeys();
                froms[1] = 0;
                isNaturals[1] = false;
            }

            drawKeySignatureChange(g2, lineIndex, keyTypes, keys, froms, isNaturals);
        }
    }

    private void drawTuplets(Graphics2D g2, int lineIndex, Line line) {
        for (ListIterator<Interval> li = line.getTuplets().listIterator(); li.hasNext(); ) {
            Interval interval = li.next();
            boolean odd = (interval.getB() - interval.getA() + 1) % 2 == 1;
            Note firstNote = line.getNote(interval.getA());
            int upper = firstNote.isUpper() ? -1 : 0;
            int lx = firstNote.getXPos() + (int) crotchetWidth;
            int ly = (ms.getNoteYPos(firstNote.getYPos(), lineIndex) - Note.HOT_SPOT.y) + (upper * firstNote.a.lengthening);
            ly -= 5;

            int cx;

            if (odd) {
                Note centerNote = line.getNote((interval.getB() - interval.getA()) / 2 + interval.getA());
                cx = centerNote.getXPos() + (int) crotchetWidth;
            }
            else {
                Note cn1 = line.getNote((interval.getB() - interval.getA()) / 2 + interval.getA());
                Note cn2 = line.getNote((interval.getB() - interval.getA()) / 2 + interval.getA() + 1);
                cx = (cn2.getXPos() - cn1.getXPos()) / 2 + cn1.getXPos() + (int) crotchetWidth;
            }

            Note lastNote = line.getNote(interval.getB());
            int rx = lastNote.getXPos() + (int) crotchetWidth;
            int ry = ms.getNoteYPos(lastNote.getYPos(), lineIndex) - Note.HOT_SPOT.y + upper * lastNote.a.lengthening;
            ry -= 5;

            if (!firstNote.isUpper()) {
                lx -= (int) crotchetWidth / 2;
                ly += Note.HOT_SPOT.y - 3;
                cx -= (int) crotchetWidth / 2;
                rx -= (int) crotchetWidth / 2;
                ry += Note.HOT_SPOT.y - 3;
            }

            if (TupletIntervalData.isVerticalAdjusted(interval)) {
                ly += TupletIntervalData.getVerticalPosition(interval);
                ry += TupletIntervalData.getVerticalPosition(interval);
            }

            g2.setStroke(lineStroke);
            TupletCalc tc = new TupletCalc(lx, ly, rx, ry);

            g2.draw(new QuadCurve2D.Float(lx, ly,
                    (float) (cx - lx) / 4 + lx, tc.getRate((cx - lx) / 4 + lx) - 10, cx - 7, tc.getRate(cx - 7) - 8));
            g2.draw(new QuadCurve2D.Float(
                    cx + 7, tc.getRate(cx + 7) - 8, (float) (rx - cx) * 3 / 4 + cx, tc.getRate((rx - cx) * 3 / 4 + cx) - 10, rx, ry));
            g2.setFont(tupletFont);
            drawString(g2, Integer.toString(TupletIntervalData.getGrade(interval)), cx - 3, tc.getRate(cx - 3) - 5);

            /*g2.setColor(Color.red);
            g2.fill(new Rectangle2D.Double(triplet.getX1()-1, triplet.getY1()-1, 2, 2));
            g2.fill(new Rectangle2D.Double(triplet.getX2()-1, triplet.getY2()-1, 2, 2));
            g2.setColor(Color.orange);
            g2.fill(new Rectangle2D.Double(triplet.getCtrlX1()-1, triplet.getCtrlY1()-1, 2, 2));
            g2.fill(new Rectangle2D.Double(triplet.getCtrlX2()-1, triplet.getCtrlY2()-1, 2, 2));
            g2.setColor(Color.black);*/
        }
    }

    private void drawBeamsOnLine(Graphics2D g2, int lineIndex, Line line) {
        for (ListIterator<Interval> li = line.getBeamings().listIterator(); li.hasNext(); ) {
            Interval interval = li.next();
            Line2D.Double beamLine;
            Note firstNote = line.getNote(interval.getA());
            Note lastNote = line.getNote(interval.getB());

            // TODO: maybe i should just pass the first/last note to drawBeams, that should be sufficient
            if (firstNote.isUpper()) {
                beamLine = new Line2D.Double(
                        firstNote.getXPos() + crotchetWidth,
                        ms.getNoteYPos(firstNote.getYPos(), lineIndex) - Note.HOT_SPOT.y - firstNote.a.lengthening,
                        lastNote.getXPos() + crotchetWidth, ms.getNoteYPos(lastNote.getYPos(), lineIndex) - Note.HOT_SPOT.y - lastNote.a.lengthening);
            }
            else {
                beamLine = new Line2D.Double(firstNote.getXPos(),
                        ms.getNoteYPos(firstNote.getYPos(), lineIndex) + Note.HOT_SPOT.y - firstNote.a.lengthening,
                        lastNote.getXPos() + 1, ms.getNoteYPos(lastNote.getYPos(), lineIndex) + Note.HOT_SPOT.y - lastNote.a.lengthening);
            }

            //Shape clip = g2.getClip();
            g2.setStroke(beamStroke);
            beamLine.setLine(
                    beamLine.x1 - 10,
                    beamLine.y1 + 10 * (beamLine.y1 - beamLine.y2) / (beamLine.x2 - beamLine.x1),
                    beamLine.x2 + 10, beamLine.y2 - 10 * (beamLine.y1 - beamLine.y2) / (beamLine.x2 - beamLine.x1));
            drawBeams(g2, BEAM_LEVELS.length - 1, line, lineIndex, interval.getA(), interval.getB());
            //g2.setClip(clip);
        }
    }

    private void drawSlur(Graphics2D g2, int lineIndex, Line line) {
        for (ListIterator<Interval> li = line.getSlurs().listIterator(); li.hasNext(); ) {
            Interval interval = li.next();
            Note firstNote = line.getNote(interval.getA());
            Note lastNote = line.getNote(interval.getB());
            SlurData slurData;

            // apply default values;
            if (interval.getData() == null) {
                boolean slurUpper = firstNote.isUpper();
                int xPos1 = getHalfNoteWidthForTie(firstNote) + 2;
                int xPos2 = getHalfNoteWidthForTie(lastNote) - 3;

                if (firstNote.isUpper() != lastNote.isUpper() && firstNote.isUpper()) {
                    slurUpper = false;
                    xPos1 += 7;
                    xPos2 -= 5;
                }

                int yPos1 = (slurUpper ? MusicSheet.LINE_DIST / 2 + 2 : -MusicSheet.LINE_DIST / 2 - 2);
                int yPos2 = (slurUpper ? MusicSheet.LINE_DIST / 2 + 2 : -MusicSheet.LINE_DIST / 2 - 2);
                int ctrlY = slurUpper ? 16 : -18;
                slurData = new SlurData(xPos1, xPos2, yPos1, yPos2, ctrlY);
                interval.setData(slurData.toString());
            }
            else {
                slurData = new SlurData(interval.getData());
            }

            g2.setStroke(lineStroke);
            GeneralPath tie = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
            int xPos1 = firstNote.getXPos() + slurData.getXPos1();
            int xPos2 = lastNote.getXPos() + slurData.getXPos2();
            int yPos1 = ms.getNoteYPos(firstNote.getYPos(), lineIndex) + slurData.getYPos1();
            int yPos2 = ms.getNoteYPos(lastNote.getYPos(), lineIndex) + slurData.getYPos2();
            int ctrlY = (ms.getNoteYPos(firstNote.getYPos(), lineIndex) + ms.getNoteYPos(lastNote.getYPos(), lineIndex)) / 2 + slurData.getCtrlY();
            int gap = xPos2 - xPos1;
            tie.moveTo(xPos1, yPos1);
            tie.quadTo(xPos1 + gap / 2, ctrlY, xPos1 + gap, yPos2);
            tie.quadTo(xPos1 + gap / 2, ctrlY + 2, xPos1, yPos1);
            tie.closePath();
            g2.draw(tie);
            g2.fill(tie);
            /*g2.setPaint(Color.red);
            g2.drawRect(xPos, yPos, 1, 1);
            g2.setPaint(Color.green);
            g2.drawRect(note.getXPos(), yPos, 1, 1);
            g2.drawRect(note.getXPos()+note.getRealUpNoteRect().width, yPos, 1, 1);
            g2.setPaint(Color.black);*/
        }
    }

    private void drawTrill(Graphics2D g2, int lineIndex, Line line, int noteIndex, Note note) {
        if (note.isTrill() && (noteIndex == 0 || !line.getNote(noteIndex - 1).isTrill())) {
            int trillEnd = noteIndex + 1;

            while (trillEnd < line.noteCount() && line.getNote(trillEnd).isTrill()) {
                trillEnd++;
            }

            trillEnd--;
            int x = note.getXPos();
            int y = ms.getNoteYPos(0, lineIndex) + line.getTrillYPos();
            g2.setFont(fughetta);
            drawString(g2, TRILL, x, y);

            if (noteIndex < trillEnd) {
                drawGlissando(g2, x + 18, y - 3, (int) Math.round(line.getNote(trillEnd).getXPos() + crotchetWidth), y - 3);
            }
        }
    }

    private void drawAnnotation(Graphics2D g2, int lineIndex, Boolean lastLine, Note note) {
        if (note.getAnnotation() != null) {
            g2.setFont(getAnnotationFont());
            int y = getAnnotationYPos(lineIndex, note);
            drawAntialiasedString(g2, note.getAnnotation().getAnnotation(), getAnnotationXPos(g2, note), y);

            if (lastLine && lyricsMaxY == 0) {
                y += g2.getFontMetrics().getMaxDescent();

                if (y > height) {
                    height = y;
                }
            }
        }
    }

    private int drawLyrics(Graphics2D g2, Composition composition, int lineIndex, Boolean lastLine, Line line, int lyricsDrawn, int noteIndex, Note note) {
        int lyricsY = ms.getNoteYPos(0, lineIndex) + line.getLyricsYPos();

        if (lastLine && lyricsMaxY == 0) {
            height = lyricsY + lyricsMaxDescent;
        }

        int dashY = lyricsY - composition.getLyricsFont().getSize() / 4;
        g2.setFont(composition.getLyricsFont());
        int syllableWidth = 0;

        if (note.a.syllable != null && !note.a.syllable.equals(Constants.UNDERSCORE)) {
            syllableWidth = g2.getFontMetrics().stringWidth(note.a.syllable);
            int lyricsX = note.getXPos() + Note.HOT_SPOT.x - syllableWidth / 2 + note.getSyllableMovement();
            drawAntialiasedString(g2, note.a.syllable, lyricsX, lyricsY);

            if (noteIndex == 0 && line.beginRelation == Note.SyllableRelation.ONE_DASH) {
                g2.setStroke(longDashStroke);
                g2.draw(new Line2D.Float(lyricsX - longDashWidth - 10, dashY, lyricsX - 10, dashY));
            }
        }

        if (lyricsDrawn <= noteIndex &&
            (note.a.syllableRelation != Note.SyllableRelation.NO || noteIndex == 0 && line.beginRelation == Note.SyllableRelation.EXTENDER
            )) {
            Note.SyllableRelation relation =
                    note.a.syllableRelation != Note.SyllableRelation.NO ? note.a.syllableRelation : line.beginRelation;
            int c;

            if (relation == Note.SyllableRelation.DASH || relation == Note.SyllableRelation.ONE_DASH) {
                for (c = noteIndex + 1;
                     c < line.noteCount() && (line.getNote(c).a.syllable.equals(Constants.UNDERSCORE) || line.getNote(c).a.syllable.isEmpty());
                    ) {
                    c++;
                }
            }
            else {
                for (c = noteIndex;
                     c < line.noteCount() && (line.getNote(c).a.syllableRelation == relation || line.getNote(c).a.syllable.isEmpty());
                    ) {
                    c++;
                }
            }

            lyricsDrawn = c;

            int startX = noteIndex == 0 && line.beginRelation == Note.SyllableRelation.EXTENDER ?
                    note.getXPos() - 10 : note.getXPos() + Note.HOT_SPOT.x + syllableWidth / 2 + note.getSyllableMovement() + 2;
            int endX;

            if (c ==
                line.noteCount()/* || c==line.noteCount()-1 && l+1<composition.lineCount() && composition.getLine(l+1).beginRelation!=Note.SyllableRelation.NO*/) {
                endX = relation == Note.SyllableRelation.ONE_DASH ? startX + (int) (longDashWidth * 2f) : composition.getLineWidth();
            }
            else {
                if (relation == Note.SyllableRelation.EXTENDER) {
                    endX = line.getNote(c).getXPos() + 12;
                }
                else if (relation == Note.SyllableRelation.ONE_DASH && line.getNote(c).a.syllable.isEmpty()) {
                    endX = startX + (int) (longDashWidth * 2f);
                }
                else {
                    endX = line.getNote(c).getXPos() + Note.HOT_SPOT.x - g2.getFontMetrics().stringWidth(line.getNote(c).a.syllable) / 2 +
                           line.getNote(c).getSyllableMovement() - 2;
                }
            }

            if (relation == Note.SyllableRelation.DASH) {
                g2.setStroke(dashStroke);
                float dashPhase = dashStroke.getDashArray()[0] + dashStroke.getDashArray()[1];
                int length = Math.round((float) Math.floor((endX - startX - dashStroke.getDashArray()[1]) / dashPhase) * dashPhase +
                                        dashStroke.getDashArray()[0]);
                int gap = (endX - startX - length) / 2;
                drawWithEmptySyllablesExclusion(g2, startX + gap, dashY, endX - gap, dashY, line, noteIndex, c + 1);
            }
            else if (relation == Note.SyllableRelation.EXTENDER) {
                g2.setStroke(underScoreStroke);
                drawWithEmptySyllablesExclusion(g2, startX, lyricsY, endX, lyricsY, line, noteIndex, c + 1);
            }
            else if (relation == Note.SyllableRelation.ONE_DASH) {
                g2.setStroke(longDashStroke);
                note.a.longDashPosition = (endX - startX) / 2f + startX;
                float centerX = note.getSyllableRelationMovement() == 0 ? note.a.longDashPosition :
                        note.getXPos() + note.getSyllableRelationMovement();
                g2.draw(new Line2D.Float(centerX - longDashWidth / 2f, dashY, centerX + longDashWidth / 2f, dashY));
            }
        }

        return lyricsDrawn;
    }

    private void drawTie(Graphics2D g2, int lineIndex, Line line, int noteIndex, Note note) {
        Interval tieVal = line.getTies().findInterval(noteIndex);

        if (tieVal != null && noteIndex != tieVal.getB()) {
            boolean tieUpper = note.isUpper();
            int xPos = note.getXPos() + getHalfNoteWidthForTie(note) + 2;
            int gap = line.getNote(noteIndex + 1).getXPos() + getHalfNoteWidthForTie(line.getNote(noteIndex + 1)) - xPos - 3;

            if (note.isUpper() != line.getNote(noteIndex + 1).isUpper() && note.isUpper()) {
                tieUpper = false;
                xPos += 7;
                gap -= 5;
            }

            int yPos = ms.getNoteYPos(note.getYPos(), lineIndex) + (tieUpper ? MusicSheet.LINE_DIST / 2 + 2 : -MusicSheet.LINE_DIST / 2 - 2);
            g2.setStroke(lineStroke);
            GeneralPath tie = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
            tie.moveTo(xPos, yPos);
            tie.quadTo(xPos + gap / 2, yPos + (tieUpper ? 6 : -6), xPos + gap, yPos);
            tie.quadTo(xPos + gap / 2, yPos + (tieUpper ? 8 : -8), xPos, yPos);
            tie.closePath();
            g2.draw(tie);
            g2.fill(tie);
            /*g2.setPaint(Color.red);
            g2.drawRect(xPos, yPos, 1, 1);
            g2.setPaint(Color.green);
            g2.drawRect(note.getXPos(), yPos, 1, 1);
            g2.drawRect(note.getXPos()+note.getRealUpNoteRect().width, yPos, 1, 1);
            g2.setPaint(Color.black);*/
        }
    }

    private void drawStaffLines(Graphics2D g2, Composition composition, int lineIndex) {
        g2.setPaint(lineIndex != ms.getSelectedLine() ? Color.black : selectionColor);
        g2.setStroke(lineStroke);

        // draw the lines
        for (int i = -2; i <= 2; i++) {
            g2.drawLine(0, ms.getNoteYPos(i * 2, lineIndex), composition.getLineWidth(), ms.getNoteYPos(i * 2, lineIndex));
        }

        g2.drawLine(0, ms.getNoteYPos(-4, lineIndex), 0, ms.getNoteYPos(4, lineIndex));

        g2.setPaint(Color.black);
    }

    private void drawTempo(Graphics2D g2, Composition composition) {
        if (composition.getLine(0).noteCount() > 0) {
            drawTempoChange(g2, composition.getTempo(), 0, 0);
        }
    }

    private void drawUnderLyrics(Graphics2D g2, Composition composition) {
        if (composition.getUnderLyrics().length() > 0) {
            lyricsMaxY = drawTextBox(g2, composition.getUnderLyrics(), ms.getUnderLyricsYPos(), Component.CENTER_ALIGNMENT, 0);
        }

        if (composition.getTranslatedLyrics().length() > 0) {
            lyricsMaxY = drawTextBox(g2, composition.getTranslatedLyrics(), ms.getUnderLyricsYPos() +
                                                                            (Utilities.lineCount(composition.getUnderLyrics()) + 1) *
                                                                            g2.getFontMetrics().getAscent(), Component.CENTER_ALIGNMENT, 0);
        }
    }

    private void drawRightInfo(Graphics2D g2, Composition composition) {
        g2.setFont(composition.getGeneralFont());

        if (composition.getRightInfo().length() > 0) {
            drawTextBox(g2, composition.getRightInfo(),
                    composition.getRightInfoStartY() + composition.getGeneralFont().getSize(), Component.RIGHT_ALIGNMENT, -20);
        }
    }

    private void drawTitle(Graphics2D g2, Composition composition) {
        if (composition.getSongTitle().length() > 0) {
            g2.setFont(composition.getSongTitleFont());
            int i = 0;

            for (String titleLine : composition.getSongTitle().split("\n")) {
                if (i == 0 && composition.getNumber().length() > 0) {
                    titleLine = composition.getNumber() + ". " + titleLine;
                }

                int titleWidth = g2.getFontMetrics().stringWidth(titleLine);
                drawAntialiasedString(g2, titleLine,
                        (composition.getLineWidth() - titleWidth) / 2, (i + 1) * composition.getSongTitleFont().getSize());
                i++;
            }
        }
    }

    private void drawWithEmptySyllablesExclusion(Graphics2D g2, int x1, int y1, int x2, int y2, Line line, int startIndex, int endIndex) {
        ArrayList<Integer> emptySyllables = new ArrayList<Integer>();
        endIndex = Math.min(line.noteCount(), endIndex);

        for (int i = startIndex; i < endIndex; i++) {
            if (line.getNote(i).a.syllable.isEmpty()) {
                emptySyllables.add(i);
            }
        }

        if (emptySyllables.isEmpty()) {
            g2.drawLine(x1, y1, x2, y2);
        }
        else {
            IntervalSet intervalSet = new IntervalSet();
            intervalSet.addInterval(startIndex, endIndex);

            for (Integer i : emptySyllables) {
                intervalSet.removeInterval(i, i + 1);
            }

            for (ListIterator<Interval> intervalListIterator = intervalSet.listIterator(); intervalListIterator.hasNext(); ) {
                Interval interval = intervalListIterator.next();
                int drawX1 = interval.getA() == startIndex ? x1 : line.getNote(interval.getA()).getXPos();
                int drawX2 = interval.getB() == endIndex ? x2 : line.getNote(interval.getB() - 1).getXPos() + 12;
                g2.drawLine(drawX1, y1, drawX2, y2);
            }
        }
    }

    public int getAnnotationYPos(int l, Note note) {
        return ms.getNoteYPos(0, l) + note.getAnnotation().getYPos();
    }

    public double getAnnotationXPos(Graphics2D g2, Note note) {
        Annotation a = note.getAnnotation();
        double xPos = note.getXPos() + crotchetWidth / 2;

        if (a.getXAlignment() == Component.CENTER_ALIGNMENT) {
            xPos -= g2.getFontMetrics(getAnnotationFont()).stringWidth(a.getAnnotation()) / 2;
        }
        else if (a.getXAlignment() == Component.RIGHT_ALIGNMENT) {
            xPos -= g2.getFontMetrics(getAnnotationFont()).stringWidth(a.getAnnotation());
        }

        return xPos;
    }

    private Font getAnnotationFont() {
        if (oldGeneralFont != ms.getComposition().getGeneralFont()) {
            oldGeneralFont = ms.getComposition().getGeneralFont();
            annotationFont = Utilities.deriveFont(oldGeneralFont, Font.ITALIC, oldGeneralFont.getSize());
        }

        return annotationFont;
    }

    private int getHalfNoteWidthForTie(Note note) {
        if (note.getNoteType() == NoteType.SEMIBREVE || note.getNoteType() == NoteType.MINIM) {
            return note.getRealUpNoteRect().width / 2;
        }
        else {
            return (int) Math.round(crotchetWidth / 2);
        }
    }

    private boolean isNoteTypeInLevel(Line line, int noteIndex, int level) {
        NoteType nt = line.getNote(noteIndex).getNoteType();

        if (!nt.isGraceNote()) {
            for (int i = 0; i < BEAM_LEVELS.length; i++) {
                if (BEAM_LEVELS[i] == nt) {
                    return i <= level;
                }
            }

            return false;
        }
        else {
            int begin = noteIndex - 1, end = noteIndex + 1;

            while (begin > 0 && line.getNote(begin).getNoteType().isGraceNote()) {
                begin--;
            }

            while (end < line.noteCount() && line.getNote(end).getNoteType().isGraceNote()) {
                end++;
            }

            return begin >= 0 && isNoteTypeInLevel(line, begin, level) && end < line.noteCount() &&
                   isNoteTypeInLevel(line, end, level);
        }
    }

    private void drawBeams(Graphics2D g2, int level, Line line, int lineIndex, int beginIndex, int endIndex) {
        Point outerNotes = new Point(beginIndex, endIndex);
        doDrawBeams(g2, level, line, lineIndex, outerNotes, beginIndex, endIndex, beginIndex, endIndex, false, 0);
    }

    private void doDrawBeams(Graphics2D g2, int level, Line line, int lineIndex, Point outerNotes, int beginIndex, int endIndex, int prevBeginIndex, int prevEndIndex, boolean isPrevLeftOriented, int recursionLevel) {
        if (level == -1) {
            return;
        }

        Note beginNote = line.getNote(beginIndex);
        boolean isUpper = beginNote.isUpper();
        boolean leftOriented = false;

        // half beam
        if (beginIndex == endIndex) {
            if (beginNote.getNoteType().isGraceNote()) {
                return;
            }

            // NOTE: left oriented actually means the beam is attached to the right hand note stem
            leftOriented = prevBeginIndex == prevEndIndex ? isPrevLeftOriented :
                    (beginIndex == prevBeginIndex) == beginNote.isInvertFractionBeamOrientation();

            int begin, end;

            if (leftOriented) {
                begin = outerNotes.x;
                end = endIndex;
            }
            else {
                begin = beginIndex;
                end = outerNotes.y;
            }

            BeamType type = leftOriented ? BeamType.ATTACH_RIGHT : BeamType.ATTACH_LEFT;
            drawBeam(g2, line, lineIndex, begin, end, isUpper, type, recursionLevel);
        }

        // top beam
        else {
            drawBeam(g2, line, lineIndex, beginIndex, endIndex, isUpper, BeamType.FULL, recursionLevel);
        }

        // sub-beams
        --level;
        int startSubBeam = -1;

        for (int i = beginIndex; i <= endIndex + 1; i++) {
            if (i <= endIndex && isNoteTypeInLevel(line, i, level)) {
                if (startSubBeam == -1) {
                    startSubBeam = i;
                }
            }
            else if (startSubBeam != -1) {
                doDrawBeams(g2, level, line, lineIndex, outerNotes, startSubBeam, i - 1, beginIndex, endIndex, leftOriented, recursionLevel + 1);
                startSubBeam = -1;
            }
        }
    }

    private void drawBeam(Graphics2D g2, Line line, int lineIndex, int beginIndex, int endIndex, boolean isUpper, BeamType type, int recursionLevel) {
        /*
            We draw beams using a Path2D. Because of rendering inaccuracies with antialiasing,
            we first stroke the path with the stem stroke so that the antialiasing at the edge
            will match the stem antialiasing, then we fill the path.
        */
        Note beginNote = line.getNote(beginIndex);
        Note endNote = line.getNote(endIndex);
        Line2D.Double firstStem = beginNote.a.stem;
        Line2D.Double lastStem = endNote.a.stem;

        // Reduce the height top and bottom by half the stem stroke width since we stroke the path
        double halfStemWidth = stemStroke.getLineWidth() / 2;
        double halfBeamWidth = (beamStroke.getLineWidth() / 2) - halfStemWidth;

        double noteX = beginNote.getXPos();
        double firstX = firstStem.x1 + noteX;

        double yOffset = INNER_BEAM_OFFSET * recursionLevel * (isUpper ? 1 : -1);
        double noteY = ms.getNoteYPos(beginNote.getYPos(), lineIndex) + yOffset;
        double firstY = noteY + firstStem.y1 - beginNote.a.lengthening;

        if (isUpper) {
            firstY += -Note.HOT_SPOT.y + halfBeamWidth;
        }
        else {
            firstY += Note.HOT_SPOT.y - halfBeamWidth;
        }

        // bottom left
        Path2D.Double beam = new Path2D.Double(Path2D.WIND_NON_ZERO, 4);
        beam.moveTo(firstX, firstY + halfBeamWidth);
        // top left
        beam.lineTo(firstX, firstY - halfBeamWidth);

        noteX = endNote.getXPos();
        noteY = ms.getNoteYPos(endNote.getYPos(), lineIndex) + yOffset;
        double lastX = lastStem.x1 + noteX;
        double lastY = noteY + lastStem.y1 - endNote.a.lengthening;

        if (isUpper) {
            lastY += -Note.HOT_SPOT.y + halfBeamWidth;
        }
        else {
            lastY += Note.HOT_SPOT.y - halfBeamWidth;
        }

        // top right
        beam.lineTo(lastX, lastY - halfBeamWidth);
        // bottom right
        beam.lineTo(lastX, lastY + halfBeamWidth);
        beam.closePath();

        Shape oldClip = null;
        Rectangle2D clip = null;

        if (type != BeamType.FULL) {
            // If drawing an inner beam, clip the beam to the inner beam length,
            // leaving slop on the stem side to account for inaccuracies when printing.
            clip = beam.getBounds2D();
            double clipSlop = 2d;
            double x1;

            if (type == BeamType.ATTACH_LEFT) {
                x1 = firstX - clipSlop;
            }
            else {  // type == BeamType.ATTACH_RIGHT
                x1 = lastX - INNER_BEAM_LENGTH;
            }

            clip.setRect(x1, clip.getMinY() - clipSlop, INNER_BEAM_LENGTH + clipSlop, clip.getHeight() + (clipSlop * 2));
            oldClip = g2.getClip();
            g2.setClip(clip);
        }

        Stroke stroke = g2.getStroke();
        g2.setStroke(stemStroke);
        g2.draw(beam);
        g2.fill(beam);
        g2.setStroke(stroke);

        if (clip != null) {
            g2.setClip(oldClip);
        }
    }

    public void drawGlissando(Graphics2D g2, int xIndex, Note.Glissando glissando, int l) {
        Line line = ms.getComposition().getLine(l);
        int x1 = getGlissandoX1Pos(xIndex, glissando, l);
        int x2 = getGlissandoX2Pos(xIndex, glissando, l);
        drawGlissando(g2, x1, ms.getNoteYPos(line.getNote(xIndex).getYPos(), l), x2, ms.getNoteYPos(glissando.pitch, l));

        g2.setStroke(lineStroke);
        //drawing the stave-longitude
        // TODO: check with Tanima
        //        if (Math.abs(glissando.pitch) > 5 && (xIndex+1==line.noteCount() ||
        //                Math.abs(line.getNote(xIndex+1).getYPos())<Math.abs(glissando.pitch))) {
        //            for (int i = glissando.pitch + (glissando.pitch % 2 == 0 ? 0 : glissando.pitch > 0 ? -1 : 1); Math.abs(i) > 5; i += glissando.pitch > 0 ? -2 : 2)
        //                g2.drawLine(x2-5, ms.getNoteYPos(i, l),
        //                        x2+5, ms.getNoteYPos(i, l));
        //        }
    }

    public int getGlissandoX1Pos(int xIndex, Note.Glissando glissando, int l) {
        Line line = ms.getComposition().getLine(l);
        Note note = line.getNote(xIndex);
        int x1 = note.getXPos() + 15 + glissando.x1Translate;
        NoteType noteType = note.getNoteType();

        if (noteType == NoteType.SEMIBREVE) {
            x1 += 3;
        }
        else if (noteType.isGraceNote()) {
            x1 -= 3;

            if (noteType == NoteType.GRACE_SEMIQUAVER) {
                x1 += ((GraceSemiQuaver) note).getX2DiffPos();
            }
        }

        x1 += note.getDotted() * 6;
        return x1;
    }

    public int getGlissandoX2Pos(int xIndex, Note.Glissando glissando, int l) {
        Line line = ms.getComposition().getLine(l);
        float x2 = -glissando.x2Translate;

        if (xIndex + 1 < line.noteCount()) {
            x2 += line.getNote(xIndex + 1).getXPos() - 3;
            int accNum = line.getNote(xIndex + 1).getAccidental().ordinal();

            if (accNum > 0) {
                x2 -= FughettaDrawer.getAccidentalWidth(line.getNote(xIndex + 1));
                x2 -= 1.6;
            }
        }
        else {
            x2 += line.getNote(xIndex).getXPos() + 45;
        }
        return Math.round(x2);
    }

    protected int getFermataYPos(Note note) {
        if (note.isUpper() && note.getYPos() < 2) {
            return note.getYPos() - 11;
        }
        else if (!note.isUpper() && note.getYPos() < -4) {
            return note.getYPos() - 5;
        }
        else {
            return -9;
        }
    }

    private void drawGlissando(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double l = Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2) + Math.abs(y1 - y2) * Math.abs(y1 - y2));
        int m = (int) Math.round(l / glissandoLength);
        m = Math.max(2, m); // minimum two glissando parts
        g2.setFont(fughetta);
        AffineTransform at = g2.getTransform();
        g2.translate(x1, y1 + 2.25d);
        g2.rotate(Math.atan((double) (y2 - y1) / (double) (x2 - x1)));
        double scale = l / glissandoLength / m;
        g2.scale(scale, 1d);

        for (int i = 0; i < m; i++) {
            drawString(g2, GLISSANDO, (int) Math.round(i * glissandoLength), 0);
        }

        g2.setTransform(at);
    }

    private void drawTempoChange(Graphics2D g2, Tempo tempo, int line, int note) {
        Line l = ms.getComposition().getLine(line);
        Note n = l.getNote(note);
        int yPos = ms.getMiddleLine() + l.getTempoChangeYPos() + line * ms.getRowHeight();
        StringBuilder tempoBuilder = new StringBuilder(25);
        Note tempoTypeNote = tempo.getTempoType().getNote();

        if (tempo.isShowTempo()) {
            drawTempoChangeNote(g2, tempoTypeNote, n.getXPos(), yPos);
            tempoBuilder.append("= ");
            tempoBuilder.append(tempo.getVisibleTempo());
            tempoBuilder.append(' ');
        }

        tempoBuilder.append(tempo.getTempoDescription());
        g2.setFont(ms.getComposition().getGeneralFont());
        drawAntialiasedString(g2, tempoBuilder.toString(), n.getXPos() + (tempo.isShowTempo() ? crotchetWidth + 5 + (tempoTypeNote.getDotted() == 1 ||
                                                                                                                     tempoTypeNote.getNoteType() ==
                                                                                                                     NoteType.QUAVER ? 6 : 0
        ) : 0
        ), yPos);
    }

    private void drawBeatChange(Graphics2D g2, int line, Note note) {
        BeatChange beatChange = note.getBeatChange();
        int yPos = ms.getNoteYPos(0, line) + ms.getComposition().getLine(line).getBeatChangeYPos();
        drawBeatChange(g2, beatChange, note.getXPos(), yPos);
    }

    public void drawBeatChange(Graphics2D g2, BeatChange beatChange, int xPos, int yPos) {
        drawTempoChangeNote(g2, beatChange.getFirstNote(), xPos, yPos);
        g2.setFont(ms.getComposition().getGeneralFont());
        double eqXPos = xPos + crotchetWidth + 7;
        drawAntialiasedString(g2, "=", eqXPos, yPos);
        drawTempoChangeNote(g2, beatChange.getSecondNote(), (int) Math.round(eqXPos + 12), yPos);
    }

    protected void drawEnding(Graphics2D g2, Line line, int lineIndex, double x1, double x2, int number) {
        double y = ms.getNoteYPos(0, lineIndex) + line.getFsEndingYPos();
        int height = fsEndingFont.getSize() + 2;

        Path2D.Double bracket = new Path2D.Double();
        bracket.moveTo(x1, y);
        bracket.lineTo(x1, y - height);
        bracket.lineTo(x2, y - height);

        if (number == 1) {
            bracket.lineTo(x2, y);
        }

        g2.setStroke(stemStroke);
        g2.draw(bracket);

        g2.setFont(fsEndingFont);
        drawAntialiasedString(g2, "" + number, x1 + 4, y - 3);
    }

    protected void drawArticulation(Graphics2D g2, Note note, int line) {
        // todo exact y2 for all articulations
        // draw the accent
        int xPos = note.getXPos();

        if (note.getForceArticulation() == ForceArticulation.ACCENT) {
            int x2 = xPos + (int) crotchetWidth + 2;
            int y;

            if (note.isUpper()) {
                if (note.getYPos() <= 3) {
                    y = ms.getNoteYPos(6, line);
                }
                else {
                    y = ms.getNoteYPos(note.getYPos() + 3, line);
                }
            }
            else {
                if (note.getYPos() >= -3) {
                    y = ms.getNoteYPos(-6, line);
                }
                else {
                    y = ms.getNoteYPos(note.getYPos() - 3, line);
                }
            }

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawLine(xPos, y - 3, x2, y);
            g2.drawLine(xPos, y + 3, x2, y);
        }

        int dir = note.isUpper() ? 1 : -1;
        int durY = ms.getNoteYPos(note.getYPos() + dir * 2 + dir * (1 - note.getYPos() % 2), line);

        if (note.getDurationArticulation() == DurationArticulation.STACCATO) {
            AffineTransform at = g2.getTransform();
            g2.translate(xPos + getHalfNoteWidthForTie(note) - 2, durY - 2);
            g2.fill(staccatoEllipse);
            g2.setTransform(at);
        }
        else if (note.getDurationArticulation() == DurationArticulation.TENUTO) {
            g2.setStroke(tenutoStroke);
            double width =
                    note.getNoteType() == NoteType.SEMIBREVE || note.getNoteType() == NoteType.MINIM ? note.getRealUpNoteRect().width : crotchetWidth;
            g2.draw(new Line2D.Double(xPos, durY, xPos + width, durY));
        }
    }

    protected void drawAntialiasedString(Graphics2D g2, String str, int x, int y) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString(str, x, y);
    }

    protected void drawAntialiasedString(Graphics2D g2, String str, double x, double y) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString(str, (float) x, (float) y);
    }

    protected void drawString(Graphics2D g2, String str, float x, float y) {
        if (!drawStringWithShapes) {
            g2.drawString(str, x, y);
        } else {
            GlyphVector glyphVector = g2.getFont().createGlyphVector(g2.getFontRenderContext(), str);
            g2.translate(x, y);
            g2.fill(glyphVector.getOutline());
            g2.translate(-x, -y);
        }
    }
    protected void drawString(Graphics2D g2, String str, int x, int y) {
        if (!drawStringWithShapes) {
            g2.drawString(str, x, y);
        } else {
            GlyphVector glyphVector = g2.getFont().createGlyphVector(g2.getFontRenderContext(), str);
            g2.translate(x, y);
            g2.fill(glyphVector.getOutline());
            g2.translate(-x, -y);
        }
    }


    private int drawTextBox(Graphics2D g2, String str, int y, float xAlignment, int xTranslate) {
        ArrayList<String> rightVector = new ArrayList<String>(4);
        int prevIndex = 0;
        int maxWidth = 0;

        for (int i = 0; i <= str.length(); i++) {
            if (i == str.length() || str.charAt(i) == '\n' && i < str.length() - 1) {
                String tmp = str.substring(prevIndex, i);
                rightVector.add(tmp);
                int thisWidth = g2.getFontMetrics().stringWidth(tmp);

                if (maxWidth < thisWidth) {
                    maxWidth = thisWidth;
                }

                prevIndex = i + 1;
            }
        }

        int x = 0;

        if (xAlignment == Component.RIGHT_ALIGNMENT) {
            x = ms.getComposition().getLineWidth() - maxWidth;
        }
        else if (xAlignment == Component.CENTER_ALIGNMENT) {
            x = (ms.getComposition().getLineWidth() - maxWidth) / 2;
        }

        x += xTranslate;
        FontMetrics metrics = g2.getFontMetrics();
        int height = Math.round(metrics.getHeight());

        for (int i = 0; i < rightVector.size(); i++) {
            drawAntialiasedString(g2, rightVector.get(i), x, y + i * height);
        }

        int lastBaseline = y + ((rightVector.size() - 1) * height);
        return lastBaseline + metrics.getMaxDescent();
    }

    protected void drawGraceSemiQuaverBeam(Graphics2D g2, Note note, int line) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int dir = note.isUpper() ? -1 : 1;
        int x1 = note.getXPos() + (note.isUpper() ? 8 : 2);
        int x2 = x1 + ((GraceSemiQuaver) note).getX2DiffPos();
        int y1Pos = ((GraceSemiQuaver) note).getY0Pos();
        int y2Pos = note.getYPos();

        g2.setStroke(graceSemiQuaverStemStroke);
        int yHead1 = ms.getNoteYPos(y1Pos, line);
        int lengthening1 = Math.max(dir * (y2Pos - y1Pos) - 2, 0);
        int yUpper1 = ms.getNoteYPos(y1Pos + dir * (4 + lengthening1), line);
        g2.drawLine(x1, yHead1, x1, yUpper1 + dir);

        int lengthening2 = Math.max(dir * (y1Pos - y2Pos) - 2, 0);
        int yUpper2 = ms.getNoteYPos(y2Pos + dir * (4 + lengthening2), line);
        int yHead2 = ms.getNoteYPos(y2Pos, line);
        g2.drawLine(x2, yHead2, x2, yUpper2 + dir);

        // draw the beams
        g2.setStroke(graceSemiQuaverBeamStroke);
        g2.setClip(x1, 0, x2 - x1, Integer.MAX_VALUE);
        g2.drawLine(x1, yUpper1, x2, yUpper2);
        yUpper1 -= dir * 3;
        yUpper2 -= dir * 3;
        g2.drawLine(x1, yUpper1, x2, yUpper2);
        g2.setClip(null);

        // draw the grace strike
        g2.setStroke(graceSemiQuaverStrikeStroke);
        yUpper1 += -3 * dir * MusicSheet.HALF_LINE_DIST + dir * 5;
        yUpper2 += dir * MusicSheet.HALF_LINE_DIST + dir * 4;
        g2.drawLine(x1 - 5, yUpper1, x2 - 3, yUpper2);

        // draw steve-longitudes
        g2.setStroke(lineStroke);
        drawStaveLongitude(g2, y1Pos, line, x1 - 8, x1 + 3);
        drawStaveLongitude(g2, y2Pos, line, x2 - 8, x2 + 3);
    }

    protected void drawStaveLongitude(Graphics g2, int yPos, int line, int x1Pos, int x2Pos) {
        if (Math.abs(yPos) > 5) {
            for (int i = yPos + (yPos % 2 == 0 ? 0 : (yPos > 0 ? -1 : 1)); Math.abs(i) > 5; i += yPos > 0 ? -2 : 2) {
                int y1 = ms.getNoteYPos(i, line);
                g2.drawLine(x1Pos, y1, x2Pos, y1);
            }
        }
    }

    public abstract void paintNote(Graphics2D g2, Note note, int line, boolean beamed, Color color);

    public int getHeight() {
        return height;
    }

    protected abstract int drawLineBeginning(Graphics2D g2, Line line, int l);

    protected abstract void drawKeySignatureChange(Graphics2D g2, int l, KeyType[] keyTypes, int[] keys, int[] froms, boolean[] isNatural);

    protected abstract void drawTempoChangeNote(Graphics2D g2, Note tempoNote, int x, int y);

    protected static enum BeamType {
        FULL, ATTACH_LEFT, ATTACH_RIGHT
    }

    private class TupletCalc {
        int lx, ly, rx, ry;

        public TupletCalc(int lx, int ly, int rx, int ry) {
            this.lx = lx;
            this.ly = ly;
            this.rx = rx;
            this.ry = ry;
        }

        float getRate(int x) {
            return (float) (ry - ly) * (x - lx) / (rx - lx) + ly;
        }
    }
}
