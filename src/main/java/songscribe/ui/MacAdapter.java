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

import com.apple.eawt.*;

import java.io.File;
import java.util.List;


/**
 * @author Aparajita Fishman
 */

public class MacAdapter {

    private MainFrame frame;

    public MacAdapter(MainFrame frame, boolean hasPrefs) {
        this.frame = frame;
        Application app = Application.getApplication();
        app.setAboutHandler(new MacAboutHandler());

        if (hasPrefs) {
            app.setPreferencesHandler(new MacPreferencesHandler());
        }

        app.setOpenFileHandler(new MacOpenFilesHandler());
        app.setPrintFileHandler(new MacPrintFilesHanler());
        app.setQuitHandler(new MacQuitHandler());
    }

    private class MacAboutHandler implements AboutHandler {
        public void handleAbout(AppEvent.AboutEvent event) {
            frame.handleAbout();
        }
    }

    private class MacPreferencesHandler implements PreferencesHandler {
        public void handlePreferences(AppEvent.PreferencesEvent event) {
            frame.handlePrefs();
        }
    }

    private class MacQuitHandler implements QuitHandler {
        public void handleQuitRequestWith(AppEvent.QuitEvent event, QuitResponse response) {
            boolean shouldQuit = frame.handleQuit();

            if (shouldQuit) {
                response.performQuit();
            }
            else {
                response.cancelQuit();
            }
        }
    }

    private class MacOpenFilesHandler implements OpenFilesHandler {
        public void openFiles(AppEvent.OpenFilesEvent event) {
            List<File> files = event.getFiles();
            frame.handleOpenFile(files.get(0));
        }
    }

    private class MacPrintFilesHanler implements PrintFilesHandler {
        public void printFiles(AppEvent.PrintFilesEvent event) {
            List<File> files = event.getFiles();
            frame.handlePrintFile(files.get(0));
        }
    }
}
