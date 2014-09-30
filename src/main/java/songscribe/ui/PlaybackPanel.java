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

    Created on 2006.01.13.
*/
package songscribe.ui;

import songscribe.data.PropertyChangeListener;
import songscribe.ui.playsubmenu.InstrumentDialog;
import songscribe.ui.playsubmenu.PlaybackListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author Csaba Kávai
 */
public class PlaybackPanel extends JPanel implements PropertyChangeListener, PlaybackListener {
    private MainFrame mainFrame;
    private JComboBox instrumentCombo;
    private JCheckBox withRepeatCheckBox;
    private JSlider tempoChangeSlider = tempoChangeSliderFactory();
    private JButton playPauseButton;

    public PlaybackPanel(MainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Playback"));
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        instrumentCombo = new JComboBox(InstrumentDialog.INSTRUMENT_STRING);
        instrumentCombo.setToolTipText("Instrument");
        center.add(instrumentCombo);
        withRepeatCheckBox = new JCheckBox(mainFrame.getPlayMenu().getWithRepeatAction());
        withRepeatCheckBox.setIcon(null);
        center.add(withRepeatCheckBox);
        center.add(tempoChangeSlider);
        add(center);

        MusicComponentsAction musicComponentsAction = new MusicComponentsAction();
        instrumentCombo.addActionListener(musicComponentsAction);
        instrumentCombo.addPopupMenuListener(musicComponentsAction);
        tempoChangeSlider.addChangeListener(musicComponentsAction);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        playPauseButton = new JButton(mainFrame.getPlayMenu().getPlayAction());
        JButton stopButton = new JButton(mainFrame.getPlayMenu().getStopAction());
        south.add(playPauseButton);
        south.add(stopButton);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(south);

        mainFrame.getMusicSheet().addFocusLostExceptions(instrumentCombo);
        mainFrame.addProperyChangeListener(this);
        mainFrame.getPlayMenu().addPlaybackListener(this);
    }

    static JSlider tempoChangeSliderFactory() {
        JSlider s = new JSlider(20, 180);
        s.setMajorTickSpacing(40);
        s.setMinorTickSpacing(20);
        s.setSnapToTicks(true);
        s.setPaintLabels(true);
        s.setPaintTicks(true);
        s.setToolTipText("Tempo change");
        Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(20, new JLabel("Slower"));
        labels.put(100, new JLabel("Tempo"));
        labels.put(180, new JLabel("Faster"));
        s.setLabelTable(labels);

        return s;
    }

    public void musicChanged(Properties props) {
        if (MainFrame.sequencer != null) {
            instrumentCombo.setSelectedIndex(Integer.parseInt(props.getProperty(Constants.INSTRUMENT_PROP)));
        }

        withRepeatCheckBox.setSelected(props.getProperty(Constants.WITH_REPEAT_PROP).equals(Constants.TRUE_VALUE));
        tempoChangeSlider.setValue(Integer.parseInt(props.getProperty(Constants.TEMPO_CHANGE_PROP)));
    }

    public void enableNotActionComponents(boolean enabled) {
        playPauseButton.setAction(enabled ? mainFrame.getPlayMenu().getPlayAction() : mainFrame.getPlayMenu().getPauseAction());
        instrumentCombo.setEnabled(enabled);
        tempoChangeSlider.setEnabled(enabled);
    }

    private class MusicComponentsAction implements ActionListener, ChangeListener, PopupMenuListener {
        public void actionPerformed(ActionEvent e) {
            mainFrame.getProperties().setProperty(Constants.INSTRUMENT_PROP, Integer.toString(instrumentCombo.getSelectedIndex()));
            mainFrame.fireMusicChanged(PlaybackPanel.this);
        }

        public void stateChanged(ChangeEvent e) {
            mainFrame.getProperties().setProperty(Constants.TEMPO_CHANGE_PROP, Integer.toString(tempoChangeSlider.getValue()));
            mainFrame.fireMusicChanged(PlaybackPanel.this);
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            mainFrame.getMusicSheet().requestFocusInWindow();
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

}
