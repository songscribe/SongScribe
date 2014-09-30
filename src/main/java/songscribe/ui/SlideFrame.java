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

    Created on: 2006.03.11.
*/
package songscribe.ui;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;
import songscribe.IO.SliderIO;
import songscribe.data.FileExtensions;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.ui.playsubmenu.InstrumentDialog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Csaba Kávai
 */
public class SlideFrame extends MainFrame {
    private static Logger logger = Logger.getLogger(SlideFrame.class);

    private DefaultListModel listModel = new DefaultListModel();
    private JList list = new JList(listModel);
    private JComboBox instrumentCombo;
    private JCheckBox playWithRepeatCheck;
    private JCheckBox colorizeNoteCheck;

    private WardAction forwardAction = new WardAction(1);
    private WardAction backwardAction = new WardAction(-1);
    private FirstAction firstAction = new FirstAction();
    private LastAction lastAction = new LastAction();
    private NewAction newAction = new NewAction();
    private OpenAction openAction = new OpenAction();

    private ArrayList<File> files = new ArrayList<File>();
    private FullScreenSheet fullScreenSheet;
    private SAXParser saxParser;
    private int currentSlide;

    public SlideFrame() {
        PROG_NAME = "Song Show";
        lastWordForDoYouWannaSaveDialog = "list";
        setTitle(PROG_NAME);
        setIconImage(getImage("ssicon.png"));

        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        }
        catch (Exception e) {
            showErrorMessage(PROG_NAME + " cannot start because of an initialization error.");
            logger.error("SaxParser configuration", e);
            System.exit(0);
        }

