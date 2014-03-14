/*  
Music of The Supreme song notation program
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

Created on Sep 15, 2007
*/
package songscribe.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Csaba Kávai
 */
public class NotesMenu extends JMenu {
    public static final String SEPARATOR = "|";
    private MainFrame mainFrame;
    private JMenuItem[] buttons;
    
    private class NotesMenuItem extends JMenuItem {
        private NotesMenuItem(String name, String image, ActionListener actionListener) {
            super(name, new ImageIcon(MainFrame.getImage(image)));
            addActionListener(actionListener);
        }
    }

    public NotesMenu(MainFrame mainFrame) {
        super("Notes");
        this.mainFrame = mainFrame;
        final MusicSheet musicSheet = mainFrame.getMusicSheet();

        add(new NotesMenuItem("Beam", "beam_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.beamSelectedNotes(true);
            }
        }));
        add(new NotesMenuItem("Unbeam", "unbeam_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.beamSelectedNotes(false);
            }
        }));
        
        addSeparator();
        add(new NotesMenuItem("Triplet", "triplet_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.tupletSelectedNotes(3);
            }
        }));
        add(createTupletMenu());
        add(new NotesMenuItem("Untuplet", "untriplet_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.untupletSelectedNotes();
            }
        }));
        addSeparator();
        
        add(new NotesMenuItem("Tie", "tie_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.tieSelectedNotes(true);
            }
        }));
        add(new NotesMenuItem("Untie", "untie_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.tieSelectedNotes(false);
            }
        }));
        addSeparator();

        add(new NotesMenuItem("Add Slur", "addslur_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.slurSelectedNotes(true);
            }
        }));
        add(new NotesMenuItem("Remove Slur", "blank.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.slurSelectedNotes(false);
            }
        }));
        addSeparator();

        add(new NotesMenuItem("First-second ending", "fsending_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.makeFsEndingOnSelectedNotes(true);
            }
        }));
        add(new NotesMenuItem("Remove first-second ending", "unfsending_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.makeFsEndingOnSelectedNotes(false);
            }
        }));
        addSeparator();

        add(new NotesMenuItem("Trill", "trill_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.makeTrillOnSelectedNotes(true);
            }
        }));
        add(new NotesMenuItem("Remove Trill", "blank.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.makeTrillOnSelectedNotes(false);
            }
        }));
        addSeparator();
        add(new NotesMenuItem("Invert Stem Direction", "upsidedown.gif", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.invertStemDirectionOnSelectedNotes();
            }
        }));
        add(new NotesMenuItem("Allow / Disallow Lyrics Under Rest", "lyricsunderrest_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.invertLyricsUnderRests();
            }
        }));
        add(new NotesMenuItem("Invert orientation of fractional beams", "invertfractionalbeam_menu.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicSheet.invertFractionBeamOrientation();
            }
        }));
    }

    private JMenu createTupletMenu() {
        String[] tuplets = {"Duplet (2)", "Quadruplet (4)", "Quintuplet (5)", "Sextuplet (6)", "Septuplet (7)"};
        final int[] tupletValues = new int[]{2, 4, 5, 6, 7};
        JMenu tupletMenu = new JMenu("Other tuplets");
        tupletMenu.setIcon(mainFrame.blankIcon);
        for(int i=0;i<tuplets.length;i++){
            final int iFinal = i;
            JMenuItem tupletButton = new JMenuItem(tuplets[i], mainFrame.blankIcon);
            tupletButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mainFrame.getMusicSheet().tupletSelectedNotes(tupletValues[iFinal]);
                }
            });
            tupletMenu.add(tupletButton);
        }
        return tupletMenu;
    }
}
