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

import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.publisheractions.KeyAction;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ListIterator;
import java.util.Vector;

/**
 * @author Csaba Kávai
 */
public class Book extends JComponent implements MouseListener, MouseMotionListener{
    private Vector<Page> pages = new Vector<Page>(10, 10);

    private static final int PAGESSPACE = 30;
    private static final BasicStroke lineStroke = new BasicStroke(1f);
    private static final BasicStroke pageSelectStroke = new BasicStroke(3f);

    private Rectangle pageSize;
    private Rectangle oddMargin, evenMargin;
    private boolean mirroredMargin;
    private Dimension size = new Dimension();
    private PageNumber pageNumber;

    private double scale = 1d;

    //selection
    public enum Selection{COMPONENTS, PAGES}
    private Selection selection = Selection.COMPONENTS;
    private PageComponent selectedComponent;
    private int selectedPage;
    private Rectangle[] selectionRects = new Rectangle[4];
    private enum Dragging{NONE, SELECTED, MOVING, STRECHING}
    private boolean insertMoving;
    private Dragging dragging = Dragging.NONE;
    private Rectangle selectionRect = new Rectangle();
    private Rectangle repaintRect = new Rectangle();
    private Point movingRelPoint = new Point();

    private Publisher publisher;

    //accelerator
    private Rectangle currentAccRect = new Rectangle();
    private static Rectangle accShape;
    private static BufferedImage accImage;
    static{
        Rectangle maxWindow = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        accShape = new Rectangle(0, 0, maxWindow.width*2, maxWindow.height*4);
        while(true){
            try {
                accImage = new BufferedImage(accShape.width, accShape.height, BufferedImage.TYPE_INT_RGB);
                break;
            } catch (OutOfMemoryError e) {
                accShape.width = Math.round(0.86f*accShape.width);
                accShape.height = Math.round(0.86f*accShape.height);
            }
        }
    }

    //scrolling
    private JScrollPane scroll;
    private ChangeListener currentPageWriter;

