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

import songscribe.music.Note;
import songscribe.ui.TempoChangeDialog;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class TempoChangeAction extends AbstractAction {
    private TempoChangeDialog tempoChangeDialog;
    private MainFrame mainFrame;

    public TempoChangeAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Tempo Change...");
        putValue(Action.SMALL_ICON, mainFrame.blankIcon);
    }

    public void actionPerformed(ActionEvent e) {
        Note sel = mainFrame.getMusicSheet().getSingleSelectedNote();
        if(sel==null){
            JOptionPane.showMessageDialog(mainFrame, "You must select a note first from which the tempo change should be applied.",
                        mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
        }else if(mainFrame.getMusicSheet().getComposition().getLine(0).noteCount()>0 && mainFrame.getMusicSheet().getComposition().getLine(0).getNote(0)==sel){
            JOptionPane.showMessageDialog(mainFrame, "You cannot change the tempo on the first note.",
                        mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
        }else{
            if(tempoChangeDialog==null)tempoChangeDialog = new TempoChangeDialog(mainFrame);
            tempoChangeDialog.setVisible(true);
        }
    }
}
