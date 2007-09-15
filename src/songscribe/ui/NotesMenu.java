/*  
Music of The Supreme song notation program
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

Created on Sep 15, 2007
*/
package songscribe.ui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class NotesMenu extends JMenu implements ActionListener {
    private MainFrame mainFrame;
    private JMenuItem[] buttons;

    public NotesMenu(MainFrame mainFrame) {
        super("Notes");
        this.mainFrame = mainFrame;
        String[] images = {"beam_menu.gif", "unbeam_menu.gif", "triplet_menu.gif", "untriplet_menu.gif", "tie_menu.gif", "untie_menu.gif", "fsending_menu.gif", "unfsending_menu.gif"};
        String[] names = {"Beam", "Unbeam", "Triplet", "Untriplet", "Tie", "Untie", "First-second ending", "Remove first-second ending"};
        buttons = new JMenuItem[names.length];
        for(int i=0;i<names.length;i++){
            buttons[i] = new JMenuItem(names[i], new ImageIcon(MainFrame.getImage(images[i])));
            buttons[i].addActionListener(this);
            add(buttons[i]);
            if(i%2==1 && i<names.length-1)addSeparator();
        }
    }

    public void actionPerformed(ActionEvent e) {
        MusicSheet musicSheet = mainFrame.getMusicSheet();
        if(e.getSource()==buttons[0]){
            musicSheet.beamSelectedNotes(true);
        }else if(e.getSource()==buttons[1]){
            musicSheet.beamSelectedNotes(false);
        }else if(e.getSource()==buttons[2]){
            musicSheet.tripletSelectedNotes(true);
        }else if(e.getSource()==buttons[3]){
            musicSheet.tripletSelectedNotes(false);
        }else if(e.getSource()==buttons[4]){
            musicSheet.tieSelectedNotes(true);
        }else if(e.getSource()==buttons[5]){
            musicSheet.tieSelectedNotes(false);
        }else if(e.getSource()==buttons[6]){
            musicSheet.makeFsEndingOnSelectedNotes(true);
        }else if(e.getSource()==buttons[7]){
            musicSheet.makeFsEndingOnSelectedNotes(false);
        }
    }
}
