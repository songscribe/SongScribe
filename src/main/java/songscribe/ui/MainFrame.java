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

    Created on 2005.01.06., 22:15:02
*/

package songscribe.ui;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;
import songscribe.IO.CompositionIO;
import songscribe.SongScribe;
import songscribe.Version;
import songscribe.data.PropertyChangeListener;
import songscribe.music.Crotchet;
import songscribe.music.CrotchetRest;
import songscribe.music.Note;
import songscribe.music.NoteType;
import songscribe.music.RepeatLeft;
import songscribe.ui.mainframeactions.*;
import songscribe.ui.playsubmenu.PlayMenu;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;


/**
 * @author Csaba Kávai
 */
public class MainFrame extends JFrame {
    public static final Dimension OUTER_SELECTION_PANEL_IMAGE_DIM = new Dimension(34, 38);
    public static final Color OUTER_SELECTION_IMAGE_BORDER_COLOR = new Color(51, 102, 102);
    public static final Point CENTER_POINT = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
    public static final String COULD_NOT_SAVE_MESSAGE = "Could not save the file. Check if you have the permission to create a file in this directory or the overwrited file may be write-protected.";
    public static final File SS_HOME = new File(System.getProperty("user.home"), ".songscribe");

    static {
        if (!SS_HOME.exists() && !SS_HOME.mkdir()) {
            JOptionPane.showMessageDialog(null, "Cannot make \".songscribe\" directory in the user home. Please give proper permissions.\nUntil then the program cannot save properties.", Constants.PACKAGE_NAME, JOptionPane.ERROR_MESSAGE);
        }

        File logFile = new File(SS_HOME, "log");

        if (logFile.length() > 1000000L) {
            // noinspection ResultOfMethodCallIgnored
            logFile.delete();
        }
    }

    private static final File PROPS_FILE = new File(SS_HOME, "props");
    public static Sequencer sequencer;
    public static Receiver receiver;
    public static Synthesizer synthesizer;
    private static Logger LOG = Logger.getLogger(MainFrame.class);
    private static MediaTracker mediaTracker = new MediaTracker(new JLabel());
    private static JWindow splashWindow;
    public final Icon blankIcon = new ImageIcon(getImage("blank.png"));
    private final Properties defaultProps = new Properties();

    {
        try {
            defaultProps.load(new FileInputStream(SongScribe.basePath + "/conf/defprops"));
            LOG.debug(
                    "Default properties loaded e.g showmemusage=" + defaultProps.getProperty(Constants.SHOW_MEM_USAGE));
        }
        catch (IOException e) {
            showErrorMessage("The program could not start, because a necessary file is not available. Please reinstall the software.");
            LOG.fatal("Could not read default properties file.", e);
            System.exit(0);
        }
    }

    protected final Properties properties = new Properties(defaultProps);

    {
        try {
            properties.load(new FileInputStream(PROPS_FILE));
            LOG.debug("Normal properties loaded e.g previousdirectory=" +
                      defaultProps.getProperty(Constants.PREVIOUS_DIRECTORY));
        }
        catch (IOException e) {
            LOG.error("Could not read properties file.", e);
        }
    }

