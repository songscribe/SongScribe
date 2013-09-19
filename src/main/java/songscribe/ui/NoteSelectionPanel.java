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

Created on 2005.01.19., 8:34:36
*/

package songscribe.ui;

import songscribe.music.Crotchet;
import songscribe.music.Note;
import songscribe.music.NoteType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Csaba Kávai
 *
 */
public final class NoteSelectionPanel extends SelectionPanel{
    private JToggleButton dotButton = new JToggleButton();
    private JToggleButton naturalButton = new JToggleButton();
    private JToggleButton flatButton = new JToggleButton();
    private JToggleButton accentButton = new JToggleButton();

    public NoteSelectionPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.mainFrame = mainFrame;

        for (NoteType nt : NoteType.values()) {
            if(nt.isNote() && nt!=NoteType.GRACESEMIQUAVER){
                JToggleButton noteTypeButton = new JToggleButton();
                noteTypeButton.setToolTipText(nt.getCompoundName());
                noteTypeButton.setIcon(new ImageIcon(Note.clipNoteImage(nt.getInstance().getUpImage(), nt.getInstance().getRealUpNoteRect(), Color.yellow, SELECTIONIMAGEDIM)));
                noteTypeButton.addActionListener(this);
                noteTypeButton.setActionCommand(nt.name());
                addSelectionComponent(noteTypeButton);
                selectionGroup.add(noteTypeButton);
            }
        }

        //adding gracesemiquaver
        JToggleButton graceSemiQuaverButton = new JToggleButton();
        graceSemiQuaverButton.setToolTipText(NoteType.GRACESEMIQUAVER.getCompoundName());
        graceSemiQuaverButton.setIcon(new ImageIcon(MainFrame.getImage("graceSemiQuaver.gif")));
        graceSemiQuaverButton.addActionListener(this);
        graceSemiQuaverButton.setActionCommand(NoteType.GRACESEMIQUAVEREDITSTEP1.name());
        addSelectionComponent(graceSemiQuaverButton);
        selectionGroup.add(graceSemiQuaverButton);

        //making the glissando button
        JToggleButton glissandoButton = new JToggleButton();
        glissandoButton.setToolTipText(NoteType.GLISSANDO.getCompoundName());
        glissandoButton.setIcon(new ImageIcon(Note.clipNoteImage(MainFrame.getImage("glissando.gif"), new Rectangle(new Point(0,0), SELECTIONIMAGEDIM), Color.yellow, SELECTIONIMAGEDIM)));
        glissandoButton.addActionListener(this);
        glissandoButton.setActionCommand(NoteType.GLISSANDO.name());
        addSelectionComponent(glissandoButton);
        selectionGroup.add(glissandoButton);

        //making the dot button
        BufferedImage dotImage = new BufferedImage(SELECTIONIMAGEDIM.width, SELECTIONIMAGEDIM.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = dotImage.createGraphics();
        g2.drawImage(Note.clipNoteImage(Crotchet.UPIMAGE, Crotchet.REALUPNOTERECT, Color.orange, SELECTIONIMAGEDIM), 0, 0, null);
        g2.drawImage(Note.DOTIMAGE, (dotImage.getWidth()-Crotchet.REALUPNOTERECT.width)/2, (dotImage.getHeight()-Crotchet.REALUPNOTERECT.height)/2, null);
        g2.dispose();
        dotButton.setIcon(new ImageIcon(dotImage));
        dotButton.setToolTipText("Dot (.)");
        dotButton.setActionCommand("DOT");
        dotButton.addActionListener(this);
        addSeparator();
        addSelectionComponent(dotButton);

        //making the accidental buttons
        naturalButton.setToolTipText("Natural (N)");
        naturalButton.setIcon(new ImageIcon(Note.clipNoteImage(MainFrame.getImage("natural.gif"), Note.REALNATURALFLATSHARPRECT[0], Color.orange, SELECTIONIMAGEDIM)));
        naturalButton.addActionListener(this);
        naturalButton.setActionCommand(Note.Accidental.NATURAL.name());
        addSelectionComponent(naturalButton);
        flatButton.setToolTipText("Flat (F)");
        flatButton.setIcon(new ImageIcon(Note.clipNoteImage(MainFrame.getImage("flat.gif"), Note.REALNATURALFLATSHARPRECT[1], Color.orange, SELECTIONIMAGEDIM)));
        flatButton.addActionListener(this);
        flatButton.setActionCommand(Note.Accidental.FLAT.name());
        addSelectionComponent(flatButton);

        //making the volume button
        Dimension vid = new Dimension(14, 8);
        BufferedImage louderImage = new BufferedImage(vid.width, vid.height, BufferedImage.TYPE_INT_RGB);
        g2 = louderImage.createGraphics();
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, vid.width, vid.height);
        g2.setPaint(Color.black);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawLine(0, 0, vid.width-1, vid.height/2-1);
        g2.drawLine(0, vid.height-1, vid.width-1, vid.height/2-1);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.dispose();
        accentButton.setIcon(new ImageIcon(Note.clipNoteImage(louderImage, new Rectangle(vid), Color.orange, SELECTIONIMAGEDIM)));
        accentButton.setToolTipText("Accent (>)");
        accentButton.setActionCommand("ACCENT");
        accentButton.addActionListener(this);
        addSelectionComponent(accentButton);

        ((AbstractButton)getComponent(2)).setSelected(true);
    }

    public void setDotSelected(boolean selected) {
        dotButton.setSelected(selected);
    }

    public void setAccentSelected(boolean selected) {
        accentButton.setSelected(selected);
    }

    public void setNaturalSelected(boolean selected) {
        naturalButton.setSelected(selected);
    }

    public void setFlatSelected(boolean selected) {
        flatButton.setSelected(selected);
    }
}
