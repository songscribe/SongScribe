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

Created on May 8, 2007
*/
package songscribe.publisher;

import songscribe.ui.MyDialog;
import songscribe.ui.MusicSheet;
import songscribe.ui.Constants;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.PImage;

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
public class PImageDialog extends MyDialog {
    private SpinnerNumberModel newWidth = new SpinnerNumberModel(1, 0.1, 10000, 0.01);
    private SpinnerNumberModel newHeight = new SpinnerNumberModel(1, 0.1, 10000, 0.01);
    private SpinnerNumberModel ratio = new SpinnerNumberModel(1, 0.1, 100, 0.01);

    private JLabel originalHeight;
    private JLabel originalWidth;
    private JComboBox unitCombo;

    private MeasureChange measureChange = new MeasureChange();

    private int msWidth, msHeight;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private boolean stopValueChange;

    private static final String INCH = "inch";
    private static final String MM = "mm";
    private static final String PX = "px";


    public PImageDialog(Publisher publisher) {
        super(publisher, "Image properties");
        dialogPanel.add(BorderLayout.CENTER, initComponents());
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
        unitCombo.setSelectedItem(PX);
        unitCombo.addActionListener(measureChange);
        ValueChange valueChange = new ValueChange();
        newWidth.addChangeListener(valueChange);
        newHeight.addChangeListener(valueChange);
        ratio.addChangeListener(valueChange);
    }

    private JPanel initComponents() {
        JLabel NH;
        JLabel NW;
        JLabel OH;
        JLabel OW;
        JLabel note1;
        JLabel note2;
        JLabel rLabel;

        rLabel = new JLabel();
        OH = new JLabel();
        OW = new JLabel();
        NW = new JLabel();
        NH = new JLabel();
        JSpinner ratioSpinner = new JSpinner(ratio);
        originalWidth = new JLabel();
        originalHeight = new JLabel();
        JSpinner newWidthSpinner = new JSpinner(newWidth);
        JSpinner newHeightSpinner = new JSpinner(newHeight);
        unitCombo = new JComboBox();
        note1 = new JLabel();
        note2 = new JLabel();

        rLabel.setText("Ratio:");

        OH.setText("Original Height:");

        OW.setText("Original Width:");

        NW.setText("New Width:");

        NH.setText("New Height:");

        originalWidth.setText("...");

        originalHeight.setText("...");

        unitCombo.setModel(new DefaultComboBoxModel(new String[] { INCH, MM, PX }));

        note1.setFont(new Font("Dialog", 2, 12));
        note1.setText("Note: If the ratio is higher then 1, the quality");

        note2.setFont(new Font("Dialog", 2, 12));
        note2.setText("of the image becomes worse.");

        JPanel panel = new JPanel();
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(rLabel)
                            .add(NW)
                            .add(NH)
                            .add(OW)
                            .add(OH))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(originalWidth)
                            .add(originalHeight)
                            .add(newWidthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(newHeightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(ratioSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(14, 14, 14)
                                .add(unitCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(note1)
                    .add(note2))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .add(31, 31, 31)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(NW)
                    .add(newWidthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(NH)
                    .add(newHeightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rLabel)
                    .add(ratioSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(unitCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(22, 22, 22)
                .add(note1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(note2)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        return panel;
    }

    protected void getData() {
        Book book = ((Publisher) mainFrame).getBook();
        PageComponent pc = book.getSelectedComponent();
        if(pc!=null && pc instanceof PImage){
            PImage image = (PImage)pc;
            msWidth = image.getImage().getWidth(null);
            msHeight = image.getImage().getHeight(null);
            stopValueChange = true;
            ratio.setValue(image.getResolution());
            stopValueChange = false;
            measureChange.actionPerformed(null);
        }
    }

    protected void setData() {
        Publisher publisher = (Publisher) mainFrame;
        PageComponent pc = publisher.getBook().getSelectedComponent();
        if(pc!=null && pc instanceof PImage){
            pc.setResolution((Double)ratio.getValue());
            publisher.getBook().repaintWhole();
        }
        if(unitCombo.getSelectedItem()==INCH){
            publisher.getProperties().setProperty(Constants.METRIC, Constants.FALSEVALUE);
        }else if(unitCombo.getSelectedItem()==MM){
            publisher.getProperties().setProperty(Constants.METRIC, Constants.TRUEVALUE);
        }
    }

    private double getLength72(int pixel){
        return unitCombo.getSelectedItem()==PX ? (int)Math.round(pixel*72.0/ MusicSheet.RESOLUTION) : Utilities.convertFromPixels(pixel, unitCombo.getSelectedItem()==INCH ? 0 : 1);
    }

    private class MeasureChange implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            PImage image = (PImage)((Publisher) mainFrame).getBook().getSelectedComponent();
            originalWidth.setText(decimalFormat.format(getLength72(image.getPos().width)));
            originalHeight.setText(decimalFormat.format(getLength72(image.getPos().height)));
            double r = (Double)ratio.getValue();
            stopValueChange = true;
            newWidth.setValue(getLength72((int)Math.round(msWidth*r)));
            newHeight.setValue(getLength72((int)Math.round(msHeight*r)));
            double stepSize = unitCombo.getSelectedItem()==INCH ? 0.01 : 1.0;
            newWidth.setStepSize(stepSize);
            newHeight.setStepSize(stepSize);
            stopValueChange = false;
        }
    }

    private class ValueChange implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if(stopValueChange)return;
            PImage image = (PImage)((Publisher) mainFrame).getBook().getSelectedComponent();
            double r;
            if(e.getSource()==newWidth){
                r = (Double)newWidth.getValue()*image.getResolution()/ getLength72(image.getPos().width);
            }else if(e.getSource()==newHeight){
                r = (Double)newHeight.getValue()*image.getResolution()/ getLength72(image.getPos().height);
            }else{
                r = (Double)ratio.getValue();
            }
            stopValueChange = true;
            if(e.getSource()!=newWidth)newWidth.setValue(getLength72((int)Math.round(msWidth*r)));
            if(e.getSource()!=newHeight)newHeight.setValue(getLength72((int)Math.round(msHeight*r)));
            if(e.getSource()!=ratio)ratio.setValue(r);
            stopValueChange = false;
        }
    }
}
