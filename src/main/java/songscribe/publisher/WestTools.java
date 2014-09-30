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

    Created on Feb 2, 2007
*/
package songscribe.publisher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public class WestTools extends JToolBar {
    private Publisher publisher;

    public WestTools(Publisher publisher) {
        super(VERTICAL);
        this.publisher = publisher;
        setFloatable(false);
        ButtonGroup bg = new ButtonGroup();
        JToggleButton selectComponent = new JToggleButton(new ImageIcon(Publisher.getImage("arrow.gif")));
        selectComponent.setToolTipText("Select component");
        selectComponent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (WestTools.this.publisher.getBook() != null) {
                    WestTools.this.publisher.getBook().setSelection(Book.Selection.COMPONENTS);
                }
            }
        });
        bg.add(selectComponent);
        add(selectComponent);
        JToggleButton selectPages = new JToggleButton(new ImageIcon(Publisher.getImage("arrowPage.png")));
        selectPages.setToolTipText("Select pages");
        selectPages.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (WestTools.this.publisher.getBook() != null) {
                    WestTools.this.publisher.getBook().setSelection(Book.Selection.PAGES);
                }
            }
        });
        bg.add(selectPages);
        add(selectPages);
        addSeparator();
        publisher.addActionToToolBarToShowShortDescription(this, publisher.getPropertiesAction()).setIcon(new ImageIcon(Publisher.getImage("propertiesTool.png")));
        addSeparator();
        publisher.addActionToToolBarToShowShortDescription(this, publisher.getInsertSongAction()).setIcon(new ImageIcon(Publisher.getImage("addSongTool.png")));
        publisher.addActionToToolBarToShowShortDescription(this, publisher.getInsertImageAction()).setIcon(new ImageIcon(Publisher.getImage("insertimageTool.png")));
        publisher.addActionToToolBarToShowShortDescription(this, publisher.getInsertTextAction()).setIcon(new ImageIcon(Publisher.getImage("inserttextTool.png")));
    }
}
