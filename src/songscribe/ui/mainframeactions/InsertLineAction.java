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

Created on Aug 6, 2006
*/
package songscribe.ui.mainframeactions;

import songscribe.music.Line;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class InsertLineAction extends AbstractAction {
    private MainFrame mainFrame;
    private int shift;

    public InsertLineAction(MainFrame mainFrame, int shift, String name) {
        this.mainFrame = mainFrame;
        this.shift = shift;
        putValue(Action.NAME, name);
        putValue(Action.SMALL_ICON, mainFrame.blankIcon);
    }

    public void actionPerformed(ActionEvent e) {
        if(mainFrame.getMusicSheet().getSelectedLine()!=-1){
            mainFrame.getMusicSheet().getComposition().addLine(mainFrame.getMusicSheet().getSelectedLine()+shift, new Line());
            mainFrame.getMusicSheet().unSetNoteSelections();
            mainFrame.modifiedDocument();
            mainFrame.getMusicSheet().setRepaintImage(true);
            mainFrame.getMusicSheet().repaint();
        }else{
            mainFrame.showErrorMessage("Please select a page first.");
        }
    }
}
