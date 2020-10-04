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

    Created on Jul 29, 2006
*/
package songscribe.ui;

import songscribe.SongScribe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * @author Csaba Kávai
 */
public class TipFrame extends JFrame {
    private MainFrame mainFrame;
    private File tipFile = new File(SongScribe.basePath + "/help/tips");
    private int index;
    private StringBuilder tipBuffer = new StringBuilder(256);

    private JTextPane tipPane;
    private JCheckBox showTip;

    public TipFrame(MainFrame mainFrame) throws IOException {
        super("Tip of the Day");
        this.mainFrame = mainFrame;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeWindow();
            }
        });
        index = Integer.parseInt(mainFrame.getProperties().getProperty(Constants.TIP_INDEX));
        initComponents();
        showTip();
        setLocation(MainFrame.CENTER_POINT.x - getWidth() / 2, MainFrame.CENTER_POINT.y - getHeight() / 2);
        setVisible(true);
    }

    private void initComponents() {
        JButton closeButton;
        JLabel didYouKnowLabel;
        JButton nextButton;
        JButton previousButton;
        JScrollPane tipScroll;

        didYouKnowLabel = new JLabel();
        tipScroll = new JScrollPane();
        tipPane = new JTextPane();
        showTip = new JCheckBox();
        closeButton = new JButton();
        nextButton = new JButton();
        previousButton = new JButton();

        didYouKnowLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        didYouKnowLabel.setIcon(new ImageIcon(MainFrame.getImage("idea.png")));
        didYouKnowLabel.setText("Did you know ... ?");

        tipPane.setEditable(false);
        tipPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        tipScroll.setViewportView(tipPane);

        showTip.setSelected(mainFrame.getProperties().getProperty(Constants.SHOW_TIP).equals(Constants.TRUE_VALUE));
        showTip.setText("Show tips on startup");
        showTip.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showTip.setMargin(new Insets(0, 0, 0, 0));

        closeButton.setIcon(MyDialog.CANCEL_ICON);
        closeButton.setText("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });

        nextButton.setIcon(new ImageIcon(MainFrame.getImage("forward16.png")));
        nextButton.setText("Next tip");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    showTip();
                }
                catch (IOException e1) {
                    mainFrame.showErrorMessage("Cannot read the tip file.");
                }
            }
        });

        previousButton.setIcon(new ImageIcon(MainFrame.getImage("back16.png")));
        previousButton.setText("Previous tip");
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (index > 1) {
                        index -= 2;
                        showTip();
                    }
                }
                catch (IOException e1) {
                    mainFrame.showErrorMessage("Cannot read the tip file.");
                }
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(didYouKnowLabel).add(showTip)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).add(layout.createSequentialGroup().add(previousButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))).add(nextButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(closeButton)).add(tipScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 376, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(didYouKnowLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(tipScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 151, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(15, 15, 15).add(showTip).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 21, Short.MAX_VALUE).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(closeButton).add(nextButton).add(previousButton)).addContainerGap()));
        pack();
    }

    private void closeWindow() {
        mainFrame.getProperties().setProperty(Constants.SHOW_TIP, showTip.isSelected() ? Constants.TRUE_VALUE : Constants.FALSE_VALUE);
        setVisible(false);
        dispose();
    }

    private void showTip() throws IOException {
        FileReader fr = new FileReader(tipFile);
        tipBuffer.delete(0, tipBuffer.length());
        int breakFound = 0;
        int ch;

        while ((ch = fr.read()) != -1 && breakFound <= index) {
            if (breakFound == index) {
                tipBuffer.append((char) ch);
            }

            if (ch == '\n') {
                breakFound++;
            }
        }

        fr.close();

        if (tipBuffer.length() == 0) {
            index = 0;
            showTip();
        }
        else {
            index++;
        }

        tipPane.setText(tipBuffer.toString());
        mainFrame.getProperties().setProperty(Constants.TIP_INDEX, Integer.toString(index));
    }
}
