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
*/
package songscribe.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public abstract class AbstractTextFocusRejectingAction extends AbstractAction {
    private final IMainFrame mainFrame;

    protected AbstractTextFocusRejectingAction(IMainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public final void actionPerformed(ActionEvent e) {
        Component focusOwner = mainFrame.getFocusOwner();

        if (!(focusOwner instanceof JTextComponent)) {
            // we don't want to receive actions when the focus is on text panels
            doActionPerformed(e);
        }
    }

    public abstract void doActionPerformed(ActionEvent e);
}
