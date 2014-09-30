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

    Created on: 2006.05.19.
*/
package songscribe.ui;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class HTMLDialog extends MyDialog {
    private static Logger logger = Logger.getLogger(HTMLDialog.class);
    private JEditorPane editorPane;

    public HTMLDialog(MainFrame mainFrame, String dialogTitle) {
        super(mainFrame, dialogTitle, false);

        southPanel = new JPanel();
        southPanel.add(okButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);

        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setBackground(dialogPanel.getBackground());

        // Put the editor pane in a scroll pane.
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setPreferredSize(new Dimension(500, 500));
        editorScrollPane.setMinimumSize(new Dimension(100, 100));
        dialogPanel.add(BorderLayout.CENTER, editorScrollPane);
    }

    public HTMLDialog(MainFrame mainFrame, String dialogTitle, String htmlPage) {
        this(mainFrame, dialogTitle);
        setPage(htmlPage);
    }

    protected void setPage(String htmlPage) {
        try {
            editorPane.setPage("file:help/" + htmlPage);
        }
        catch (IOException e) {
            mainFrame.showErrorMessage("Could not open the help file.");
            logger.error("HTML setPage", e);
        }
    }

    protected void getData() {
    }

    protected void setData() {
    }
}
