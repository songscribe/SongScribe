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

Created on Jun 26, 2006
*/
package songscribe.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class DoNotShowMessage extends MyDialog{
    private JCheckBox dontShowCheck = new JCheckBox("Don't show this message again.");
    private String propName;

    public DoNotShowMessage(MainFrame mainFrame, String dialogTitle, String info, String propName) {
        super(mainFrame, dialogTitle);
        this.propName = propName;
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dialogPanel.add(BorderLayout.NORTH, new JLabel(info));
        dialogPanel.add(BorderLayout.CENTER, dontShowCheck);
        southPanel = new JPanel();
        southPanel.add(okButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    public void setVisible(boolean visible) {
        if(visible){
            if(!mainFrame.getProperties().getProperty(propName, Constants.FALSEVALUE).equals(Constants.TRUEVALUE)){
                super.setVisible(visible);
            }
        }else{
            super.setVisible(visible);
        }
    }

    protected void getData() {
    }

    protected void setData() {
        if(dontShowCheck.isSelected()){
            mainFrame.getProperties().setProperty(propName, Constants.TRUEVALUE);
        }
    }
}
