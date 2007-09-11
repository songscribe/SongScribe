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

Created on Aug 4, 2006
*/
package songscribe.publisher.newsteps;

import songscribe.ui.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
class InfoStep extends Step{
    private static final String INFO = "<html><h2>Welcome to the New Document Wizard!</h2>You will be asked to select the songs you want to publish, set the layout etc. Naturally you can add songs and modify the paper size later, but you cannon change the layout strategy after the wizard creates the new document. Then you will be able move the songs manually on and across pages.</html>";
    private JCheckBox dontshowCheck;

    public InfoStep(Data data) {
        super(data);
        dontshowCheck = new JCheckBox("Do not show this info-step again");

        setLayout(new BorderLayout());
        add(dontshowCheck);
    }

    public String getInfo() {
        return INFO;
    }

    public void start() {
    }

    public void end() {
        data.publisher.getProperties().setProperty(Constants.SHOWPUBLISHERNEWINFO, dontshowCheck.isSelected()?Constants.FALSEVALUE:Constants.TRUEVALUE);
    }
}
