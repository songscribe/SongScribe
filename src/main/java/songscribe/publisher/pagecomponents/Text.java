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

    Created on Oct 3, 2006
*/
package songscribe.publisher.pagecomponents;


import songscribe.publisher.Publisher;
import songscribe.publisher.TextDialog;
import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class Text extends PageComponent {
    private static TextDialog textDialog;
    private static JPopupMenu popupMenu;
    private String text;
    private Font font;
    private Alignment alignment;

    // acceleration
    private Line[] lines;
    private int lineHeight, firstLineHeight;

    public Text(String text, Font font, Alignment alignment, int xPos, int yPos) {
        super(new Rectangle(xPos, yPos, 0, 0), 1.0);
        setAlignment(alignment);
        setTextAndFont(text, font);
    }

    public void paintComponent(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        int yPos = pos.y + firstLineHeight;

        for (Line line : lines) {
            int xPos = pos.x;

            switch (alignment) {
                case CENTER:
                    xPos += (pos.width - line.width) / 2;
                    break;

                case RIGHT:
                    xPos += pos.width - line.width;
                    break;
            }

            g2.drawString(line.text, xPos, yPos);
            yPos += lineHeight;
        }
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public Font getFont() {
        return font;
    }

    public String getText() {
        return text;
    }

    public void setTextAndFont(String text, Font font) {
        this.font = font;
        this.text = text;
        FontMetrics metrics = Publisher.getStaticFontMetrics(font);
        lineHeight = Math.round(metrics.getHeight());
        firstLineHeight = Math.round(metrics.getAscent());
        String[] strs = text.split("\n");
        lines = new Line[strs.length];
        pos.width = Integer.MIN_VALUE;

        for (int i = 0; i < lines.length; i++) {
            lines[i] = new Line();
            lines[i].text = strs[i];
            lines[i].width = metrics.stringWidth(lines[i].text);
            pos.width = Math.max(pos.width, lines[i].width);
        }

        pos.height = Math.round(lines.length * lineHeight);
    }

    public MyDialog getPropertiesDialog(Publisher publisher) {
        if (textDialog == null) {
            textDialog = new TextDialog(publisher, false);
        }

        return textDialog;
    }

    public JPopupMenu getPopupMenu(Publisher publisher) {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu("Text");
            addCommonPopups(publisher, popupMenu);
        }

        return popupMenu;
    }

    public static enum Alignment { LEFT, CENTER, RIGHT }

    private class Line {
        String text;
        int width;
    }
}
