package songscribe.ui;

import java.io.File;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;


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
        this.frame.handleQuit();
        event.setHandled(true);
    }

    public void handleOpenFile(ApplicationEvent event) {
        this.frame.handleOpenFile(new File(event.getFilename()));
        event.setHandled(true);
    }

    public void handlePrintFile(ApplicationEvent event) {
        this.frame.handlePrintFile(new File(event.getFilename()));
        event.setHandled(true);
    }
}
