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

Created on Jul 21, 2007
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.Song;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class SongEditAction extends AbstractAction {
    private Publisher publisher;
    private HideMainFrame editFrame;
    private Song song;

    public SongEditAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(Action.NAME, "Edit");
        putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("edit.png")));
    }

    public void actionPerformed(ActionEvent e) {
        if(editFrame==null){
            editFrame = new HideMainFrame();
            editFrame.initFrame();
            editFrame.getJMenuBar().remove(0);
            for(Component c:editFrame.getContentPane().getComponents()){
                if(c instanceof JToolBar){
                    JToolBar toolBar = (JToolBar)c;
                    int separatorCount=0;
                    while(separatorCount<2){
                        separatorCount = toolBar.getComponentAtIndex(0) instanceof JToolBar.Separator ? separatorCount+1 : 0;
                        toolBar.remove(0);
                    }
                    toolBar.add(new JButton(editFrame.saveAndDoneAction), 0);
                    toolBar.add(new JToolBar.Separator(new Dimension(10, 0)), 1);
                    break;
                }
            }
        }
        editFrame.setVisible(true);
        song = (Song) publisher.getBook().getSelectedComponent();
        editFrame.openMusicSheet(song.getSongFile(), true);
        editFrame.setSelectedTool(editFrame.getSelectSelectionPanel());
        editFrame.getSelectSelectionPanel().setActive();
    }

    private class HideMainFrame extends MainFrame{
        SaveAndDoneAction saveAndDoneAction = new SaveAndDoneAction();

        public HideMainFrame() {
            exitAction = new HideAction();
        }

        protected class HideAction extends ExitAction{
            public void actionPerformed(ActionEvent e) {
                if(!showSaveDialog())return;
                setVisible(false);
                song.reload(publisher);
                publisher.getBook().repaintWhole();
            }
        }

        public class SaveAndDoneAction extends AbstractAction {
            public SaveAndDoneAction() {
                putValue(NAME, "Save and Return");
                putValue(SMALL_ICON, new ImageIcon(getImage("filesave.png")));
                putValue(SHORT_DESCRIPTION, "Saves the changes and returns to Song Book");
            }

            public void actionPerformed(ActionEvent e) {
                saveAction.actionPerformed(null);
                exitAction.actionPerformed(null);
            }
        }
    }
}
