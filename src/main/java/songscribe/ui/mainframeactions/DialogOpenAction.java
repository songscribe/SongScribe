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

    Created on Aug 6, 2006
*/
package songscribe.ui.mainframeactions;

import org.apache.log4j.Logger;
import songscribe.ui.MainFrame;
import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class DialogOpenAction extends AbstractAction {
    private static Logger logger = Logger.getLogger(DialogOpenAction.class);

    private MyDialog dialog;
    private Class dialogClass;
    private MainFrame mainFrame;

    public DialogOpenAction(MainFrame mainFrame, String name, Class dialogClass) {
        this(mainFrame, name, (Icon) null, null, dialogClass);
    }

    public DialogOpenAction(MainFrame mainFrame, String name, String icon, Class dialogClass) {
        this(mainFrame, name, icon, null, dialogClass);
    }

    public DialogOpenAction(MainFrame mainFrame, String name, Icon icon, Class dialogClass) {
        this(mainFrame, name, icon, null, dialogClass);
    }

    public DialogOpenAction(MainFrame mainFrame, String name, String icon, KeyStroke acceleratorKey, Class dialogClass) {
        this(mainFrame, name,
                icon != null ? new ImageIcon(MainFrame.getImage(icon)) : null, acceleratorKey, dialogClass);
    }

    public DialogOpenAction(MainFrame mainFrame, String name, Icon icon, KeyStroke acceleratorKey, Class dialogClass) {
        this.dialogClass = dialogClass;
        this.mainFrame = mainFrame;
        putValue(Action.NAME, name);

        if (icon != null) {
            putValue(Action.SMALL_ICON, icon);
        }

        if (acceleratorKey != null) {
            putValue(Action.ACCELERATOR_KEY, acceleratorKey);
        }
    }

    public void actionPerformed(ActionEvent e) {
        getDialog().setVisible(true);
    }

    public MyDialog getDialog() {
        if (dialog == null) {
            try {
                dialog = (MyDialog) dialogClass.getConstructor(MainFrame.class).newInstance(mainFrame);
            }
            catch (Exception e1) {
                logger.error("DialogOpenAction", e1);
            }
        }

        return dialog;
    }
}
