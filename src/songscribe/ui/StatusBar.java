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

Created on Jul 5, 2006
*/
package songscribe.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.TimerTask;

/**
 * @author Csaba Kávai
 */
public class StatusBar extends JPanel{
    private static final long MEMPROGRESSREFRESHRATE = 1000;
    private final MemoryMXBean mmb = ManagementFactory.getMemoryMXBean();

    private MainFrame mainFrame;
    private JProgressBar memProgress;
    private JLabel modeLabel = new JLabel();
    private JLabel controlLabel = new JLabel();
    private JLabel pitchLabel = new JLabel();

    public StatusBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        Dimension size = new Dimension(120, 20);
        JPanel pitchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pitchPanel.add(pitchLabel);
        pitchPanel.setPreferredSize(size);
        pitchPanel.setMaximumSize(size);
        pitchPanel.setToolTipText("Present pitch");
        add(pitchPanel);
        add(createSeparator());
        add(Box.createGlue());
        add(createSeparator());
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(modeLabel);
        modePanel.setPreferredSize(size);
        modePanel.setMaximumSize(size);
        modePanel.setToolTipText("Editing mode");
        modePanel.addMouseListener(new ModeMouseListener());
        add(modePanel);
        add(createSeparator());
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(controlLabel);
        controlPanel.setPreferredSize(size);
        controlPanel.setMaximumSize(size);
        controlPanel.setToolTipText("Control");
        controlPanel.addMouseListener(new ControlMouseListener());
        add(controlPanel);
        if(mainFrame.getProperties().getProperty(Constants.SHOWMEMUSEAGE).equals(Constants.TRUEVALUE)) { 
            memProgress = new JProgressBar();
            memProgress.setMaximumSize(new Dimension(50, 20));
            memProgress.setStringPainted(true);
            add(createSeparator());
            add(Box.createHorizontalStrut(4));
            add(memProgress);
            JButton trashButton = new JButton(new ImageIcon(MainFrame.getImage("trashcan_full.png")));
            size = new Dimension(20, 20);
            trashButton.setPreferredSize(size);
            trashButton.setMaximumSize(size);
            trashButton.setToolTipText("Run Garbage Collector");
            trashButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    mmb.gc();
                }
            });
            add(trashButton);

            new java.util.Timer(true).schedule(new MemProgressTask(), 0, MEMPROGRESSREFRESHRATE);
        }
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setMaximumSize(new Dimension(2, 20));
        return separator;
    }

    public void setModeLabel(String mode){
        modeLabel.setText(mode);
    }

    public void setControlLabel(String control){
        controlLabel.setText(control);
    }

    public void setPitchString(String pitch){
        pitchLabel.setText(pitch);
    }

    private class MemProgressTask extends TimerTask{
        private static final String MOF = "M of ";
        private static final String M = "M";
        private static final String MUSED = "M  Used: ";
        private static final String TOTALHEAPSIZE = "Total heap size: ";

        private StringBuilder sb = new StringBuilder(50);

        public void run() {
            MemoryUsage mu = mmb.getHeapMemoryUsage();
            memProgress.setMaximum((int)(mu.getCommitted()));
            memProgress.setValue((int)(mu.getUsed()));
            int maxMem = (int)(mu.getCommitted()/1048576l);
            int usedMem = (int)(mu.getUsed()/1048576l);
            sb.delete(0, sb.length());
            sb.append(usedMem);
            sb.append(MOF);
            sb.append(maxMem);
            sb.append(M);
            memProgress.setString(sb.toString());
            sb.delete(0, sb.length());
            sb.append(TOTALHEAPSIZE);
            sb.append(maxMem);
            sb.append(MUSED);
            sb.append(usedMem);
            sb.append(M);
            memProgress.setToolTipText(sb.toString());
        }
    }

    private class ModeMouseListener extends MouseAdapter{
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount()==2){
                mainFrame.setNextMode();
            }
        }
    }

    private class ControlMouseListener extends MouseAdapter{
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount()==2){
                mainFrame.setNextControl();
            }
        }
    }
}
