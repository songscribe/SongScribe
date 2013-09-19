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

Created on Nov 6, 2007
*/
package songscribe.publisher;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import songscribe.data.DoNotShowException;
import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/**
 * @author Csaba Kávai
 */
public class PageNumberDialog extends MyDialog {

    JToggleButton boldButton;
    JComboBox fontComboBox;    
    JToggleButton italicButton;
    JComboBox marginSpaceComboBox;
    SpinnerNumberModel sizeSpinnerModel = new SpinnerNumberModel(12, 1, 256, 1);
    SpinnerNumberModel fromPageSpinnerModel = new SpinnerNumberModel(1, 1, 1, 1);
    SpinnerNumberModel marginSpaceInchSpinnerModel = new SpinnerNumberModel(0d, -5d, 5d, 0.01d);
    SpinnerNumberModel marginSpaceMmSpinnerModel = new SpinnerNumberModel(0, -70, 70, 1);
    JSpinner marginSpaceSpinner;
    ButtonGroup alignmentGroup = new ButtonGroup();
    ButtonGroup placementGroup = new ButtonGroup();
    JButton removeButton = new JButton("Remove", REMOVEICON);

    public PageNumberDialog(MainFrame mainFrame) {
        super(mainFrame, "Page number settings");
        dialogPanel.add(initComponents());
        removeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Book book = ((Publisher) PageNumberDialog.this.mainFrame).getBook();
                book.setPageNumber(null);
                book.repaintWhole();
                setVisible(false);
            }
        });
        southPanel.add(removeButton, 2);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    private JPanel initComponents() {
        JPanel panel = new JPanel();
        JPanel alignmentPanel;
        JLabel fontLabel;
        JLabel fromPageLabel;
        JLabel marginSpaceLabel;
        JPanel placementPanel;
        JLabel sizeLabel;
        JLabel styleLabel;
        JSpinner sizeSpinner;
        JSpinner fromPageSpinner;

        fontLabel = new JLabel();
        fontComboBox = new JComboBox(Publisher.FONTFAMILIES);
        sizeSpinner = new JSpinner();
        sizeLabel = new JLabel();
        boldButton = new JToggleButton();
        italicButton = new JToggleButton();
        styleLabel = new JLabel();
        alignmentPanel = new JPanel();
        placementPanel = new JPanel();
        fromPageLabel = new JLabel();
        fromPageSpinner = new JSpinner();
        marginSpaceLabel = new JLabel();
        marginSpaceSpinner = new JSpinner();
        marginSpaceComboBox = new JComboBox();

        fontLabel.setText("Font:");

        sizeSpinner.setModel(sizeSpinnerModel);

        sizeLabel.setText("Size:");

        boldButton.setText("<html><b>B</b></html>");

        italicButton.setText("<html><i>I</i></html>");

        styleLabel.setText("Bold / Italic:");

        alignmentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Alignment"));
        alignmentPanel.setLayout(new BoxLayout(alignmentPanel, BoxLayout.Y_AXIS));
        for(PageNumber.Alignment pna:PageNumber.Alignment.values()){
            JRadioButton rb = new JRadioButton(pna.getDescription());
            rb.setActionCommand(pna.name());
            alignmentGroup.add(rb);
            alignmentPanel.add(rb);            
        }
        alignmentGroup.setSelected(alignmentGroup.getElements().nextElement().getModel(), true);

        placementPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Placement"));
        placementPanel.setLayout(new BoxLayout(placementPanel, BoxLayout.Y_AXIS));
        for(PageNumber.Placement ppa:PageNumber.Placement.values()){
            JRadioButton rb = new JRadioButton(ppa.getDescription());
            rb.setActionCommand(ppa.name());
            placementGroup.add(rb);
            placementPanel.add(rb);
        }
        placementGroup.setSelected(placementGroup.getElements().nextElement().getModel(), true);

        fromPageLabel.setText("Print from this page:");

        fromPageSpinner.setModel(fromPageSpinnerModel);

        marginSpaceLabel.setText("Distance from margin:");

        marginSpaceComboBox.setModel(new DefaultComboBoxModel(new String[] { "inch", "mm" }));
        marginSpaceComboBox.addActionListener(new MeasureChange());

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(fontLabel)
                    .add(fontComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(alignmentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(fromPageLabel)
                            .add(marginSpaceLabel))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                            .add(marginSpaceSpinner)
                            .add(fromPageSpinner, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(GroupLayout.LEADING)
                                .add(sizeSpinner, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                                .add(sizeLabel))
                            .add(21, 21, 21)
                            .add(layout.createParallelGroup(GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(boldButton)
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(italicButton))
                                .add(styleLabel)))
                        .add(placementPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(marginSpaceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(fontLabel)
                    .add(sizeLabel)
                    .add(styleLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(fontComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(sizeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(boldButton)
                    .add(italicButton))
                .add(28, 28, 28)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(alignmentPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(19, 19, 19)
                        .add(layout.createParallelGroup(GroupLayout.BASELINE)
                            .add(fromPageLabel)
                            .add(fromPageSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.BASELINE)
                            .add(marginSpaceLabel)
                            .add(marginSpaceSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(marginSpaceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .add(placementPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        return panel;
    }

    protected void getData() throws DoNotShowException {
        Publisher publisher = (Publisher) mainFrame;
        if(publisher.isBookNull())throw new DoNotShowException();
        fromPageSpinnerModel.setMaximum(publisher.getBook().getTotalPage());
        marginSpaceComboBox.setSelectedIndex(publisher.getProperties().getProperty(Constants.METRIC).equals(Constants.TRUEVALUE)?1:0);
        PageNumber pn = publisher.getBook().getPageNumber();
        removeButton.setEnabled(pn!=null);
        if(pn!=null){
            fontComboBox.setSelectedItem(pn.getFont().getFamily());
            sizeSpinnerModel.setValue(pn.getFont().getSize());
            boldButton.setSelected(pn.getFont().isBold());
            italicButton.setSelected(pn.getFont().isItalic());
            for(Enumeration<AbstractButton> e = alignmentGroup.getElements();e.hasMoreElements();){
                AbstractButton ab = e.nextElement();
                if(ab.getActionCommand().equals(pn.getAlignment().name())){
                    alignmentGroup.setSelected(ab.getModel(), true);
                }
            }
            for(Enumeration<AbstractButton> e = placementGroup.getElements();e.hasMoreElements();){
                AbstractButton ab = e.nextElement();
                if(ab.getActionCommand().equals(pn.getPlacement().name())){
                    placementGroup.setSelected(ab.getModel(), true);
                }
            }
            fromPageSpinnerModel.setValue(pn.getFromPage());
            marginSpaceInchSpinnerModel.setValue(Utilities.convertFromPixels(pn.getSpaceFromMargin(), marginSpaceComboBox.getSelectedIndex()));
            marginSpaceMmSpinnerModel.setValue(Utilities.convertFromPixels(pn.getSpaceFromMargin(), marginSpaceComboBox.getSelectedIndex()));
            marginSpaceSpinner.setModel(marginSpaceComboBox.getSelectedIndex()==0?marginSpaceInchSpinnerModel:marginSpaceMmSpinnerModel);
        }
    }

    protected void setData() {
        Publisher publisher = (Publisher) mainFrame;
        PageNumber pn = publisher.getBook().getPageNumber();
        if(pn==null)pn=new PageNumber();
        pn.setFont(new Font(fontComboBox.getSelectedItem().toString(), (boldButton.isSelected()?Font.BOLD:0)|(italicButton.isSelected()?Font.ITALIC:0), (Integer)sizeSpinnerModel.getValue()));
        pn.setAlignment(PageNumber.Alignment.valueOf(alignmentGroup.getSelection().getActionCommand()));
        pn.setPlacement(PageNumber.Placement.valueOf(placementGroup.getSelection().getActionCommand()));
        pn.setFromPage((Integer)fromPageSpinnerModel.getValue());
        pn.setSpaceFromMargin(Utilities.convertToPixel(((Number)marginSpaceSpinner.getModel().getValue()).doubleValue(), marginSpaceComboBox.getSelectedIndex()));
        publisher.getProperties().setProperty(Constants.METRIC, marginSpaceComboBox.getSelectedIndex()==0 ? Constants.FALSEVALUE : Constants.TRUEVALUE);
        publisher.getBook().setPageNumber(pn);
        publisher.getBook().repaintWhole();
    }

    private class MeasureChange implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            double m = Utilities.convertFromPixels(
                    Utilities.convertToPixel(((Number)marginSpaceSpinner.getModel().getValue()).doubleValue(), 1-marginSpaceComboBox.getSelectedIndex()),
                    marginSpaceComboBox.getSelectedIndex());
            if(marginSpaceComboBox.getSelectedIndex()==0){
                marginSpaceInchSpinnerModel.setValue(m);
                marginSpaceSpinner.setModel(marginSpaceInchSpinnerModel);
            }else{
                marginSpaceMmSpinnerModel.setValue((int)Math.round(m));
                marginSpaceSpinner.setModel(marginSpaceMmSpinnerModel);
            }
        }
    }
}
