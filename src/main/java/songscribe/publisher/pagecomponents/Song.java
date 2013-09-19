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

Created on Sep 23, 2006
*/
package songscribe.publisher.pagecomponents;

import songscribe.publisher.Publisher;
import songscribe.publisher.SongDialog;
import songscribe.publisher.publisheractions.SongEditAction;
import songscribe.ui.MusicSheet;
import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class Song extends PageComponent{
    private static SongDialog songDialog;
    private static JPopupMenu popupMenu;
    private File songFile;
    MusicSheet musicSheet;

    public Song(MusicSheet musicSheet, int xPos, int yPos, double resolution, File songFile) {
        super(new Rectangle(xPos, yPos, musicSheet.getSheetWidth(), musicSheet.getSheetHeight()), resolution);
        this.musicSheet = musicSheet;
        this.songFile = songFile;
    }

    public void paintComponent(Graphics2D g2){
        AffineTransform at = g2.getTransform();
        g2.translate(pos.x, pos.y);
        musicSheet.getBestDrawer().drawMusicSheet(g2, false, resolution);
        g2.setTransform(at);
    }

    public void reload(Publisher publisher){
        musicSheet = publisher.openMusicSheet(songFile);
        pos.width = (int)Math.round(musicSheet.getSheetWidth()*resolution);
        pos.height = (int)Math.round(musicSheet.getSheetHeight()*resolution);
    }

    public MusicSheet getMusicSheet() {
        return musicSheet;
    }

    public File getSongFile() {
        return songFile;
    }

    public void setSongFile(File songFile) {
        this.songFile = songFile;
    }

    public MyDialog getPropertiesDialog(Publisher publisher) {
        if(songDialog==null)songDialog = new SongDialog(publisher);
        return songDialog;
    }

    public JPopupMenu getPopupMenu(Publisher publisher) {
        if(popupMenu==null){
            popupMenu = new JPopupMenu("Song");
            popupMenu.add(new SongEditAction(publisher));
            addCommonPopups(publisher, popupMenu);
        }
        return popupMenu;
    }
}
