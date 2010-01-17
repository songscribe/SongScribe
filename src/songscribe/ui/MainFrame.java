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

Created on 2005.01.06., 22:15:02
*/

package songscribe.ui;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;
import songscribe.IO.CompositionIO;
import songscribe.data.PropertyChangeListener;
import songscribe.music.*;
import songscribe.ui.mainframeactions.*;
import songscribe.ui.playsubmenu.PlayMenu;

import javax.sound.midi.*;
import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;


/**
 * @author Csaba Kávai
 *
 */
public class MainFrame extends JFrame {
    public String PROGNAME;
    public static final String PACKAGENAME = "SongScribe";
    public static final int MAJORVERSION = 1;
    public static final int MINORVERSION = 14;

    private static Logger LOG = Logger.getLogger(MainFrame.class);

    public static Sequencer sequencer;
    public static Receiver receiver;
    public static Synthesizer synthesizer;

    public static final Dimension OUTERSELECTIONPANELIMAGEDIM = new Dimension(34, 38);
    public static final Color OUTERSELECTIONIMAGEBORDERCOLOR = new Color(51, 102, 102);

    public static final Point CENTERPOINT = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
    public static final String[] FONTFAMILIES = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    public static final String COULDNOTSAVEMESSAGE = "Could not save the file. Check if you have the permission to create a file in this directory or the overwrited file may be write-protected.";

    public static final File SSHOME = new File(System.getProperty("user.home"), ".songscribe");
    static{
        if(!SSHOME.exists() && !SSHOME.mkdir()){
            JOptionPane.showMessageDialog(null, "Cannot make \".songscribe\" directory in the user home. Please give proper permissions.\nUntil then the program cannot save properties.", PACKAGENAME, JOptionPane.ERROR_MESSAGE);
        }
        File logFile = new File(SSHOME, "log");
        if(logFile.length()>1000000l) {
            //noinspection ResultOfMethodCallIgnored
            logFile.delete();
        }
    }
    private static final File PROPSFILE = new File(SSHOME, "props");
    private static final File DEFPROPSFILE = new File("conf/defprops");
    private SAXParser saxParser;

    private static MediaTracker mediaTracker = new MediaTracker(new JLabel());

    public final Icon blankIcon = new ImageIcon(getImage("blank.png"));

    private JPanel subWestToolsHolder;
    private ButtonGroup mainWestGroup;
    protected MusicSheet musicSheet;
    protected File saveFile;
    private boolean modifiedDocument;
    protected String lastWordForDoYouWannaSaveDialog;
    private static JWindow splashWindow;

    private final Properties defaultProps = new Properties();
    {
        try {
            defaultProps.load(new FileInputStream(DEFPROPSFILE));
            LOG.debug("Default properties loaded e.g showmemusage="+defaultProps.getProperty(Constants.SHOWMEMUSEAGE));
        } catch (IOException e) {
            showErrorMessage("The program could not start, because a necessay file is not available. Please reinstall the software.");
            LOG.error("Could not read default properties file.", e);
            System.exit(0);
        }
    }
    protected final Properties properties = new Properties(defaultProps);
    {
        try {
            properties.load(new FileInputStream(PROPSFILE));
            LOG.debug("Normal properties loaded e.g previousdirectory="+defaultProps.getProperty(Constants.PREVIOUSDIRECTORY));
        }catch (IOException e) {
            LOG.error("Could not read properties file.", e);
        }
    }
    private final ProfileManager profileManager = new ProfileManager(this);
    private Vector<PropertyChangeListener> propertyChangeListeners = new Vector<PropertyChangeListener>();

    private SelectSelectionPanel selectSelectionPanel;
    private NoteSelectionPanel noteSelectionPanel;
    private RestSelectionPanel restSelectionPanel;
    private OtherSelectionPanel otherSelectionPanel;
    private StatusBar statusBar;

    private InsertMenu insertMenu;
    private PlayMenu playMenu = new PlayMenu(this);
    private JMenu controlMenu;

