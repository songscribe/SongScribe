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

Created on Mar 31, 2006
*/
package songscribe.ui;

import songscribe.ui.insertsubmenu.*;
import songscribe.music.Note;

import javax.swing.*;

/**
 * @author Csaba Kávai
 */
public class InsertMenu extends JMenu{
    private InsertSubMenu[] subMenus;
    private MainFrame mainFrame;

    public InsertMenu(MainFrame mainFrame) {
        super("Insert");
        this.mainFrame = mainFrame;
        subMenus = new InsertSubMenu[]{new NoteMenu(mainFrame), new RestMenu(mainFrame), new OtherMenu(mainFrame), new DotMenu(mainFrame), new AccidentalMenu(mainFrame), new ArticulationMenu(mainFrame)};
        for(InsertSubMenu im:subMenus){
            if(im instanceof DotMenu){
                addSeparator();
            }
            add(im);
        }
    }

    public void updateState(){
        for(InsertSubMenu im:subMenus){
            im.resetButtons();
        }
        Note activeNote = mainFrame.getMusicSheet().getActiveNote();
        if(activeNote!=null){
            for(InsertSubMenu im:subMenus){
                im.updateState(activeNote);
            }
        }
    }

    public void doClickNote(String action){
        for(InsertSubMenu im:subMenus){
            im.doClickNote(action);
        }
    }


    /**
     * Called when a note wants to get its dots, prefices and accidentals according to the
     * present state of insert menu.
     * @param note Generally this is will be in the following moment the musicsheet's active note. It has no dots, prefices and accidentals.
     */
    public void dotPrefixNote(Note note){
        for(InsertSubMenu im:subMenus){
            im.dotPrefixNote(note);
        }
    }
}
