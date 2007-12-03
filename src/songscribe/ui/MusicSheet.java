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

Created on 2005.01.05.,14:24:51
*/

package songscribe.ui;

import songscribe.music.*;
import songscribe.data.PropertyChangeListener;
import songscribe.data.Interval;
import songscribe.data.IntervalSet;
import songscribe.ui.musicsheetdrawer.BaseMsDrawer;
import songscribe.ui.musicsheetdrawer.ImageMsDrawer;
import songscribe.ui.musicsheetdrawer.FughettaDrawer;
import songscribe.ui.adjustment.NoteXPosAdjustment;
import songscribe.ui.adjustment.VerticalAdjustment;
import songscribe.ui.adjustment.LyricsAdjustment;
import songscribe.ui.mainframeactions.PlayActiveNoteThread;

import javax.swing.*;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.Vector;
import java.util.Properties;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 *
 */

public final class MusicSheet extends JComponent implements MouseListener, MouseMotionListener, PropertyChangeListener, FocusListener, MetaEventListener{
    //view constants
    private static final int[] NOTEDIST = {70, 50, 35, 25, 25, 25, 70, 50, 35, 25, 25, 25, 30, 0, 25, 25, 25, 15, 20, 20, 20, 0};
    //private static final int ND = 35;
    //private static final int BEAMEDNOTEDIST = 23;

    private static final int INVISIBLELINESNUMBELOW = 3;
    private static final int INVISIBLELINESNUMABOVE = 3;
    public static final int VISIBLELINENUM = 5;

