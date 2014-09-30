/*
    SongScribe song notation program
    Copyright (C) 2006 Csaba Kavai

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
*/
package songscribe.uiconverter;

import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ChooseDirectoryAction extends AbstractAction {
    private PlatformFileDialog pfd;

    public ChooseDirectoryAction(UIConverter uiConverter) {
        putValue(NAME, "Choose");
        putValue(Action.SMALL_ICON, new ImageIcon(UIConverter.getImage("fileopen.png")));
        pfd = new PlatformFileDialog(uiConverter, "Open folder", true, new MyAcceptFilter("Folders"), true);
    }

    public void actionPerformed(ActionEvent e) {
        if (pfd.showDialog()) {
            firePropertyChange("directorychange", null, pfd.getFile());
        }
    }
}
