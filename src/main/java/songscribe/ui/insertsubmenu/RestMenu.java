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
import songscribe.music.NoteType;
import songscribe.ui.MainFrame;

/**
 * @author Csaba Kávai
 */
public class RestMenu extends InsertSubMenu{
    public RestMenu(MainFrame mainFrame) {
        super(mainFrame, "Rest", "crotchetrest22.gif", mainFrame.getRestSelectionPanel());
        for(NoteType nt:NoteType.values()){
            if(nt.isRest()){
                createCheckBoxMenuItem(nt.getName(), nt.name(), nt.name().toLowerCase()+"16.gif", nt.getAcceleratorKey());
            }
        }
    }

    public void dotPrefixNote(Note note) {
    }

    public void updateState(Note note) {
        selectItem(note.getNoteType().name());
    }
}