    public Book(Publisher publisher, int pageWidth, int pageHeight, int leftInnerMargin, int rightOuterMargin, int topMargin, int bottomMargin, boolean mirroredMargin) {
        this.publisher = publisher;
        setPageSize(pageWidth, pageHeight, leftInnerMargin, rightOuterMargin, topMargin, bottomMargin, mirroredMargin);
        for(int i=0;i<selectionRects.length;i++){
            selectionRects[i] = new Rectangle(0, 0, 10, 10);
        }
        addMouseListener(this);
        addMouseMotionListener(this);
        JPanel borderPanel = new BorderPanel();
        borderPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        borderPanel.add(this);
        borderPanel.setAlignmentX(0.5f);
        scroll = new JScrollPane(borderPanel);        
        int[] keyCodes = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT};
        for(int keyCode: keyCodes){
            Object o = new Object();
            getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), o);
            getActionMap().put(o, new KeyAction(this, keyCode));
        }
        currentPageWriter = new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                Book.this.publisher.getPStatusBar().setCurrentPage(getPage(scroll.getViewport().getViewPosition().y+scroll.getViewport().getExtentSize().height/2)+1);
            }
        };
        scroll.getViewport().addChangeListener(currentPageWriter);
    }

    public void setPageSize(int pageWidth, int pageHeight, int leftInnerMargin, int rightOuterMargin, int topMargin, int bottomMargin, boolean mirroredMargin){
        this.mirroredMargin = mirroredMargin;
        pageSize = new Rectangle(0, 0, pageWidth, pageHeight);
        oddMargin = new Rectangle(leftInnerMargin, topMargin, pageWidth-leftInnerMargin-rightOuterMargin, pageHeight-topMargin-bottomMargin);
        evenMargin = mirroredMargin ? new Rectangle(rightOuterMargin, topMargin, pageWidth-leftInnerMargin-rightOuterMargin, pageHeight-topMargin-bottomMargin) : oddMargin;
    }

    public JScrollPane getScrolledBook() {
        return scroll;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        if(this.scale==scale)return;
        Point p = scroll.getViewport().getViewPosition();
        p.x=Math.max(0, ((int)(pageSize.width*scale)-scroll.getViewport().getExtentSize().width)/2);
        int page = getPage(p.y);
        int start = (int)(getPageY(page, p.y)*scale/this.scale);
        this.scale = scale;
        p.y = getScreenY(page, start);
        sizeChanged(p);
    }

    private int getPage(int y){
        return Math.min(pages.size()-1, Math.max(0, y/(int)(pageSize.height*scale+PAGESSPACE)));
    }

    private int getPageY(int page, int y){
        return y-page*(int)(pageSize.height*scale+PAGESSPACE);
    }

    private int getScreenY(int page, int y){
        return y+page*(int)(pageSize.height*scale+PAGESSPACE);
    }

    public Rectangle getPageSize() {
        return pageSize;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Rectangle getMargin(int pageNumber){
        return pageNumber%2==0 ? oddMargin : evenMargin;
    }

    public Rectangle getMargin(Page page){
        return getMargin(pages.indexOf(page));
    }

    public int getTopMargin(){
        return oddMargin.y;
    }

    public int getLeftInnerMargin(){
        return oddMargin.x;
    }

    public int getRightOuterMargin(){
        return pageSize.width-oddMargin.x-oddMargin.width;
    }

    public int getBottomMargin(){
        return pageSize.height-oddMargin.y-oddMargin.height;
    }

    public boolean isMirroredMargin() {
        return mirroredMargin;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
        removeSelection();
    }

    public void removeSelection(){
        selectedPage = -1;
        selectedComponent = null;
    }

    public int getSelectedPage(){
        return selection==Selection.PAGES ? selectedPage : -1;
    }

    public void setSelectedPage(int selectedPage) {
        if(selection==Selection.PAGES) this.selectedPage = selectedPage;
    }

    public int getPresentPage(){
        return selectedPage;
    }

    public PageComponent getSelectedComponent() {
        return selection==Selection.COMPONENTS ? selectedComponent : null;
    }

    public Page getSelectedComponentsPage() {
        return selection==Selection.COMPONENTS && selectedComponent!=null ? pages.get(selectedPage) : null;
    }

    public double getNotGreaterResolution(double width, double height){
        //we suppose oddMargin.width=evebMargin.width
        return Math.min((double)oddMargin.width/width, Math.min((double)oddMargin.height/ height, 1.0));
    }

    public void repaintWhole(){
        currentAccRect.setBounds(0, 0, 0, 0);
        repaint();
    }

    public void goToPage(int page){
        page = Math.min(pages.size(), Math.max(1, page));
        JViewport viewport = scroll.getViewport();
        scroll.getViewport().removeChangeListener(currentPageWriter);
        viewport.setViewPosition(new Point(viewport.getViewPosition().x, Math.max(0, Math.min(getHeight()-viewport.getExtentSize().height, getScreenY(page-1, 0)))));
        scroll.getViewport().addChangeListener(currentPageWriter);
        publisher.getPStatusBar().setCurrentPage(page);
    }

     public void repaintSelectedComponent() {
        setSelectionRects();
        repaintRect.add(selectionRect);
        intersection(repaintRect, currentAccRect);
        Graphics2D g2 = accImage.createGraphics();
        g2.translate(repaintRect.x-currentAccRect.x, repaintRect.y-currentAccRect.y);
        paintStuff(g2, repaintRect);
        g2.dispose();
        repaint();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if(!currentAccRect.contains(scroll.getViewport().getViewRect().intersection(new Rectangle(size)))){
            paintAcceleration();
        }
        g2.drawImage(accImage, currentAccRect.x, currentAccRect.y, null);

        //drawing the selection
        if(selectedComponent!=null){
            g2.setPaint(Color.black);
            for(Rectangle2D sels:selectionRects){
                g2.fill(sels);
            }
        }else if(selection==Selection.PAGES && selectedPage!=-1){
            g2.setPaint(Color.blue);
            g2.setStroke(pageSelectStroke);
            g2.drawRect(0, getScreenY(selectedPage, 0), (int)(pageSize.width*scale), (int)(pageSize.height*scale));
        }
        if(dragging==Dragging.MOVING || dragging==Dragging.STRECHING){
            g2.setPaint(Color.black);
            g2.setStroke(lineStroke);
            g2.draw(selectionRect);
        }
    }

    private void paintAcceleration(){
        Graphics2D g2 = accImage.createGraphics();
        g2.setPaint(getBackground());
        g2.fill(accShape);
        //calculating currentAccRect
        Rectangle viewRect = scroll.getViewport().getViewRect();
        currentAccRect.x = viewRect.x-(accShape.width-viewRect.width)/2;
        currentAccRect.y = viewRect.y-(accShape.height-viewRect.height)/2;
        currentAccRect.width = accShape.width;
        currentAccRect.height = accShape.height;
        currentAccRect = currentAccRect.intersection(new Rectangle(size));
        paintStuff(g2, currentAccRect);
        g2.dispose();
    }

    private void paintStuff(Graphics2D g2, Rectangle view){
        g2.translate(-view.x, -view.y);
        g2.scale(scale, scale);
        //calculating the view
        int firstPage = getPage(view.y);
        int lastPage = getPage(view.y+view.height);
        AffineTransform at = g2.getTransform();
        for(int i=firstPage;i<=lastPage;i++){
            int startY=0, endY=(int)(pageSize.height*scale)+4;
            if(i==firstPage) startY = getPageY(firstPage, view.y);
            if(i==lastPage) endY = getPageY(firstPage, view.y+view.height);
            g2.translate(0, getScreenY(i, 0)/scale);
            pages.get(i).paint(g2, i, true, (int)(startY/scale), (int)(endY/scale));
            g2.setTransform(at);
        }
    }

    private void sizeChanged(Point newViewPort){
        size.width = (int)(pageSize.width*scale)+4;
        size.height = pages.size()*(int)(pageSize.height*scale)+(pages.size()-1)*PAGESSPACE+10;
        setPreferredSize(size);
        revalidate();
        if(newViewPort!=null)scroll.getViewport().setViewPosition(newViewPort);
        paintAcceleration();
        repaint();
    }

    public Page addPage(){
        return addPage(pages.size());
    }

    public Page addPage(int index){
        Page page = new Page(this);
        pages.add(index, page);
        sizeChanged(null);
        publisher.getPStatusBar().setTotalPage(pages.size());
        return page;
    }

    public ListIterator<Page> pageIterator(){
        return pages.listIterator();
    }

    public int getTotalPage(){
        return pages.size();
    }
    
    private class BorderPanel extends JPanel implements Scrollable{
        public Dimension getPreferredScrollableViewportSize() {
            return size;
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

    private void setSelectionRects(){
        int x1 = getRelativeX(selectedComponent.getPos().x, selectedPage);
        int y1 = getRelativeY(selectedComponent.getPos().y, selectedPage);
        int x2 = getRelativeX(selectedComponent.getPos().x+selectedComponent.getPos().width, selectedPage);
        int y2 = getRelativeY(selectedComponent.getPos().y+selectedComponent.getPos().height, selectedPage);
        selectionRect.x = x1;
        selectionRect.y = y1;
        selectionRect.width = x2-x1;
        selectionRect.height = y2-y1;
        selectionRects[0].x = x1-selectionRects[0].width;
        selectionRects[0].y = y1-selectionRects[0].height;
        selectionRects[1].x = x2;
        selectionRects[1].y = y1-selectionRects[1].height;
        selectionRects[2].x = x1-selectionRects[2].width;
        selectionRects[2].y = y2;
        selectionRects[3].x = x2;
        selectionRects[3].y = y2;
    }

    private Point getAbsoluteCoordinates(Point p, int page){
        return new Point(
            (int)(p.x/scale-getMargin(page).x),
            (int)((p.y-getScreenY(page, 0)-getMargin(page).y*scale)/scale));
    }

    private int getRelativeX(int x, int page){
        return (int)((x+getMargin(page).x)*scale);
    }

    private int getRelativeY(int y, int page){
        return getScreenY(page, (int)((y+getMargin(page).y)*scale));
    }

    public void intersection(Rectangle t, Rectangle r) {
        int tx1 = Math.max(t.x, r.x);
        int ty1 = Math.max(t.y, r.y);
        int tx2 = Math.min(t.x+t.width, r.x+r.width);
        int ty2 = Math.min(t.y+t.height, r.y+r.height);
        t.setBounds(tx1, ty1, tx2-tx1, ty2-ty1);
    }

    public void insertPageComponent(PageComponent pc){
        selectedPage = -1;
        selectedComponent = pc;
        selectionRect.width = (int)(pc.getPos().width*scale);
        selectionRect.height = (int)(pc.getPos().height*scale);
        repaintRect.setBounds(selectionRect);
        movingRelPoint.x = (int)(pc.getPos().width*scale/2);
        movingRelPoint.y = (int)(pc.getPos().height*scale/2);
        dragging = Dragging.MOVING;
        insertMoving = true;
        publisher.modifiedDocument();
    }

    public void removeSelected(){
        if(selectedComponent!=null){
            if(selectedPage!=-1){
                pages.get(selectedPage).removePageComponent(selectedComponent);
                repaintSelectedComponent();
                selectedComponent = null;
            }
            publisher.modifiedDocument();
        }else if(selection==Selection.PAGES && selectedPage!=-1){
            pages.remove(selectedPage);
            selectedPage = -1;
            sizeChanged(null);
            publisher.getPStatusBar().setTotalPage(pages.size());
            publisher.modifiedDocument();
        }
    }

    public PageNumber getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(PageNumber pageNumber) {
        this.pageNumber = pageNumber;
    }

    //------------------mouselistener interface-------------------------
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount()==2 && selectedComponent!=null){
            publisher.getPropertiesAction().actionPerformed(null);
        }
    }

    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        if(insertMoving){
            mouseReleased(e);
            insertMoving = false;
            return;
        }
        selectedPage = getPage(e.getY());
        selectedComponent = selection==Selection.COMPONENTS ? pages.get(selectedPage).findComponent(getAbsoluteCoordinates(e.getPoint(), selectedPage)) : null;
        if(selectedComponent!=null){
            setSelectionRects();
            repaintRect.setBounds(selectionRect);
            movingRelPoint.x = e.getX()-selectionRect.x;
            movingRelPoint.y = e.getY()-selectionRect.y;
            dragging = Dragging.SELECTED;
        }
        paintImmediately(0, 0, getWidth(), getHeight());
        showPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        if(dragging==Dragging.MOVING){
            moveSelectedComponent(0, 0);
        }
        dragging = Dragging.NONE;
        showPopup(e);
    }

    private void showPopup(MouseEvent e) {
        if(e.isPopupTrigger()){
            if(selectedComponent!=null){
                selectedComponent.getPopupMenu(publisher).show(this, e.getX(), e.getY());
            }else{
                publisher.getDefaultPopup().show(this, e.getX(), e.getY());
            }
        }
    }

    public void moveSelectedComponent(int relX, int relY) {
        selectionRect.translate(relX, relY);
        int page = getPage(selectionRect.y);
        if(page!=selectedPage){
                if(selectedPage!=-1) pages.get(selectedPage).removePageComponent(selectedComponent);
            pages.get(page).addPageComponent(selectedComponent);
            selectedPage = page;
        }

        Rectangle margin = getMargin(page);
        Point adjusted = new Point(
            Math.min(getRelativeX(margin.width, page)-selectionRect.width, Math.max(getRelativeX(0, page), selectionRect.x)),
            Math.min(getRelativeY(margin.height, page)-selectionRect.height, Math.max(getRelativeY(0, page), selectionRect.y)));
        selectedComponent.setPosition(getAbsoluteCoordinates(adjusted, page));
        repaintSelectedComponent();
        publisher.modifiedDocument();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    //------------------mousemotionlistener interface-------------------------
    public void mouseDragged(MouseEvent e) {
        if(dragging==Dragging.SELECTED) dragging = Dragging.MOVING;
        if(dragging==Dragging.MOVING){
            selectionRect.x = e.getX()-movingRelPoint.x;
            selectionRect.y = e.getY()-movingRelPoint.y;
            //adjusting
            int page = getPage(selectionRect.y);
            Rectangle margin = getMargin(getPage(selectionRect.y));
            selectionRect.x = Math.min(getRelativeX(margin.width, page)-selectionRect.width, Math.max(getRelativeX(0, page), selectionRect.x));
            repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
        if(insertMoving) mouseDragged(e);
    }

}
