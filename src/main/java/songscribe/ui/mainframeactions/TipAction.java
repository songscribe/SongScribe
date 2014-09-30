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

    Created on Aug 6, 2006
*/
package songscribe.ui.mainframeactions;

import songscribe.ui.MainFrame;
import songscribe.ui.TipFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class TipAction extends AbstractAction {
    private MainFrame mainFrame;

    public TipAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Tip of the Day");
        putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage("idea22.png")));
    }

    public void actionPerformed(ActionEvent e) {
        try {
            new TipFrame(mainFrame);
        }
        catch (IOException e1) {
            mainFrame.showErrorMessage("Cannot read the tip file.");
        }
    }
}
