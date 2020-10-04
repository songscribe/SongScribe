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

    Created on Jan 2, 2007
*/
package songscribe.ui;

import org.apache.log4j.Logger;
import songscribe.SongScribe;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class HelpDialog extends MyDialog implements ListCellRenderer, ListSelectionListener {
    private static Logger logger = Logger.getLogger(HelpDialog.class);

    private DefaultListModel defaultListModel = new DefaultListModel();
    private JEditorPane editorPane = new JEditorPane();

    public HelpDialog(MainFrame mainFrame, String dialogTitle) {
        this(mainFrame, dialogTitle, true);
    }

    public HelpDialog(MainFrame mainFrame, String dialogTitle, boolean modal) {
        super(mainFrame, dialogTitle, modal);
        JList leftList = new JList(defaultListModel);
        leftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftList.setCellRenderer(this);
        leftList.addListSelectionListener(this);
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(new JScrollPane(leftList));

        editorPane.setEditable(false);
        editorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setPreferredSize(new Dimension(600, 500));
        editorScrollPane.setMinimumSize(new Dimension(100, 100));
        splitPane.setRightComponent(editorScrollPane);

        dialogPanel.add(splitPane);

        southPanel.remove(applyButton);
        southPanel.remove(cancelButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    public void addToList(String name, String html) {
        ListObject listObject = new ListObject(name, html);
        defaultListModel.addElement(listObject);
    }

    protected void getData() {
    }

    protected void setData() {
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ListObject lo = (ListObject) value;

        if (lo.component == null) {
            lo.component = new JPanel();
            JLabel label = new JLabel(lo.name);
            label.setFont(new Font(Font.SERIF, Font.PLAIN, 20));
            label.setPreferredSize(new Dimension(200, 30));
            ((JPanel) lo.component).add(label);
        }

        ((JComponent) lo.component).setBorder(null);
        lo.component.setBackground(index % 2 == 0 ? Color.white : new Color(238, 246, 255));

        ((JPanel) lo.component).getComponent(0).setForeground(Color.black);

        if (isSelected) {
            lo.component.setBackground(Color.blue);
            ((JPanel) lo.component).getComponent(0).setForeground(Color.white);
        }
        else if (cellHasFocus) {
            ((JComponent) lo.component).setBorder(BorderFactory.createLineBorder(Color.black));
        }

        return lo.component;
    }

    public void valueChanged(ListSelectionEvent e) {
        JList theList = (JList) e.getSource();

        if (theList.isSelectionEmpty()) {
            editorPane.setText(null);
        }
        else {
            try {
                editorPane.setPage("file:" + SongScribe.basePath + "/help/" + ((ListObject) theList.getSelectedValue()).html);
            }
            catch (IOException e1) {
                mainFrame.showErrorMessage("Could not open the help file.");
                logger.error("Help setPage", e1);
            }
        }
    }

    private class ListObject {
        String name, html;
        Component component;

        public ListObject(String name, String html) {
            this.name = name;
            this.html = html;
        }
    }
}
