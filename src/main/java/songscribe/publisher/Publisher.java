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

    Created on Sep 21, 2006
*/
package songscribe.publisher;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;
import songscribe.SongScribe;
import songscribe.publisher.IO.BookIO;
import songscribe.publisher.newsteps.PaperSizeDialog;
import songscribe.publisher.publisheractions.AlignToCenter;
import songscribe.publisher.publisheractions.ExportBatchSongsImages;
import songscribe.publisher.publisheractions.ExportBatchSongsMidi;
import songscribe.publisher.publisheractions.ExportPDFAction;
import songscribe.publisher.publisheractions.ExportPortableAction;
import songscribe.publisher.publisheractions.ImportPortableAction;
import songscribe.publisher.publisheractions.InsertImageAction;
import songscribe.publisher.publisheractions.InsertPageAction;
import songscribe.publisher.publisheractions.InsertSelectedPageAction;
import songscribe.publisher.publisheractions.InsertSongAction;
import songscribe.publisher.publisheractions.InsertTextAction;
import songscribe.publisher.publisheractions.LowerComponentAction;
import songscribe.publisher.publisheractions.NewAction;
import songscribe.publisher.publisheractions.OpenAction;
import songscribe.publisher.publisheractions.PropertiesAction;
import songscribe.publisher.publisheractions.RaiseComponentAction;
import songscribe.publisher.publisheractions.RefreshAction;
import songscribe.publisher.publisheractions.RemoveAction;
import songscribe.publisher.publisheractions.SaveAction;
import songscribe.publisher.publisheractions.SaveAsAction;
import songscribe.publisher.publisheractions.ToBottomComponentAction;
import songscribe.publisher.publisheractions.ToTopComponentAction;
import songscribe.publisher.publisheractions.ZoomAction;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.mainframeactions.DialogOpenAction;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class Publisher extends MainFrame {
    private static Logger logger = Logger.getLogger(Publisher.class);
    private static Graphics graphics;
    private SAXParser saxParser;
    private JComboBox zoomCombo = new JComboBox(new String[] { "25%", "50%", "75%", "100%", "150%", "200%" });
    private Book book;
    private StatusBar statusBar;
    private RaiseComponentAction raiseComponentAction = new RaiseComponentAction(this);
    private LowerComponentAction lowerComponentAction = new LowerComponentAction(this);
    private ToTopComponentAction toTopComponentAction = new ToTopComponentAction(this);
    private ToBottomComponentAction toBottomComponentAction = new ToBottomComponentAction(this);

    private InsertPageAction insertPageActionBeginning = new InsertPageAction(this, InsertPageAction.Where.BEGINNING, "At the beginning", "insertbegin.png");
    private InsertSelectedPageAction insertSelectedPageActionBefore = new InsertSelectedPageAction(this, "Before the selected page", "insertbefore.png", 0);
    private InsertSelectedPageAction insertSelectedPageActionAfter = new InsertSelectedPageAction(this, "After the selected page", "insertafter.png", 1);
    private InsertPageAction insertPageActionEnd = new InsertPageAction(this, InsertPageAction.Where.END, "At the end", "insertend.png");
    private InsertSongAction insertSongAction = new InsertSongAction(this);
    private InsertImageAction insertImageAction = new InsertImageAction(this);
    private InsertTextAction insertTextAction = new InsertTextAction(this);

    private JPopupMenu defaultPopup;

    private PropertiesAction propertiesAction = new PropertiesAction(this);
    private RemoveAction removeAction = new RemoveAction(this);
    private DialogOpenAction paperSizeDialogOpenAction = new DialogOpenAction(this, "Page format", "pageproperties.png", PaperSizeDialog.class);

    public Publisher() {
        PROG_NAME = "Song Book";
        lastWordForDoYouWannaSaveDialog = "song book";
        setTitle(PROG_NAME);
        setIconImage(getImage("sbicon.png"));
        Rectangle maximumWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setPreferredSize(new Dimension(maximumWindowBounds.width * 3 / 4, maximumWindowBounds.height));

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
        graphics = getGraphics();
        automaticCheckForUpdate();
    }

    public static void main(String[] args) {
        showSplash("sbsplash.png");
        PropertyConfigurator.configure(SongScribe.basePath + "/conf/logger.properties");
        openMidi();
        Publisher pub = new Publisher();
        hideSplash();
    }

    public static FontMetrics getStaticFontMetrics(Font font) {
        if (graphics == null) {
            return null;
        }

        return graphics.getFontMetrics(font);
    }

    private void init() {
        // menu bar
        JMenuBar menuBar = new JMenuBar();

        // file menu
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
        ExportPDFAction exportPDFAction = new ExportPDFAction(this);
        fileMenu.add(exportPDFAction);
        fileMenu.add(new ExportPortableAction(this));
        fileMenu.add(new ImportPortableAction(this));

        if (!songscribe.ui.Utilities.isMac()) {
            fileMenu.addSeparator();
            fileMenu.add(exitAction);
        }

        menuBar.add(fileMenu);

        // edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(removeAction);
        editMenu.add(propertiesAction);
        editMenu.add(paperSizeDialogOpenAction);
        editMenu.add(orderMenuFactory());
        editMenu.addSeparator();
        editMenu.add(new RefreshAction(this));
        menuBar.add(editMenu);

        // insert menu
        JMenu insertMenu = insertMenuFactory();
        insertMenu.setIcon(null);
        menuBar.add(insertMenu);

        // tools menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(new DialogOpenAction(this, "Page number ...", blankIcon, PageNumberDialog.class));
        toolsMenu.add(new ExportBatchSongsImages(this));
        toolsMenu.add(new ExportBatchSongsMidi(this));
        menuBar.add(toolsMenu);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        makeCommonHelpMenu(helpMenu);
        menuBar.add(helpMenu);

        setupDesktopHandlers(true, menuBar);

        // default popup
        defaultPopup = new JPopupMenu();
        defaultPopup.add(insertMenuFactory());

        // toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addActionToToolBarToShowShortDescription(toolBar, newAction);
        addActionToToolBarToShowShortDescription(toolBar, openAction);
        toolBar.addSeparator();
        addActionToToolBarToShowShortDescription(toolBar, saveAction);
        addActionToToolBarToShowShortDescription(toolBar, exportPDFAction);
        toolBar.addSeparator();
        zoomCombo.setEditable(true);
        toolBar.add(zoomCombo);
        zoomCombo.setSelectedItem("100%");
        zoomCombo.addActionListener(new ZoomAction(this));
        zoomCombo.setToolTipText("Zoom");
        toolBar.addSeparator();
        addActionToToolBarToShowShortDescription(toolBar, new AlignToCenter(this));
        getContentPane().add(BorderLayout.NORTH, toolBar);

        getContentPane().add(BorderLayout.WEST, new WestTools(this));

        // SOUTH
        statusBar = new StatusBar(this);
        getContentPane().add(BorderLayout.SOUTH, statusBar);


    }

    public JMenu insertMenuFactory() {
        JMenu insertMenu = new JMenu("Insert");
        insertMenu.setIcon(new ImageIcon(getImage("edit_add.png")));
        JMenu insertPageMenu = new JMenu("Page");
        insertPageMenu.setIcon(new ImageIcon(getImage("filenew.png")));
        insertPageMenu.add(insertPageActionBeginning);
        insertPageMenu.add(insertSelectedPageActionBefore);
        insertPageMenu.add(insertSelectedPageActionAfter);
        insertPageMenu.add(insertPageActionEnd);
        insertMenu.add(insertPageMenu);
        insertMenu.add(insertSongAction);
        insertMenu.add(insertImageAction);
        insertMenu.add(insertTextAction);
        return insertMenu;
    }

    public JMenu orderMenuFactory() {
        JMenu orderMenu = new JMenu("Order");
        orderMenu.setIcon(new ImageIcon(getImage("order.png")));
        orderMenu.add(raiseComponentAction);
        orderMenu.add(lowerComponentAction);
        orderMenu.add(toTopComponentAction);
        orderMenu.add(toBottomComponentAction);
        return orderMenu;
    }

    public void setMode(MusicSheet.Mode mode) {
    }

    public boolean showSaveDialog() {
        return book == null || super.showSaveDialog();
    }

    public boolean isBookNull() {
        if (book == null) {
            showErrorMessage("You must begin with a new document. Click on File | New menu");
        }

        return book == null;
    }

    public MusicSheet openMusicSheet(File openFile) {
        musicSheet = new MusicSheet(this);
        musicSheet.openMusicSheet(this, openFile, false);
        return musicSheet;
    }

    public void openBook(File openFile) {
        try {
            BookIO.DocumentReader dr = new BookIO.DocumentReader(this, openFile);
            saxParser.parse(openFile, dr);
            setBook(dr.getBook());
            setSaveFile(openFile);
        }
        catch (SAXException e1) {
            showErrorMessage("Could not open the file " + openFile.getName() + ", because it is damaged.");
            logger.error("SaxParser parse", e1);
        }
        catch (IOException e1) {
            showErrorMessage(
                    "Could not open the file " + openFile.getName() + ". Check if you have the permission to open it.");
            logger.error("Song open", e1);
        }
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        if (this.book != null) {
            getContentPane().remove(this.book.getScrolledBook());
        }

        this.book = book;
        getContentPane().add(book.getScrolledBook());
        zoomCombo.setSelectedItem("100%");
        book.repaintWhole();
        validate();
    }

    public JComboBox getZoomCombo() {
        return zoomCombo;
    }

    public InsertImageAction getInsertImageAction() {
        return insertImageAction;
    }

    public InsertSongAction getInsertSongAction() {
        return insertSongAction;
    }

    public InsertTextAction getInsertTextAction() {
        return insertTextAction;
    }

    public PropertiesAction getPropertiesAction() {
        return propertiesAction;
    }

    public RemoveAction getRemoveAction() {
        return removeAction;
    }

    public JPopupMenu getDefaultPopup() {
        return defaultPopup;
    }

    public StatusBar getPStatusBar() {
        return statusBar;
    }

    public DialogOpenAction getPaperSizeDialogOpenAction() {
        return paperSizeDialogOpenAction;
    }

    public void handleOpenFile(File file) {
        if (!showSaveDialog()) {
            return;
        }

        openBook(file);
        setModifiedDocument(false);
    }

    @Override
    public void handlePrefs() throws IllegalStateException {
        getPreferencesDialog().setVisible(true);
    }
}
