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

Created on May 6, 2007
*/
package songscribe.publisher;

import songscribe.ui.MyDialog;
import songscribe.ui.MusicSheet;
import songscribe.ui.Constants;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.Song;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

/**
 * @author Csaba Kávai
 */
public class SongDialog extends MyDialog {

    private SpinnerNumberModel newHeight = new SpinnerNumberModel(1, 0.1, 10000, 0.01);
    private SpinnerNumberModel newStaffHeight = new SpinnerNumberModel(1, 0.1, 10000, 0.01);
    private SpinnerNumberModel newWidth = new SpinnerNumberModel(1, 0.1, 10000, 0.01);
    private SpinnerNumberModel ratio = new SpinnerNumberModel(1, 0.1, 100, 0.01);

    private JLabel originalHeight;
    private JLabel originalStaffHeight;
    private JLabel originalWidth;
    private JComboBox unitCombo;

    private MeasureChange measureChange = new MeasureChange();

    private int staffHeight = MusicSheet.VISIBLELINENUM*MusicSheet.LINEDIST;
    private int msWidth, msHeight;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private boolean stopValueChange;

    private static final String INCH = "inch";
    private static final String MM = "mm";
    private static final String PX = "px";

    public SongDialog(Publisher publisher) {
        super(publisher, "Song properties");
        dialogPanel.add(BorderLayout.CENTER, initComponents());
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
        unitCombo.addActionListener(measureChange);
        ValueChange valueChange = new ValueChange();
        newWidth.addChangeListener(valueChange);
        newHeight.addChangeListener(valueChange);
        newStaffHeight.addChangeListener(valueChange);
        ratio.addChangeListener(valueChange);
    }

