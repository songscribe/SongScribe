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

import songscribe.data.MyBorder;
import songscribe.music.Composition;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class ResolutionDialog extends MyDialog implements ChangeListener{
    private boolean approved;
    private MainFrame mainFrame;
    private JPanel mainPanel;
    private JTextField widthField;
    private JTextField heightField;
    private JCheckBox withoutLyricsCheck;
    private JSpinner resolutionSpinner;
    private BorderPanel borderPanel;
    private JCheckBox exportWithoutTitleCheckBox;
    private int msWidth, msHeight, msHeightWL, msHeightWT;

    public ResolutionDialog(MainFrame mainFrame) {
        super(mainFrame, "Image properties");
        this.mainFrame = mainFrame;
        borderPanel.setPackListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pack();
            }
        });
        borderPanel.addChangeListener(this);
        resolutionSpinner.addChangeListener(this);
        withoutLyricsCheck.addChangeListener(this);
        exportWithoutTitleCheckBox.addChangeListener(this);
        resolutionSpinner.setModel(new SpinnerNumberModel(300, 30, 1200, 1));
        dialogPanel.add(BorderLayout.CENTER, mainPanel);
        southPanel = new JPanel();
        southPanel.add(okButton);
        southPanel.add(cancelButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData() {
        approved = false;
        resolutionSpinner.setValue(Integer.parseInt(mainFrame.getProperties().getProperty(Constants.DPIPROP)));
        MusicSheet musicSheet = mainFrame.getMusicSheet();
        Composition composition = musicSheet.getComposition();
        msWidth = musicSheet.getSheetWidth();
        msHeight = musicSheet.getSheetHeight();

        String underLyrics = composition.getUnderLyrics();
        String transletedLyrics = composition.getTranslatedLyrics();
        composition.setUnderLyrics("");
        composition.setTranslatedLyrics("");
        msHeightWL = msHeight-musicSheet.getSheetHeight();
        composition.setUnderLyrics(underLyrics);
        composition.setTranslatedLyrics(transletedLyrics);

        String songTitle = composition.getSongTitle();
        composition.setSongTitle("");
        msHeightWT = msHeight-musicSheet.getSheetHeight();
        composition.setSongTitle(songTitle);

        if(underLyrics.length()==0 && transletedLyrics.length()==0){
            withoutLyricsCheck.setSelected(false);
            withoutLyricsCheck.setEnabled(false);
        }else withoutLyricsCheck.setEnabled(true);

        if(composition.getSongTitle().length()==0){
            exportWithoutTitleCheckBox.setSelected(false);
            exportWithoutTitleCheckBox.setEnabled(false);
        }else exportWithoutTitleCheckBox.setEnabled(true);

        borderPanel.setExpertBorder(false);
        stateChanged(null);
    }

    protected void setData() {
        approved = true;
        mainFrame.getProperties().setProperty(Constants.DPIPROP, resolutionSpinner.getValue().toString());
    }

    public boolean isApproved() {
        return approved;
    }

    public int getResolution(){
        return (Integer)resolutionSpinner.getValue();
    }

    public boolean isWithoutLyrics(){
        return withoutLyricsCheck.isSelected();
    }

    public boolean isWithoutTitle(){
        return exportWithoutTitleCheckBox.isSelected();
    }

    public MyBorder getBorder(){
        return borderPanel.getMyBorder();
    }

    public void stateChanged(ChangeEvent e) {
        float scale = (float)getResolution()/(float)MusicSheet.RESOLUTION;
        MyBorder myBorder = borderPanel.getMyBorder();
        widthField.setText(Integer.toString(Math.round(scale*msWidth)+myBorder.getWidth()));
        int height = msHeight;
        if(withoutLyricsCheck.isSelected())height-=msHeightWL;
        if(exportWithoutTitleCheckBox.isSelected())height-=msHeightWT;
        heightField.setText(Integer.toString(Math.round(scale*height)+myBorder.getHeight()));
    }
}