    protected AbstractAction saveAction;
    protected AbstractAction saveAsAction;
    protected ExitAction exitAction = new ExitAction();
    private DialogOpenAction aboutAction = new DialogOpenAction(this, "About", "info.png", AboutDialog.class);
    private DialogOpenAction prefAction = new DialogOpenAction(this, "Preferences...", "configure.png", PreferencesDialog.class);    
    private DialogOpenAction compositionSettingsAction = new DialogOpenAction(this, "Composition settings...", "compositionsettings.png", KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), CompositionSettingsDialog.class);

    private ModeAction[] modeActions;
    private ControlAction[] controlActions;
    protected File previousDirectory;
    private PrintAction printAction;

    public MainFrame() {
        PROGNAME = "Song Writer";
        lastWordForDoYouWannaSaveDialog = "song";
        if(properties.getProperty(Constants.FIRSTRUN).equals(Constants.TRUEVALUE))firstRun();
        previousDirectory = new File(properties.getProperty(Constants.PREVIOUSDIRECTORY));
        if(previousDirectory.getName().length()==0 || !previousDirectory.exists()){
            previousDirectory = new File(new JFileChooser().getCurrentDirectory(),"SongScribe");
        }
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            showErrorMessage(PROGNAME+" cannot start because of an initialization error.");
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

    private void firstRun() {
        File ssDocs = new File(new JFileChooser().getCurrentDirectory(), "SongScribe");
        boolean ssDocsCreated = ssDocs.mkdir();
        if(ssDocsCreated){
            for(File file:new File("examples").listFiles()){
                try {
                    Utilities.copyFile(file, new File(ssDocs, file.getName()));
                } catch (IOException e) {
                    LOG.error("Cannot copy example file", e);
                }
            }
            previousDirectory = ssDocs;
        }
        properties.setProperty(Constants.FIRSTRUN, Constants.FALSEVALUE);
    }

    public void initFrame(){
        setTitle(PROGNAME);
        setIconImage(getImage("swicon.png"));
        init();

        if (Utilities.isMac())
            MacAdapter.attachTo(this, true);
        
        pack();
        musicSheet.requestFocusInWindow();
        Rectangle mxBound = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(getSize().width, mxBound.width), Math.min(getSize().height, mxBound.height));
        setLocation(Math.max(CENTERPOINT.x-getWidth()/2, 0), Math.max(CENTERPOINT.y-getHeight()/2, 0));
        setVisible(true);
        fireMusicChanged(this);
        automaticCheckForUpdate();
    }

    private void init() {
        //CENTER
        musicSheet = new MusicSheet(this);
        musicSheet.initComponent();
        PlaybackPanel playbackPanel = new PlaybackPanel(this);
//        JPanel center = new JPanel();
//        center.setLayout(new BorderLayout());
        JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        center.setContinuousLayout(true);
        center.setResizeWeight(0.8);
        center.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        center.setTopComponent(musicSheet.getScrolledMusicSheet());
        center.setBottomComponent(playbackPanel);
        getContentPane().add(center);

        //WEST
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
        westToggleButton = new WestToggleButton(Note.clipNoteImage(Crotchet.UPIMAGE, Crotchet.REALUPNOTERECT, OUTERSELECTIONIMAGEBORDERCOLOR, OUTERSELECTIONPANELIMAGEDIM), noteSelectionPanel, "Note addition");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        westToggleButton.doClick();
        westToggleButton = new WestToggleButton(Note.clipNoteImage(CrotchetRest.IMAGE, CrotchetRest.REALNOTERECT, OUTERSELECTIONIMAGEBORDERCOLOR, OUTERSELECTIONPANELIMAGEDIM), restSelectionPanel, "Rest addition");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        westToggleButton = new WestToggleButton(MainFrame.getImage("triplettab.gif"), beamSelectionPanel, "Beamings, triplets, ties");
        mainWestGroup.add(westToggleButton);
        mainWestTools.add(westToggleButton);
        westToggleButton = new WestToggleButton(Note.clipNoteImage(RepeatLeft.IMAGE, RepeatLeft.REALNOTERECT, OUTERSELECTIONIMAGEBORDERCOLOR, OUTERSELECTIONPANELIMAGEDIM), otherSelectionPanel, "Others");
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

        //SOUTH
        statusBar = new StatusBar(this);
        getContentPane().add(BorderLayout.SOUTH, statusBar);

        //MENUBAR
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
        fileMenu.addSeparator();
        printAction = new PrintAction(this);
        fileMenu.add(printAction);
        if(!Utilities.isMac()) {
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
        for(MusicSheet.Control control:MusicSheet.Control.values()){
            controlActions[control.ordinal()] = new ControlAction(this, control);
            JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(controlActions[control.ordinal()]);
            controlActions[control.ordinal()].addAbstractButton(jrbmi);
            bg.add(jrbmi);
            controlMenu.add(jrbmi);
        }
        editMenu.add(controlMenu);
        if(!Utilities.isMac()) {
            editMenu.add(prefAction);
        }
        /*JMenu renderMenu = new JMenu("Rendering");
        renderMenu.setIcon(new ImageIcon(getImage("looknfeel.png")));
        ButtonGroup renderGroup = new ButtonGroup();
        for(final MusicSheet.DrawerType dt:MusicSheet.DrawerType.values()){
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(dt.getMenuName());            
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    musicSheet.setDrawer(dt);
                    properties.setProperty(Constants.RENDERING, dt.name());
                    musicSheet.setRepaintImage(true);
                    musicSheet.repaint();
                }
            });
            renderGroup.add(item);
            renderMenu.add(item);
            if(dt.name().equals(properties.getProperty(Constants.RENDERING))){
                item.setSelected(true);
            }
        }
        editMenu.add(renderMenu);    */

        JMenu modeMenu = new JMenu("Mode");
        bg = new ButtonGroup();
        modeActions = new ModeAction[MusicSheet.Mode.values().length];
        for(MusicSheet.Mode mode:MusicSheet.Mode.values()){
            modeActions[mode.ordinal()] = new ModeAction(this, mode);
            JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(modeActions[mode.ordinal()]);
            modeActions[mode.ordinal()].addAbstractButton(jrbmi);
            bg.add(jrbmi);
            modeMenu.add(jrbmi);
        }

        insertMenu = new InsertMenu(this);
        insertMenu.addSeparator();
        insertMenu.add(new TempoChangeAction(this));
        insertMenu.add(new BeatChangeAction(this));
        insertMenu.add(new AnnotationAction(this));
        insertMenu.add(new KeySignatureChangeAction(this));
        AddLineAction addLineAction =  new AddLineAction(this);
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

        //TOOLBAR
        Dimension separator = new Dimension(10, 0);
        JToolBar toolBar =  new JToolBar();
        addActionToToolBarToShowShortDescription(toolBar, newAction);
        addActionToToolBarToShowShortDescription(toolBar, openAction);
        toolBar.addSeparator(separator);
        addActionToToolBarToShowShortDescription(toolBar, saveAction);
        addActionToToolBarToShowShortDescription(toolBar, exportMidiAction);
        addActionToToolBarToShowShortDescription(toolBar, exportMusicSheetImageAction);
        addActionToToolBarToShowShortDescription(toolBar, exportPDFAction);
        toolBar.addSeparator(separator);
        toolBar.addSeparator(new Dimension());//this is important, because when publisher opens mainframe for editing a song, it removes all buttons antil the double separator found
        bg = new ButtonGroup();
        for(int i=0;i<2;i++){
            JToggleButton b = new JToggleButton(modeActions[i]);
            b.setText(null);
            toolBar.add(b);
            modeActions[i].addAbstractButton(b);
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

    protected void makeCommonHelpMenu(JMenu helpMenu) {
        helpMenu.add(new DialogOpenAction(this, "Check for Update...", new ImageIcon(getImage("download_manager.png")), UpdateDialog.class));
        helpMenu.add(new DialogOpenAction(this, "Report a Bug", new ImageIcon(getImage("bug.png")), ReportBugDialog.class));
        if(!Utilities.isMac()) {
            helpMenu.add(aboutAction);
        }
    }

    public JButton addActionToToolBarToShowShortDescription(JToolBar toolBar, Action action) {
        if(action.getValue(Action.SHORT_DESCRIPTION)==null) {
            JButton button = new JButton(action);
            button.setText(null);
            button.setToolTipText((String) action.getValue(Action.NAME));
            toolBar.add(button);
            return button;
        }else{
            return toolBar.add(action);
        }
    }

    public void setSelectedTool(SelectionPanel selectionPanel) {
        for(Enumeration e = mainWestGroup.getElements();e.hasMoreElements();){
            WestToggleButton wtb = (WestToggleButton)e.nextElement();
            if(wtb.getSelectionPanel()==selectionPanel) {
                wtb.doClick();
                return;
            }
        }
    }

    public boolean showSaveDialog() {
        if(!modifiedDocument)return true;
        int answ = JOptionPane.showConfirmDialog(this, "The document has been modified.\nDo you want to save the "+lastWordForDoYouWannaSaveDialog+"?", PROGNAME, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(answ==JOptionPane.YES_OPTION) {
            saveAction.actionPerformed(null);
        }
        return answ!=JOptionPane.CANCEL_OPTION;
    }

    public void modifiedDocument(){
        modifiedDocument = true;
    }

    public void unmodifiedDocument(){
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

    public StatusBar getStatusBar() {
        return statusBar;
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

    public void addProperyChangeListener(PropertyChangeListener pcl){
        propertyChangeListeners.add(pcl);
    }

    public void fireMusicChanged(Object sender){
        for(PropertyChangeListener pcl : propertyChangeListeners){
            if(pcl!=sender){
                pcl.musicChanged(properties);
            }
        }
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, PROGNAME, JOptionPane.ERROR_MESSAGE);
    }

    public void setSaveFile(File saveFile){
        this.saveFile = saveFile;
        if(saveFile==null){
            setTitle(PROGNAME);
        }else{
            setTitle(PROGNAME+" - "+saveFile.getName());
        }
    }

    public File getSaveFile() {
        return saveFile;
    }

    public Properties getProperties() {
        return properties;
    }

    public AbstractAction getSaveAction() {
        return saveAction;
    }

    public AbstractAction getSaveAsAction() {
        return saveAsAction;
    }

    public void setNextMode(){
        MusicSheet.Mode mode = musicSheet.getMode();
        modeActions[mode.ordinal()+1==modeActions.length ? 0 : mode.ordinal()+1].actionPerformed(null);
    }

    public void setMode(MusicSheet.Mode mode){
        if(mode!=musicSheet.getMode()) {
            modeActions[mode.ordinal()].actionPerformed(null);
        }
    }

    public void setNextControl(){
        if(controlMenu.isEnabled()){
            MusicSheet.Control control = musicSheet.getControl();
            controlActions[control.ordinal()+1==controlActions.length ? 0 : control.ordinal()+1].actionPerformed(null);
        }
    }

    protected static void openMidi() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            Soundbank sb = MidiSystem.getSoundbank(new File("libs/sound/soundbank-deluxe.gm"));
            if(!synthesizer.loadAllInstruments(sb))throw new IOException();
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            receiver = MidiSystem.getReceiver();
        } catch (MidiUnavailableException e) {
            hideSplash();
            JOptionPane.showMessageDialog(null, String.format("You may be already running %s or another program that uses sound.\n" +
                    "Please try to quit them and restart %s.\n" +
                    "In this session playback will be disabled.", PACKAGENAME, PACKAGENAME),
                    PACKAGENAME, JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            hideSplash();
            JOptionPane.showMessageDialog(null, "Cannot read the soundbank. Playing will be disabled. Try to reinstall the program.", PACKAGENAME, JOptionPane.WARNING_MESSAGE);
        } catch (InvalidMidiDataException e) {
            hideSplash();
            JOptionPane.showMessageDialog(null, "The soundbank file looks like damaged. Playing will be disabled. Try to reinstall the program.", PACKAGENAME, JOptionPane.WARNING_MESSAGE);
        }
    }

    protected static void closeMidi(){
        if(receiver!=null)receiver.close();
        if(sequencer!=null)sequencer.close();
        if(synthesizer!=null)synthesizer.close();
    }

    public static void main(String[] args) {
        showSplash("swsplash.png");
        PropertyConfigurator.configure("conf/logger.properties");        
        openMidi();
        try{
            MainFrame mf = new MainFrame();
            mf.initFrame();
            try{
                if(mf.properties.getProperty(Constants.SHOWTIP).equals(Constants.TRUEVALUE)){
                    new TipFrame(mf);
                }
            }catch(IOException e){
                hideSplash();
                mf.showErrorMessage("Cannot read the tip file");
                mf.properties.setProperty(Constants.SHOWTIP, Constants.FALSEVALUE);
            }
            if(args.length>0){
                File f = new File(args[0]);
                if(f.exists()){
                    mf.handleOpenFile(f);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            LOG.error("main", e);
        }

        hideSplash();
    }

    protected static void showSplash(String splashScreen) {
        splashWindow = new JWindow((Frame)null);
        JLabel splashLabel = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().createImage("images/"+splashScreen)));        
        splashWindow.getContentPane().add(splashLabel);
        splashWindow.pack();
        splashWindow.setLocation(CENTERPOINT.x-splashWindow.getWidth()/2, CENTERPOINT.y-splashWindow.getHeight()/2);
        splashWindow.setVisible(true);
        splashWindow.getRootPane().setGlassPane(new JComponent(){
            protected void paintComponent(Graphics g) {
                g.drawImage(getImage("version.png"), 10, 481, null);
                g.setFont(new Font("Serif", Font.BOLD, 24));
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawString(Utilities.getVersion(), 75, 500);
                g.drawImage(getImage("years.png"), 322, 488, null);
                g.drawImage(getImage("name.png"), 419, 485, null);
            }
        });
        splashWindow.getRootPane().getGlassPane().setVisible(true);
    }

    protected static void hideSplash() {
        if (splashWindow != null) {
            splashWindow.dispose();
        }
    }

    protected void automaticCheckForUpdate(){
        try{
            if(System.currentTimeMillis()-Long.parseLong(properties.getProperty(Constants.LASTAUTOUPDATE))>Long.parseLong(properties.getProperty(Constants.AUTOUPDATEPERIOD))){
                new UpdateDialog(this).new UpdateInternetThread(true).start();
                properties.setProperty(Constants.LASTAUTOUPDATE, Long.toString(System.currentTimeMillis()));
            }
        }catch(NumberFormatException nf){
            LOG.error("Lastupdate property is not a number.");
        }
    }

    public static Image getImage(File file){
        Image img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
        if (img == null) {
            return null;
        }
        try {
            mediaTracker.addImage(img, 0);
            mediaTracker.waitForID(0);
        } catch (InterruptedException ignored) {}
        return img;
    }

    public static Image getImage(String fileName) {
        Image img = Toolkit.getDefaultToolkit().createImage("images/" + fileName);

        if (img == null) {
            return null;
        }
        try {
            mediaTracker.addImage(img, 0);
            mediaTracker.waitForID(0);
        } catch (InterruptedException ignored) {}
        return img;
    }

    public void openMusicSheet(File openFile, boolean setTitle){
        boolean previousModifiedDocument = modifiedDocument;
        try {
            CompositionIO.DocumentReader dr = new CompositionIO.DocumentReader(this);
            saxParser.parse(openFile, dr);
            musicSheet.setComposition(dr.getComposion());
            if(setTitle)setSaveFile(openFile);
        } catch (SAXException e1) {
            showErrorMessage("Could not open the file "+openFile.getName()+", because it is damaged.");
            LOG.error("SaxParser parse", e1);
        } catch (IOException e1) {
            showErrorMessage("Could not open the file "+openFile.getName()+". Check if you have the permission to open it.");
            LOG.error("Song open", e1);
        }
        modifiedDocument = previousModifiedDocument;
    }
    public static void setMediaTracker(MediaTracker mt) {
        MainFrame.mediaTracker = mt;
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
            if(!quit)return;
            if(musicSheet!=null)musicSheet.saveProperties();
            properties.setProperty(Constants.PREVIOUSDIRECTORY, previousDirectory.getAbsolutePath());
            try {
                properties.store(new FileOutputStream(PROPSFILE), null);
            } catch (IOException e1) {
                e1.printStackTrace();
                showErrorMessage("Could not save the properties file. Please reinstall the software.");
                LOG.error("Save props", e1);
            }
            closeMidi();
            System.exit(0);
        }
    }

    private class WestToggleButton extends JToggleButton{
        private final String ID = toString();
        private SelectionPanel selectionPanel;
        public WestToggleButton(Image icon, final SelectionPanel selectionPanel, String tip) {
            super(new ImageIcon(icon));
            this.selectionPanel = selectionPanel;
            addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    selectionPanel.setActive();
                    ((CardLayout)subWestToolsHolder.getLayout()).show(subWestToolsHolder, ID);
                    if(selectionPanel!=selectSelectionPanel) setMode(MusicSheet.Mode.NOTEEDIT);
                }
            });
            setToolTipText(tip);
            subWestToolsHolder.add(selectionPanel, ID);
        }

        public SelectionPanel getSelectionPanel() {
            return selectionPanel;
        }
    }


    public void handleAbout() {
        aboutAction.actionPerformed(null);
    }

    public void handlePrefs() throws IllegalStateException {
        prefAction.actionPerformed(null);
    }

    public boolean handleQuit() {
        exitAction.actionPerformed(null);
        return (Boolean) exitAction.getValue("quit");
    }

    public void handleOpenFile(File file) {
        if(!showSaveDialog())return;
        openMusicSheet(file, true);
        setSelectedTool(selectSelectionPanel);
        selectSelectionPanel.setActive();
        unmodifiedDocument();
    }

    public void handlePrintFile(File file) {
        handleOpenFile(file);
        printAction.actionPerformed(null);
    }
}