        init();
        pack();
        setLocation(CENTER_POINT.x - getWidth() / 2, CENTER_POINT.y - getHeight() / 2);
        setVisible(true);
        properties.setProperty(Constants.TEMPO_CHANGE_PROP, "100");
        fireMusicChanged(this);
        automaticCheckForUpdate();
    }

    public static void main(String[] args) {
        showSplash("sssplash.png");
        PropertyConfigurator.configure("conf/logger.properties");
        openMidi();
        SlideFrame sf = new SlideFrame();
        hideSplash();

        if (Utilities.isMac()) {
            MacAdapter.attachTo(sf, true);
        }
    }

    private void initComponents() {
        JButton addButton;
        JButton addFolderButton;
        JButton downButton;
        JScrollPane listScroll;
        JPanel playBackPanel;
        JButton removeButton;
        JLabel slideShowList;
        JPanel southPanel;
        JButton startButton;
        JButton upButton;

        slideShowList = new JLabel();
        listScroll = new JScrollPane();
        list = new JList();
        addButton = new JButton();
        addFolderButton = new JButton();
        removeButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        playBackPanel = new JPanel();
        playWithRepeatCheck = new JCheckBox();
        colorizeNoteCheck = new JCheckBox();
        instrumentCombo = new JComboBox();
        southPanel = new JPanel();
        startButton = new JButton();

        slideShowList.setText("SlideShow List:");

        list.setModel(listModel);
        listScroll.setViewportView(list);

        addButton.setAction(new AddAction());

        addFolderButton.setAction(new AddFolderAction());

        removeButton.setAction(new RemoveAction());

        upButton.setAction(new UpAction());

        downButton.setAction(new DownAction());

        playBackPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Playback"));
        playWithRepeatCheck.setText("Play with repeats");
        playWithRepeatCheck.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        playWithRepeatCheck.setMargin(new Insets(0, 0, 0, 0));

        colorizeNoteCheck.setText("<html>Colorize the currently played note<br>when playing the song back.<html>");
        colorizeNoteCheck.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        colorizeNoteCheck.setMargin(new Insets(0, 0, 0, 0));

        instrumentCombo.setModel(new DefaultComboBoxModel(InstrumentDialog.INSTRUMENT_STRING));

        org.jdesktop.layout.GroupLayout playBackPanelLayout = new org.jdesktop.layout.GroupLayout(playBackPanel);
        playBackPanel.setLayout(playBackPanelLayout);
        playBackPanelLayout.setHorizontalGroup(playBackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(playBackPanelLayout.createSequentialGroup().addContainerGap().add(playBackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(playWithRepeatCheck).add(colorizeNoteCheck).add(instrumentCombo, 0, 241, Short.MAX_VALUE)).addContainerGap()));
        playBackPanelLayout.setVerticalGroup(playBackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(playBackPanelLayout.createSequentialGroup().add(playWithRepeatCheck).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(colorizeNoteCheck).add(17, 17, 17).add(instrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(13, Short.MAX_VALUE)));

        startButton.setAction(new SlideAction());
        southPanel.add(startButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(slideShowList).add(layout.createSequentialGroup().add(listScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 151, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(18, 18, 18).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(layout.createSequentialGroup().add(upButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(downButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(addFolderButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))).addContainerGap(24, Short.MAX_VALUE)).add(layout.createSequentialGroup().add(playBackPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))).add(org.jdesktop.layout.GroupLayout.TRAILING, southPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(slideShowList).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(listScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(layout.createSequentialGroup().add(addButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(addFolderButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(removeButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, downButton).add(org.jdesktop.layout.GroupLayout.TRAILING, upButton)))).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(playBackPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(southPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pack();
    }

    private void init() {
        musicSheet = new MusicSheet(this);
        musicSheet.initComponent();
        initComponents();
        PlaybackChangeAction pca = new PlaybackChangeAction();
        playWithRepeatCheck.setSelected(properties.getProperty(Constants.WITH_REPEAT_PROP).equals(Constants.TRUE_VALUE));
        playWithRepeatCheck.addActionListener(pca);
        colorizeNoteCheck.setSelected(properties.getProperty(Constants.COLORIZE_NOTE).equals(Constants.TRUE_VALUE));
        colorizeNoteCheck.addActionListener(pca);
        instrumentCombo.setSelectedIndex(Integer.parseInt(properties.getProperty(Constants.INSTRUMENT_PROP)));
        instrumentCombo.addActionListener(pca);

        // menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem(newAction));
        fileMenu.add(new JMenuItem(openAction));
        fileMenu.addSeparator();
        saveAction = new SaveAction();
        fileMenu.add(new JMenuItem(saveAction));
        saveAsAction = new SaveAsAction();
        fileMenu.add(new JMenuItem(saveAsAction));

        if (!Utilities.isMac()) {
            fileMenu.addSeparator();
            fileMenu.add(new JMenuItem(exitAction));
        }

        JMenu helpMenu = new JMenu("Help");
        makeCommonHelpMenu(helpMenu);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    public void setMode(MusicSheet.Mode mode) {
    }

    private String getListName(String fileName) {
        if (fileName.endsWith(FileExtensions.SONGWRITER)) {
            return fileName.substring(0, fileName.length() - 5);
        }
        else {
            return fileName;
        }
    }

    private void openSlide(File openFile) {
        previousDirectory = openFile.getParentFile();

        try {
            /*BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(openFile), "UTF-8"));
            files.clear();
            listModel.clear();
            String line;
            while((line=br.readLine())!=null){
                File f = new File(line);
                if(f.exists()){
                    listModel.addElement(getListName(f.getName()));
                    files.addElement(f);
                }
            }*/
            SliderIO.DocumentReader dr = new SliderIO.DocumentReader(this, openFile);
            saxParser.parse(openFile, dr);
            files = dr.getFiles();
            listModel.clear();

            for (File file : files) {
                listModel.addElement(getListName(file.getName()));
            }

            unmodifiedDocument();
        }
        catch (FileNotFoundException e1) {
            showErrorMessage("Could not open the file. Check if you have the permission to open it.");
            logger.error("SlideFrame open", e1);
        }
        catch (IOException e1) {
            showErrorMessage("Could not open the file. Check if you have the permission to open it.");
            logger.error("SlideFrame open", e1);
        }
        catch (SAXException e1) {
            showErrorMessage(e1.getMessage());
            logger.error("SlideFrame open", e1);
        }
    }

    public void handleOpenFile(File file) {
        if (!showSaveDialog()) {
            return;
        }

        openSlide(file);
    }

    private class AddAction extends AbstractAction {
        private PlatformFileDialog pfd;

        public AddAction() {
            putValue(Action.NAME, "Add");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("add.png")));
            pfd = new PlatformFileDialog(SlideFrame.this, "Open song", true, new MyAcceptFilter("SongScribe song files", FileExtensions.SONGWRITER.substring(1)));
            pfd.setMultiSelectionEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
            if (pfd.showDialog()) {
                File[] openFile = pfd.getFiles();

                for (File of : openFile) {
                    files.add(of);
                    listModel.addElement(getListName(of.getName()));
                }

                modifiedDocument();
            }
        }
    }

    private class AddFolderAction extends AbstractAction {
        private PlatformFileDialog pfd;

        public AddFolderAction() {
            putValue(Action.NAME, "Add Folder");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("addfolder.png")));
            pfd = new PlatformFileDialog(SlideFrame.this, "Open folder", true, new MyAcceptFilter("Folders"), true);
        }

        public void actionPerformed(ActionEvent e) {
            if (pfd.showDialog()) {
                int answ = JOptionPane.showConfirmDialog(SlideFrame.this, "All song files will be added from the selected folder.\nDo you want to add songs from its subfolders, too?", PROG_NAME, JOptionPane.YES_NO_CANCEL_OPTION);

                if (answ == JOptionPane.CANCEL_OPTION) {
                    return;
                }

                addSongFiles(pfd.getFile(), answ == JOptionPane.YES_OPTION);
                modifiedDocument();
            }
        }

        private void addSongFiles(File dir, boolean descend) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory() && descend) {
                    addSongFiles(file, descend);
                }
                else if (file.isFile() && file.getName().endsWith(FileExtensions.SONGWRITER)) {
                    files.add(file);
                    listModel.addElement(getListName(file.getName()));
                }
            }
        }
    }

    private class RemoveAction extends AbstractAction {
        public RemoveAction() {
            putValue(Action.NAME, "Remove");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("remove.png")));
        }

        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedIndex() != -1) {
                files.remove(list.getSelectedIndex());
                listModel.remove(list.getSelectedIndex());
                modifiedDocument();
            }
        }
    }

    private class UpAction extends AbstractAction {
        public UpAction() {
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("1uparrow32.png")));
            putValue(Action.SHORT_DESCRIPTION, "Move the selected item up in the list");
        }

        public void actionPerformed(ActionEvent e) {
            int sel = list.getSelectedIndex();

            if (sel > 0) {
                File f = files.get(sel);
                files.set(sel, files.get(sel - 1));
                files.set(sel - 1, f);
                Object o = listModel.get(sel);
                listModel.set(sel, listModel.get(sel - 1));
                listModel.set(sel - 1, o);
                list.setSelectedIndex(sel - 1);
                modifiedDocument();
            }
        }
    }

    private class DownAction extends AbstractAction {
        public DownAction() {
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("1downarrow32.png")));
            putValue(Action.SHORT_DESCRIPTION, "Move the selected item down in the list");
        }

        public void actionPerformed(ActionEvent e) {
            int sel = list.getSelectedIndex();

            if (sel != -1 && sel < listModel.size() - 1) {
                File f = files.get(sel);
                files.set(sel, files.get(sel + 1));
                files.set(sel + 1, f);
                Object o = listModel.get(sel);
                listModel.set(sel, listModel.get(sel + 1));
                listModel.set(sel + 1, o);
                list.setSelectedIndex(sel + 1);
                modifiedDocument();
            }
        }
    }

    private class PlaybackChangeAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            properties.setProperty(Constants.INSTRUMENT_PROP, Integer.toString(instrumentCombo.getSelectedIndex()));
            properties.setProperty(Constants.WITH_REPEAT_PROP, playWithRepeatCheck.isSelected() ? Constants.TRUE_VALUE : Constants.FALSE_VALUE);
            properties.setProperty(Constants.COLORIZE_NOTE, colorizeNoteCheck.isSelected() ? Constants.TRUE_VALUE : Constants.FALSE_VALUE);
        }
    }

    private class SlideAction extends AbstractAction {
        public SlideAction() {
            putValue(Action.NAME, "Start slide show");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("player_play.png")));
        }

        public void actionPerformed(ActionEvent e) {
            if (listModel.size() > 0) {
                if (currentSlide >= listModel.size()) {
                    currentSlide = 0;
                }

                openMusicSheet(files.get(currentSlide), false);
                fullScreenSheet = new FullScreenSheet(SlideFrame.this, new SliderTempoChangeListener(), firstAction, backwardAction, forwardAction, lastAction);
                fullScreenSheet.setVisible(true);
            }
        }
    }

    private class WardAction extends AbstractAction {
        int direction;

        public WardAction(int direction) {
            this.direction = direction;
            putValue(Action.SHORT_DESCRIPTION, direction ==
                                               -1 ? "Previous song (Left Arrow, Page Up)" : "Next song (Space, Enter, Right Arrow, Page Down)");
            putValue(Action.SMALL_ICON, new ImageIcon(SlideFrame.getImage(
                    direction == -1 ? "back.png" : "forward.png")));
            putValue(Constants.ACCELERATOR_KEYS, direction == -1 ? new KeyStroke[] {
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)
            } : new KeyStroke[] {
                    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
                    KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
            });
        }

        public void actionPerformed(ActionEvent e) {
            int next = currentSlide + direction;
            if (next >= 0 && next < listModel.size()) {
                openMusicSheet(files.get(next), false);
                fullScreenSheet.setMusicSheet();
                currentSlide = next;
            }
        }
    }

    private class FirstAction extends AbstractAction {
        public FirstAction() {
            putValue(Action.SHORT_DESCRIPTION, "First song (Home)");
            putValue(Action.SMALL_ICON, new ImageIcon(SlideFrame.getImage("start.png")));
            putValue(Constants.ACCELERATOR_KEYS, new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0) });
        }

        public void actionPerformed(ActionEvent e) {
            currentSlide = 0;
            openMusicSheet(files.get(currentSlide), false);
            fullScreenSheet.setMusicSheet();
        }
    }

    private class LastAction extends AbstractAction {
        public LastAction() {
            putValue(Action.SHORT_DESCRIPTION, "Last song (End)");
            putValue(Action.SMALL_ICON, new ImageIcon(SlideFrame.getImage("finish.png")));
            putValue(Constants.ACCELERATOR_KEYS, new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_END, 0) });
        }

        public void actionPerformed(ActionEvent e) {
            currentSlide = listModel.size() - 1;
            openMusicSheet(files.get(currentSlide), false);
            fullScreenSheet.setMusicSheet();
        }
    }

    private class SliderTempoChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            properties.setProperty(Constants.TEMPO_CHANGE_PROP, Integer.toString(((JSlider) e.getSource()).getValue()));
            fireMusicChanged(SlideFrame.this);
        }
    }

    private class NewAction extends AbstractAction {
        public NewAction() {
            putValue(Action.NAME, "New");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("filenew.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            if (!showSaveDialog()) {
                return;
            }

            setSaveFile(null);
            files.clear();
            listModel.clear();
            unmodifiedDocument();
        }
    }

    private class SaveAction extends AbstractAction {
        public SaveAction() {
            putValue(Action.NAME, "Save");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("filesave.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            if (saveFile == null) {
                saveAsAction.actionPerformed(e);
                return;
            }

            try {
                SliderIO.writeSlider(files.listIterator(), saveFile);
                unmodifiedDocument();
            }
            catch (IOException e1) {
                showErrorMessage(COULD_NOT_SAVE_MESSAGE);
                logger.error("Saving slideframe", e1);
            }
        }
    }

    private class SaveAsAction extends AbstractAction {
        private PlatformFileDialog pfd;

        public SaveAsAction() {
            putValue(Action.NAME, "Save as...");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("filesaveas.png")));
            pfd = new PlatformFileDialog(SlideFrame.this, "Save As", false, new MyAcceptFilter("SongScribe Song Show files", FileExtensions.SONGSHOW.substring(1)));
        }

        public void actionPerformed(ActionEvent e) {
            if (pfd.showDialog()) {
                File saveFile = pfd.getFile();

                if (!saveFile.getName().toLowerCase().endsWith(FileExtensions.SONGSHOW)) {
                    saveFile = new File(saveFile.getAbsolutePath() + FileExtensions.SONGSHOW);
                }

                if (saveFile.exists()) {
                    int answ = JOptionPane.showConfirmDialog(SlideFrame.this, "The file " + saveFile.getName() +
                                                                              " already exists. Do you want to overwrite it?", PROG_NAME, JOptionPane.YES_NO_OPTION);
                    if (answ == JOptionPane.NO_OPTION) {
                        return;
                    }
                }

                previousDirectory = saveFile.getParentFile();
                setSaveFile(saveFile);
                saveAction.actionPerformed(e);
            }
        }
    }

    private class OpenAction extends AbstractAction {
        private PlatformFileDialog pfd;

        public OpenAction() {
            putValue(Action.NAME, "Open...");
            putValue(Action.SMALL_ICON, new ImageIcon(getImage("fileopen.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            pfd = new PlatformFileDialog(SlideFrame.this, "Open", true, new MyAcceptFilter("SongScribe Song Show files", FileExtensions.SONGSHOW.substring(1)));
        }

        public void actionPerformed(ActionEvent e) {
            if (!showSaveDialog()) {
                return;
            }

            if (pfd.showDialog()) {
                openSlide(pfd.getFile());
            }
        }
    }
}
