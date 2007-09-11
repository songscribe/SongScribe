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

Created on Aug 5, 2006
*/
package songscribe.publisher.newsteps;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import songscribe.publisher.Utilities;
import songscribe.ui.Constants;

/**
 * @author Csaba Kávai
 */
class PaperSizeStep extends Step{
    private static Logger logger = Logger.getLogger(PaperSizeStep.class);
    private static final String INFO = "<html>Set the outcoming paper size and margins</html>";

    private final double mmPerIn = 25.4;

    private DefaultComboBoxModel templates = new DefaultComboBoxModel();
    private SpinnerModel widthSpinnerModel = new SpinnerNumberModel(0.1, 0.1, Double.MAX_VALUE, 1.0);
    private SpinnerModel heightSpinnerModel = new SpinnerNumberModel(0.1, 0.1, Double.MAX_VALUE, 1.0);
    private SpinnerModel leftInnerSpinnerModel = new SpinnerNumberModel(0.1, 0.1, Double.MAX_VALUE, 1.0);
    private SpinnerModel rightOuterSpinnerModel = new SpinnerNumberModel(0.1, 0.1, Double.MAX_VALUE, 1.0);
    private SpinnerModel topSpinnerModel = new SpinnerNumberModel(0.1, 0.1, Double.MAX_VALUE, 1.0);
    private SpinnerModel bottomSpinnerModel = new SpinnerNumberModel(0.1, 0.1, Double.MAX_VALUE, 1.0);
    private JComboBox unitsCombo = new JComboBox(new String[]{"inch", "mm"});
    private JLabel leftInnerLabel = new JLabel();
    private JLabel rightOuterLabel = new JLabel();
    private JCheckBox mirroredCheck = new JCheckBox("Mirrored");

    private int currentUnit;

