package songscribe.uiconverter;

import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ChooseDirectoryAction extends AbstractAction{
    private PlatformFileDialog pfd;

    public ChooseDirectoryAction(UIConverter uiConverter) {
        putValue(NAME, "Choose");
        putValue(Action.SMALL_ICON, new ImageIcon(UIConverter.getImage("fileopen.png")));
        pfd = new PlatformFileDialog(uiConverter, "Open folder", true, new MyAcceptFilter("Folders"), true);
    }

    public void actionPerformed(ActionEvent e) {
        if(pfd.showDialog()) {
            firePropertyChange("directorychange", null, pfd.getFile());
        }
    }
}
