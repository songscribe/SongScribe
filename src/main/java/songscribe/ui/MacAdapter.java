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
*/
package songscribe.ui;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import java.io.File;


/**
 * @author Aparajita Fishman
 */

public class MacAdapter extends ApplicationAdapter {

    private MainFrame frame;

    public MacAdapter(MainFrame frame) {
        this.frame = frame;
    }


    // MacAdapter methods

    public static void attachTo(MainFrame frame, boolean hasPrefs) {
        Application app = Application.getApplication();
        app.setEnabledAboutMenu(true);

        if (hasPrefs)
            app.setEnabledPreferencesMenu(true);
        else
            app.removePreferencesMenuItem();

        app.addApplicationListener(new MacAdapter(frame));
    }

    public void handleAbout(ApplicationEvent event) {
        this.frame.handleAbout();
        event.setHandled(true);
    }

    public void handlePreferences(ApplicationEvent event) {
        this.frame.handlePrefs();
        event.setHandled(true);
    }

    public void handleQuit(ApplicationEvent event) {
        boolean quit = this.frame.handleQuit();

        // Documentation says to set isHandled to false to reject the quit
        event.setHandled(quit);
    }

    public void handleOpenFile(ApplicationEvent event) {
        this.frame.handleOpenFile(new File(event.getFilename()));
    }

    public void handlePrintFile(ApplicationEvent event) {
        this.frame.handlePrintFile(new File(event.getFilename()));
    }
}
