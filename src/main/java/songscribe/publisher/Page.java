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

import songscribe.publisher.pagecomponents.PageComponent;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class Page {
    private ArrayList<PageComponent> pageComponents = new ArrayList<PageComponent>();

    private Book book;

    public Page(Book book) {
        this.book = book;
    }

    public void paint(Graphics2D g2, int pageNumber, boolean drawDecorations, int startY, int endY) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle pageSize = book.getPageSize();
        Rectangle margin = book.getMargin(pageNumber);
        Shape clip = g2.getClip();
        g2.setClip(0, startY, pageSize.width + 4, endY - startY);

        if (drawDecorations) {
            float scale = (float) book.getScale();
            // repaint
            g2.setPaint(Color.white);
            g2.fill(pageSize);
            // draw the border
            g2.setPaint(Color.black);
            g2.setStroke(new BasicStroke(1f / scale));
            g2.draw(pageSize);
            // draw the margin
            g2.setPaint(Color.lightGray);
            g2.draw(margin);
            // draw the shadow
            g2.setPaint(Color.black);
            g2.setStroke(new BasicStroke(3f / scale));
            g2.draw(new Line2D.Float(
                    pageSize.width + 1f / scale, 4f / scale, pageSize.width + 1 / scale, pageSize.height + 1f / scale));
            g2.draw(new Line2D.Float(
                    4f / scale,
                    pageSize.height + 1f / scale, pageSize.width + 1 / scale, pageSize.height + 1f / scale));
        }
        //g2.drawRect(0, startY, pageSize.width, (endY-startY));

        g2.translate(margin.x, margin.y);

        // draw the page numbers
        PageNumber pn = book.getPageNumber();

        if (pn != null && pn.getFromPage() <= pageNumber + 1) {
            g2.setFont(pn.getFont());
            int width = g2.getFontMetrics().stringWidth(Integer.toString(pageNumber + 1));
            int pnX = 0;

            if (pn.getAlignment() == PageNumber.Alignment.CENTER) {
                pnX = (margin.width - width) / 2;
            }
            else if (pn.getAlignment() == PageNumber.Alignment.RIGHT ||
                     pn.getAlignment() == PageNumber.Alignment.BOOK_INNER && pageNumber % 2 == 1 ||
                     pn.getAlignment() == PageNumber.Alignment.BOOK_OUTER && pageNumber % 2 == 0) {
                pnX = margin.width - width;
            }

            int pnY = pn.getPlacement() == PageNumber.Placement.BOTTOM ?
                    margin.height + pn.getSpaceFromMargin() + pn.getFont().getSize() : -pn.getSpaceFromMargin();
            g2.drawString(Integer.toString(pageNumber + 1), pnX, pnY);
        }

        // draw the songs
        startY -= margin.y;
        endY -= margin.y;
        paintPageComponents(g2, startY, endY);
        g2.translate(-margin.x, -margin.y);
        g2.setClip(clip);
    }

    private void paintPageComponents(Graphics2D g2, int startY, int endY) {
        for (PageComponent pageComponent : pageComponents) {
            Rectangle pos = pageComponent.getPos();

            if (endY >= pos.y && pos.y + pos.height >= startY) {
                pageComponent.paintComponent(g2);
            }
        }
    }

    public void addPageComponent(PageComponent pageComponent) {
        pageComponents.add(pageComponent);
    }

    public void removePageComponent(PageComponent pageComponent) {
        pageComponents.remove(pageComponent);
    }

    public ListIterator<PageComponent> getPageComponentIterator() {
        return pageComponents.listIterator();
    }

    public PageComponent findComponent(Point p) {
        for (int i = pageComponents.size() - 1; i >= 0; i--) {
            if (pageComponents.get(i).getPos().contains(p)) {
                return pageComponents.get(i);
            }
        }

        return null;
    }

    public void raiseComponent(PageComponent pc) {
        int index = pageComponents.indexOf(pc);

        if (index < pageComponents.size() - 1) {
            pageComponents.set(index, pageComponents.get(index + 1));
            pageComponents.set(index + 1, pc);
        }
    }

    public void lowerComponent(PageComponent pc) {
        int index = pageComponents.indexOf(pc);

        if (index > 0) {
            pageComponents.set(index, pageComponents.get(index - 1));
            pageComponents.set(index - 1, pc);
        }
    }

    public void toTopComponent(PageComponent pc) {
        for (int i = pageComponents.indexOf(pc) + 1; i < pageComponents.size(); i++) {
            pageComponents.set(i - 1, pageComponents.get(i));
        }

        pageComponents.set(pageComponents.size() - 1, pc);
    }

    public void toBottomComponent(PageComponent pc) {
        for (int i = pageComponents.indexOf(pc) - 1; i >= 0; i--) {
            pageComponents.set(i + 1, pageComponents.get(i));
        }

        pageComponents.set(0, pc);
    }
}
