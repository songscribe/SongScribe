package songscribe.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public abstract class AbstractTextFocusRejectingAction extends AbstractAction {
    private final MainFrame mainFrame;

    protected AbstractTextFocusRejectingAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public final void actionPerformed(ActionEvent e) {
        if (!(mainFrame.getFocusOwner() instanceof JTextComponent)) {
            // we don't want to receive actions when the focus is on text panels
            doActionPerformed(e);
        }   
    }
    
    public abstract void doActionPerformed(ActionEvent e); 
}
