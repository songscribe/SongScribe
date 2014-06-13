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

Created on: 2006.03.18.
*/
package songscribe.ui;

import org.apache.log4j.Logger;
import songscribe.Version;
import songscribe.data.DoNotShowException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class WhatsNewDialog extends MyDialog{
    public static final String WHATSNEWFILE = "help/release-notes-" + Version.PUBLIC_VERSION + ".html";
    private static Logger logger = Logger.getLogger(WhatsNewDialog.class);
    private boolean noReleaseNotes;

    public WhatsNewDialog(MainFrame mainFrame) {
        super(mainFrame, "What's new");

        try {
            dialogPanel.add(BorderLayout.CENTER, createTextPane("file:" + WHATSNEWFILE));
        } catch (IOException e) {
            noReleaseNotes = true;
        }
        JPanel southPanel = new JPanel();
        southPanel.add(okButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    private Component createTextPane(String url) throws IOException{
        JTextPane textPane = new JTextPane();
        textPane.setPage(url);
        textPane.setEditable(false);
        JScrollPane textScroll = new JScrollPane(textPane);
        textScroll.setPreferredSize(new Dimension(500, 300));
        return textScroll;
    }

    @Override
    protected void getData() throws DoNotShowException {
        if (noReleaseNotes) {
            throw new DoNotShowException();
        }
    }

    @Override
    protected void setData() {

    }
}