    public PaperSizeStep(Data data) {
        super(data);

        try {
            BufferedReader br = new BufferedReader(new FileReader("conf/papertemplates"));
            String line;
            while((line=br.readLine())!=null && line.length()>0){
                templates.addElement(new TemplateObject(line));
            }
        } catch (FileNotFoundException e) {
            logger.error("No papertemplate file", e);
        } catch (IOException e) {
            logger.error("Papertemplate file", e);
        }
        templates.addElement("Custom");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(5, 5, 0, 5);
        panel.add(new JLabel("From template:"), c);
        c.gridy = 1;
        panel.add(new JLabel("Paper size:"), c);
        c.gridy = 2;
        panel.add(new JLabel("Width:"), c);
        c.gridy = 3;
        panel.add(new JLabel("Height:"), c);
        c.gridy = 4;
        panel.add(new JLabel("Margins:"), c);
        c.gridy = 5;
        panel.add(leftInnerLabel, c);
        c.gridy = 6;
        panel.add(rightOuterLabel, c);
        c.gridy = 7;
        panel.add(new JLabel("Top:"), c);
        c.gridy = 8;
        panel.add(new JLabel("Bottom:"), c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        JComboBox templateCombo = new JComboBox(templates);
        templateCombo.addActionListener(new TemplateAction());
        panel.add(templateCombo, c);
        c.gridy = 1;
        panel.add(new JSeparator());
        c.gridwidth = 1;
        c.gridy = 2;
        JSpinner spinner = new JSpinner(widthSpinnerModel);
        Dimension spinnerSize = new Dimension(70, 20);
        spinner.setPreferredSize(spinnerSize);
        panel.add(spinner, c);
        c.gridy = 3;
        spinner = new JSpinner(heightSpinnerModel);
        spinner.setPreferredSize(spinnerSize);
        panel.add(spinner, c);
        c.gridy = 4;
        panel.add(new JSeparator());
        c.gridy = 5;
        spinner = new JSpinner(leftInnerSpinnerModel);
        spinner.setPreferredSize(spinnerSize);
        panel.add(spinner, c);
        c.gridy = 6;
        spinner = new JSpinner(rightOuterSpinnerModel);
        spinner.setPreferredSize(spinnerSize);
        panel.add(spinner, c);
        c.gridy = 7;
        spinner = new JSpinner(topSpinnerModel);
        spinner.setPreferredSize(spinnerSize);
        panel.add(spinner, c);
        c.gridy = 8;
        spinner = new JSpinner(bottomSpinnerModel);
        spinner.setPreferredSize(spinnerSize);
        panel.add(spinner, c);
        c.gridx = 2;
        c.gridy = 2;
        c.gridheight = 2;
        unitsCombo.addActionListener(new UnitAction());
        panel.add(unitsCombo, c);
        c.gridy = 5;
        c.gridheight = 4;
        MirroredAction mirroredAction = new MirroredAction();
        mirroredCheck.addActionListener(mirroredAction);
        panel.add(mirroredCheck, c);

        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(panel);
        mirroredAction.actionPerformed(null);
    }

    public String getInfo() {
        return INFO;
    }

    public void start() {
        boolean metric = data.publisher.getProperties().getProperty(Constants.METRIC).equals(Constants.TRUEVALUE);
        for(int i=0;i<templates.getSize();i++){
            if(((TemplateObject)templates.getElementAt(i)).metric==metric){
                templates.setSelectedItem(templates.getElementAt(i));
                new TemplateAction().actionPerformed(null);
                break;
            }
        }
    }

    public void end() {
        data.paperWidth = getInPixel(widthSpinnerModel);
        data.paperHeight = getInPixel(heightSpinnerModel);
        data.leftInnerMargin = getInPixel(leftInnerSpinnerModel);
        data.rightOuterMargin = getInPixel(rightOuterSpinnerModel);
        data.topMargin = getInPixel(topSpinnerModel);
        data.bottomMargin = getInPixel(bottomSpinnerModel);
        data.mirrored = mirroredCheck.isSelected();
        if(unitsCombo.getSelectedItem() instanceof TemplateObject){
            data.publisher.getProperties().setProperty(Constants.METRIC, ((TemplateObject)unitsCombo.getSelectedItem()).metric?Constants.TRUEVALUE:Constants.FALSEVALUE);
        }else{
            data.publisher.getProperties().setProperty(Constants.METRIC, unitsCombo.getSelectedIndex()==0?Constants.FALSEVALUE:Constants.TRUEVALUE);
        }
    }

    public int getInPixel(SpinnerModel model){
        return Utilities.convertToPixel((Double)model.getValue(), currentUnit);
    }

    public void setValues(int pageWidth, int pageHeight, int leftInnerMargin, int rightOuterMargin, int topMargin, int bottomMargin, boolean mirroredMargin){
        int metric = data.publisher.getProperties().getProperty(Constants.METRIC).equals(Constants.FALSEVALUE) ? 0 : 1;
        unitsCombo.setSelectedIndex(metric);
        widthSpinnerModel.setValue(Utilities.convertFromPixels(pageWidth, metric));
        heightSpinnerModel.setValue(Utilities.convertFromPixels(pageHeight, metric));
        templates.setSelectedItem(templates.getElementAt(templates.getSize()-1));
        for(int i=0;i<templates.getSize();i++){
            if(templates.getElementAt(i) instanceof TemplateObject){
                TemplateObject to = (TemplateObject)templates.getElementAt(i);
                if(to.width.equals(widthSpinnerModel.getValue()) && to.height.equals(heightSpinnerModel.getValue())){
                    templates.setSelectedItem(to);
                }
            }
        }
        leftInnerSpinnerModel.setValue(Utilities.convertFromPixels(leftInnerMargin, metric));
        rightOuterSpinnerModel.setValue(Utilities.convertFromPixels(rightOuterMargin, metric));
        topSpinnerModel.setValue(Utilities.convertFromPixels(topMargin, metric));
        bottomSpinnerModel.setValue(Utilities.convertFromPixels(bottomMargin, metric));
        mirroredCheck.setSelected(mirroredMargin);
    }

    private class TemplateObject{
        String name;
        Double width;
        Double height;
        Double margin;
        String unit;
        boolean metric;

        public String toString() {
            return name;
        }

        public TemplateObject(String line) {
            int semiColonPos = line.indexOf(';');
            name = line.substring(0, semiColonPos);
            int nextSemiColonPos = line.indexOf(';', semiColonPos+1);
            width = Double.parseDouble(line.substring(semiColonPos+1, nextSemiColonPos));
            semiColonPos = nextSemiColonPos;
            nextSemiColonPos = line.indexOf(';', semiColonPos+1);
            height = Double.parseDouble(line.substring(semiColonPos+1, nextSemiColonPos));
            semiColonPos = nextSemiColonPos;
            nextSemiColonPos = line.indexOf(';', semiColonPos+1);
            margin = Double.parseDouble(line.substring(semiColonPos+1, nextSemiColonPos));
            semiColonPos = nextSemiColonPos;
            nextSemiColonPos = line.indexOf(';', semiColonPos+1);
            unit = line.substring(semiColonPos+1, nextSemiColonPos);
            metric = line.substring(nextSemiColonPos+1).equals("metric");
        }
    }

    private class UnitAction implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            if(currentUnit==-1){
                currentUnit = unitsCombo.getSelectedIndex();
            }
            if(currentUnit!=unitsCombo.getSelectedIndex()){
                double multiplier = currentUnit==0 ? mmPerIn : 1.0/mmPerIn;
                widthSpinnerModel.setValue((Double) widthSpinnerModel.getValue() *multiplier);
                heightSpinnerModel.setValue((Double) heightSpinnerModel.getValue() *multiplier);
                leftInnerSpinnerModel.setValue((Double) leftInnerSpinnerModel.getValue() *multiplier);
                rightOuterSpinnerModel.setValue((Double) rightOuterSpinnerModel.getValue() *multiplier);
                topSpinnerModel.setValue((Double) topSpinnerModel.getValue() *multiplier);
                bottomSpinnerModel.setValue((Double) bottomSpinnerModel.getValue() *multiplier);
                currentUnit=unitsCombo.getSelectedIndex();
            }
        }
    }

    private class TemplateAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(templates.getSelectedItem() instanceof TemplateObject){
                TemplateObject to = (TemplateObject) templates.getSelectedItem();
                if(to==null)return;
                widthSpinnerModel.setValue(to.width);
                heightSpinnerModel.setValue(to.height);
                leftInnerSpinnerModel.setValue(to.margin);
                rightOuterSpinnerModel.setValue(to.margin);
                topSpinnerModel.setValue(to.margin);
                bottomSpinnerModel.setValue(to.margin);
                currentUnit = -1;
                unitsCombo.setSelectedItem(to.unit);
            }
        }
    }

    private class MirroredAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            leftInnerLabel.setText(mirroredCheck.isSelected() ? "Inner:" : "Left:");
            rightOuterLabel.setText(mirroredCheck.isSelected() ? "Outer:" : "Right:");
        }
    }
}
