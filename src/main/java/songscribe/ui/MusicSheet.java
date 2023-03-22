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

    Created on 2005.01.05.,14:24:51
*/

package songscribe.ui;

import com.bulenkov.iconloader.JBHiDPIScaledImage;
import com.bulenkov.iconloader.util.UIUtil;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import songscribe.IO.CompositionIO;
import songscribe.data.Interval;
import songscribe.data.IntervalSet;
import songscribe.data.MyBorder;
import songscribe.data.PropertyChangeListener;
import songscribe.data.TupletIntervalData;
import songscribe.music.Composition;
import songscribe.music.Crotchet;
import songscribe.music.GraceSemiQuaver;
import songscribe.music.Line;
import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.music.RepeatLeftRight;
import songscribe.ui.adjustment.LyricsAdjustment;
import songscribe.ui.adjustment.NoteXPosAdjustment;
import songscribe.ui.adjustment.VerticalAdjustment;
import songscribe.ui.mainframeactions.PlayActiveNoteThread;
import songscribe.ui.musicsheetdrawer.BaseMsDrawer;
import songscribe.ui.musicsheetdrawer.FughettaDrawer;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author Csaba Kávai
 */

public final class MusicSheet extends JComponent implements MouseListener, MouseMotionListener, PropertyChangeListener, FocusListener, MetaEventListener {
    public static final int VISIBLE_LINE_NUM = 5;
    public static final int LINE_DIST = 8;
    public static final float HALF_LINE_DIST = LINE_DIST / 2;
    public static final int RESOLUTION = 100; //in dpi
    public static final float PAGE_WIDTH = 7;
    public static final float PAGE_HEIGHT = 9.5f;
    public static final Dimension PAGE_SIZE = new Dimension(Math.round(PAGE_WIDTH * RESOLUTION), Math.round(
            PAGE_HEIGHT * RESOLUTION));
    // view constants
    private static final Map<NoteType, Integer> NOTE_DIST = new HashMap<NoteType, Integer>();

    static {
        NOTE_DIST.put(NoteType.SEMIBREVE, 70);
        NOTE_DIST.put(NoteType.MINIM, 50);
        NOTE_DIST.put(NoteType.CROTCHET, 35);
        NOTE_DIST.put(NoteType.QUAVER, 25);
        NOTE_DIST.put(NoteType.SEMIQUAVER, 25);
        NOTE_DIST.put(NoteType.DEMI_SEMIQUAVER, 25);
        NOTE_DIST.put(NoteType.SEMIBREVE_REST, 70);
        NOTE_DIST.put(NoteType.MINIM_REST, 50);
        NOTE_DIST.put(NoteType.CROTCHET_REST, 35);
        NOTE_DIST.put(NoteType.QUAVER_REST, 25);
        NOTE_DIST.put(NoteType.SEMIQUAVER_REST, 25);
        NOTE_DIST.put(NoteType.DEMI_SEMIQUAVER_REST, 25);
        NOTE_DIST.put(NoteType.GRACE_QUAVER, 30);
        NOTE_DIST.put(NoteType.GRACE_SEMIQUAVER, 50);
        NOTE_DIST.put(NoteType.GLISSANDO, 0);
        NOTE_DIST.put(NoteType.REPEAT_LEFT, 25);
        NOTE_DIST.put(NoteType.REPEAT_RIGHT, 25);
        NOTE_DIST.put(NoteType.REPEAT_LEFT_RIGHT, 25);
        NOTE_DIST.put(NoteType.BREATH_MARK, 15);
        NOTE_DIST.put(NoteType.SINGLE_BARLINE, 60);
        NOTE_DIST.put(NoteType.DOUBLE_BARLINE, 60);
        NOTE_DIST.put(NoteType.FINAL_DOUBLE_BARLINE, 60);
        NOTE_DIST.put(NoteType.PASTE, 0);
    }