    public static final int LINEDIST = 8;
    public static final float HALFLINEDIST = LINEDIST/2;
    private static final double MAXBEAMANGLE = 0.4;
    private static final int FIRSTNOTEINLINEMOVEMENT = -15;
    private static final int FIXPREFIXWIDTH = 7;
    public static final int RESOLUTION = 100; //in dpi
    public static final float PAGEWIDTH = 7;
    public static final float PAGEHEIGHT = 9.5f;
    public static final Dimension PAGESIZE = new Dimension(Math.round(PAGEWIDTH*RESOLUTION), Math.round(PAGEHEIGHT*RESOLUTION));
    private static Color activeNoteColor = Color.blue;
    private final static BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 2.0f}, 1.0f);
    private static final String NEWLINESTRING = "\n";

    //musicsheet image acceleration
    private boolean repaintImage = true;
    private BufferedImage msImage;
    private int playingLine=-1, playingNote=-1;

    private boolean playInsertingNote;
    private NoteXPosAdjustment noteXPosAdjustment;
    private VerticalAdjustment verticalAdjustment;
    private LyricsAdjustment lyricsAdjustment;

    private MainFrame mainFrame;

    public enum Mode{
        NOTEEDIT("Song editing"),
        NOTEADJUSTMENT("Note adjustment"),
        LYRICSADJUSTMENT("Lyrics adjustment"),
        VERTICALADJUSTMENT("Vertical adjustment");

        private String description;
        private Mode(String description) {
            this.description = description;
        }
        public String getDescription() {
            return description;
        }
    }
    private Mode mode = Mode.NOTEEDIT;
    private boolean dragDisabled;

    public enum Control{
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
    private Control control;

    //mouse control fields
    private boolean isActiveNoteIn;

    //selection fields
    private boolean inSelection;
    private int selectedLine=-1;
    private int selectedNotesLine=-1;
    private int selectionBegin, selectionEnd;
    private boolean startedDrag;
    private Rectangle dragRectangle = new Rectangle();
    private Point startDrag = new Point();

    //note positions
    private static class NotePosition {
        int xIndex, y, line;
        int movement;

        public boolean equals(NotePosition np) {
            return xIndex == np.xIndex && y == np.y && line == np.line && movement == np.movement;
        }
    }

    private Note activeNote;
    private NotePosition newActiveNotePoint = new NotePosition();
    private NotePosition activeNotePoint = new NotePosition();

    //model
    private Composition composition;

    //scrolling
    private JScrollPane scroll;
    private JPanel pageOutsidePanel;
    private JPanel marginPanel;

     //adjustable fields
    private int underLyricsYPos = 0;
    private int leadingKeysPos = 40;
    private int rowHeight;
    private int middleLine;
    private static int startLocX = 100;

    //popup
    JPopupMenu popup;
    DeleteAction deleteAction;
    CutAction cutAction;
    CopyAction copyAction;
    PasteAction pasteAction;

    //rendering
    public enum DrawerType{
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
    private BaseMsDrawer drawer;
    private BaseMsDrawer[] drawers = new BaseMsDrawer[2];

    public MusicSheet(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        control = Control.valueOf(mainFrame.getProperties().getProperty(Constants.CONTROLPROP));

        try {
            drawers[0] = new ImageMsDrawer(this);
        } catch (Exception e) {
            mainFrame.showErrorMessage("Could not open a necessary font. The program cannot work without it.");
            System.exit(0);
        }
        try{
            drawers[1] = new FughettaDrawer(this);
        } catch (Exception e) {
            drawers[1] = null;
        }
        drawer = drawers[1];
    }

    public void initComponent(){
        composition = new Composition(mainFrame);
        viewChanged();
        msImage = new BufferedImage((int)(LineWidthChangeDialog.MAXIMUMLINEWIDTH*RESOLUTION), (int)(LineWidthChangeDialog.MAXIMUMLINEWIDTH*RESOLUTION*PAGEHEIGHT/PAGEWIDTH), BufferedImage.TYPE_INT_RGB);
        noteXPosAdjustment = new NoteXPosAdjustment(this);
        verticalAdjustment = new VerticalAdjustment(this);
        lyricsAdjustment = new LyricsAdjustment(this);
        //scroll settings
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
        //popup menu settings
        popup = new JPopupMenu();
        popup.add(new JMenuItem(cutAction = new CutAction()));
        popup.add(new JMenuItem(copyAction = new CopyAction()));
        popup.add(new JMenuItem(pasteAction = new PasteAction()));
        popup.addSeparator();
        popup.add(new JMenuItem(deleteAction = new DeleteAction()));
        updateEditActions();
        int[] keyCodes = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_ENTER, KeyEvent.VK_BACK_SPACE};
        for(int keyCode: keyCodes){
            Object o = new Object();
            getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), o);
            getActionMap().put(o, new KeyAction(keyCode));
        }
        int[] beamKeyCodes = {KeyEvent.VK_B, KeyEvent.VK_T, KeyEvent.VK_T};
        int[] beamKeyMasks = {0, 0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()};
        for(int i=0;i<beamKeyCodes.length;i++){
            Object o = new Object();
            getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(beamKeyCodes[i], beamKeyMasks[i]), o);
            getActionMap().put(o, new BeamingKeyActions(i));
        }

        mainFrame.addProperyChangeListener(this);
        if(MainFrame.sequencer!=null)MainFrame.sequencer.addMetaEventListener(this);
        mainFrame.unmodifiedDocument();
    }

    public void musicChanged(Properties props) {
        composition.musicChanged(props);
        playInsertingNote = props.getProperty(Constants.PLAYINSERTINGNOTE).equals(Constants.TRUEVALUE);
    }

    public void viewChanged(){
        middleLine = (INVISIBLELINESNUMABOVE + 3) * LINEDIST + composition.getTopSpace();
        rowHeight = (INVISIBLELINESNUMBELOW + VISIBLELINENUM + INVISIBLELINESNUMABOVE + 1)*LINEDIST + composition.getRowHeight();
    }

    public JScrollPane getScrolledMusicSheet() {
        return scroll;
    }

    public void setRepaintImage(boolean repaintImage) {
        this.repaintImage = repaintImage;
    }

    public boolean isNoteSelected(int xIndex, int line){
        return selectedNotesLine==line && selectionBegin<=xIndex && xIndex<=selectionEnd;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(msImage==null)return;
        Graphics2D g2 = (Graphics2D) g;

        if(repaintImage){
            Graphics2D g2MsImage = msImage.createGraphics();
            g2MsImage.setPaint(Color.white);
            g2MsImage.fillRect(0, 0, msImage.getWidth(), msImage.getHeight());
            drawer.drawMusicSheet(g2MsImage, true, 1d);
            g2MsImage.dispose();
            repaintImage = false;
        }
        g2.drawImage(msImage, 0, 0, null);

        if(mode==Mode.NOTEEDIT){
            //drawing the activeNote
            if (activeNote != null && (control==MusicSheet.Control.KEYBOARD || isActiveNoteIn)) {
                if(activeNote!=Note.GLISSANDONOTE){
                    int actX = activeNote.getXPos();
                    if(actX>composition.getLineWidth()-10){
                        activeNote.setXPos(composition.getLineWidth()-12);
                    }
                    drawer.paintNote(g2, activeNote, activeNotePoint.line, false, activeNoteColor);
                    activeNote.setXPos(actX);
                }else if(activeNotePoint.xIndex>0){
                    g2.setPaint(activeNoteColor);
                    drawer.drawGlissando(g2, activeNotePoint.xIndex-1, activeNote.getYPos(), activeNotePoint.line);
                }
            }
        }else if(mode==Mode.NOTEADJUSTMENT){
            noteXPosAdjustment.repaint(g2);
        }else if(mode==Mode.VERTICALADJUSTMENT){
            verticalAdjustment.repaint(g2);
        }else if(mode==Mode.LYRICSADJUSTMENT){
            lyricsAdjustment.repaint(g2);
        }

        //painting selection
        if (startedDrag) {
            g2.setStroke(dashedStroke);
            g2.draw(dragRectangle);
        }

        //painting playing
        if(playingLine!=-1 && playingNote!=-1){
            Line line = composition.getLine(playingLine);
            drawer.paintNote(g2, line.getNote(playingNote), playingLine, line.getBeamings().findInterval(playingNote)!=null, Color.magenta);
            playingLine = playingNote = -1;
        }
    }

    public int getNoteYPos(int yPos, int line){
        return (int)(middleLine+yPos*HALFLINEDIST+line*rowHeight);
    }

    public int getUnderLyricsYPos() {
        return middleLine+composition.lineCount()*rowHeight+underLyricsYPos;
    }

    public void setActiveNote(Note activeNote) {
        if(activeNote!=null){
            if(this.activeNote!=null){
                activeNote.setYPos(this.activeNote.getYPos());
                activeNote.setXPos(this.activeNote.getXPos());
            }else{
                this.activeNote = activeNote;
                setActiveNotePositionToEnd();
            }
            activeNote.setUpper(defaultUpperNote(activeNote));
        }
        this.activeNote = activeNote;
        repaint();
        //setCursor(activeNote==null ? Cursor.getDefaultCursor() : emptyCursor);
    }

    public Note getActiveNote() {
        return activeNote;
    }

    public void unSetNoteSelections() {
        selectedLine = -1;
        selectedNotesLine = -1;
        updateEditActions();
    }

    private void updateEditActions() {
        deleteAction.setEnabled(selectedNotesLine!=-1 || selectedLine!=-1);
        copyAction.setEnabled(selectedNotesLine!=-1);
        cutAction.setEnabled(selectedNotesLine!=-1);
        pasteAction.setEnabled(copyAction.copyBuffer.size()>0);
    }

    private boolean commonAddInsertModifyActiveNoteCommands(int xIndex, Line line){
        unSetNoteSelections();
        //if the active note is glissando, it needs different care
        if(activeNote.getNoteType()==NoteType.GLISSANDO){
            if(activeNotePoint.xIndex>0){
                line.getNote(activeNotePoint.xIndex-1).setGlissando(activeNote.getYPos());
            }
            repaintImage = true;
            repaint();
            return true;
        }
        if(activeNote.getNoteType()==NoteType.REPEATLEFT &&
                xIndex-1>=0 && line.getNote(xIndex-1).getNoteType()==NoteType.REPEATRIGHT){
            Note repeatLeftRight = new RepeatLeftRight();
            repeatLeftRight.setXPos(line.getNote(xIndex-1).getXPos());
            line.setNote(xIndex-1, repeatLeftRight);
            repaintImage = true;
            repaint();
            return true;
        }
        if(activeNote.getNoteType()==NoteType.REPEATRIGHT &&
                xIndex<line.noteCount() && line.getNote(xIndex).getNoteType()==NoteType.REPEATLEFT){
            Note repeatLeftRight = new RepeatLeftRight();
            repeatLeftRight.setXPos(line.getNote(xIndex).getXPos());                        
            line.setNote(xIndex, repeatLeftRight);
            repaintImage = true;
            repaint();
            return true;
        }
        if(activeNote.getNoteType()==NoteType.PASTE){
            //if the user tries to insert into triplet, he will get an error message
            Interval iv = line.getTuplets().findInterval(xIndex-1);
            if(iv!=null && xIndex-1<iv.getB()){
                mainFrame.showErrorMessage("Cannot insert into a triplet.");
                return true;
            }

            line.removeInterval(xIndex-1, xIndex);
            int diff = (xIndex==line.noteCount()?calculateLastNoteXPos(line, copyAction.copyBuffer.get(0)):line.getNote(xIndex).getXPos())-copyAction.copyBuffer.get(0).getXPos();
            int copySize = copyAction.copyBuffer.size();
            for(int i=0;i<copySize;i++){
                Note note = copyAction.copyBuffer.get(i);
                note.setXPos(note.getXPos()+diff);
                line.addNote(xIndex+i, note.clone());
            }
            line.pasteIntervals(copyAction.intervalSetsCopyBuffer, xIndex);
            Note lastNote = copyAction.copyBuffer.get(copySize-1);
            int shift = Math.round((NOTEDIST[lastNote.getNoteType().ordinal()]+lastNote.getAccidental().getNb()*FIXPREFIXWIDTH)*line.getNoteDistChangeRatio())+lastNote.getXPos()-copyAction.copyBuffer.get(0).getXPos();
            //int shift = Math.round((ND+lastNote.getAccidental().getNb()*FIXPREFIXWIDTH)*line.getNoteDistChangeRatio())+lastNote.getXPos()-copyAction.copyBuffer.get(0).getXPos();
            for (int i=xIndex+copySize;i<line.noteCount();i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos()+shift);
            }
            control = pasteAction.prevPasteControl;
            drawWidthIfWiderLine(line, false);
            spellLyrics(line);
            for(int i=xIndex;i<xIndex+copySize;i++){
                Interval interval=line.getBeamings().findInterval(i);
                if(interval!=null){
                    calculateLenghtenings(i, line);
                    i=interval.getB();
                }
            }
            activeNote = null;
            inSelection = true;
            repaintImage = true;
            repaint();
            return true;
        }
        if(playInsertingNote && activeNote.getNoteType().isNote()){
            PlayActiveNoteThread playActiveNoteThread = new PlayActiveNoteThread(activeNote.getActiveNotePitch(line));
            playActiveNoteThread.start();
        }
        return false;
    }

    private void calculateActiveNoteXPos(){
        if(activeNote==null){
            return;
        }
        Line line =composition.getLine(activeNotePoint.line);
        if(line.noteCount()==activeNotePoint.xIndex){
            activeNote.setXPos(calculateLastNoteXPos(line, activeNote));
        }else{
            activeNote.setXPos(line.getNote(activeNotePoint.xIndex).getXPos()+activeNotePoint.movement);
        }

    }

    public void setActiveNotePositionToEnd(){
        activeNotePoint.movement = 0;
        activeNotePoint.line = composition.lineCount()-1;
        activeNotePoint.xIndex = composition.getLine(activeNotePoint.line).noteCount();
        calculateActiveNoteXPos();
    }

    public static boolean defaultUpperNote(Note note){
        return note.getYPos()>=0;
    }

    public static int calculateLastNoteXPos(Line line, Note note){
        Note lastNote = line.noteCount()>0 ? line.getNote(line.noteCount()-1) : null;
        return line.noteCount()==0 ? startLocX :
                lastNote.getXPos()+Math.round((NOTEDIST[lastNote.getNoteType().ordinal()]+note.getAccidental().getNb()*FIXPREFIXWIDTH+(note.isAccidentalInParenthesis()?8:0))*line.getNoteDistChangeRatio());
                //lastNote.getXPos()+Math.round((ND+note.getAccidental().getNb()*FIXPREFIXWIDTH)*line.getNoteDistChangeRatio());
    }

    private void postCommonAddInsertModifyActiveNoteCommands(Line line) {
        //mainFrame.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, new ModifyUndoableEdit(oldNote, oldNoteInfo, xIndex)));
        setActiveNote(activeNote.getNoteType().newInstance());
        mainFrame.getInsertMenu().updateState();
        spellLyrics(line);
        drawWidthIfWiderLine(line, false);
        repaintImage = true;
        repaint();
    }

    public void addActiveNote(Line line) {
        if (activeNote != null) {
            if(commonAddInsertModifyActiveNoteCommands(line.noteCount(), line))return;
            activeNote.setXPos(calculateLastNoteXPos(line, activeNote));
            line.addNote(activeNote);
            //mainFrame.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, new InsertUndoableEdit(cloneActiveNote, ni, noteInfo.size()-2)));

            //deciding automatic beaming
            if(activeNote.getNoteType().isBeamable() &&
                    line.noteCount()>=2 && line.getTuplets().findInterval(line.noteCount()-2)==null){
                int sum=0;
                for(int i = line.noteCount()-2;i>=0;i--){
                    if(line.getNote(i).getNoteType()==NoteType.QUAVER){
                        sum+=2;
                    }else if(line.getNote(i).getNoteType()==NoteType.SEMIQUAVER || line.getNote(i).getNoteType()==NoteType.DEMISEMIQUAVER){
                        sum+=1;
                    }else{
                        break;
                    }
                    Interval interval = line.getBeamings().findInterval(i);
                    if(interval!=null && interval.getA()==i){
                        break;
                    }
                }
                if(activeNote.getNoteType()==NoteType.QUAVER && sum>0 && sum%2==0 && sum%4!=0 ||
                   (activeNote.getNoteType()==NoteType.SEMIQUAVER || activeNote.getNoteType()==NoteType.DEMISEMIQUAVER) && sum>0 && sum%4!=0){
                    line.getBeamings().addInterval(line.noteCount()-2, line.noteCount()-1);
                    //activeNote.setXPos(activeNote.getXPos()-(ND-BEAMEDNOTEDIST));
                }
                calculateLenghtenings(line.noteCount()-1, line);
            }
            postCommonAddInsertModifyActiveNoteCommands(line);
        }
    }

    private void insertActiveNote(int xIndex, Line line) {
        if (activeNote != null) {
            if(commonAddInsertModifyActiveNoteCommands(xIndex, line))return;
            //if the user tries to insert into triplet, he will get an error message
            Interval iv = line.getTuplets().findInterval(xIndex-1);
            if(iv!=null && xIndex-1<iv.getB()){
                mainFrame.showErrorMessage("Cannot insert into a triplet.");
                return;
            }
            line.removeInterval(xIndex-1, xIndex);
            activeNote.setXPos(line.getNote(xIndex).getXPos()+activeNote.getAccidental().getNb()*FIXPREFIXWIDTH);
            line.addNote(xIndex, activeNote);
            int shift = Math.round((NOTEDIST[activeNote.getNoteType().ordinal()]+activeNote.getAccidental().getNb()*FIXPREFIXWIDTH)*line.getNoteDistChangeRatio());
            //int shift = Math.round((ND+activeNote.getAccidental().getNb()*FIXPREFIXWIDTH)*line.getNoteDistChangeRatio());
            for (int i=xIndex+1;i<line.noteCount();i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos()+shift);
            }
            postCommonAddInsertModifyActiveNoteCommands(line);
        }
    }

    private void modifyActiveNote(int xIndex, Line line) {
        if (activeNote != null) {
            if(commonAddInsertModifyActiveNoteCommands(xIndex, line))return;
            Note oldNote = line.getNote(xIndex);
            if(line.getTuplets().findInterval(xIndex)!=null && oldNote.getNoteType()!=activeNote.getNoteType()){
                mainFrame.showErrorMessage("Cannot modify a triplet with different note type.");
                return;
            }
            activeNote.setXPos(oldNote.getXPos()+(activeNote.getAccidental().getNb()-oldNote.getAccidental().getNb())*FIXPREFIXWIDTH);
            int shift = Math.round((NOTEDIST[activeNote.getNoteType().ordinal()] - NOTEDIST[oldNote.getNoteType().ordinal()]
                    +(activeNote.getAccidental().getNb()-oldNote.getAccidental().getNb())*FIXPREFIXWIDTH)*line.getNoteDistChangeRatio());
            line.setNote(xIndex, activeNote);
            for (int i=xIndex+1;i<line.noteCount();i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos()+shift);
            }
            //arranging to beaming
            if(oldNote.getNoteType()!=activeNote.getNoteType()){
                line.removeInterval(xIndex-1, xIndex+1);
                calculateLenghtenings(xIndex-1, line);
                calculateLenghtenings(xIndex+1, line);
            }else{
                calculateLenghtenings(xIndex, line);
            }

            //arranging the ties
            if(oldNote.getYPos()!=activeNote.getYPos()){
                line.getTies().removeInterval(xIndex-1, xIndex+1);
            }

            postCommonAddInsertModifyActiveNoteCommands(line);
        }
    }

    private void deleteNote(int xIndex, Line line) {
        if(xIndex<line.noteCount()-1){
            int shift = line.getNote(xIndex).getXPos()-line.getNote(xIndex+1).getXPos();
            for (int i=xIndex+1;i<line.noteCount();i++) {
                line.getNote(i).setXPos(line.getNote(i).getXPos()+shift);
            }
        }
        line.removeNote(xIndex);
    }

    public void beamSelectedNotes(boolean beam){
        if(selectedNotesLine==-1 || selectionBegin==selectionEnd){
            JOptionPane.showMessageDialog(mainFrame, "You must select more than one note first to "+(beam?"beam":"unbeam")+" them.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Line line = composition.getLine(selectedNotesLine);

        for(int i=selectionBegin;i<=selectionEnd;i++){
            NoteType nt = line.getNote(i).getNoteType();
            if(!nt.isBeamable() && nt!=NoteType.GRACEQUAVER){
                JOptionPane.showMessageDialog(mainFrame, "You can "+(beam?"beam":"unbeam")+" only quavers, semiquavers and demisemiquavers.",
                        mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        if(beam){
            line.getBeamings().addInterval(selectionBegin, selectionEnd);
            calculateLenghtenings(selectionBegin, line);
        }else{
            line.getBeamings().removeInterval(selectionBegin, selectionEnd);
            calculateLenghtenings(selectionBegin, line);
            calculateLenghtenings(selectionEnd, line);
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void untupletSelectedNotes(){
        if(selectedNotesLine==-1 || selectionBegin==selectionEnd){
            JOptionPane.showMessageDialog(mainFrame, "You must select more than one note first to remove a tuplet.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Line line = composition.getLine(selectedNotesLine);
        Interval begin = line.getTuplets().findInterval(selectionBegin);
        Interval end = line.getTuplets().findInterval(selectionEnd);
        if(begin==null || begin!=end){
            JOptionPane.showMessageDialog(mainFrame, "You must select exactly the notes that are in one tuplet to remove it.",
                mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        line.getTuplets().removeInterval(selectionBegin, selectionEnd);
        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void tupletSelectedNotes(int numeral){
        if(selectedNotesLine==-1 || selectionBegin==selectionEnd){
            JOptionPane.showMessageDialog(mainFrame, "You must select more than one note first to make a tuplet.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        int minL=Integer.MAX_VALUE, sumL=0;
        boolean canBeamed = true;
        for(int i=selectionBegin;i<=selectionEnd;i++){
            Note note = line.getNote(i);
            Interval iv = line.getTuplets().findInterval(i);
            if(iv!=null){
                JOptionPane.showMessageDialog(mainFrame, "You cannot tuplet notes that already are tuplet.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if(note.getDuration()<minL && note.getDuration()>0){
                minL=note.getDuration();
            }
            sumL+=note.getDuration();
            if(!note.getNoteType().isBeamable()){
                canBeamed = false;
            }
        }

        if(minL*numeral!=sumL){
            JOptionPane.showMessageDialog(mainFrame, "Unappliable tupleting.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if(canBeamed){
            beamSelectedNotes(true);
        }

        line.getTuplets().addInterval(selectionBegin, selectionEnd, Integer.toString(numeral));

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void tieSelectedNotes(boolean tie){
        if(selectedNotesLine==-1 || selectionBegin==selectionEnd){
            JOptionPane.showMessageDialog(mainFrame, "You must select more than one note first to "+(tie?"tie":"untie")+" them.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        int pitch=line.getNote(selectionBegin).getPitch();
        for(int i=selectionBegin+1;i<=selectionEnd;i++){
            if(line.getNote(i).getPitch()!=pitch){
                JOptionPane.showMessageDialog(mainFrame, "The selected notes must be of the same pitch.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        if(tie){
            line.getTies().addInterval(selectionBegin, selectionEnd);
        }else{
            line.getTies().removeInterval(selectionBegin, selectionEnd);
        }
        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void makeFsEndingOnSelectedNotes(boolean fsEnding){
        if(selectedNotesLine==-1 || selectionBegin==selectionEnd){
            JOptionPane.showMessageDialog(mainFrame, "You must select more than one note first to "+(fsEnding?"make":"remove")+" first-second endings.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Line line = composition.getLine(selectedNotesLine);
        if(fsEnding){
            boolean existsRepeat = false;
            for(int i=selectionBegin;i<=selectionEnd;i++){
                if(line.getNote(i).getNoteType()==NoteType.REPEATRIGHT){
                    existsRepeat = true;
                    break;
                }
            }
            if(!existsRepeat){
                int answ = JOptionPane.showConfirmDialog(mainFrame, "It does not make sense to create a first-second ending without a right side repeat.\nDo you want to continue anyway?",
                        mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if(answ==JOptionPane.NO_OPTION) return;
            }
        }

        if(fsEnding){
            line.getFsEndings().addInterval(selectionBegin, selectionEnd);
        }else{
            line.getFsEndings().removeInterval(selectionBegin, selectionEnd);
        }
        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    public void makeTrillOnSelectedNotes(boolean trill){
        if(selectedNotesLine==-1){
            JOptionPane.showMessageDialog(mainFrame, "You must select at least one note first to "+(trill?"make a":"remove")+" trill.",
                    mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Line line = composition.getLine(selectedNotesLine);

        for(int i=selectionBegin;i<=selectionEnd;i++){
            line.getNote(i).setTrill(trill);
        }

        composition.modifiedComposition();
        repaintImage = true;
        repaint();
    }

    private void calculateSelection(boolean fromRectangle) {
        Rectangle helper = new Rectangle();
        selectedLine = selectedNotesLine = selectionBegin = selectionEnd = -1;
        for (int l=0;l<composition.lineCount();l++) {
            if(selectedNotesLine!=-1 && selectedNotesLine!=l){
                break;
            }
            Line line = composition.getLine(l);
            for(int n=0;n<line.noteCount();n++){
                Note note = line.getNote(n);
                if(line.getBeamings().findInterval(n)!=null){
                    helper.setBounds(note.isUpper() ? Crotchet.REALUPNOTERECT : Crotchet.REALDOWNNOTERECT);
                }else{
                    helper.setBounds(note.isUpper() ? note.getRealUpNoteRect() : note.getRealDownNoteRect());
                }
                helper.translate(note.getXPos(), getNoteYPos(note.getYPos(), l)-Note.HOTSPOT.y);
                if (fromRectangle && dragRectangle.contains(helper) ||
                        !fromRectangle && helper.contains(startDrag)) {
                    selectedNotesLine = l;
                    if(selectionBegin==-1){
                        selectionBegin = n;
                    }
                    selectionEnd = n;
                }
            }
        }
        updateEditActions();
    }
    
    private void calculateLenghtenings(int xIndex, Line line){
        //determining startindex, endindex
        Interval interval = line.getBeamings().findInterval(xIndex);
        if(interval==null){
            return;
        }
        int startIndex=interval.getA(), endIndex=interval.getB();

        //decide wheater beaming should be upside or downside
        int sumY=0;
        for(int i=startIndex;i<=endIndex;i++){
            sumY+=line.getNote(i).getYPos();
        }
        //+1: upper
        //-1: lower
        int beaming = sumY==0 ? 1 : sumY/Math.abs(sumY);

        double k = Math.atan((double) (line.getNote(endIndex).getYPos() - line.getNote(startIndex).getYPos()) * HALFLINEDIST /
                    (line.getNote(endIndex).getXPos()-line.getNote(startIndex).getXPos()));
        k = Math.max(-MAXBEAMANGLE, Math.min(k, MAXBEAMANGLE));

        int goodIndex = -1;
        outer:
        for(int i=startIndex;i<=endIndex;i++){
            double n=line.getNote(i).getYPos()*HALFLINEDIST-k*line.getNote(i).getXPos();
            for(int left=i-1;left>=startIndex;left--){
                if((int)Math.round(k*line.getNote(left).getXPos()+n)*beaming>line.getNote(left).getYPos()*HALFLINEDIST*beaming){
                    continue outer;
                }
            }
            for(int right=i+1;right<=endIndex;right++){
                if((int)Math.round(k*line.getNote(right).getXPos()+n)*beaming>line.getNote(right).getYPos()*HALFLINEDIST*beaming){
                    continue outer;
                }
            }
            goodIndex = i;
            break;
        }
        Note note = line.getNote(goodIndex);
        note.a.lengthening = 0;
        note.setUpper(beaming==1);
        double n=note.getYPos()*HALFLINEDIST-k*note.getXPos();
        for(int left=goodIndex-1;left>=startIndex;left--){
            note = line.getNote(left);
            note.setUpper(beaming==1);
            note.a.lengthening = note.getNoteType()!=NoteType.GRACEQUAVER ?
                    (int)Math.round(note.getYPos()*HALFLINEDIST-(k*note.getXPos()+n)) : 0;
        }
        for(int right=goodIndex+1;right<=endIndex;right++){
            note = line.getNote(right);
            note.setUpper(beaming==1);
            note.a.lengthening = note.getNoteType()!=NoteType.GRACEQUAVER ?
                    (int)Math.round(note.getYPos()*HALFLINEDIST-(k*note.getXPos()+n)) : 0;
        }
    }

    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition){
        mainFrame.getPlayMenu().getStopAction().actionPerformed(null);
        this.composition = composition;
        selectedNotesLine = -1;
        setLineWidth(composition.getLineWidth());
        //global calculate lenghenings
        for(int l=0;l<composition.lineCount();l++){
            Line line = composition.getLine(l);
            for(ListIterator<Interval> li = line.getBeamings().listIterator();li.hasNext();){
                calculateLenghtenings(li.next().getA(), line);
            }
        }
        for(int i=0;i<composition.lineCount();i++){
            drawWidthIfWiderLine(composition.getLine(i), true);
        }

        spellLyrics();
        setActiveNotePositionToEnd();
        mainFrame.setMode(Mode.NOTEEDIT);        
        mainFrame.fireMusicChanged(null);
        viewChanged();
        repaintImage = true;
        repaint();
    }

    public void spellLyrics(){
        for(int l=0;l<composition.lineCount();l++){
            spellLyrics(composition.getLine(l));
        }
    }

    public void spellLyrics(Line line){
        //deleting the current values
        line.beginRelation = Note.SyllableRelation.NO;
        for(int n=0;n<line.noteCount();n++){
            Note note = line.getNote(n);
            note.a.syllable = "";
            note.a.syllableRelation = Note.SyllableRelation.NO;
        }

        //getting the lyrics slice
        int beginIndex = 0;
        for(int j=composition.indexOfLine(line);j>0;j--){
            beginIndex = composition.getLyrics().indexOf('\n', beginIndex)+1;
            if(beginIndex==0)return;
        }
        int endIndex = composition.getLyrics().indexOf('\n', beginIndex);
        if(endIndex==-1)endIndex = composition.getLyrics().length();
        if(beginIndex==endIndex)return;
        String lyrics = composition.getLyrics().substring(beginIndex, endIndex)+NEWLINESTRING;

        //making the lyrics
        int begin = 0;
        int noteIndex = 0;
        for(int i=0;i<lyrics.length();i++){
            char c = lyrics.charAt(i);
            if(c=='\n' || c==' '|| c=='-' || c=='_'){ //word end
                String syllable = begin<i ? lyrics.substring(begin, i) : Constants.UNDERSCORE;
                Note.SyllableRelation syllableRelation;
                if(c=='\n' || c==' '){
                    syllableRelation = Note.SyllableRelation.NO;
                    noteIndex = setSyllableForNextNote(line, noteIndex, syllable, syllableRelation);
                }else if(c=='-'){
                    if(lyrics.charAt(i+1)=='-' || lyrics.charAt(i+1)=='\n'){
                        syllableRelation = Note.SyllableRelation.ONEDASH;
                        i++;
                    }else{
                        syllableRelation = Note.SyllableRelation.DASH;
                    }
                    noteIndex = setSyllableForNextNote(line, noteIndex, syllable, syllableRelation);
                }else if(c=='_'){
                    int eus = beginIndex+i+1;
                    while(eus<composition.getLyrics().length() && composition.getLyrics().charAt(eus)=='_' ||
                                eus+1<composition.getLyrics().length() && composition.getLyrics().charAt(eus)=='\n' && composition.getLyrics().charAt(eus+1)=='_')eus++;
                    if(eus<composition.getLyrics().length()){
                        char eusc = composition.getLyrics().charAt(eus);
                        syllableRelation = eusc==' ' || eusc=='\n' || eusc=='-'? Note.SyllableRelation.EXTENDER : Note.SyllableRelation.DASH;
                    }else{
                        syllableRelation = Note.SyllableRelation.EXTENDER;
                    }
                    if(i>0){
                        noteIndex = setSyllableForNextNote(line, noteIndex, syllable, syllableRelation);
                    }else{
                        line.beginRelation = syllableRelation;
                    }
                    if(syllable!=Constants.UNDERSCORE && syllableRelation==Note.SyllableRelation.DASH){
                        noteIndex = setSyllableForNextNote(line, noteIndex, Constants.UNDERSCORE, Note.SyllableRelation.DASH);
                    }
                }
                if(noteIndex>=line.noteCount())break;
                begin=i+1;
            }
        }

        /*System.out.println("Line: "+composition.indexOfLine(line));
        System.out.println("BeginRelation: "+line.beginRelation);
        for(int i=0;i<line.noteCount();i++){
            System.out.println(line.getNote(i).a.syllable+"   Relation: "+line.getNote(i).a.syllableRelation.name());
        }
        System.out.println();*/
    }

    private int setSyllableForNextNote(Line line, int noteIndex, String syllable, Note.SyllableRelation syllableRelation){
        while(noteIndex<line.noteCount() && !line.getNote(noteIndex).getNoteType().isNote()) noteIndex++;
        if(noteIndex<line.noteCount()){
            line.getNote(noteIndex).a.syllable = syllable;
            line.getNote(noteIndex).a.syllableRelation = syllableRelation;
        }
        return noteIndex+1;
    }

    public Note getSingleSelectedNote(){
        if(selectedNotesLine!=-1 || selectionBegin!=selectionEnd){
            return composition.getLine(selectedNotesLine).getNote(selectionBegin);
        }else{
            return null;
        }
    }

    public int getStartY(){
        if(composition.getSongTitle().length()==0){
            int tempoStartY = middleLine + composition.getLine(0).getTempoChangeYPos()-LINEDIST*VISIBLELINENUM;
            if(composition.getRightInfo().length()==0){
                return tempoStartY;
            }else{
                return Math.min(tempoStartY, composition.getRightInfoStartY());
            }
        }
        return 0;
    }

    public int getSheetWidth(){
        return composition.getLineWidth();
    }

    public int getSheetHeight(){
        int height = composition.lineCount()*rowHeight+composition.getTopSpace()+composition.getLyricsFont().getSize()*2/3;
        if(composition.getUnderLyrics().length()!=0 || composition.getTranslatedLyrics().length()!=0){
            height+=rowHeight/2+underLyricsYPos;
            height+=(Utilities.lineCount(composition.getUnderLyrics())+Utilities.lineCount(composition.getTranslatedLyrics())-1)*composition.getLyricsFont().getSize();
            if(composition.getTranslatedLyrics().length()!=0){
                height+=composition.getLyricsFont().getSize();
            }
        }else{
            height+=-rowHeight/2+composition.getLine(composition.lineCount()-1).getLyricsYPos();
        }
        height-=getStartY();
        return height;
    }

    public void drawWidthIfWiderLine(Line line, boolean strict){
        if(line.noteCount()>1){
            Note endNote = line.getNote(line.noteCount()-1);
            float idealSpace;
            if(strict){
                idealSpace = endNote.getRealUpNoteRect().width;
            }else{
                idealSpace = NOTEDIST[endNote.getNoteType().ordinal()]*line.getNoteDistChangeRatio()+20;
                //idealSpace = ND*line.getNoteDistChangeRatio()+20;
            }
            if(line.getNote(line.noteCount()-1).getXPos()>composition.getLineWidth()-idealSpace){
                int firstX = line.getNote(0).getXPos();
                float ratio = (composition.getLineWidth()-idealSpace-firstX)/(endNote.getXPos()-firstX);
                for(int i=1;i<line.noteCount();i++){
                    Note note = line.getNote(i);
                    note.setXPos(firstX+Math.round((note.getXPos()-firstX)*ratio));
                }
                line.mulNoteDistChange(ratio);
            }
        }
    }

    public void setInSelection(boolean inSelection) {
        this.inSelection = inSelection;
    }

    public void setControl(Control control) {
        this.control = control;
        setActiveNotePositionToEnd();
        repaint();
    }

    public Control getControl() {
        return control;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        noteXPosAdjustment.setEnabled(mode==Mode.NOTEADJUSTMENT);
        verticalAdjustment.setEnabled(mode==Mode.VERTICALADJUSTMENT);
        lyricsAdjustment.setEnabled(mode==Mode.LYRICSADJUSTMENT);
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

    public static void setStartLocX(int startLocX) {
        MusicSheet.startLocX = startLocX;
    }

    public BaseMsDrawer setDrawer(DrawerType dt){
        drawer = drawers[dt.ordinal()];
        if(drawer==null){
            drawer = drawers[0];
        }
        return drawer;
    }

    public boolean isAvailableDrawer(DrawerType dt){
        return drawers[dt.ordinal()]!=null;
    }

    public BaseMsDrawer getBestDrawer(){
        return drawers[drawers[1]==null ? 0 : 1];
    }

    public BaseMsDrawer getDrawer() {
        return drawer;
    }

    public void setDragDisabled(boolean dragDisabled) {
        this.dragDisabled = dragDisabled;
    }

    public void saveProperties(){
        Properties props = mainFrame.getProperties();
        props.setProperty(Constants.CONTROLPROP, control.name());
    }

    private Dimension thisPrefSize = new Dimension();
    private Dimension marginPrefSize = new Dimension();
    public void setLineWidth(int lineWidth) {
        composition.setLineWidth(lineWidth);
        thisPrefSize.width = lineWidth;
        thisPrefSize.height = Math.round(lineWidth>PAGESIZE.width ? lineWidth*PAGEHEIGHT/PAGEWIDTH : PAGESIZE.height);
        setPreferredSize(thisPrefSize);
        if(marginPanel!=null){
            marginPrefSize.width = Math.max(lineWidth, PAGESIZE.width)+80;
            marginPrefSize.height = thisPrefSize.height+80;
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

    public class NoteSelection{
        public Line line;
        public int begin, end;

        public NoteSelection(Line line, int begin, int end) {
            this.begin = begin;
            this.end = end;
            this.line = line;
        }
    }
    public NoteSelection getSelection(){
        if(selectedLine!=-1){
            Line line = composition.getLine(selectedLine);
            return new NoteSelection(line, 0, line.noteCount()-1);
        }else if(selectedNotesLine!=-1){
            return new NoteSelection(composition.getLine(selectedNotesLine), selectionBegin, selectionEnd);
        }else{
            return null;
        }
    }

    /*
    ----------------------------------------------------------------------
    MouseMotionListner methods
    ------------------------------------------------------------------------
    */
    public void mouseDragged(MouseEvent e) {
        if(dragDisabled)return;
        if (!startedDrag) {
            startedDrag = true;
            startDrag.setLocation(e.getX(), e.getY());
        }
        int realX = e.getX() < 0 ? 0 : e.getX() >= getWidth() ? getWidth() - 1 : e.getX();
        int realY = e.getY() < 0 ? 0 : e.getY() >= getHeight() ? getHeight() - 1 : e.getY();
        dragRectangle.setBounds(Math.min(startDrag.x, realX), Math.min(startDrag.y, realY),
                Math.abs(startDrag.x - realX), Math.abs(startDrag.y - realY));
        repaint();
    }

    public void mouseMoved(MouseEvent me) {
        if (activeNote == null || control!=Control.MOUSE || mode!=Mode.NOTEEDIT) return;
        int x = me.getX();
        int y = me.getY();
        newActiveNotePoint.line = (y-composition.getTopSpace()) / rowHeight;
        if(newActiveNotePoint.line<0 || newActiveNotePoint.line>=composition.lineCount()){
            return;
        }
        newActiveNotePoint.y = (int)((y-composition.getTopSpace() - newActiveNotePoint.line * rowHeight - HALFLINEDIST / 2) / HALFLINEDIST);
        if (newActiveNotePoint.y <= 0 || newActiveNotePoint.y > (INVISIBLELINESNUMBELOW + VISIBLELINENUM + INVISIBLELINESNUMABOVE) * 2 + 1) {
            return;
        }
        setNewActiveNotePoint(x, newActiveNotePoint.line);
        activeNote.setYPos(newActiveNotePoint.y - (INVISIBLELINESNUMABOVE + 3) * 2);
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
        xPos -= Note.HOTSPOT.x;
        int foundX = 0;
        Line l = composition.getLine(line);
        for (int i = 0;i<l.noteCount()-1;i++) {
            if (l.getNote(i).getXPos() < xPos && xPos <= l.getNote(i + 1).getXPos()) {
                foundX = i + 1;
                break;
            }
        }
        if (foundX == 0) {
            if(l.noteCount()==0){
                newActiveNotePoint.movement = 0;
                newActiveNotePoint.xIndex = 0;
            }else if (xPos <= l.getNote(0).getXPos()) {
                newActiveNotePoint.movement = FIRSTNOTEINLINEMOVEMENT;
                newActiveNotePoint.xIndex = 0;
            } else {
                newActiveNotePoint.movement = 0;
                newActiveNotePoint.xIndex = l.noteCount();
            }
        }else {
            int period = (xPos-l.getNote(foundX-1).getXPos())*4/(l.getNote(foundX).getXPos()-l.getNote(foundX-1).getXPos());
            //if(foundX==endNote-1 && period!=0) period=3;
            switch(period){
                case 0:
                    newActiveNotePoint.movement = 0;
                    newActiveNotePoint.xIndex = foundX - 1;
                    break;
                case 1:
                case 2:
                    newActiveNotePoint.movement = -(l.getNote(foundX).getXPos()-l.getNote(foundX-1).getXPos())/2;
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
            if (inSelection) {
                unSetNoteSelections();
                startDrag.setLocation(e.getX(), e.getY());
                calculateSelection(false);
                if(selectionBegin==-1 &&
                        Math.abs(e.getY()-getNoteYPos(0, (e.getY()-composition.getTopSpace())/rowHeight))<=2*LINEDIST){
                    selectedLine = (e.getY()-composition.getTopSpace())/rowHeight;
                    if(selectedLine<0 || selectedLine>=composition.lineCount())selectedLine=-1;
                }
                updateEditActions();
                repaintImage = true;
                repaint();
            }else if (control==Control.MOUSE) {
                Line line = composition.getLine(activeNotePoint.line);
                if (activeNotePoint.xIndex == line.noteCount()) {
                    addActiveNote(line);
                } else if (activeNotePoint.movement != 0) {
                    insertActiveNote(activeNotePoint.xIndex + (activeNotePoint.movement < 0 ? 0 : 1), line);
                } else {
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
        } else if (startedDrag) {
            startedDrag = false;
            calculateSelection(true);
            repaintImage = true;
            repaint();
        }
    }

    public void mouseEntered(MouseEvent e) {
        if (!isActiveNoteIn && control==Control.MOUSE && mode==Mode.NOTEEDIT) {
            isActiveNoteIn = true;
            //setCursor(activeNote==null ? Cursor.getDefaultCursor() : emptyCursor);
        }
    }

    public void mouseExited(MouseEvent e) {
        if (isActiveNoteIn && control==Control.MOUSE && mode==Mode.NOTEEDIT) {
            isActiveNoteIn = false;
            repaint();
            //setCursor(Cursor.getDefaultCursor());
        }
    }

    private Vector<Object> focusLostExceptions = new Vector<Object>(5, 5);

    public void addFocusLostExceptions(Object exception){
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
        if(focusLostExceptions.indexOf(e.getOppositeComponent())==-1){
            new FocusLostThread().start();
        }
    }

    private class FocusLostThread extends Thread{
        public void run() {
            try {
                sleep(500);
            } catch (InterruptedException e) {}
            MusicSheet.this.requestFocusInWindow();
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction() {
            putValue(Action.NAME, "Delete");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editdelete.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        }

        public void actionPerformed(ActionEvent e) {
            if(selectedNotesLine!=-1){
                Line line = composition.getLine(selectedNotesLine);
                for (int i=selectionEnd;i>=selectionBegin;i--) {
                    deleteNote(i, line);
                }
                calculateLenghtenings(selectionBegin-1, line);
                calculateLenghtenings(selectionBegin, line);
                spellLyrics(line);
            }else if(selectedLine!=-1){
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

    class CopyAction extends AbstractAction {
        Vector<Note> copyBuffer = new Vector<Note>(10, 30);
        IntervalSet[] intervalSetsCopyBuffer;

        public CopyAction() {
            putValue(Action.NAME, "Copy");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editcopy.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            if(selectedNotesLine>-1){
                Line line = composition.getLine(selectedNotesLine);
                copyBuffer.clear();
                for(int i=selectionBegin;i<=selectionEnd;i++){
                    copyBuffer.add(line.getNote(i).clone());
                }
                intervalSetsCopyBuffer = line.copyIntervals(selectionBegin, selectionEnd);
            }
        }
    }

    class CutAction extends AbstractAction {
        public CutAction() {
            putValue(Action.NAME, "Cut");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editcut.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            copyAction.actionPerformed(e);
            deleteAction.actionPerformed(e);
        }
    }

    class PasteAction extends AbstractAction {
        Control prevPasteControl;

        public PasteAction() {
            putValue(Action.NAME, "Paste");
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("editpaste.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
           if(copyAction.copyBuffer.size()>0){
               prevPasteControl = control;
               setActiveNote(Note.PASTENOTE);
               control = Control.MOUSE;
               inSelection = false;
               repaint();
           }
        }
    }

    class KeyAction extends AbstractAction{
        private int code;

        public KeyAction(int code) {
            this.code = code;
        }

        public void actionPerformed(ActionEvent e) {
            if(mode!=Mode.NOTEEDIT || control!=Control.KEYBOARD) return;
            if(activeNote!=null){
               Line line = composition.getLine(activeNotePoint.line);
               if(code==KeyEvent.VK_LEFT){
                   if(activeNotePoint.xIndex==0 && (activeNotePoint.movement!=0 || line.noteCount()==0)){
                        return;
                   }
                   if(activeNotePoint.movement==0 && activeNotePoint.xIndex<line.noteCount()){
                       activeNotePoint.movement = activeNotePoint.xIndex!=0 ?
                               (line.getNote(activeNotePoint.xIndex-1).getXPos()-line.getNote(activeNotePoint.xIndex).getXPos())/2 : FIRSTNOTEINLINEMOVEMENT;
                   }else{
                       activeNotePoint.movement = 0;
                       activeNotePoint.xIndex--;
                   }
               }else if(code==KeyEvent.VK_RIGHT){
                   if(activeNotePoint.xIndex==line.noteCount()){
                       return;
                   }
                   if(activeNotePoint.movement==0){
                       activeNotePoint.xIndex++;
                       if(activeNotePoint.xIndex<line.noteCount()){
                           activeNotePoint.movement = activeNotePoint.xIndex!=0 ?
                                   (line.getNote(activeNotePoint.xIndex-1).getXPos()-line.getNote(activeNotePoint.xIndex).getXPos())/2 : FIRSTNOTEINLINEMOVEMENT;
                       }else{
                           activeNotePoint.movement = 0;
                       }
                   }else{
                       activeNotePoint.movement = 0;
                   }
               }else if(code==KeyEvent.VK_UP){
                   if(activeNote.getYPos()>=-(INVISIBLELINESNUMABOVE+2)*2){
                       activeNote.setYPos(activeNote.getYPos()-1);
                       mainFrame.getStatusBar().setPitchString(activeNote.getActiveNotePitchString(line));
                   }
               }else if(code==KeyEvent.VK_DOWN){
                   if(activeNote.getYPos()<=(INVISIBLELINESNUMBELOW+2)*2){
                       activeNote.setYPos(activeNote.getYPos()+1);
                       mainFrame.getStatusBar().setPitchString(activeNote.getActiveNotePitchString(line));
                   }
               }else if(code==KeyEvent.VK_ENTER){
                   if (activeNotePoint.xIndex==line.noteCount()) {
                       addActiveNote(line);
                       activeNotePoint.xIndex = line.noteCount();
                       activeNotePoint.movement=0;
                    } else if (activeNotePoint.movement != 0) {
                       insertActiveNote(activeNotePoint.xIndex + (activeNotePoint.movement < 0 ? 0 : 1), line);
                    } else {
                       modifyActiveNote(activeNotePoint.xIndex, line);
                    }
               }else if(code==KeyEvent.VK_PAGE_UP){
                   if(activeNotePoint.line>0){
                       activeNotePoint.line--;
                       setNewActiveNotePoint(activeNote.getXPos(), activeNotePoint.line);
                       activeNotePoint.xIndex = newActiveNotePoint.xIndex;
                       activeNotePoint.movement = newActiveNotePoint.movement;
                   }
               }else if(code==KeyEvent.VK_PAGE_DOWN){
                   if(activeNotePoint.line+1<composition.lineCount()){
                       activeNotePoint.line++;
                       setNewActiveNotePoint(activeNote.getXPos(), activeNotePoint.line);
                       activeNotePoint.xIndex = newActiveNotePoint.xIndex;
                       activeNotePoint.movement = newActiveNotePoint.movement;
                   }
               }else if(code==KeyEvent.VK_BACK_SPACE){
                   if(line.noteCount()>0){
                       deleteNote(line.noteCount()-1, line);
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

    private int selectedNoteStorage[] = new int[3];

    private class BeamingKeyActions extends AbstractAction {
        //types: 0:BEAM 1:TRIPLET 2:TIE

        private int type;

        public BeamingKeyActions(int type) {
            this.type = type;
        }

        public void actionPerformed(ActionEvent e) {
            selectedNoteStorage[0] = selectedNotesLine;
            selectedNoteStorage[1] = selectionBegin;
            selectedNoteStorage[2] = selectionEnd;
            selectedNotesLine = activeNotePoint.line;
            Line line = composition.getLine(selectedNotesLine);
            if(type==0 && line.noteCount()>=2){
                selectionBegin = line.noteCount()-2;
                selectionEnd = line.noteCount()-1;
                beamSelectedNotes(line.getBeamings().findInterval(selectionEnd)==null);
            }else if(type==1){
                int minL = Integer.MAX_VALUE, sumL = 0;
                for(int i=line.noteCount()-1;i>=0;i--){
                    Note note = line.getNote(i);
                    if(note.getDuration()<minL && note.getDuration()>0){
                        minL = note.getDuration();
                    }
                    sumL+=note.getDuration();
                    if(sumL==minL*3){
                        selectionBegin = i;
                        selectionEnd =line.noteCount()-1;
                        Interval tupletInterval = line.getTuplets().findInterval(selectionEnd);
                        if(tupletInterval==null){
                            tupletSelectedNotes(3);
                        }else if(tupletInterval.getData().equals("3")){
                            untupletSelectedNotes();
                        }
                    }
                }
            }else if(type==2 && line.noteCount()>=2){
                selectionBegin = line.noteCount()-2;
                selectionEnd = line.noteCount()-1;
                tieSelectedNotes(line.getTies().findInterval(selectionEnd)==null);
            }
            selectedNotesLine = selectedNoteStorage[0];
            selectionBegin = selectedNoteStorage[1];
            selectionEnd = selectedNoteStorage[2];
        }
    }

    public BufferedImage createMusicSheetImageForExport(Color background, double scale){
        BufferedImage image = new BufferedImage((int)(getSheetWidth()*scale), (int)(getSheetHeight()*scale), BufferedImage.TYPE_INT_RGB);
        createMusicSheetImageForExport(image, background, scale);
        return image;
    }

    public Point createMusicSheetImageForExport(BufferedImage image, Color background, double scale){
        Graphics2D g2 = image.createGraphics();
        g2.setPaint(background);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        BaseMsDrawer origDrawer = drawer;
        setDrawer(DrawerType.FUGHETTA);
        Point point = new Point((int)(image.getWidth()-getSheetWidth()*scale)/2, (int)(image.getHeight()-getSheetHeight()*scale)/2);
        g2.translate(point.x, point.y);
        drawer.drawMusicSheet(g2, false, scale);
        drawer = origDrawer;
        g2.dispose();
        return point;
    }

    public void meta(MetaMessage meta) {
        if(meta.getType()==0){
            byte[] data = meta.getData();
            playingLine = data[0]<<8|data[1];
            playingNote = data[2]<<8|data[3];
            repaint();
        }
    }

    //-------------------------------scrollable-------------------------
    private class MsPanel extends JPanel implements Scrollable{
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 30;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation==SwingConstants.VERTICAL ? visibleRect.height-10 : visibleRect.width-20;
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}