    private final ProfileManager profileManager = new ProfileManager(this);
    public String PROG_NAME;
    protected MusicSheet musicSheet;
    protected File saveFile;
    protected String lastWordForDoYouWannaSaveDialog;
    protected AbstractAction saveAction;
    protected AbstractAction saveAsAction;
    protected ExitAction exitAction = new ExitAction();
    protected File previousDirectory;
    private SAXParser saxParser;
    private JPanel subWestToolsHolder;
    private ButtonGroup mainWestGroup;
    private boolean modifiedDocument;
    private ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    private SelectSelectionPanel selectSelectionPanel;
    private NoteSelectionPanel noteSelectionPanel;
    private RestSelectionPanel restSelectionPanel;
    private OtherSelectionPanel otherSelectionPanel;
    private StatusBar statusBar;
    private LyricsModePanel lyricsModePanel;
    private InsertMenu insertMenu;
    private PlayMenu playMenu = new PlayMenu(this);
    private JMenu controlMenu;
    private DialogOpenAction aboutAction = new DialogOpenAction(this, "About", "info.png", AboutDialog.class);
    private DialogOpenAction prefAction = new DialogOpenAction(this, "Preferences...", "configure.png", PreferencesDialog.class);
    private DialogOpenAction compositionSettingsAction = new DialogOpenAction(this, "Composition settings...", "compositionsettings.png", KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), CompositionSettingsDialog.class);
    private ModeAction[] modeActions;
    private ControlAction[] controlActions;
    private PrintAction printAction;

    public MainFrame() {
        PROG_NAME = "Song Writer";
        lastWordForDoYouWannaSaveDialog = "song";

        if (properties.getProperty(Constants.FIRST_RUN).equals(Constants.TRUE_VALUE)) {
            firstRun();
        }

        previousDirectory = new File(properties.getProperty(Constants.PREVIOUS_DIRECTORY));

        if (previousDirectory.getName().length() == 0 || !previousDirectory.exists()) {
            previousDirectory = new File(new JFileChooser().getCurrentDirectory(), "SongScribe");
        }

        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        }
        catch (Exception e) {
            showErrorMessage(PROG_NAME + " cannot start because of an initialization error.");
            LOG.error("SaxParser configuration", e);
            System.exit(0);
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAction.actionPerformed(null);
            }
        });
    }

    protected static void openMidi() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            receiver = MidiSystem.getReceiver();
        }
        catch (MidiUnavailableException e) {
            hideSplash();
            LOG.warn("No MIDI", e);
            JOptionPane.showMessageDialog(null, String.format(
                            "You may be already running %s or another program that uses sound.\n" +
                            "Please try to quit them and restart %s.\n" +
                            "In this session playback will be disabled.", Constants.PACKAGE_NAME, Constants.PACKAGE_NAME), Constants.PACKAGE_NAME, JOptionPane.WARNING_MESSAGE);
        }
    }

    protected static void closeMidi() {
        if (receiver != null) {
            receiver.close();
        }

        if (sequencer != null) {
            sequencer.close();
        }

        if (synthesizer != null) {
            synthesizer.close();
        }
    }

    public static void main(String[] args) {
        showSplash("swsplash.png");
        PropertyConfigurator.configure(SongScribe.basePath + "/conf/logger.properties");
        openMidi();

        try {
            MainFrame mf = new MainFrame();
            mf.initFrame();

            if (mf.properties.getProperty(Constants.SHOW_WHATS_NEW + Version.PUBLIC_VERSION) == null &&
                new File(WhatsNewDialog.WHATS_NEW_FILE).exists()) {
                mf.properties.setProperty(Constants.SHOW_WHATS_NEW + Version.PUBLIC_VERSION, Constants.TRUE_VALUE);
                new WhatsNewDialog(mf).setVisible(true);
            }
            else {
                try {
                    if (mf.properties.getProperty(Constants.SHOW_TIP).equals(Constants.TRUE_VALUE)) {
                        new TipFrame(mf);
                    }
                }
                catch (IOException e) {
                    hideSplash();
                    mf.properties.setProperty(Constants.SHOW_TIP, Constants.FALSE_VALUE);
                }
            }

            if (Utilities.isMac()) {
                new MacAdapter(mf, true);
            }

            if (args.length > 0) {
                File f = new File(args[0]);

                if (f.exists()) {
                    mf.handleOpenFile(f);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            LOG.error("main", e);
        }

        hideSplash();
    }

    protected static void showSplash(String splashScreen) {
        splashWindow = new JWindow((Frame) null);
        JLabel splashLabel = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().createImage(
            SongScribe.basePath + "/images/" + splashScreen)));
        splashWindow.getContentPane().add(splashLabel);
        splashWindow.pack();
        splashWindow.setLocation(
                CENTER_POINT.x - splashWindow.getWidth() / 2, CENTER_POINT.y - splashWindow.getHeight() / 2);
        splashWindow.setVisible(true);
        splashWindow.getRootPane().setGlassPane(new JComponent() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(new Font(Font.SERIF, Font.BOLD, 20));
                g2.drawString("Version " + Utilities.getPublicVersion(), 20, 503);
                g2.drawString("© 2006-" + Utilities.getYear() + " Himádri", 305, 503);
            }
        });
        splashWindow.getRootPane().getGlassPane().setVisible(true);
    }

    protected static void hideSplash() {
        if (splashWindow != null) {
            splashWindow.dispose();
        }
    }

    public static Image getImage(File file) {
        Image img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());

        if (img == null) {
            return null;
        }

        try {
            mediaTracker.addImage(img, 0);
            mediaTracker.waitForID(0);
        }
        catch (InterruptedException ignored) {
        }

        return img;
    }

    public static Image getImage(String fileName) {
        Image img = Toolkit.getDefaultToolkit().createImage(SongScribe.basePath + "/images/" + fileName);

        if (img == null) {
            return null;
        }

        try {
            mediaTracker.addImage(img, 0);
            mediaTracker.waitForID(0);
        }
        catch (InterruptedException ignored) {
        }

        return img;
    }

    public static void setMediaTracker(MediaTracker mt) {
        MainFrame.mediaTracker = mt;
    }

    private void firstRun() {
        File ssDocs = new File(new JFileChooser().getCurrentDirectory(), "SongScribe");
        boolean ssDocsCreated = ssDocs.mkdir();

        if (ssDocsCreated) {
            try {
                for (File file : new File("examples").listFiles()) {
                    Utilities.copyFile(file, new File(ssDocs, file.getName()));
                }
            }
            catch (IOException e) {
                LOG.error("Cannot copy example file", e);
            }

            previousDirectory = ssDocs;
        }

        properties.setProperty(Constants.FIRST_RUN, Constants.FALSE_VALUE);
    }

    public void initFrame() {
        setTitle(PROG_NAME);
        setIconImage(getImage("swicon.png"));
        init();
        setFrameSize();
        setLocation(Math.max(CENTER_POINT.x - getWidth() / 2, 0), Math.max(CENTER_POINT.y - getHeight() / 2, 0));
        setVisible(true);
        musicSheet.requestFocusInWindow();
        fireMusicChanged(this);
        automaticCheckForUpdate();
    }

    private void init() {
        // CENTER
        musicSheet = new MusicSheet(this);
        musicSheet.initComponent();
        JTabbedPane southTabbedPane = new JTabbedPane();
        southTabbedPane.add("Playback", new PlaybackPanel(this));
        lyricsModePanel = new LyricsModePanel(this);
        southTabbedPane.add("Lyrics", lyricsModePanel.getLyricsModePanel());
        JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        center.setContinuousLayout(true);
        center.setResizeWeight(Utilities.isLinux() ? 0.85 : 1.0); // unknown rendering problem in Linux
        center.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        center.setTopComponent(musicSheet.getScrolledMusicSheet());
        center.setBottomComponent(southTabbedPane);
        getContentPane().add(center);

        // WEST
        selectSelectionPanel = new SelectSelectionPanel(this);
        noteSelectionPanel = new NoteSelectionPanel(this);
        restSelectionPanel = new RestSelectionPanel(this);
        BeamSelectionPanel beamSelectionPanel = new BeamSelectionPanel(this);
        otherSelectionPanel = new OtherSelectionPanel(this);
        subWestToolsHolder = new JPanel(new CardLayout());
        JToolBar mainWestTools = new JToolBar(JToolBar.VERTICAL);
        mainWestTools.setFloatable(false);
        mainWestGroup = new ButtonGroup();
        WestToggleButton westToggleButton;
        westToggleButton = new WestToggleButton(MainFrame.getImage("arrowtab.gif"), selectSelectionPanel, "Selection");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        westToggleButton = new WestToggleButton(Note.clipNoteImage(Crotchet.UP_IMAGE, Crotchet.REAL_UP_NOTE_RECT, OUTER_SELECTION_IMAGE_BORDER_COLOR, OUTER_SELECTION_PANEL_IMAGE_DIM), noteSelectionPanel, "Note addition");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        westToggleButton.doClick();
        westToggleButton = new WestToggleButton(Note.clipNoteImage(CrotchetRest.IMAGE, CrotchetRest.REAL_NOTE_RECT, OUTER_SELECTION_IMAGE_BORDER_COLOR, OUTER_SELECTION_PANEL_IMAGE_DIM), restSelectionPanel, "Rest addition");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        westToggleButton = new WestToggleButton(MainFrame.getImage("triplettab.gif"), beamSelectionPanel, "Beamings, triplets, ties");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        westToggleButton = new WestToggleButton(Note.clipNoteImage(RepeatLeft.IMAGE, RepeatLeft.REAL_NOTE_RECT, OUTER_SELECTION_IMAGE_BORDER_COLOR, OUTER_SELECTION_PANEL_IMAGE_DIM), otherSelectionPanel, "Others");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        JPanel west = new JPanel();
        west.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        west.setLayout(new BoxLayout(west, BoxLayout.X_AXIS));
        mainWestTools.setAlignmentY(0f);
        west.add(mainWestTools);
        west.add(Box.createHorizontalStrut(3));
        subWestToolsHolder.setAlignmentY(0f);
        west.add(subWestToolsHolder);
        getContentPane().add(BorderLayout.WEST, west);

        // SOUTH
        statusBar = new StatusBar(this);
        getContentPane().add(BorderLayout.SOUTH, statusBar);

        // MENU BAR
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        NewAction newAction = new NewAction(this);
        fileMenu.add(newAction);
        OpenAction openAction = new OpenAction(this);
        fileMenu.add(openAction);
        fileMenu.addSeparator();
        saveAction = new SaveAction(this);
        fileMenu.add(saveAction);
        saveAsAction = new SaveAsAction(this);
        fileMenu.add(saveAsAction);
        fileMenu.addSeparator();
        ExportMidiAction exportMidiAction = new ExportMidiAction(this);
        fileMenu.add(exportMidiAction);
        ExportMusicSheetImageAction exportMusicSheetImageAction = new ExportMusicSheetImageAction(this);
        fileMenu.add(exportMusicSheetImageAction);
        ExportPDFAction exportPDFAction = new ExportPDFAction(this);
        fileMenu.add(exportPDFAction);
        fileMenu.add(new ExportABCAnnotationAction(this));
        fileMenu.add(new ExportLilypondAnnotationAction(this));
        fileMenu.addSeparator();
        printAction = new PrintAction(this);
        fileMenu.add(printAction);

        if (!Utilities.isMac()) {
            fileMenu.addSeparator();
            fileMenu.add(exitAction);
        }

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(musicSheet.cutAction);
        editMenu.add(musicSheet.copyAction);
        editMenu.add(musicSheet.pasteAction);
        editMenu.addSeparator();
        editMenu.add(musicSheet.deleteAction);
        editMenu.addSeparator();
        controlMenu = new JMenu("Control");
        controlMenu.setIcon(blankIcon);
        ButtonGroup bg = new ButtonGroup();
        controlActions = new ControlAction[MusicSheet.Control.values().length];

        for (MusicSheet.Control control : MusicSheet.Control.values()) {
            controlActions[control.ordinal()] = new ControlAction(this, control);
            JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(controlActions[control.ordinal()]);
            controlActions[control.ordinal()].addAbstractButton(jrbmi);
            bg.add(jrbmi);
            controlMenu.add(jrbmi);
        }

        editMenu.add(controlMenu);

        if (!Utilities.isMac()) {
            editMenu.add(prefAction);
        }

        JMenu modeMenu = new JMenu("Mode");
        bg = new ButtonGroup();
        modeActions = new ModeAction[MusicSheet.Mode.values().length];

        for (MusicSheet.Mode mode : MusicSheet.Mode.values()) {
            ModeAction action = new ModeAction(this, mode);
            modeActions[mode.ordinal()] = action;
            JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(action);
            action.addAbstractButton(jrbmi);
            bg.add(jrbmi);
            modeMenu.add(jrbmi);
        }

        insertMenu = new InsertMenu(this);
        insertMenu.addSeparator();
        insertMenu.add(new TempoChangeAction(this));
        insertMenu.add(new BeatChangeAction(this));
        insertMenu.add(new AnnotationAction(this));
        insertMenu.add(new KeySignatureChangeAction(this));
        AddLineAction addLineAction = new AddLineAction(this);
        JMenu lineMenu = new JMenu("Line");
        lineMenu.setIcon((Icon) addLineAction.getValue(Action.SMALL_ICON));
        lineMenu.add(addLineAction);
        lineMenu.add(new InsertLineAction(this, 0, "Before the selected line"));
        lineMenu.add(new InsertLineAction(this, 1, "After the selected line"));
        insertMenu.addSeparator();
        insertMenu.add(lineMenu);

        JMenu compositionMenu = new JMenu("Composition");
        compositionMenu.add(compositionSettingsAction);
        compositionMenu.add(new DialogOpenAction(this, "Lyrics...", "edit.png", KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), LyricsDialog.class));
        compositionMenu.add(new DialogOpenAction(this, "Line width...", "changelinewidth.png", LineWidthChangeDialog.class));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new DialogOpenAction(this, "Basic Tutorial", "kontact_contacts.png", TutorialDialog.class));
        helpMenu.add(new PDFTutorialOpenAction(this, "Extended Tutorial (PDF)", "blockdevice.png"));
        helpMenu.add(new TipAction(this));
        helpMenu.add(new DialogOpenAction(this, "Keymap", "keyboard.png", KeyMapDialog.class));
        helpMenu.addSeparator();
        makeCommonHelpMenu(helpMenu);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(modeMenu);
        menuBar.add(insertMenu);
        menuBar.add(new NotesMenu(this));
        menuBar.add(playMenu);
        menuBar.add(compositionMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // TOOLBAR
        Dimension separator = new Dimension(10, 0);
        JToolBar toolBar = new JToolBar();
        addActionToToolBarToShowShortDescription(toolBar, newAction);
        addActionToToolBarToShowShortDescription(toolBar, openAction);
        toolBar.addSeparator(separator);
        addActionToToolBarToShowShortDescription(toolBar, saveAction);
        addActionToToolBarToShowShortDescription(toolBar, exportMidiAction);
        addActionToToolBarToShowShortDescription(toolBar, exportMusicSheetImageAction);
        addActionToToolBarToShowShortDescription(toolBar, exportPDFAction);
        toolBar.addSeparator(separator);
        toolBar.addSeparator(new Dimension()); // this is important, because when publisher opens mainframe for editing a song, it removes all buttons antil the double separator found
        bg = new ButtonGroup();

        for (ModeAction modeAction : modeActions) {
            JToggleButton b = new JToggleButton(modeAction);
            b.setText(null);
            toolBar.add(b);
            modeAction.addAbstractButton(b);
            bg.add(b);
        }

        toolBar.addSeparator(separator);
        addActionToToolBarToShowShortDescription(toolBar, compositionSettingsAction);
        addActionToToolBarToShowShortDescription(toolBar, addLineAction);
        toolBar.addSeparator(separator);
        addActionToToolBarToShowShortDescription(toolBar, new FullScreenAction(this));
        toolBar.getInputMap().clear();
        toolBar.getActionMap().clear();
        getContentPane().add(toolBar, BorderLayout.NORTH);

        modeActions[musicSheet.getMode().ordinal()].actionPerformed(null);
        controlActions[musicSheet.getControl().ordinal()].actionPerformed(null);
        insertMenu.doClickNote(NoteType.CROTCHET.name());
    }

    private void setFrameSize() {
        Dimension size = getLayout().preferredLayoutSize(this);
        int scrollBarWidth = ((Integer) UIManager.get("ScrollBar.width"));
        Rectangle mxBound = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(size.width + scrollBarWidth, mxBound.width), Math.min(size.height, mxBound.height));
    }

    protected void makeCommonHelpMenu(JMenu helpMenu) {
        helpMenu.add(new DialogOpenAction(this, "Check for Update...", new ImageIcon(getImage("download_manager.png")), UpdateDialog.class));
        helpMenu.add(new DialogOpenAction(this, "Report a Bug", new ImageIcon(getImage("bug.png")), ReportBugDialog.class));

        if (new File(WhatsNewDialog.WHATS_NEW_FILE).exists()) {
            helpMenu.add(new DialogOpenAction(this,
                    "What's new in " + Version.PUBLIC_VERSION, "blank.png", WhatsNewDialog.class));
        }

        if (!Utilities.isMac()) {
            helpMenu.add(aboutAction);
        }
    }

    public JButton addActionToToolBarToShowShortDescription(JToolBar toolBar, Action action) {
        if (action.getValue(Action.SHORT_DESCRIPTION) == null) {
            JButton button = new JButton(action);
            button.setText(null);
            button.setToolTipText((String) action.getValue(Action.NAME));
            toolBar.add(button);
            return button;
        }
        else {
            return toolBar.add(action);
        }
    }

    public void setSelectedTool(SelectionPanel selectionPanel) {
        for (Enumeration e = mainWestGroup.getElements(); e.hasMoreElements(); ) {
            WestToggleButton wtb = (WestToggleButton) e.nextElement();

            if (wtb.getSelectionPanel() == selectionPanel) {
                wtb.doClick();
                return;
            }
        }
    }

    public boolean showSaveDialog() {
        if (!modifiedDocument) {
            return true;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "The document has been modified.\nDo you want to save the " + lastWordForDoYouWannaSaveDialog +
                "?", PROG_NAME, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.YES_OPTION) {
            saveAction.actionPerformed(null);
        }

        return answer != JOptionPane.CANCEL_OPTION;
    }

    public void modifiedDocument() {
        modifiedDocument = true;
    }

    public void unmodifiedDocument() {
        modifiedDocument = false;
    }

    public MusicSheet getMusicSheet() {
        return musicSheet;
    }

    public void setMusicSheet(MusicSheet musicSheet) {
        this.musicSheet = musicSheet;
    }

    public SelectSelectionPanel getSelectSelectionPanel() {
        return selectSelectionPanel;
    }

    public NoteSelectionPanel getNoteSelectionPanel() {
        return noteSelectionPanel;
    }

    public RestSelectionPanel getRestSelectionPanel() {
        return restSelectionPanel;
    }

    public OtherSelectionPanel getOtherSelectionPanel() {
        return otherSelectionPanel;
    }

    public InsertMenu getInsertMenu() {
        return insertMenu;
    }

    public PlayMenu getPlayMenu() {
        return playMenu;
    }

    public CompositionSettingsDialog getCompositionSettingsDialog() {
        return (CompositionSettingsDialog) compositionSettingsAction.getDialog();
    }

    public PreferencesDialog getPreferencesDialog() {
        return (PreferencesDialog) prefAction.getDialog();
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public LyricsModePanel getLyricsModePanel() {
        return lyricsModePanel;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public JMenu getControlMenu() {
        return controlMenu;
    }

    public File getPreviousDirectory() {
        return previousDirectory;
    }

    public void setPreviousDirectory(File previousDirectory) {
        this.previousDirectory = previousDirectory;
    }

    public void addProperyChangeListener(PropertyChangeListener pcl) {
        propertyChangeListeners.add(pcl);
    }

    public void fireMusicChanged(Object sender) {
        for (PropertyChangeListener pcl : propertyChangeListeners) {
            if (pcl != sender) {
                pcl.musicChanged(properties);
            }
        }
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, PROG_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public File getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(File saveFile) {
        this.saveFile = saveFile;

        if (saveFile == null) {
            setTitle(PROG_NAME);
        }
        else {
            setTitle(PROG_NAME + " - " + saveFile.getName());
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public Properties getDefaultProps() {
        return defaultProps;
    }

    public AbstractAction getSaveAction() {
        return saveAction;
    }

    public AbstractAction getSaveAsAction() {
        return saveAsAction;
    }

    public void setNextMode() {
        MusicSheet.Mode mode = musicSheet.getMode();
        modeActions[mode.ordinal() + 1 == modeActions.length ? 0 : mode.ordinal() + 1].actionPerformed(null);
    }

    public void setMode(MusicSheet.Mode mode) {
        if (mode != musicSheet.getMode()) {
            modeActions[mode.ordinal()].actionPerformed(null);
        }
    }

    public void setNextControl() {
        if (controlMenu.isEnabled()) {
            MusicSheet.Control control = musicSheet.getControl();
            controlActions[
                    control.ordinal() + 1 == controlActions.length ? 0 : control.ordinal() + 1].actionPerformed(null);
        }
    }

    protected void automaticCheckForUpdate() {
        try {
            if (System.currentTimeMillis() - Long.parseLong(properties.getProperty(Constants.LAST_AUTO_UPDATE)) >
                Long.parseLong(properties.getProperty(Constants.AUTO_UPDATE_PERIOD))) {
                new UpdateDialog(this).new UpdateInternetThread(true).start();
                properties.setProperty(Constants.LAST_AUTO_UPDATE, Long.toString(System.currentTimeMillis()));
            }
        }
        catch (NumberFormatException nf) {
            LOG.error("Lastupdate property is not a number.");
        }
    }

    public void openMusicSheet(File openFile, boolean setTitle) {
        boolean previousModifiedDocument = modifiedDocument;

        try {
            CompositionIO.DocumentReader dr = new CompositionIO.DocumentReader(this);
            saxParser.parse(openFile, dr);
            musicSheet.setComposition(dr.getComposition());
            setFrameSize();
            if (setTitle) {
                setSaveFile(openFile);
            }
        }
        catch (SAXException e1) {
            showErrorMessage("Could not open the file " + openFile.getName() + ", because it is damaged.");
            LOG.error("SaxParser parse", e1);
        }
        catch (IOException e1) {
            showErrorMessage(
                    "Could not open the file " + openFile.getName() + ". Check if you have the permission to open it.");
            LOG.error("Song open", e1);
        }

        modifiedDocument = previousModifiedDocument;
    }

    public void handleAbout() {
        aboutAction.actionPerformed(null);
    }

    public void handlePrefs() throws IllegalStateException {
        getCompositionSettingsDialog().setVisible(true);
    }

    public boolean handleQuit() {
        exitAction.actionPerformed(null);
        return (Boolean) exitAction.getValue("quit");
    }

    public void handleOpenFile(File file) {
        if (!showSaveDialog()) {
            return;
        }

        openMusicSheet(file, true);
        setSelectedTool(selectSelectionPanel);
        selectSelectionPanel.setActive();
        unmodifiedDocument();
    }

    public void handlePrintFile(File file) {
        handleOpenFile(file);
        printAction.actionPerformed(null);
    }

    protected class ExitAction extends AbstractAction {
        public ExitAction() {
            putValue(Action.NAME, "Exit");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("exit.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            boolean quit = showSaveDialog();

            // Save the result of the dialog so Mac quit handler can access it
            this.putValue("quit", quit);

            if (!quit) {
                return;
            }

            if (musicSheet != null) {
                musicSheet.saveProperties();
            }

            properties.setProperty(Constants.PREVIOUS_DIRECTORY, previousDirectory.getAbsolutePath());

            try {
                properties.store(new FileOutputStream(PROPS_FILE), null);
            }
            catch (IOException e1) {
                e1.printStackTrace();
                showErrorMessage("Could not save the properties file. Please reinstall the software.");
                LOG.error("Save props", e1);
            }

            closeMidi();
            System.exit(0);
        }
    }

    private class WestToggleButton extends JToggleButton {
        private final String ID = toString();
        private SelectionPanel selectionPanel;

        public WestToggleButton(Image icon, final SelectionPanel selectionPanel, String tip) {
            super(new ImageIcon(icon));
            this.selectionPanel = selectionPanel;
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectionPanel.setActive();
                    ((CardLayout) subWestToolsHolder.getLayout()).show(subWestToolsHolder, ID);
                    if (selectionPanel != selectSelectionPanel) {
                        setMode(MusicSheet.Mode.NOTE_EDIT);
                    }
                }
            });
            setToolTipText(tip);
            subWestToolsHolder.add(selectionPanel, ID);
        }

        public SelectionPanel getSelectionPanel() {
            return selectionPanel;
        }
    }
}
