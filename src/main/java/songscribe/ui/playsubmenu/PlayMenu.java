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

Created on Oct 8, 2006
*/
package songscribe.ui.playsubmenu;

import songscribe.data.PropertyChangeListener;
import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.mainframeactions.DialogOpenAction;

import javax.swing.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * @author Csaba Kávai
 */
public class PlayMenu extends JMenu implements PropertyChangeListener{
    private MainFrame mainFrame;

    DisableComponentsThread disableComponentsThread = new DisableComponentsThread(this, 0);

    private PlayAction playAction = new PlayAction(this);
    private PauseAction pauseAction = new PauseAction(this);
    private StopAction stopAction = new StopAction(this);
    private WithRepeatAction withRepeatAction = new WithRepeatAction(this);

    private JMenuItem playPauseMenu;
    private JMenuItem instrumentMenu;
    private JCheckBoxMenuItem withRepeatCheck = new JCheckBoxMenuItem(withRepeatAction);
    private JCheckBoxMenuItem playContinuouslyCheck = new JCheckBoxMenuItem(new PlayContinuously(this));
    private ButtonGroup tempoChangeGroup = new ButtonGroup();
    private JMenu tempoChangeMenu;

    private Vector<PlaybackListener> playbackListeners = new Vector<PlaybackListener>(3, 2);

    public PlayMenu(MainFrame mainFrame) {
        super("Play");
        this.mainFrame = mainFrame;
        playPauseMenu = new JMenuItem(playAction);
        add(playPauseMenu);
        add(new JMenuItem(stopAction));
        addSeparator();
        add(withRepeatCheck);
        add(playContinuouslyCheck);
        tempoChangeMenu = new JMenu("Tempo Change");
        tempoChangeMenu.setIcon(mainFrame.blankIcon);
        for(int i=20;i<=180;i+=20){
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(new TempoChangeAction(this, i));
            tempoChangeMenu.add(mi);
            tempoChangeGroup.add(mi);
        }
        add(tempoChangeMenu);
        instrumentMenu = new JMenuItem(new DialogOpenAction(mainFrame, "Instruments...", "instrument.png", InstrumentDialog.class));
        add(instrumentMenu);
        mainFrame.addProperyChangeListener(this);
    }

    public void addPlaybackListener(PlaybackListener pl){
        playbackListeners.add(pl);
    }

    public void removePlaybackListener(PlaybackListener pl){
        playbackListeners.remove(pl);
    }

    public void enableAllComponents(boolean enabled){
        instrumentMenu.setEnabled(enabled);
        withRepeatAction.setEnabled(enabled);
        playContinuouslyCheck.setEnabled(enabled);
        tempoChangeMenu.setEnabled(enabled);
        playPauseMenu.setAction(enabled ? playAction : pauseAction);
        for(PlaybackListener pl:playbackListeners){
            pl.enableNotActionComponents(enabled);
        }
    }

    public void musicChanged(Properties props) {
        withRepeatCheck.setSelected(props.getProperty(Constants.WITHREPEATPROP).equals(Constants.TRUEVALUE));
        playContinuouslyCheck.setSelected(props.getProperty(Constants.PLAYCONTINUOUSLYPROP).equals(Constants.TRUEVALUE));
        int tempoChange = Integer.parseInt(props.getProperty(Constants.TEMPOCHANGEPROP));
        for(Enumeration<AbstractButton> enab=tempoChangeGroup.getElements();enab.hasMoreElements();){
            AbstractButton ab = enab.nextElement();
            if(((TempoChangeAction)ab.getAction()).getRatio()==tempoChange){
                tempoChangeGroup.setSelected(ab.getModel(), true);
            }
        }
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public Action getPlayAction() {
        return playAction;
    }

    public Action getPauseAction() {
        return pauseAction;
    }

    public Action getStopAction() {
        return stopAction;
    }

    public Action getWithRepeatAction() {
        return withRepeatAction;
    }
}
