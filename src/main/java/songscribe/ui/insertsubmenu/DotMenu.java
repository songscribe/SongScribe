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

Created on May 27, 2006
*/
package songscribe.ui.insertsubmenu;

import songscribe.music.Note;
import songscribe.ui.MainFrame;

import javax.swing.*;

/**
 * @author Csaba Kávai
 */
public class DotMenu extends InsertSubMenu{
    private JMenuItem dotButton, doubleDotButton;

    public DotMenu(MainFrame mainFrame) {
        super(mainFrame, "Augmentation Dot", "dot22.gif", null);
        dotButton = createCheckBoxMenuItem("Dot", "DOT", "dot16.gif", KeyStroke.getKeyStroke('.'));
        doubleDotButton = createCheckBoxMenuItem("Double dot", "DOUBLEDOT", "doubledot16.gif", null);
    }

    protected void realActionPerformed(String actionCommand) {
        resetButtons();
        Note activeNote = mainFrame.getMusicSheet().getActiveNote();
        if(activeNote==null)return;
        if(actionCommand.equals(dotButton.getActionCommand())){
            activeNote.setDotted(activeNote.getDotted()==1 ? 0 : 1);
        }else if(actionCommand.equals(doubleDotButton.getActionCommand())){
            activeNote.setDotted(activeNote.getDotted()==2 ? 0 : 2);
        }
        updateState(activeNote);
        mainFrame.getMusicSheet().repaint();
    }

    public void updateState(Note note) {
        if(note.getDotted()==1){
            dotButton.setSelected(true);
        }else if(note.getDotted()==2){
            doubleDotButton.setSelected(true);
        }
        mainFrame.getNoteSelectionPanel().setDotSelected(dotButton.isSelected());
    }

    public void dotPrefixNote(Note note) {
        if(dotButton.isSelected()){
            note.setDotted(1);
        }else if(doubleDotButton.isSelected()){
            note.setDotted(2);
        }
    }
}