    private static final int INVISIBLE_LINES_NUM_BELOW = 4;
    private static final int INVISIBLE_LINES_NUM_ABOVE = 3;
    private static final double MAX_BEAM_ANGLE = 0.4;
    private static final int FIRST_NOTE_IN_LINE_MOVEMENT = -15;
    private static final int FIX_PREFIX_WIDTH = 7;
    private final static BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {
            3.0f, 2.0f
    }, 1.0f);
    private static final String NEWLINE_STRING = "\n";
    private static Logger LOG = Logger.getLogger(MusicSheet.class);
    private static Color activeNoteColor = Color.blue;
    private static int startLocX = 100;
    // popup
    JPopupMenu popup;
    DeleteAction deleteAction;
    CutAction cutAction;
    CopyAction copyAction;
    PasteAction pasteAction;
    // music sheet image acceleration
    private boolean repaintImage = true;
    private SAXParser saxParser;
    private BufferedImage msImage;
    private int playingLine = -1, playingNote = -1;
    private boolean playInsertingNote;
    private NoteXPosAdjustment noteXPosAdjustment;
    private VerticalAdjustment verticalAdjustment;
    private LyricsAdjustment lyricsAdjustment;
    private IMainFrame mainFrame;
    private Mode mode = Mode.NOTE_EDIT;
    private boolean dragDisabled;
    private Control control;
    //mouse control fields
    private boolean isActiveNoteIn;
    //selection fields
    private boolean inSelection;
    private int selectedLine = -1;
    private int selectedNotesLine = -1;
    private int selectionBegin, selectionEnd;
    private boolean startedDrag;
    private Rectangle dragRectangle = new Rectangle();
    private Point startDrag = new Point();
    private Note activeNote;
    private NotePosition newActiveNotePoint = new NotePosition();
    private NotePosition activeNotePoint = new NotePosition();
    // model
    private Composition composition;
    // scrolling
    private JScrollPane scroll;
    private JPanel pageOutsidePanel;
    private JPanel marginPanel;
    // adjustable fields
    private int underLyricsYPos = 0;
    private int leadingKeysPos = 40;
    private int rowHeight;
    private int middleLine;
    private BaseMsDrawer drawer;
    private BaseMsDrawer[] drawers = new BaseMsDrawer[2];
    private Dimension thisPrefSize = new Dimension();
    private Dimension marginPrefSize = new Dimension();
    private ArrayList<Object> focusLostExceptions = new ArrayList<Object>();
    private int selectedNoteStorage[] = new int[3];

    public MusicSheet(IMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        String property = mainFrame.getProperties().getProperty(Constants.CONTROL_PROP);
        control = property != null ? Control.valueOf(property) : Control.MOUSE;

        try {
            drawers[0] = new FughettaDrawer(this);
        }
        catch (Exception e) {
            mainFrame.showErrorMessage("Could not open a necessary font. The program cannot work without it.");
            System.exit(0);
        }

        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        }
        catch (Exception e) {
            mainFrame.showErrorMessage(Constants.PACKAGE_NAME + " cannot start because of an initialization error.");
            LOG.error("SaxParser configuration", e);
            System.exit(0);
        }

        // We use the FughettaDrawer as the default now
        drawer = drawers[0];
        setFocusable(true);
    }

    public static boolean defaultUpperNote(Note note) {
        return note.getYPos() >= 0 || note.getNoteType().isGraceNote() ||
               note.getNoteType() == NoteType.GRACE_SEMIQUAVER_EDIT_STEP1;
    }

    public static int calculateLastNoteXPos(Line line, Note note) {
        if (line.noteCount() == 0) {
            return startLocX;
        }

        Note lastNote = line.getNote(line.noteCount() - 1);
        return lastNote.getXPos() + Math.round(
                (NOTE_DIST.get(lastNote.getNoteType()) + note.getAccidental().getNb() * FIX_PREFIX_WIDTH +
                 (note.isAccidentalInParenthesis() ? 8 : 0)
                ) * line.getNoteDistChangeRatio());
        //lastNote.getXPos()+Math.round((ND+note.getAccidental().getNb()*FIX_PREFIX_WIDTH)*line.getNoteDistChangeRatio());
    }

    public static void setStartLocX(int startLocX) {
        MusicSheet.startLocX = startLocX;
    }

    public void initComponent() {
        composition = new Composition(mainFrame);
        Dimension sheetSize = new Dimension((int) (LineWidthChangeDialog.MAX_LINE_WIDTH * RESOLUTION), (int) (
                LineWidthChangeDialog.MAX_LINE_WIDTH * RESOLUTION * PAGE_HEIGHT / PAGE_WIDTH
        ));

        if (UIUtil.isRetina()) {
            msImage = new JBHiDPIScaledImage(sheetSize.width, sheetSize.height, BufferedImage.TYPE_INT_RGB);
        }
        else {
            msImage = new BufferedImage(sheetSize.width, sheetSize.height, BufferedImage.TYPE_INT_RGB);
        }

        viewChanged();
        noteXPosAdjustment = new NoteXPosAdjustment(this);
        verticalAdjustment = new VerticalAdjustment(this);
        lyricsAdjustment = new LyricsAdjustment(this);
        // scroll settings
        marginPanel = new JPanel();
        marginPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(40, 40, 40, 40)));
        marginPanel.setBackground(Color.white);
        marginPanel.add(this);
        pageOutsidePanel = new MsPanel();
        pageOutsidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pageOutsidePanel.add(marginPanel);
        scroll = new JScrollPane(pageOutsidePanel);
        setLineWidth(composition.getLineWidth());
        addMouseMotionListener(this);
        addMouseListener(this);
        addFocusListener(this);
        // popup menu settings
        popup = new JPopupMenu();
        popup.add(new JMenuItem(cutAction = new CutAction()));
        popup.add(new JMenuItem(copyAction = new CopyAction()));
        popup.add(new JMenuItem(pasteAction = new PasteAction()));
        popup.addSeparator();
        popup.add(new JMenuItem(deleteAction = new DeleteAction()));
        updateEditActions();
        int[] keyCodes = {
                KeyEvent.VK_UP,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT,
                KeyEvent.VK_PAGE_UP,
                KeyEvent.VK_PAGE_DOWN,
                KeyEvent.VK_ENTER,
                KeyEvent.VK_BACK_SPACE
        };

        for (int keyCode : keyCodes) {
            Object o = new Object();
            getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), o);
            getActionMap().put(o, new KeyAction(keyCode));
        }

        int[] beamKeyCodes = { KeyEvent.VK_B, KeyEvent.VK_T, KeyEvent.VK_T };
        int[] beamKeyMasks = { 0, 0, NoteType.getMenuShortcutKeyMask() };
        BeamingType beamingTypes[] = { BeamingType.BEAM, BeamingType.TRIPLET, BeamingType.TIE };

        for (int i = 0; i < beamKeyCodes.length; i++) {
            Object o = new Object();
            getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(beamKeyCodes[i], beamKeyMasks[i]), o);
            getActionMap().put(o, new BeamingKeyActions(beamingTypes[i]));
        }

        mainFrame.addProperyChangeListener(this);

        if (MainFrame.sequencer != null) {
            MainFrame.sequencer.addMetaEventListener(this);
        }

        mainFrame.setModifiedDocument(false);
    }

    public void openMusicSheet(IMainFrame mainFrame, File openFile, boolean setTitle) {
        boolean previousModifiedDocument = mainFrame.isModifiedDocument();
        mainFrame.setModifiedDocument(true);

        try {
            CompositionIO.DocumentReader dr = new CompositionIO.DocumentReader(mainFrame);
            saxParser.parse(openFile, dr);
            setComposition(dr.getComposition());
            mainFrame.setFrameSize();
            if (setTitle) {
                mainFrame.setSaveFile(openFile);
            }
        }
        catch (SAXException e1) {
            mainFrame.showErrorMessage("Could not open the file " + openFile.getName() + ", because it is damaged.");
            LOG.error("SaxParser parse", e1);
        }
        catch (IOException e1) {
            mainFrame.showErrorMessage(
                    "Could not open the file " + openFile.getName() + ". Check if you have the permission to open it.");
            LOG.error("Song open", e1);
        }

        mainFrame.setModifiedDocument(previousModifiedDocument);
    }

    public void musicChanged(Properties props) {
        composition.musicChanged(props);
        playInsertingNote = props.getProperty(Constants.PLAY_INSERTING_NOTE).equals(Constants.TRUE_VALUE);
    }

    public void viewChanged() {
        middleLine = (INVISIBLE_LINES_NUM_ABOVE + 3) * LINE_DIST + composition.getTopSpace();
        rowHeight = (INVISIBLE_LINES_NUM_BELOW + VISIBLE_LINE_NUM + INVISIBLE_LINES_NUM_ABOVE + 1) * LINE_DIST +
                    composition.getRowHeight();
    }

    public JScrollPane getScrolledMusicSheet() {
        return scroll;
    }

    public void setRepaintImage(boolean repaintImage) {
        this.repaintImage = repaintImage;
    }

    public boolean isNoteSelected(int xIndex, int line) {
        return selectedNotesLine == line && selectionBegin <= xIndex && xIndex <= selectionEnd;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (msImage == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (repaintImage) {
            Graphics2D g2MsImage = msImage.createGraphics();
            g2MsImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2MsImage.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2MsImage.setPaint(Color.white);
            g2MsImage.fillRect(0, 0, msImage.getWidth(), msImage.getHeight());
            drawer.drawMusicSheet(g2MsImage, true, 1d);
            g2MsImage.dispose();
            repaintImage = false;
        }

        UIUtil.drawImage(g2, msImage, 0, 0, null);

        if (mode == Mode.NOTE_EDIT) {
            // draw the active note
            if (activeNote != null && (control == MusicSheet.Control.KEYBOARD || isActiveNoteIn)) {
                if (activeNote != Note.GLISSANDO_NOTE) {
                    int actX = activeNote.getXPos();

                    if (actX > composition.getLineWidth() - 10) {
                        activeNote.setXPos(composition.getLineWidth() - 12);
                    }

                    drawer.paintNote(g2, activeNote, activeNotePoint.line, false, activeNoteColor);
                    activeNote.setXPos(actX);
                }
                else if (activeNotePoint.xIndex > 0) {
                    g2.setPaint(activeNoteColor);
                    drawer.drawGlissando(g2,
                            activeNotePoint.xIndex - 1, new Note.Glissando(activeNote.getYPos()), activeNotePoint.line);
                }
            }
        }
        else if (mode == Mode.NOTE_ADJUSTMENT) {
            noteXPosAdjustment.repaint(g2);
        }
        else if (mode == Mode.VERTICAL_ADJUSTMENT) {
            verticalAdjustment.repaint(g2);
        }
        else if (mode == Mode.LYRICS_ADJUSTMENT) {
            lyricsAdjustment.repaint(g2);
        }

        // paint selection
        if (startedDrag) {
            g2.setStroke(dashedStroke);
            g2.draw(dragRectangle);
        }

        // paint playing
        if (playingLine != -1 && playingNote != -1) {
            Line line = composition.getLine(playingLine);
            drawer.paintNote(g2, line.getNote(playingNote), playingLine,
                    line.getBeamings().findInterval(playingNote) != null, Color.magenta);
            playingLine = playingNote = -1;
        }
    }

    public int getNoteYPos(int yPos, int line) {
        return (int) (middleLine + yPos * HALF_LINE_DIST + line * rowHeight);
    }

    public int getUnderLyricsYPos() {
        return middleLine + composition.lineCount() * rowHeight + underLyricsYPos;
    }

    public Note getActiveNote() {
        return activeNote;
    }

    public void setActiveNote(Note activeNote) {
        if (activeNote != null) {
            if (this.activeNote != null) {
                activeNote.setYPos(this.activeNote.getYPos());
                activeNote.setXPos(this.activeNote.getXPos());
            }
            else {
                this.activeNote = activeNote;
                setActiveNotePositionToEnd();
            }

            activeNote.setUpper(defaultUpperNote(activeNote));
        }

        this.activeNote = activeNote;
        repaint();
        //setCursor(activeNote==null ? Cursor.getDefaultCursor() : emptyCursor);
    }

    public void unSetNoteSelections() {
        selectedLine = -1;
        selectedNotesLine = -1;
        updateEditActions();
    }

    private void updateEditActions() {
        deleteAction.setEnabled(selectedNotesLine != -1 || selectedLine != -1);
        copyAction.setEnabled(selectedNotesLine != -1);
        cutAction.setEnabled(selectedNotesLine != -1);
        pasteAction.setEnabled(copyAction.copyBuffer.size() > 0);
    }

    private boolean commonAddInsertModifyActiveNoteCommands(int xIndex, Line line) {
        unSetNoteSelections();
        // if the active note is glissando, it needs different handling
        if (activeNote.getNoteType() == NoteType.GLISSANDO) {
            if (activeNotePoint.xIndex > 0) {
                line.getNote(activeNotePoint.xIndex - 1).setGlissando(activeNote.getYPos());
            }

            return true;
        }

        if (activeNote.getNoteType() == NoteType.REPEAT_LEFT &&
            xIndex - 1 >= 0 && line.getNote(xIndex - 1).getNoteType() == NoteType.REPEAT_RIGHT) {
            Note repeatLeftRight = new RepeatLeftRight();
            repeatLeftRight.setXPos(line.getNote(xIndex - 1).getXPos());
            line.setNote(xIndex - 1, repeatLeftRight);
            return true;
        }

        if (activeNote.getNoteType() == NoteType.REPEAT_RIGHT &&
            xIndex < line.noteCount() && line.getNote(xIndex).getNoteType() == NoteType.REPEAT_LEFT) {
            Note repeatLeftRight = new RepeatLeftRight();
            repeatLeftRight.setXPos(line.getNote(xIndex).getXPos());
            line.setNote(xIndex, repeatLeftRight);
            return true;
        }

        if (activeNote.getNoteType() == NoteType.GRACE_SEMIQUAVER_EDIT_STEP1) {
            return true;
        }

        if (activeNote.getNoteType() == NoteType.PASTE) {
            //if the user tries to insert into triplet, he will get an error message
            Interval iv = line.getTuplets().findInterval(xIndex - 1);

            if (iv != null && xIndex - 1 < iv.getB()) {
                mainFrame.showErrorMessage("Cannot insert into a triplet.");
                return true;
            }

            line.removeInterval(xIndex - 1, xIndex);
            int diff = (xIndex ==
                        line.noteCount() ? calculateLastNoteXPos(line, copyAction.copyBuffer.get(0)) : line.getNote(xIndex).getXPos()
                       ) - copyAction.copyBuffer.get(0).getXPos();
            int copySize = copyAction.copyBuffer.size();

            for (int i = 0; i < copySize; i++) {
                Note note = copyAction.copyBuffer.get(i);
                note.setXPos(note.getXPos() + diff);
                line.addNote(xIndex + i, note.clone());
            }

            line.pasteIntervals(copyAction.intervalSetsCopyBuffer, xIndex);
            Note lastNote = copyAction.copyBuffer.get(copySize - 1);
            int shift = Math.round(
                    (NOTE_DIST.get(lastNote.getNoteType()) + lastNote.getAccidental().getNb() * FIX_PREFIX_WIDTH) *
                    line.getNoteDistChangeRatio()) + lastNote.getXPos() - copyAction.copyBuffer.get(0).getXPos();
            //int shift = Math.round((ND+lastNote.getAccidental().getNb()*FIX_PREFIX_WIDTH)*line.getNoteDistChangeRatio())+lastNote.getXPos()-copyAction.copyBuffer.get(0).getXPos();

            for (int i = xIndex + copySize; i < line.noteCount(); i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos() + shift);
            }

            control = pasteAction.prevPasteControl;

            for (int i = xIndex; i < xIndex + copySize; i++) {
                Interval interval = line.getBeamings().findInterval(i);

                if (interval != null) {
                    calculateLengthenings(i, line, true);
                    i = interval.getB();
                }
            }

            inSelection = true;
            return true;
        }

        if (playInsertingNote && activeNote.getNoteType().isNote()) {
            PlayActiveNoteThread playActiveNoteThread = new PlayActiveNoteThread(activeNote.getActiveNotePitch(line));
            playActiveNoteThread.start();
        }

        return false;
    }

    private void calculateActiveNoteXPos() {
        if (activeNote == null) {
            return;
        }

        Line line = composition.getLine(activeNotePoint.line);

        if (line.noteCount() == activeNotePoint.xIndex) {
            activeNote.setXPos(calculateLastNoteXPos(line, activeNote));
        }
        else {
            activeNote.setXPos(line.getNote(activeNotePoint.xIndex).getXPos() + activeNotePoint.movement);
        }
    }

    public void setActiveNotePositionToEnd() {
        activeNotePoint.movement = 0;
        activeNotePoint.line = composition.lineCount() - 1;
        activeNotePoint.xIndex = composition.getLine(activeNotePoint.line).noteCount();
        calculateActiveNoteXPos();
    }

    private void postCommonAddInsertModifyActiveNoteCommands(Line line, int xIndex) {
        //mainFrame.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, new ModifyUndoableEdit(oldNote, oldNoteInfo, xIndex)));
        Note nextNote;

        if (activeNote.getNoteType().isGraceNote()) {
            nextNote = NoteType.GLISSANDO.newInstance();
        }
        else if (activeNote.getNoteType() == NoteType.GLISSANDO) {
            NoteType nextNoteType;

            if (!line.getNote(xIndex).getNoteType().isGraceNote()) {
                nextNoteType = line.getNote(xIndex).getNoteType();
            }
            else if (xIndex > 0) {
                nextNoteType = line.getNote(xIndex - 1).getNoteType();
            }
            else {
                nextNoteType = NoteType.CROTCHET;
            }

            nextNote = nextNoteType.newInstance();
        }
        else if (activeNote.getNoteType() == NoteType.GRACE_SEMIQUAVER_EDIT_STEP1) {
            nextNote = new GraceSemiQuaver();
            ((GraceSemiQuaver) nextNote).setY0Pos(activeNote.getYPos());
            ((GraceSemiQuaver) nextNote).setX2DiffPos(15);
            nextNote.setUpper(true);
        }
        else {
            nextNote = activeNote.getNoteType().newInstance();
        }

        setActiveNote(nextNote);
        mainFrame.getInsertMenu().updateState();
        spellLyrics(line);
        drawWidthIfWiderLine(line, false);
        repaintImage = true;
        repaint();
    }

    public void addActiveNote(Line line) {
        if (activeNote != null) {
            if (commonAddInsertModifyActiveNoteCommands(line.noteCount(), line)) {
                postCommonAddInsertModifyActiveNoteCommands(line, line.noteCount() - 1);
                return;
            }

            activeNote.setXPos(calculateLastNoteXPos(line, activeNote));
            line.addNote(activeNote);
            //mainFrame.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, new InsertUndoableEdit(cloneActiveNote, ni, noteInfo.size()-2)));

            // decide automatic beaming
            if (activeNote.getNoteType().isBeamable() &&
                line.noteCount() >= 2 && line.getTuplets().findInterval(line.noteCount() - 2) == null) {
                int sum = 0;

                for (int i = line.noteCount() - 2; i >= 0; i--) {
                    if (line.getNote(i).getNoteType() == NoteType.QUAVER) {
                        sum += 2;
                    }
                    else if (line.getNote(i).getNoteType() == NoteType.SEMIQUAVER ||
                             line.getNote(i).getNoteType() == NoteType.DEMI_SEMIQUAVER) {
                        sum += 1;
                    }
                    else {
                        break;
                    }

                    Interval interval = line.getBeamings().findInterval(i);

                    if (interval != null && interval.getA() == i) {
                        break;
                    }
                }

                if (activeNote.getNoteType() == NoteType.QUAVER && sum > 0 && sum % 2 == 0 && sum % 4 != 0 ||
                    (activeNote.getNoteType() == NoteType.SEMIQUAVER ||
                     activeNote.getNoteType() == NoteType.DEMI_SEMIQUAVER
                    ) && sum > 0 && sum % 4 != 0) {
                    line.getBeamings().addInterval(line.noteCount() - 2, line.noteCount() - 1);
                    //activeNote.setXPos(activeNote.getXPos()-(ND-BEAMEDNOTEDIST));
                }

                calculateLengthenings(line.noteCount() - 1, line, true);
            }

            postCommonAddInsertModifyActiveNoteCommands(line, line.noteCount() - 1);
        }
    }

    private void insertActiveNote(int xIndex, Line line) {
        if (activeNote != null) {
            if (commonAddInsertModifyActiveNoteCommands(xIndex, line)) {
                postCommonAddInsertModifyActiveNoteCommands(line, line.noteCount() - 1);
                return;
            }

            // if the user tries to insert into triplet, he will get an error message
            Interval iv = line.getTuplets().findInterval(xIndex - 1);

            if (iv != null && xIndex - 1 < iv.getB()) {
                mainFrame.showErrorMessage("Cannot insert into a triplet.");
                return;
            }

            line.removeInterval(xIndex - 1, xIndex);
            activeNote.setXPos(line.getNote(xIndex).getXPos() + activeNote.getAccidental().getNb() * FIX_PREFIX_WIDTH);
            line.addNote(xIndex, activeNote);
            int shift = Math.round(
                    (NOTE_DIST.get(activeNote.getNoteType()) + activeNote.getAccidental().getNb() * FIX_PREFIX_WIDTH) *
                    line.getNoteDistChangeRatio());
            //int shift = Math.round((ND+activeNote.getAccidental().getNb()*FIX_PREFIX_WIDTH)*line.getNoteDistChangeRatio());

            for (int i = xIndex + 1; i < line.noteCount(); i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos() + shift);
            }

            postCommonAddInsertModifyActiveNoteCommands(line, xIndex);
        }
    }

    private void modifyActiveNote(int xIndex, Line line) {
        if (activeNote != null) {
            if (commonAddInsertModifyActiveNoteCommands(xIndex, line)) {
                postCommonAddInsertModifyActiveNoteCommands(line, line.noteCount() - 1);
                return;
            }

            Note oldNote = line.getNote(xIndex);

            if (line.getTuplets().findInterval(xIndex) != null && oldNote.getNoteType() != activeNote.getNoteType()) {
                mainFrame.showErrorMessage("Cannot modify a triplet with different note type.");
                return;
            }

            activeNote.setXPos(oldNote.getXPos() +
                               (activeNote.getAccidental().getNb() - oldNote.getAccidental().getNb()) *
                               FIX_PREFIX_WIDTH);
            int shift = Math.round((NOTE_DIST.get(activeNote.getNoteType()) - NOTE_DIST.get(oldNote.getNoteType()) +
                                    (activeNote.getAccidental().getNb() - oldNote.getAccidental().getNb()) *
                                    FIX_PREFIX_WIDTH
                                   ) * line.getNoteDistChangeRatio());
            line.setNote(xIndex, activeNote);

            for (int i = xIndex + 1; i < line.noteCount(); i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos() + shift);
            }

            // arrange to beaming
            if (oldNote.getNoteType() != activeNote.getNoteType()) {
                line.removeInterval(xIndex - 1, xIndex + 1);
                calculateLengthenings(xIndex - 1, line, true);
                calculateLengthenings(xIndex + 1, line, true);
            }
            else {
                calculateLengthenings(xIndex, line, true);
            }

            // arrange the ties
            if (oldNote.getYPos() != activeNote.getYPos()) {
                line.getTies().removeInterval(xIndex - 1, xIndex + 1);
            }

            postCommonAddInsertModifyActiveNoteCommands(line, xIndex);
        }
    }

    private void deleteNote(int xIndex, Line line) {
        if (xIndex < line.noteCount() - 1) {
            int shift = line.getNote(xIndex).getXPos() - line.getNote(xIndex + 1).getXPos();

            for (int i = xIndex + 1; i < line.noteCount(); i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos() + shift);
            }
        }

        line.removeNote(xIndex);
    }

    public void beamSelectedNotes(boolean beam) {
        if (selectedNotesLine == -1 || selectionBegin == selectionEnd) {
            mainFrame.showInfoMessage(
                    "You must select more than one note first to " + (beam ? "beam" : "unbeam") +
                    " them.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);

        for (int i = selectionBegin; i <= selectionEnd; i++) {
            NoteType nt = line.getNote(i).getNoteType();

            if (!nt.isBeamable() && !nt.isGraceNote()) {
                mainFrame.showInfoMessage("You can " + (beam ? "beam" : "unbeam") +
                                                         " only quavers, semiquavers and demisemiquavers.");
                return;
            }
        }

        if (beam) {
            line.getBeamings().addInterval(selectionBegin, selectionEnd);
            calculateLengthenings(selectionBegin, line, true);
        }
        else {
            line.getBeamings().removeInterval(selectionBegin, selectionEnd);
            calculateLengthenings(selectionBegin, line, true);
            calculateLengthenings(selectionEnd, line, true);
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void untupletSelectedNotes() {
        if (selectedNotesLine == -1 || selectionBegin == selectionEnd) {
            mainFrame.showInfoMessage("You must select more than one note first to remove a tuplet.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        Interval begin = line.getTuplets().findInterval(selectionBegin);
        Interval end = line.getTuplets().findInterval(selectionEnd);

        if (begin == null || begin != end) {
            mainFrame.showInfoMessage("You must select exactly the notes that are in one tuplet to remove it.");
            return;
        }

        line.getTuplets().removeInterval(selectionBegin, selectionEnd);
        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void tupletSelectedNotes(int numeral) {
        if (selectedNotesLine == -1 || selectionBegin == selectionEnd) {
            mainFrame.showInfoMessage("You must select more than one note first to make a tuplet.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        boolean canBeamed = true;

        for (int i = selectionBegin; i <= selectionEnd; i++) {
            Note note = line.getNote(i);
            Interval iv = line.getTuplets().findInterval(i);

            if (iv != null) {
                mainFrame.showInfoMessage("You cannot tuplet notes that already are tuplet.");
                return;
            }

            if (!note.getNoteType().isBeamable()) {
                canBeamed = false;
            }
        }

        if (canBeamed) {
            beamSelectedNotes(true);
        }

        Interval interval = line.getTuplets().addInterval(selectionBegin, selectionEnd);
        TupletIntervalData.setGrade(interval, numeral);

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void tieSelectedNotes(boolean tie) {
        if (selectedNotesLine == -1 || selectionBegin == selectionEnd) {
            mainFrame.showInfoMessage(
                    "You must select more than one note first to " + (tie ? "tie" : "untie") +
                    " them.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        int pitch = line.getNote(selectionBegin).getPitch();

        for (int i = selectionBegin + 1; i <= selectionEnd; i++) {
            if (line.getNote(i).getPitch() != pitch) {
                mainFrame.showInfoMessage("The selected notes must be of the same pitch.\n" +
                                                         "If you want to tie multiple notes of different pitches, please use Slur.");
                return;
            }
        }

        if (tie) {
            line.getTies().addInterval(selectionBegin, selectionEnd);
        }
        else {
            line.getTies().removeInterval(selectionBegin, selectionEnd);
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void slurSelectedNotes(boolean slur) {
        if (selectedNotesLine == -1 || selectionBegin == selectionEnd) {
            mainFrame.showInfoMessage(
                    "You must select more than one note first to " + (slur ? "make" : "remove") +
                    " slur.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);

        if (!slur) {
            Interval beginSlur = line.getSlurs().findInterval(selectionBegin);
            Interval endSlur = line.getSlurs().findInterval(selectionEnd);

            if (beginSlur == null || beginSlur != endSlur) {
                mainFrame.showInfoMessage("Please select a slur or part of it to remove.");
                return;
            }
        }
        else if (selectionEnd - selectionBegin == 1 &&
                 line.getNote(selectionBegin).getPitch() == line.getNote(selectionEnd).getPitch()) {
            mainFrame.showInfoMessage("You cannot slur two notes of the same pitch. Use Tie instead.");
            return;
        }

        if (slur) {
            line.getSlurs().addInterval(selectionBegin, selectionEnd);
        }
        else {
            line.getSlurs().removeInterval(selectionBegin, selectionEnd);
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();

        if (slur) {
            mainFrame.showInfoMessage(Constants.PACKAGE_NAME +
                 " recommends to change to Vertical Alignment Mode in Mode menu to align this slur.");
        }
    }

    public void crescendoOrDiminuendoSelectedNotes(boolean crescendoOrDiminuendo) {
        String typeString = crescendoOrDiminuendo ? "crescendo" : "diminuendo";

        if (selectedNotesLine == -1 || selectionBegin == selectionEnd) {
            mainFrame.showInfoMessage("You must select more than one note first to create " + typeString);
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        IntervalSet intervalSet = crescendoOrDiminuendo ? line.getCrescendo() : line.getDiminuendo();
        intervalSet.addInterval(selectionBegin, selectionEnd);

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void removeCrescendoOrDiminuendoSelectedNotes() {
        if (selectedNotesLine == -1) {
            mainFrame.showInfoMessage("You must select at least one note first to remove crescendo and diminuendo");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        boolean removed = false;

        for (int i = selectionBegin; i <= selectionEnd; i++) {
            Interval cresInterval = line.getCrescendo().findInterval(i);

            if (cresInterval != null) {
                line.getCrescendo().removeInterval(cresInterval);
                removed = true;
            }

            Interval dimInterval = line.getDiminuendo().findInterval(i);

            if (dimInterval != null) {
                line.getDiminuendo().removeInterval(dimInterval);
                removed = true;
            }
        }

        if (!removed) {
            mainFrame.showInfoMessage("You must select a note which is part of a crescendo and diminuendo");
            return;
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void makeFsEndingOnSelectedNotes(boolean fsEnding) {
        if (selectedNotesLine == -1 || selectionBegin == selectionEnd) {
            mainFrame.showInfoMessage(
                    "You must select more than one note first to " + (fsEnding ? "make" : "remove") +
                    " first-second endings.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);

        if (fsEnding) {
            boolean existsRepeat = false;

            for (int i = selectionBegin; i <= selectionEnd; i++) {
                if (line.getNote(i).getNoteType() == NoteType.REPEAT_RIGHT) {
                    existsRepeat = true;
                    break;
                }
            }

            if (!existsRepeat) {
                int answ = mainFrame.showConfirmDialog("It does not make sense to create a first-second ending without a right side repeat.\nDo you want to continue anyway?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (answ == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }

        if (fsEnding) {
            line.getFsEndings().addInterval(selectionBegin, selectionEnd);
        }
        else {
            line.getFsEndings().removeInterval(selectionBegin, selectionEnd);
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void makeTrillOnSelectedNotes(boolean trill) {
        if (selectedNotesLine == -1) {
            mainFrame.showInfoMessage(
                    "You must select at least one note first to " + (trill ? "make a" : "remove") +
                    " trill.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);

        for (int i = selectionBegin; i <= selectionEnd; i++) {
            line.getNote(i).setTrill(trill);
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void invertLyricsUnderRests() {
        try {
            if (selectedNotesLine == -1 || selectionBegin != selectionEnd) {
                throw new IllegalArgumentException();
            }

            Line line = composition.getLine(selectedNotesLine);
            Note note = line.getNote(selectionBegin);

            if (!note.getNoteType().isRest()) {
                throw new IllegalArgumentException();
            }

            note.setForceSyllable(!note.isForceSyllable());

            spellLyrics(line);
            composition.modifiedComposition();
            repaintImage = true;
            repaint();
        }
        catch (IllegalArgumentException e) {
            mainFrame.showInfoMessage("You must select one rest to allow / disallow lyrics.");
        }
    }

    public void invertFractionBeamOrientation() {
        try {
            if (selectedNotesLine == -1 || selectionBegin != selectionEnd) {
                throw new IllegalArgumentException();
            }

            Line line = composition.getLine(selectedNotesLine);

            if (!line.getBeamings().isInsideAnyInterval(selectionBegin)) {
                throw new IllegalArgumentException();
            }

            Note note = line.getNote(selectionBegin);
            note.setInvertFractionBeamOrientation(!note.isInvertFractionBeamOrientation());

            composition.modifiedComposition();
            repaintImage = true;
            repaint();
        }
        catch (IllegalArgumentException e) {
            mainFrame.showInfoMessage("You must select one beamed note to invert fraction beam orientation.");
        }
    }

    public void invertStemDirectionOnSelectedNotes() {
        if (selectedNotesLine == -1) {
            mainFrame.showInfoMessage("You must select at least one note to invert its/their stem direction.");
            return;
        }

        Line line = composition.getLine(selectedNotesLine);

        for (int i = selectionBegin; i <= selectionEnd; i++) {
            Note note = line.getNote(i);
            note.setUpper(!note.isUpper());
        }

        Interval lastInterval = null;

        for (int i = selectionBegin; i <= selectionEnd; i++) {
            Interval inverval = line.getBeamings().findInterval(i);

            if (inverval != null && inverval != lastInterval) {
                calculateLengthenings(i, line, false);
                lastInterval = inverval;
            }
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    private void calculateSelection(boolean fromRectangle) {
        Rectangle helper = new Rectangle();
        selectedLine = selectedNotesLine = selectionBegin = selectionEnd = -1;

        for (int l = 0; l < composition.lineCount(); l++) {
            if (selectedNotesLine != -1 && selectedNotesLine != l) {
                break;
            }

            Line line = composition.getLine(l);

            for (int n = 0; n < line.noteCount(); n++) {
                Note note = line.getNote(n);

                if (line.getBeamings().findInterval(n) != null) {
                    helper.setBounds(note.isUpper() ? Crotchet.REAL_UP_NOTE_RECT : Crotchet.REAL_DOWN_NOTE_RECT);
                }
                else {
                    helper.setBounds(note.isUpper() ? note.getRealUpNoteRect() : note.getRealDownNoteRect());
                }

                helper.translate(note.getXPos(), getNoteYPos(note.getYPos(), l) - Note.HOT_SPOT.y);

                if (fromRectangle && dragRectangle.intersects(helper) || !fromRectangle && helper.contains(startDrag)) {
                    selectedNotesLine = l;

                    if (selectionBegin == -1) {
                        selectionBegin = n;
                    }

                    selectionEnd = n;
                }
            }
        }

        updateEditActions();
    }

    private void calculateLengthenings(int xIndex, Line line, boolean automaticStemDirection) {
        // determine start index, end index
        Interval interval = line.getBeamings().findInterval(xIndex);

        if (interval == null) {
            return;
        }

        int startIndex = interval.getA(), endIndex = interval.getB();

        // decide whether beaming should be up or down
        int sumY = 0;

        for (int i = startIndex; i <= endIndex; i++) {
            Note note = line.getNote(i);
            sumY += automaticStemDirection ? note.getYPos() : (note.isUpper() ? 1 : -1);
        }

        // +1: upper
        // -1: lower
        int beaming = sumY == 0 ? 1 : sumY / Math.abs(sumY);

        double k = Math.atan(
                (double) (line.getNote(endIndex).getYPos() - line.getNote(startIndex).getYPos()) * HALF_LINE_DIST /
                (line.getNote(endIndex).getXPos() - line.getNote(startIndex).getXPos()));
        k = Math.max(-MAX_BEAM_ANGLE, Math.min(k, MAX_BEAM_ANGLE));

        int goodIndex = -1;

        outer:
        for (int i = startIndex; i <= endIndex; i++) {
            double n = line.getNote(i).getYPos() * HALF_LINE_DIST - k * line.getNote(i).getXPos();

            for (int left = i - 1; left >= startIndex; left--) {
                if ((int) Math.round(k * line.getNote(left).getXPos() + n) * beaming > line.getNote(left).getYPos() *
                                                                                       HALF_LINE_DIST * beaming) {
                    continue outer;
                }
            }

            for (int right = i + 1; right <= endIndex; right++) {
                if ((int) Math.round(k * line.getNote(right).getXPos() + n) * beaming > line.getNote(right).getYPos() * HALF_LINE_DIST * beaming) {
                    continue outer;
                }
            }

            goodIndex = i;
            break;
        }

        Note note = line.getNote(goodIndex);
        note.a.lengthening = 0;
        note.setUpper(beaming == 1);
        double n = note.getYPos() * HALF_LINE_DIST - k * note.getXPos();

        for (int left = goodIndex - 1; left >= startIndex; left--) {
            note = line.getNote(left);
            note.setUpper(beaming == 1);
            note.a.lengthening = !note.getNoteType().isGraceNote() ?
                    (int) Math.round(note.getYPos() * HALF_LINE_DIST - (k * note.getXPos() + n)) :
                    0;
        }

        for (int right = goodIndex + 1; right <= endIndex; right++) {
            note = line.getNote(right);
            note.setUpper(beaming == 1);
            note.a.lengthening = !note.getNoteType().isGraceNote() ?
                    (int) Math.round(note.getYPos() * HALF_LINE_DIST - (k * note.getXPos() + n)) :
                    0;
        }
    }

    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;

        if (composition == null) {
            return;
        }

        if ( mainFrame.getPlayMenu().getStopAction() != null) {
            mainFrame.getPlayMenu().getStopAction().actionPerformed(null);
        }
        selectedNotesLine = -1;
        setLineWidth(composition.getLineWidth());

        // global calculate lengthening
        for (int l = 0; l < composition.lineCount(); l++) {
            Line line = composition.getLine(l);

            for (ListIterator<Interval> li = line.getBeamings().listIterator(); li.hasNext(); ) {
                calculateLengthenings(li.next().getA(), line, false);
            }
        }

        for (int i = 0; i < composition.lineCount(); i++) {
            drawWidthIfWiderLine(composition.getLine(i), true);
        }

        if (mainFrame.getLyricsModePanel() != null) {
            mainFrame.getLyricsModePanel().getData();
        }

        spellLyrics();
        setActiveNotePositionToEnd();
        mainFrame.setMode(Mode.NOTE_EDIT);
        mainFrame.fireMusicChanged(null);
        viewChanged();
        repaintImage = true;
        repaint();
    }

    public void spellLyrics() {
        for (int l = 0; l < composition.lineCount(); l++) {
            spellLyrics(composition.getLine(l));
        }
    }

    public void spellLyrics(Line line) {
        // delete the current values
        line.beginRelation = Note.SyllableRelation.NO;

        for (int n = 0; n < line.noteCount(); n++) {
            Note note = line.getNote(n);
            note.a.syllable = "";
            note.a.syllableRelation = Note.SyllableRelation.NO;
        }

        // get the lyrics slice
        int beginIndex = 0;

        for (int j = composition.indexOfLine(line); j > 0; j--) {
            beginIndex = composition.getLyrics().indexOf('\n', beginIndex) + 1;
            if (beginIndex == 0) {
                return;
            }
        }

        int endIndex = composition.getLyrics().indexOf('\n', beginIndex);

        if (endIndex == -1) {
            endIndex = composition.getLyrics().length();
        }

        if (beginIndex == endIndex) {
            return;
        }

        String lyrics = composition.getLyrics().substring(beginIndex, endIndex) + NEWLINE_STRING;

        // calculate the begin relations
        if (lyrics.startsWith("--")) {
            line.beginRelation = Note.SyllableRelation.ONE_DASH;
            lyrics = lyrics.substring(2);
            beginIndex += 2;
        }

        // make the lyrics
        int begin = 0;
        int noteIndex = 0;

        for (int i = 0; i < lyrics.length(); i++) {
            char c = lyrics.charAt(i);

            if (c == '\n' || c == ' ' || c == '-' || c == '_') { //word end
                String syllable = begin < i ? lyrics.substring(begin, i) : Constants.UNDERSCORE;
                Note.SyllableRelation syllableRelation;

                if (c == '\n' || c == ' ') {
                    syllableRelation = Note.SyllableRelation.NO;
                    noteIndex = setSyllableForNextNote(line, noteIndex, syllable, syllableRelation);
                }
                else if (c == '-') {
                    if (lyrics.charAt(i + 1) == '-' || lyrics.charAt(i + 1) == '\n') {
                        syllableRelation = Note.SyllableRelation.ONE_DASH;
                        i++;
                    }
                    else {
                        syllableRelation = Note.SyllableRelation.DASH;
                    }

                    noteIndex = setSyllableForNextNote(line, noteIndex, syllable, syllableRelation);
                }
                else if (c == '_') {
                    int eus = beginIndex + i + 1;

                    while (eus < composition.getLyrics().length() && composition.getLyrics().charAt(eus) == '_' ||
                           eus + 1 < composition.getLyrics().length() && composition.getLyrics().charAt(eus) == '\n' &&
                           composition.getLyrics().charAt(eus + 1) == '_') {
                        eus++;
                    }

                    if (eus < composition.getLyrics().length()) {
                        char eusc = composition.getLyrics().charAt(eus);
                        syllableRelation = eusc == ' ' || eusc == '\n' ||
                                           eusc == '-' ? Note.SyllableRelation.EXTENDER : Note.SyllableRelation.DASH;
                    }
                    else {
                        syllableRelation = Note.SyllableRelation.EXTENDER;
                    }

                    if (i > 0) {
                        noteIndex = setSyllableForNextNote(line, noteIndex, syllable, syllableRelation);
                    }
                    else {
                        line.beginRelation = syllableRelation;
                    }

                    if (!syllable.equals(Constants.UNDERSCORE) && syllableRelation == Note.SyllableRelation.DASH) {
                        noteIndex = setSyllableForNextNote(line, noteIndex, Constants.UNDERSCORE, Note.SyllableRelation.DASH);
                    }
                }

                if (noteIndex >= line.noteCount()) {
                    break;
                }

                begin = i + 1;
            }
        }

        /*System.out.println("Line: "+composition.indexOfLine(line));
        System.out.println("BeginRelation: "+line.beginRelation);
        for(int i=0;i<line.noteCount();i++){
            System.out.println(line.getNote(i).a.syllable+"   Relation: "+line.getNote(i).a.syllableRelation.name());
        }
        System.out.println();*/
    }

    private int setSyllableForNextNote(Line line, int noteIndex, String syllable, Note.SyllableRelation syllableRelation) {
        while (noteIndex < line.noteCount() && !line.getNote(noteIndex).getNoteType().isNote() &&
               !line.getNote(noteIndex).isForceSyllable()) {
            noteIndex++;
        }

        if (noteIndex < line.noteCount()) {
            line.getNote(noteIndex).a.syllable = syllable;
            line.getNote(noteIndex).a.syllableRelation = syllableRelation;
        }

        return noteIndex + 1;
    }

    public Note getSingleSelectedNote() {
        if (selectedNotesLine != -1 || selectionBegin != selectionEnd) {
            return composition.getLine(selectedNotesLine).getNote(selectionBegin);
        }
        else {
            return null;
        }
    }

    public int getStartY() {
        if (composition.getSongTitle().length() == 0) {
            int tempoStartY = middleLine + composition.getLine(0).getTempoChangeYPos() - LINE_DIST * VISIBLE_LINE_NUM;

            if (composition.getRightInfo().length() == 0) {
                return tempoStartY;
            }
            else {
                return Math.min(tempoStartY, composition.getRightInfoStartY());
            }
        }

        return 0;
    }

    public int getSheetWidth() {
        return composition.getLineWidth();
    }

    public int getSheetHeight() {
        if (drawer.getHeight() == 0) {
            Dimension sheetSize = new Dimension((int) (LineWidthChangeDialog.MAX_LINE_WIDTH * RESOLUTION), (int) (
                    LineWidthChangeDialog.MAX_LINE_WIDTH * RESOLUTION * PAGE_HEIGHT / PAGE_WIDTH
            ));
            BufferedImage image = new BufferedImage(sheetSize.width, sheetSize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(Color.white);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            drawer.drawMusicSheet(g2, true, 1d);
            g2.dispose();
        }

        return drawer.getHeight();
    }

    public void drawWidthIfWiderLine(Line line, boolean strict) {
        if (line.noteCount() > 1) {
            Note endNote = line.getNote(line.noteCount() - 1);
            float idealSpace;

            if (strict) {
                idealSpace = endNote.getRealUpNoteRect().width;
            }
            else {
                idealSpace = NOTE_DIST.get(endNote.getNoteType()) * line.getNoteDistChangeRatio() + 20;
                //idealSpace = ND*line.getNoteDistChangeRatio()+20;
            }

            if (line.getNote(line.noteCount() - 1).getXPos() > composition.getLineWidth() - idealSpace) {
                int firstX = line.getNote(0).getXPos();
                float ratio = (composition.getLineWidth() - idealSpace - firstX) / (endNote.getXPos() - firstX);

                for (int i = 1; i < line.noteCount(); i++) {
                    Note note = line.getNote(i);
                    note.setXPos(firstX + Math.round((note.getXPos() - firstX) * ratio));
                }

                line.mulNoteDistChange(ratio);
            }
        }
    }

    public void setInSelection(boolean inSelection) {
        this.inSelection = inSelection;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
        setActiveNotePositionToEnd();
        repaint();
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        noteXPosAdjustment.setEnabled(mode == Mode.NOTE_ADJUSTMENT);
        verticalAdjustment.setEnabled(mode == Mode.VERTICAL_ADJUSTMENT);
        lyricsAdjustment.setEnabled(mode == Mode.LYRICS_ADJUSTMENT);
        repaint();
    }

    public int getLeadingKeysPos() {
        return leadingKeysPos;
    }

    public void setLeadingKeysPos(int leadingKeysPos) {
        this.leadingKeysPos = leadingKeysPos;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    public int getMiddleLine() {
        return middleLine;
    }

    public void setMiddleLine(int middleLine) {
        this.middleLine = middleLine;
    }

    public BaseMsDrawer setDrawer(DrawerType dt) {
        drawer = drawers[dt.ordinal()];

        if (drawer == null) {
            drawer = drawers[0];
        }

        return drawer;
    }

    public boolean isAvailableDrawer(DrawerType dt) {
        return drawers[dt.ordinal()] != null;
    }

    public BaseMsDrawer getBestDrawer() {
        return drawers[0];
    }

    public BaseMsDrawer getDrawer() {
        return drawer;
    }

    public void setDragDisabled(boolean dragDisabled) {
        this.dragDisabled = dragDisabled;
    }

    public void saveProperties() {
        Properties props = mainFrame.getProperties();
        props.setProperty(Constants.CONTROL_PROP, control.name());
    }

    public void setLineWidth(int lineWidth) {
        composition.setLineWidth(lineWidth);
        thisPrefSize.width = lineWidth;
        thisPrefSize.height = Math.round(
                lineWidth > PAGE_SIZE.width ? lineWidth * PAGE_HEIGHT / PAGE_WIDTH : PAGE_SIZE.height);
        setPreferredSize(thisPrefSize);

        if (marginPanel != null) {
            marginPrefSize.width = Math.max(lineWidth, PAGE_SIZE.width) + 80;
            marginPrefSize.height = thisPrefSize.height + 80;
            marginPanel.setPreferredSize(marginPrefSize);
            invalidate();
            marginPanel.invalidate();
            pageOutsidePanel.invalidate();
            scroll.validate();
            repaintImage = true;
            repaint();
        }
    }

    public int getSelectedLine() {
        return selectedLine;
    }

    public NoteSelection getSelection() {
        if (selectedLine != -1) {
            Line line = composition.getLine(selectedLine);
            return new NoteSelection(line, 0, line.noteCount() - 1);
        }
        else if (selectedNotesLine != -1) {
            return new NoteSelection(composition.getLine(selectedNotesLine), selectionBegin, selectionEnd);
        }
        else {
            return null;
        }
    }

    /*
    ----------------------------------------------------------------------
    MouseMotionListner methods
    ------------------------------------------------------------------------
    */
    public void mouseDragged(MouseEvent e) {
        if (dragDisabled) {
            return;
        }

        if (!startedDrag) {
            startedDrag = true;
            startDrag.setLocation(e.getX(), e.getY());
        }

        int realX = e.getX() < 0 ? 0 : e.getX() >= getWidth() ? getWidth() - 1 : e.getX();
        int realY = e.getY() < 0 ? 0 : e.getY() >= getHeight() ? getHeight() - 1 : e.getY();
        dragRectangle.setBounds(Math.min(startDrag.x, realX), Math.min(startDrag.y, realY), Math.abs(
                startDrag.x - realX), Math.abs(startDrag.y - realY));
        repaint();
    }

    public void mouseMoved(MouseEvent me) {
        if (activeNote == null || control != Control.MOUSE || mode != Mode.NOTE_EDIT) {
            return;
        }

        int x = me.getX();
        int y = me.getY();
        newActiveNotePoint.line = (y - composition.getTopSpace()) / rowHeight;

        if (newActiveNotePoint.line < 0 || newActiveNotePoint.line >= composition.lineCount()) {
            return;
        }

        newActiveNotePoint.y = (int) (
                (y - composition.getTopSpace() - newActiveNotePoint.line * rowHeight - HALF_LINE_DIST / 2) /
                HALF_LINE_DIST
        );

        if (newActiveNotePoint.y <= 0 ||
            newActiveNotePoint.y > (INVISIBLE_LINES_NUM_BELOW + VISIBLE_LINE_NUM + INVISIBLE_LINES_NUM_ABOVE) * 2 + 1) {
            return;
        }

        setNewActiveNotePoint(x, newActiveNotePoint.line);
        activeNote.setYPos(newActiveNotePoint.y - (INVISIBLE_LINES_NUM_ABOVE + 3) * 2);

        if (!newActiveNotePoint.equals(activeNotePoint)) {
            repaint();
            activeNotePoint.xIndex = newActiveNotePoint.xIndex;
            activeNotePoint.y = newActiveNotePoint.y;
            activeNotePoint.movement = newActiveNotePoint.movement;
            activeNotePoint.line = newActiveNotePoint.line;
            activeNote.setUpper(defaultUpperNote(activeNote));
            calculateActiveNoteXPos();
        }

        mainFrame.getStatusBar().setPitchString(activeNote.getActiveNotePitchString(composition.getLine(activeNotePoint.line)));
    }

    private void setNewActiveNotePoint(int xPos, int line) {
        xPos -= Note.HOT_SPOT.x;
        int foundX = 0;
        Line l = composition.getLine(line);

        for (int i = 0; i < l.noteCount() - 1; i++) {
            if (l.getNote(i).getXPos() < xPos && xPos <= l.getNote(i + 1).getXPos()) {
                foundX = i + 1;
                break;
            }
        }

        if (foundX == 0) {
            if (l.noteCount() == 0) {
                newActiveNotePoint.movement = 0;
                newActiveNotePoint.xIndex = 0;
            }
            else if (xPos <= l.getNote(0).getXPos()) {
                newActiveNotePoint.movement = FIRST_NOTE_IN_LINE_MOVEMENT;
                newActiveNotePoint.xIndex = 0;
            }
            else {
                newActiveNotePoint.movement = 0;
                newActiveNotePoint.xIndex = l.noteCount();
            }
        }
        else {
            int period = (xPos - l.getNote(foundX - 1).getXPos()) * 4 /
                         (l.getNote(foundX).getXPos() - l.getNote(foundX - 1).getXPos());
            //if(foundX==endNote-1 && period!=0) period=3;
            switch (period) {
                case 0:
                    newActiveNotePoint.movement = 0;
                    newActiveNotePoint.xIndex = foundX - 1;
                    break;

                case 1:
                case 2:
                    newActiveNotePoint.movement = -(l.getNote(foundX).getXPos() - l.getNote(foundX - 1).getXPos()) / 2;
                    newActiveNotePoint.xIndex = foundX;
                    break;

                case 3:
                case 4:
                    newActiveNotePoint.movement = 0;
                    newActiveNotePoint.xIndex = foundX;
                    break;
            }
        }
    }

    /*
    ----------------------------------------------------------------------
    MouseListner methods
    ------------------------------------------------------------------------
    */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            requestFocusInWindow();

            if (inSelection) {
                unSetNoteSelections();
                startDrag.setLocation(e.getX(), e.getY());
                calculateSelection(false);

                if (selectionBegin == -1 &&
                    Math.abs(e.getY() - getNoteYPos(0, (e.getY() - composition.getTopSpace()) / rowHeight)) <=
                    2 * LINE_DIST) {
                    selectedLine = (e.getY() - composition.getTopSpace()) / rowHeight;

                    if (selectedLine < 0 || selectedLine >= composition.lineCount()) {
                        selectedLine = -1;
                    }
                }

                updateEditActions();
                repaintImage = true;
                repaint();
            }
            else if (control == Control.MOUSE) {
                Line line = composition.getLine(activeNotePoint.line);

                if (activeNotePoint.xIndex == line.noteCount()) {
                    addActiveNote(line);
                }
                else if (activeNotePoint.movement != 0) {
                    insertActiveNote(activeNotePoint.xIndex + (activeNotePoint.movement < 0 ? 0 : 1), line);
                }
                else {
                    modifyActiveNote(activeNotePoint.xIndex, line);
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(this, e.getX(), e.getY());
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(this, e.getX(), e.getY());
        }
        else if (startedDrag) {
            startedDrag = false;
            calculateSelection(true);
            repaintImage = true;
            repaint();
        }
    }

    public void mouseEntered(MouseEvent e) {
        if (!isActiveNoteIn && control == Control.MOUSE && mode == Mode.NOTE_EDIT) {
            isActiveNoteIn = true;
            //setCursor(activeNote==null ? Cursor.getDefaultCursor() : emptyCursor);
        }
    }

    public void mouseExited(MouseEvent e) {
        if (isActiveNoteIn && control == Control.MOUSE && mode == Mode.NOTE_EDIT) {
            isActiveNoteIn = false;
            repaint();
            //setCursor(Cursor.getDefaultCursor());
        }
    }

    public void addFocusLostExceptions(Object exception) {
        focusLostExceptions.add(exception);
    }

    /*
    ----------------------------------------------------------------------
    FocusListner methods
    ------------------------------------------------------------------------
    */
    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        if (focusLostExceptions.indexOf(e.getOppositeComponent()) == -1) {
            new FocusLostThread().start();
        }
    }

    public BufferedImage createMusicSheetImageForExport(Color background, double scale, MyBorder border) {
        BufferedImage image = new BufferedImage(
                (int) ((getSheetWidth()) * scale) + border.getWidth(),
                (int) ((getSheetHeight()) * scale) + border.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        createMusicSheetImageForExport(image, background, scale, border);
        return image;
    }

    public void createMusicSheetImageForExport(BufferedImage image, Color background, double scale, MyBorder border) {
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setPaint(background);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        BaseMsDrawer origDrawer = drawer;
        setDrawer(DrawerType.FUGHETTA);
        g2.translate(border.getLeft(), border.getTop());
        drawer.drawMusicSheet(g2, false, scale);
        drawer = origDrawer;
        g2.dispose();
    }

    public void meta(MetaMessage meta) {
        if (meta.getType() == 0) {
            byte[] data = meta.getData();
            playingLine = data[0] << 8 | data[1];
            playingNote = data[2] << 8 | data[3];
            repaint();
        }
    }

    public enum Mode {
        NOTE_EDIT("Song editing"),
        NOTE_ADJUSTMENT("Note adjustment"),
        LYRICS_ADJUSTMENT("Lyrics adjustment"),
        VERTICAL_ADJUSTMENT("Vertical adjustment");

        private String description;

        private Mode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Control {
        MOUSE("Mouse"),
        KEYBOARD("Keyboard");

        private String description;

        private Control(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // rendering
    public enum DrawerType {
        IMAGE("Draft"),
        FUGHETTA("Enhanced");
        private String menuName;

        DrawerType(String menuName) {
            this.menuName = menuName;
        }

        public String getMenuName() {
            return menuName;
        }
    }

    private enum BeamingType {BEAM, TRIPLET, TIE}

    // note positions
    private static class NotePosition {
        int xIndex, y, line;
        int movement;

        public boolean equals(NotePosition np) {
            return xIndex == np.xIndex && y == np.y && line == np.line && movement == np.movement;
        }
    }

    public class NoteSelection {
        public Line line;
        public int begin, end;

        public NoteSelection(Line line, int begin, int end) {
            this.begin = begin;
            this.end = end;
            this.line = line;
        }
    }

    private class FocusLostThread extends Thread {
        public void run() {
            try {
                sleep(500);
            }
            catch (InterruptedException e) {
                LOG.error(e);
            }

            MusicSheet.this.requestFocusInWindow();
        }
    }

    class DeleteAction extends AbstractTextFocusRejectingAction {
        public DeleteAction() {
            super(mainFrame);
            putValue(Action.NAME, "Delete");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editdelete.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        }

        public void doActionPerformed(ActionEvent e) {
            if (selectedNotesLine != -1) {
                Line line = composition.getLine(selectedNotesLine);

                for (int i = selectionEnd; i >= selectionBegin; i--) {
                    deleteNote(i, line);
                }

                calculateLengthenings(selectionBegin - 1, line, true);
                calculateLengthenings(selectionBegin, line, true);
                spellLyrics(line);
            }
            else if (selectedLine != -1 && composition.lineCount() > 1) {
                composition.removeLine(selectedLine);
                spellLyrics();
            }

            unSetNoteSelections();
            setActiveNotePositionToEnd();
            repaintImage = true;
            repaint();
            //mainFrame.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, due));
        }
    }

    class CopyAction extends AbstractTextFocusRejectingAction {
        ArrayList<Note> copyBuffer = new ArrayList<Note>();
        IntervalSet[] intervalSetsCopyBuffer;

        public CopyAction() {
            super(mainFrame);
            putValue(Action.NAME, "Copy");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editcopy.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void doActionPerformed(ActionEvent e) {
            if (selectedNotesLine > -1) {
                Line line = composition.getLine(selectedNotesLine);
                copyBuffer.clear();

                for (int i = selectionBegin; i <= selectionEnd; i++) {
                    copyBuffer.add(line.getNote(i).clone());
                }

                intervalSetsCopyBuffer = line.copyIntervals(selectionBegin, selectionEnd);
            }
        }
    }

    class CutAction extends AbstractTextFocusRejectingAction {
        public CutAction() {
            super(mainFrame);
            putValue(Action.NAME, "Cut");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editcut.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void doActionPerformed(ActionEvent e) {
            copyAction.actionPerformed(e);
            deleteAction.actionPerformed(e);
        }
    }

    class PasteAction extends AbstractTextFocusRejectingAction {
        Control prevPasteControl;

        public PasteAction() {
            super(mainFrame);
            putValue(Action.NAME, "Paste");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editpaste.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void doActionPerformed(ActionEvent e) {
            if (copyAction.copyBuffer.size() > 0) {
                prevPasteControl = control;
                setActiveNote(Note.PASTE_NOTE);
                control = Control.MOUSE;
                inSelection = false;
                repaint();
            }
        }
    }

    class KeyAction extends AbstractAction {
        private int code;

        public KeyAction(int code) {
            this.code = code;
        }

        public void actionPerformed(ActionEvent e) {
            if (mode != Mode.NOTE_EDIT || control != Control.KEYBOARD) {
                return;
            }

            if (activeNote != null) {
                Line line = composition.getLine(activeNotePoint.line);

                if (code == KeyEvent.VK_LEFT) {
                    if (activeNotePoint.xIndex == 0 && (activeNotePoint.movement != 0 || line.noteCount() == 0)) {
                        if (activeNotePoint.line > 0) {
                            activeNotePoint.line--;
                            activeNotePoint.xIndex = composition.getLine(activeNotePoint.line).noteCount();
                            activeNotePoint.movement = 0;
                        }
                        else {
                            return;
                        }
                    }
                    else if (activeNotePoint.movement == 0 && activeNotePoint.xIndex < line.noteCount()) {
                        activeNotePoint.movement = activeNotePoint.xIndex != 0 ?
                                (line.getNote(activeNotePoint.xIndex - 1).getXPos() -
                                 line.getNote(activeNotePoint.xIndex).getXPos()
                                ) / 2 : FIRST_NOTE_IN_LINE_MOVEMENT;
                    }
                    else {
                        activeNotePoint.movement = 0;
                        activeNotePoint.xIndex--;
                    }
                }
                else if (code == KeyEvent.VK_RIGHT) {
                    if (activeNotePoint.xIndex == line.noteCount()) {
                        if (activeNotePoint.line < composition.lineCount() - 1) {
                            activeNotePoint.line++;
                            activeNotePoint.xIndex = 0;
                            activeNotePoint.movement = composition.getLine(activeNotePoint.line).noteCount() ==
                                                       0 ? 0 : FIRST_NOTE_IN_LINE_MOVEMENT;
                        }
                        else {
                            return;
                        }
                    }
                    else if (activeNotePoint.movement == 0) {
                        activeNotePoint.xIndex++;

                        if (activeNotePoint.xIndex < line.noteCount()) {
                            activeNotePoint.movement = activeNotePoint.xIndex != 0 ?
                                    (line.getNote(activeNotePoint.xIndex - 1).getXPos() -
                                     line.getNote(activeNotePoint.xIndex).getXPos()
                                    ) / 2 : FIRST_NOTE_IN_LINE_MOVEMENT;
                        }
                        else {
                            activeNotePoint.movement = 0;
                        }
                    }
                    else {
                        activeNotePoint.movement = 0;
                    }
                }
                else if (code == KeyEvent.VK_UP) {
                    if (activeNote.getYPos() >= -(INVISIBLE_LINES_NUM_ABOVE + 2) * 2) {
                        activeNote.setYPos(activeNote.getYPos() - 1);
                        mainFrame.getStatusBar().setPitchString(activeNote.getActiveNotePitchString(line));
                    }
                }
                else if (code == KeyEvent.VK_DOWN) {
                    if (activeNote.getYPos() <= (INVISIBLE_LINES_NUM_BELOW + 2) * 2) {
                        activeNote.setYPos(activeNote.getYPos() + 1);
                        mainFrame.getStatusBar().setPitchString(activeNote.getActiveNotePitchString(line));
                    }
                }
                else if (code == KeyEvent.VK_ENTER) {
                    if (activeNotePoint.xIndex == line.noteCount()) {
                        addActiveNote(line);
                        activeNotePoint.xIndex = line.noteCount();
                        activeNotePoint.movement = 0;
                    }
                    else if (activeNotePoint.movement != 0) {
                        insertActiveNote(activeNotePoint.xIndex + (activeNotePoint.movement < 0 ? 0 : 1), line);
                    }
                    else {
                        modifyActiveNote(activeNotePoint.xIndex, line);
                    }
                }
                else if (code == KeyEvent.VK_PAGE_UP) {
                    if (activeNotePoint.line > 0) {
                        activeNotePoint.line--;
                        setNewActiveNotePoint(activeNote.getXPos(), activeNotePoint.line);
                        activeNotePoint.xIndex = newActiveNotePoint.xIndex;
                        activeNotePoint.movement = newActiveNotePoint.movement;
                    }
                }
                else if (code == KeyEvent.VK_PAGE_DOWN) {
                    if (activeNotePoint.line + 1 < composition.lineCount()) {
                        activeNotePoint.line++;
                        setNewActiveNotePoint(activeNote.getXPos(), activeNotePoint.line);
                        activeNotePoint.xIndex = newActiveNotePoint.xIndex;
                        activeNotePoint.movement = newActiveNotePoint.movement;
                    }
                }
                else if (code == KeyEvent.VK_BACK_SPACE) {
                    if (line.noteCount() > 0) {
                        deleteNote(line.noteCount() - 1, line);
                        spellLyrics(line);
                        setActiveNotePositionToEnd();
                        repaintImage = true;
                    }
                }

                calculateActiveNoteXPos();
                activeNote.setUpper(defaultUpperNote(activeNote));
                repaint();
            }
        }
    }

    private class BeamingKeyActions extends AbstractAction {
        private BeamingType type;

        public BeamingKeyActions(BeamingType type) {
            this.type = type;
        }

        public void actionPerformed(ActionEvent e) {
            selectedNoteStorage[0] = selectedNotesLine;
            selectedNoteStorage[1] = selectionBegin;
            selectedNoteStorage[2] = selectionEnd;
            selectedNotesLine = activeNotePoint.line;
            Line line = composition.getLine(selectedNotesLine);

            switch (type) {
                case BEAM:
                    if (line.noteCount() >= 2) {
                        selectionBegin = line.noteCount() - 2;

                        while (selectionBegin > 0 && line.getNote(selectionBegin).getNoteType().isGraceNote()) {
                            selectionBegin--;
                        }

                        selectionEnd = line.noteCount() - 1;
                        beamSelectedNotes(line.getBeamings().findInterval(selectionEnd) == null);
                    }
                    break;

                case TRIPLET:
                    int minL = Integer.MAX_VALUE, sumL = 0;

                    for (int i = line.noteCount() - 1; i >= 0; i--) {
                        Note note = line.getNote(i);

                        if (note.getDuration() < minL && note.getDuration() > 0) {
                            minL = note.getDuration();
                        }

                        sumL += note.getDuration();

                        if (sumL == minL * 3) {
                            selectionBegin = i;
                            selectionEnd = line.noteCount() - 1;
                            Interval tupletInterval = line.getTuplets().findInterval(selectionEnd);

                            if (tupletInterval == null) {
                                tupletSelectedNotes(3);
                            }
                            else if (TupletIntervalData.getGrade(tupletInterval) == 3) {
                                untupletSelectedNotes();
                            }
                        }
                    }
                    break;

                case TIE:
                    if (line.noteCount() >= 2) {
                        selectionBegin = line.noteCount() - 2;
                        selectionEnd = line.noteCount() - 1;
                        tieSelectedNotes(line.getTies().findInterval(selectionEnd) == null);
                    }
                    break;

                default:
                    throw new IllegalArgumentException();
            }

            selectedNotesLine = selectedNoteStorage[0];
            selectionBegin = selectedNoteStorage[1];
            selectionEnd = selectedNoteStorage[2];
        }
    }

    //-------------------------------scrollable-------------------------
    private class MsPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 30;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visibleRect.height - 10 : visibleRect.width - 20;
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
