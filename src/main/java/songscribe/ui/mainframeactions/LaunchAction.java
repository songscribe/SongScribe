package songscribe.ui.mainframeactions;

import org.apache.log4j.Logger;
import songscribe.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaunchAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(LaunchAction.class);

    public enum App {
        SONGBOOK("Song Book", "sbicon.png", "sb"),
        SONGSHOW("Song Show", "ssicon.png", "ss");
        private final String name;
        private final String icon;
        private final String command;

        App(String name, String icon, String command) {
            this.name = name;
            this.icon = icon;
            this.command = command;
        }
    }

    private final App app;

    public LaunchAction(App app) {
        this.app = app;
        putValue(Action.NAME, app.name);
        if (app.icon != null) {
            putValue(Action.SMALL_ICON, new ImageIcon(MainFrame.getImage(app.icon).getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {

            final ProcessHandle.Info currentProcessInfo = ProcessHandle.current().info();
            List<String> commandList = new ArrayList<>();
            commandList.add(currentProcessInfo.command().get());
            commandList.addAll(Arrays.asList(currentProcessInfo.arguments().get()));
            commandList.add(app.command);
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.start();
        } catch (Exception ex) {
            logger.error("Could not start another process", ex);
        }
    }
}
