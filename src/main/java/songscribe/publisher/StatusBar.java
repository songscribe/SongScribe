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

Created on Sep 26, 2007
*/
package songscribe.publisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public class StatusBar extends JPanel {
    private Publisher publisher;
    private JTextField currentPage = new JTextField(2);
    private JTextField totalPage = new JTextField(2);
    private ActionListener currentPageAction;

    public StatusBar(Publisher publisher) {
        this.publisher = publisher;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        Dimension size = new Dimension(180, 25);
        add(Box.createGlue());
        add(createSeparator());
        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pagePanel.setPreferredSize(size);
        pagePanel.setMaximumSize(size);
        pagePanel.add(new LittleJButton("2leftarrow.png", new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(StatusBar.this.publisher.isBookNull())return;
                currentPage.setText("1");
                currentPageAction.actionPerformed(null);
            }
        }));
        pagePanel.add(new LittleJButton("1leftarrow.png", new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(StatusBar.this.publisher.isBookNull())return;
                currentPage.setText(Integer.toString(Integer.parseInt(currentPage.getText())-1));
                currentPageAction.actionPerformed(null);
            }
        }));
        pagePanel.add(currentPage);
        pagePanel.setToolTipText("Current page");
        pagePanel.add(new JLabel("/"));
        pagePanel.add(totalPage);
        totalPage.setToolTipText("Total page");
        totalPage.setEditable(false);
        add(pagePanel);
        pagePanel.add(new LittleJButton("1rightarrow.png", new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(StatusBar.this.publisher.isBookNull())return;
                currentPage.setText(Integer.toString(Integer.parseInt(currentPage.getText())+1));
                currentPageAction.actionPerformed(null);
            }
        }));
        pagePanel.add(new LittleJButton("2rightarrow.png", new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(StatusBar.this.publisher.isBookNull())return;
                currentPage.setText(totalPage.getText());
                currentPageAction.actionPerformed(null);
            }
        }));
        add(createSeparator());
        add(Box.createGlue());
        currentPageAction = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(StatusBar.this.publisher.isBookNull())return;
                Book book = StatusBar.this.publisher.getBook();
                int page = 1;
                try{
                    page = Integer.parseInt(currentPage.getText());
                }catch(NumberFormatException ex){}
                book.goToPage(page);
                book.requestFocusInWindow();
            }
        };
        currentPage.addActionListener(currentPageAction);
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setMaximumSize(new Dimension(2, 20));
        return separator;
    }

    public void setCurrentPage(int page){
        currentPage.setText(Integer.toString(page));
    }

    public void setTotalPage(int page){
        totalPage.setText(Integer.toString(page));
    }

    private static class LittleJButton extends JButton{
        private static final Dimension preferredSize = new Dimension(20, 20);

        public LittleJButton(String icon, ActionListener actionListener) {
            super(new ImageIcon(Publisher.getImage(icon)));
            setPreferredSize(preferredSize);
            setMaximumSize(preferredSize);
            addActionListener(actionListener);
        }
    }
}
