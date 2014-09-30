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

    Created on Jul 21, 2007
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Book;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Csaba Kávai
 */
public class KeyAction extends AbstractAction {
    private static final int GROWTH = 2;
    private static final int[][] MATRIX = {
            { KeyEvent.VK_UP, 0, -GROWTH },
            { KeyEvent.VK_DOWN, 0, GROWTH },
            { KeyEvent.VK_LEFT, -GROWTH, 0 },
            { KeyEvent.VK_RIGHT, GROWTH, 0 }
    };
    private int keyCode;
    private Book book;

    public KeyAction(Book book, int keyCode) {
        this.keyCode = keyCode;
        this.book = book;
    }

    public void actionPerformed(ActionEvent e) {
        if (book.getSelectedComponent() != null) {
            for (int[] m : MATRIX) {
                if (keyCode == m[0]) {
                    book.moveSelectedComponent(m[1], m[2]);
                    break;
                }
            }
        }
    }
}
