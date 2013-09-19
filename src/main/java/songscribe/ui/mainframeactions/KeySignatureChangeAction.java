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

Created on Jan 3, 2007
*/
package songscribe.ui.mainframeactions;

import songscribe.ui.KeySignatureChangeDialog;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class KeySignatureChangeAction extends AbstractAction{
    private KeySignatureChangeDialog kscd;
    private MainFrame mainFrame;

    public KeySignatureChangeAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Key Signature Change...");
        putValue(Action.SMALL_ICON, mainFrame.blankIcon);
    }

    public void actionPerformed(ActionEvent e) {
        int l = mainFrame.getMusicSheet().getSelectedLine();
        if(l==-1){
            JOptionPane.showMessageDialog(mainFrame, "You must select a line first.",
                        mainFrame.PROGNAME, JOptionPane.INFORMATION_MESSAGE);
        }else{
            if(kscd==null)kscd = new KeySignatureChangeDialog(mainFrame);
            kscd.setVisible(true);
        }
    }
}