    private JPanel initComponents() {
        JLabel NH;
        JLabel NW;
        JLabel OH;
        JLabel OW;
        JLabel note1;
        JLabel note2;
        JLabel note3;
        JLabel nsh;
        JLabel osh;
        JLabel rLabel;

        OW = new JLabel();
        OH = new JLabel();
        originalWidth = new JLabel();
        originalHeight = new JLabel();
        NW = new JLabel();
        NH = new JLabel();
        unitCombo = new JComboBox();
        JSpinner newWidthSpinner = new JSpinner(newWidth);
        JSpinner newHeightSpinner = new JSpinner(newHeight);
        nsh = new JLabel();
        JSpinner newStaffHeightSpinner = new JSpinner(newStaffHeight);
        osh = new JLabel();
        originalStaffHeight = new JLabel();
        rLabel = new JLabel();
        JSpinner ratioSpinner = new JSpinner(ratio);
        note1 = new JLabel();
        note2 = new JLabel();
        note3 = new JLabel();

        OW.setText("Original Width:");

        OH.setText("Original Height:");

        originalWidth.setText("...");

        originalHeight.setText("...");

        NW.setText("New Width:");

        NH.setText("New Height:");

        unitCombo.setModel(new DefaultComboBoxModel(new String[]{INCH, MM, PX}));

        nsh.setText("New Staff Height:");

        osh.setText("Original Staff Height:");

        originalStaffHeight.setText("...");

        rLabel.setText("Ratio:");

        note1.setFont(new Font("Dialog", 2, 12));
        note1.setText("Note: Since the graphics is vector-based");

        note2.setFont(new Font("Dialog", 2, 12));
        note2.setText("it does not ruin the quality if you enlarge");

        note3.setFont(new Font("Dialog", 2, 12));
        note3.setText("the song on more than 1.0 ratio.");

        JPanel returnPanel = new JPanel();
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(returnPanel);
        returnPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(15, 15, 15)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(rLabel)
                            .add(nsh)
                            .add(NH)
                            .add(NW)
                            .add(osh)
                            .add(OH)
                            .add(OW))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(originalWidth)
                            .add(originalHeight)
                            .add(originalStaffHeight)
                            .add(newWidthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(newHeightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(newStaffHeightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(unitCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(ratioSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(note2))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(note3))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(note1)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(OW)
                    .add(originalWidth))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(OH)
                    .add(originalHeight))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(osh)
                    .add(originalStaffHeight))
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(NW)
                    .add(newWidthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(NH)
                    .add(newHeightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nsh)
                    .add(newStaffHeightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(unitCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rLabel)
                    .add(ratioSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15)
                .add(note1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(note2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(note3)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        return returnPanel;
    }

    protected void getData() {
        Book book = ((Publisher) mainFrame).getBook();
        PageComponent pc = book.getSelectedComponent();
        if(pc!=null && pc instanceof Song){
            Song song = (Song)pc;
            msWidth = song.getMusicSheet().getSheetWidth();
            msHeight = song.getMusicSheet().getSheetHeight();
            stopValueChange = true;
            ratio.setValue(song.getResolution());
            stopValueChange = false;
            measureChange.actionPerformed(null);
        }
    }

    protected void setData() {
        Publisher publisher = (Publisher) mainFrame;
        PageComponent pc = publisher.getBook().getSelectedComponent();
        if(pc!=null && pc instanceof Song){
            pc.setResolution((Double)ratio.getValue());
            publisher.getBook().repaintWhole();
        }
        unitCombo.setSelectedItem(publisher.getProperties().getProperty(Constants.METRIC).equals(Constants.TRUEVALUE) ? MM : INCH);
        if(unitCombo.getSelectedItem()==INCH){
            publisher.getProperties().setProperty(Constants.METRIC, Constants.FALSEVALUE);
        }else if(unitCombo.getSelectedItem()==MM){
            publisher.getProperties().setProperty(Constants.METRIC, Constants.TRUEVALUE);
        }
    }

    private double getLength72(int pixel){
        return unitCombo.getSelectedItem()==PX ? (int)Math.round(pixel*72.0/MusicSheet.RESOLUTION) : Utilities.convertFromPixels(pixel, unitCombo.getSelectedItem()==INCH ? 0 : 1);
    }

    private class MeasureChange implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            Song song = (Song)((Publisher) mainFrame).getBook().getSelectedComponent();
            originalWidth.setText(decimalFormat.format(getLength72(song.getPos().width)));
            originalHeight.setText(decimalFormat.format(getLength72(song.getPos().height)));
            originalStaffHeight.setText(decimalFormat.format(getLength72((int)Math.round(staffHeight*song.getResolution()))));
            double r = (Double)ratio.getValue();
            stopValueChange = true;
            newWidth.setValue(getLength72((int)Math.round(msWidth*r)));
            newHeight.setValue(getLength72((int)Math.round(msHeight*r)));
            newStaffHeight.setValue(getLength72((int)Math.round(staffHeight*r)));
            double stepSize = unitCombo.getSelectedItem()==INCH ? 0.01 : 1.0;
            newWidth.setStepSize(stepSize);
            newHeight.setStepSize(stepSize);
            newStaffHeight.setStepSize(stepSize);
            stopValueChange = false;
        }
    }

    private class ValueChange implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if(stopValueChange)return;
            Song song = (Song)((Publisher) mainFrame).getBook().getSelectedComponent();
            double r;
            if(e.getSource()==newWidth){
                r = (Double)newWidth.getValue()*song.getResolution()/ getLength72(song.getPos().width);
            }else if(e.getSource()==newHeight){
                r = (Double)newHeight.getValue()*song.getResolution()/ getLength72(song.getPos().height);
            }else if(e.getSource()==newStaffHeight){
                r = (Double)newStaffHeight.getValue()/ getLength72(staffHeight);
            }else{
                r = (Double)ratio.getValue();
            }
            stopValueChange = true;
            if(e.getSource()!=newWidth)newWidth.setValue(getLength72((int)Math.round(msWidth*r)));
            if(e.getSource()!=newHeight)newHeight.setValue(getLength72((int)Math.round(msHeight*r)));
            if(e.getSource()!=newStaffHeight)newStaffHeight.setValue(getLength72((int)Math.round(staffHeight*r)));
            if(e.getSource()!=ratio)ratio.setValue(r);
            stopValueChange = false;
        }
    }
}
