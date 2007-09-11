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

Created on 2005.01.25., 9:25:19
*/

package songscribe.ui;

import songscribe.music.Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 *
 */
public abstract class SelectionPanel extends JToolBar implements ActionListener{
    protected ButtonGroup selectionGroup = new ButtonGroup();
    protected Note selectedNote;
    protected MainFrame mainFrame;

    protected static final Dimension SELECTIONIMAGEDIM = new Dimension(32, 36);

    public SelectionPanel(MainFrame mainFrame) {
        super(JToolBar.VERTICAL);
        this.mainFrame = mainFrame;
        setFloatable(false);
    }

    protected void addSelectionComponent(JComponent component){
        add(component);
    }

    public void setActive() {
        for(int i=0;i<getComponentCount();i++){
            if(getComponent(i) instanceof AbstractButton){
                AbstractButton ab = (AbstractButton) getComponent(i);
                if(ab.isSelected() && mainFrame.getInsertMenu()!=null){
                    mainFrame.getInsertMenu().doClickNote(ab.getActionCommand());
                }
            }
        }
        mainFrame.getMusicSheet().setSelectionType(null);
        mainFrame.getMusicSheet().repaint();
    }

    public void setSelected(String actionCommand){
        for(int i=0;i<getComponentCount();i++){
            if(getComponent(i) instanceof AbstractButton){
                AbstractButton ab = (AbstractButton)getComponent(i);
                if(actionCommand.equals(ab.getActionCommand())){
                    selectionGroup.setSelected(ab.getModel(), true);
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        mainFrame.getInsertMenu().doClickNote(e.getActionCommand());
    }
}
