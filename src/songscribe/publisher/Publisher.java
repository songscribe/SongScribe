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

Created on Sep 21, 2006
*/
package songscribe.publisher;

import songscribe.ui.mainframeactions.DialogOpenAction;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.publisher.publisheractions.*;
import songscribe.publisher.IO.BookIO;
import songscribe.publisher.newsteps.PaperSizeDialog;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.util.Date;
import java.io.File;
import java.io.IOException;

import com.apple.mrj.MRJApplicationUtils;

/**
 * @author Csaba Kávai
 */
public class Publisher extends MainFrame {
    private static Logger logger = Logger.getLogger(Publisher.class);
    private SAXParser saxParser;

    private JComboBox zoomCombo = new JComboBox(new String[]{"25%", "50%", "75%", "100%", "150%", "200%"});

    private Book book;
    private StatusBar statusBar;
    private static Graphics graphics;

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
        PROGNAME = "Song Book";
        lastWordForDoYouWannaSaveDialog = "song book";
        setTitle(PROGNAME);
        setIconImage(getImage("sbicon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Rectangle maximumWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setPreferredSize(new Dimension(maximumWindowBounds.width*3/4, maximumWindowBounds.height));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAction.actionPerformed(null);
            }
        });
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            showErrorMessage(PROGNAME+" cannot start because of an initialization error.");
            logger.error("SaxParser configuration", e);
            System.exit(0);
        }
        init();
        pack();
        setLocation(CENTERPOINT.x-getWidth()/2, CENTERPOINT.y-getHeight()/2);
        setVisible(true);
        MRJApplicationUtils.registerPrefsHandler(null);
        MRJApplicationUtils.registerPrintDocumentHandler(null);
        graphics = getGraphics();
        automaticCheckForUpdate();
    }

    private void init(){
        //menu
        JMenuBar menuBar = new JMenuBar();
        //file menu
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
        if(!songscribe.ui.Utilities.isMac()){
            fileMenu.addSeparator();
            fileMenu.add(exitAction);
        }

        menuBar.add(fileMenu);

        //edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(removeAction);
        editMenu.add(propertiesAction);
        editMenu.add(paperSizeDialogOpenAction);
        editMenu.add(orderMenuFactory());
        editMenu.addSeparator();
        editMenu.add(new RefreshAction(this));
        menuBar.add(editMenu);

        //insert menu
        JMenu insertMenu = insertMenuFactory();
        insertMenu.setIcon(null);
        menuBar.add(insertMenu);

        //help menu
        JMenu helpMenu = new JMenu("Help");
        makeCommonHelpMenu(helpMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        //default popup
        defaultPopup = new JPopupMenu();
        defaultPopup.add(insertMenuFactory());

        //toolbar
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
        getContentPane().add(BorderLayout.NORTH, toolBar);

        getContentPane().add(BorderLayout.WEST, new WestTools(this));

        //SOUTH
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
        if(book==null) {
            showErrorMessage("You must begin with a new document. Click on File | New menu");
        }
        return book==null;
    }

    public static void main(String[] args) {
        showSplash("sbsplash.png");
        PropertyConfigurator.configure("conf/logger.properties");
        logger.info("Song Book started at "+new Date());
        new Publisher();
        hideSplash();
    }

    public MusicSheet openMusicSheet(File openFile) {
        musicSheet = new MusicSheet(this);
        super.openMusicSheet(openFile, false);
        return musicSheet;
    }

    public void openBook(File openFile) {
        try {
            BookIO.DocumentReader dr = new BookIO.DocumentReader(this, openFile);
            saxParser.parse(openFile, dr);
            setBook(dr.getBook());
            setSaveFile(openFile);
        } catch (SAXException e1) {
            showErrorMessage("Could not open the file "+openFile.getName()+", because it is damaged.");
            logger.error("SaxParser parse", e1);
        } catch (IOException e1) {
            showErrorMessage("Could not open the file "+openFile.getName()+". Check if you have the permission to open it.");
            logger.error("Song open", e1);
        }
    }

    public void setBook(Book book) {
        if(this.book!=null)getContentPane().remove(this.book.getScrolledBook());
        this.book = book;
        getContentPane().add(book.getScrolledBook());
        zoomCombo.setSelectedItem("100%");
        book.repaintWhole();
        validate();
    }

    public Book getBook() {
        return book;
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

    public static FontMetrics getStaticFontMetrics(Font font){
        if(graphics==null)return null;
        return graphics.getFontMetrics(font);
    }

    public DialogOpenAction getPaperSizeDialogOpenAction() {
        return paperSizeDialogOpenAction;
    }

    public void handleOpenFile(File file) {
        if(!showSaveDialog())return;
        openBook(file);
        unmodifiedDocument();
    }
}
