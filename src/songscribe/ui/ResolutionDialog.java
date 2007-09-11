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

Created on Jun 26, 2006
*/
package songscribe.ui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class ResolutionDialog extends MyDialog implements ChangeListener{
    private SpinnerModel resModel = new SpinnerNumberModel(300, 30, 1200, 1);
    private boolean approved;
    private MainFrame mainFrame;
    private JTextField widthField = new JTextField(5);
    private JTextField heightField = new JTextField(5);
    private int msWidth, msHeight;

    public ResolutionDialog(MainFrame mainFrame) {
        super(mainFrame, "Resolution");
        this.mainFrame = mainFrame;
        JPanel north = new JPanel();
        north.add(new JLabel("Enter the resolution of the image: "));
        JSpinner spinner = new JSpinner(resModel);
        spinner.addChangeListener(this);
        north.add(spinner);
        north.add(new JLabel("DPI"));
        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "The size of the outcoming image:"));
        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0.5, 0.5, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0);
        center.add(new JLabel("Width:"), c);
        c.gridy=1;
        center.add(new JLabel("Height:"), c);
        c.gridx=2;c.gridy=0;
        widthField.setEditable(false);
        center.add(widthField, c);
        c.gridy=1;
        heightField.setEditable(false);
        center.add(heightField, c);
        c.gridx=3;c.gridy=0;
        center.add(new JLabel("pt"), c);
        c.gridy=1;
        center.add(new JLabel("pt"), c);
        dialogPanel.add(BorderLayout.NORTH, north);
        JPanel realCenter = new JPanel();
        realCenter.add(center);
        dialogPanel.add(BorderLayout.CENTER, realCenter);
        southPanel = new JPanel();
        southPanel.add(okButton);
        southPanel.add(cancelButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData() {
        approved = false;
        resModel.setValue(Integer.parseInt(mainFrame.getProperties().getProperty(Constants.DPIPROP)));
        msWidth = mainFrame.getMusicSheet().getSheetWidth();
        msHeight = mainFrame.getMusicSheet().getSheetHeight();
        stateChanged(null);
    }

    protected void setData() {
        approved = true;
        mainFrame.getProperties().setProperty(Constants.DPIPROP, resModel.getValue().toString());
    }

    public boolean isApproved() {
        return approved;
    }

    public int getResolution(){
        return (Integer)resModel.getValue();
    }

    public void stateChanged(ChangeEvent e) {
        float scale = (float)getResolution()/(float)MusicSheet.RESOLUTION;
        widthField.setText(Integer.toString(Math.round(scale*msWidth)));
        heightField.setText(Integer.toString(Math.round(scale*msHeight)));
    }
}
