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

Created on Feb 1, 2007
*/
package songscribe.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public class LineWidthChangeDialog extends MyDialog implements ActionListener, ChangeListener {
    private SpinnerNumberModel lineWidthSpinner = new SpinnerNumberModel(0d, 0d, 0d, 0.01d);
    private JComboBox measureMents = new JComboBox(new String[]{"cm", "inch"});
    private JSlider lineWidthSlider = new JSlider((int)Math.round(MINIMUMLINEWIDTH*MusicSheet.RESOLUTION), (int)Math.round(MAXIMUMLINEWIDTH*MusicSheet.RESOLUTION));
    private int origWidth;
    private boolean stopChange;
    public static final double MINIMUMLINEWIDTH = 4.0;
    public static final double MAXIMUMLINEWIDTH = 10.0;

    public LineWidthChangeDialog(MainFrame mainFrame) {
        super(mainFrame, "Line width");
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lineWidthSlider.setPaintTrack(true);
        lineWidthSlider.setPaintTicks(true);
        lineWidthSlider.setMajorTickSpacing(MusicSheet.RESOLUTION);
        lineWidthSlider.setMinorTickSpacing(MusicSheet.RESOLUTION/4);
        lineWidthSlider.addChangeListener(this);
        measureMents.addActionListener(this);
        lineWidthSpinner.addChangeListener(this);
        measureMents.setSelectedIndex(mainFrame.getProperties().getProperty(Constants.METRIC).equals(Constants.TRUEVALUE) ? 0 : 1);
        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);c.gridwidth = 3;c.anchor = GridBagConstraints.WEST;
        north.add(new JLabel("Set line width: "), c);
        c.gridy=1;c.gridwidth=1;
        north.add(lineWidthSlider, c);
        c.gridx=1;
        JSpinner lineWidthSpinner = new JSpinner(this.lineWidthSpinner);
        lineWidthSpinner.setPreferredSize(new Dimension(70, 24));
        north.add(lineWidthSpinner, c);
        c.gridx=2;
        north.add(measureMents, c);
        dialogPanel.add(BorderLayout.NORTH, north);
        dialogPanel.add(new JLabel("<html><i>Note: if the line is wider than the page width,<br>you will see here, in the Song Writer as the page<br>has grown, but in the Song Book the song will<br>be shrunk.</i></html>"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LineWidthChangeDialog.this.mainFrame.getMusicSheet().setLineWidth(origWidth);
            }
        });
        southPanel.remove(applyButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }


    protected void getData() {
        stopChange = false;
        actionPerformed(null);
        origWidth = mainFrame.getMusicSheet().getComposition().getLineWidth();
    }

    protected void setData() {
        mainFrame.getProperties().setProperty(Constants.METRIC, measureMents.getSelectedIndex()==0 ? Constants.TRUEVALUE : Constants.FALSEVALUE);
    }


    public void actionPerformed(ActionEvent e) {
        if(measureMents.getSelectedIndex()==0){
            lineWidthSpinner.setValue((double)mainFrame.getMusicSheet().getComposition().getLineWidth()/(double)MusicSheet.RESOLUTION*2.54);
            lineWidthSpinner.setMinimum(MINIMUMLINEWIDTH*2.54);
            lineWidthSpinner.setMaximum(MAXIMUMLINEWIDTH*2.54);
        }else{
            lineWidthSpinner.setValue((double)mainFrame.getMusicSheet().getComposition().getLineWidth()/(double)MusicSheet.RESOLUTION);
            lineWidthSpinner.setMinimum(MINIMUMLINEWIDTH);
            lineWidthSpinner.setMaximum(MAXIMUMLINEWIDTH);
        }
    }


    public void stateChanged(ChangeEvent e) {
        if(stopChange){
            stopChange = false;
        }else if(e.getSource()==lineWidthSpinner){
            mainFrame.getMusicSheet().setLineWidth((int) Math.round((Double) lineWidthSpinner.getValue() / (measureMents.getSelectedIndex() == 0 ? 2.54 : 1d) * MusicSheet.RESOLUTION));
            stopChange = true;
            lineWidthSlider.setValue(mainFrame.getMusicSheet().getComposition().getLineWidth());
        }else if(e.getSource()==lineWidthSlider){
            mainFrame.getMusicSheet().setLineWidth(lineWidthSlider.getValue());
            stopChange = true;
            actionPerformed(null);
        }
    }
}
