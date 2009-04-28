package songscribe.ui.insertsubmenu;

import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FermataMenuItem extends JCheckBoxMenuItem implements ActionListener{
    private MainFrame mainFrame;

    public FermataMenuItem(MainFrame mainFrame) {
        super("Fermata", new ImageIcon(MainFrame.getImage("fermata22.png")));
        this.mainFrame = mainFrame;
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        mainFrame.getMusicSheet().getActiveNote().setFermata(isSelected());
        mainFrame.getMusicSheet().repaint();
    }
}
