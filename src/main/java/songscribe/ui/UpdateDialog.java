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

    Created on: Dec 24, 2006
*/
package songscribe.ui;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import songscribe.Version;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Csaba Kávai
 */
public class UpdateDialog extends MyDialog {
    private static final Logger logger = Logger.getLogger(UpdateDialog.class);
    private final JLabel label = new JLabel("Checking for updates...");
    private String downloadUri;
    private final JButton downloadButton = new JButton("Download", new ImageIcon(MainFrame.getImage("download_manager16.png")));

    public UpdateDialog(MainFrame mainFrame) {
        super(mainFrame, "Update", true);
        southPanel.remove(applyButton);
        southPanel.remove(okButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
        JPanel center = new JPanel();
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setPreferredSize(new Dimension(500, 70));
        label.setAlignmentX(0.5f);
        center.add(label);
        center.add(Box.createVerticalStrut(10));
        dialogPanel.add(BorderLayout.CENTER, center);
        southPanel.add(downloadButton, 0);
        downloadButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(downloadUri));
                setVisible(false);
            } catch (IOException | URISyntaxException ex) {
                label.setText(label.getText() + " Navigate to: " + downloadUri);
            }
        });
        downloadButton.setVisible(false);
    }


    protected void getData() {
        new UpdateInternetThread(false).start();
    }

    protected void setData() {
    }

    public class UpdateInternetThread extends Thread {
        private boolean automatic;

        public UpdateInternetThread(boolean automatic) {
            this.automatic = automatic;
        }

        public void run() {

            HttpClient httpClient = new HttpClient();
            String platform;
            if (Utilities.isMac()) {
                platform = "mac";
            } else if (Utilities.isWindows()) {
                platform = "windows";
            } else if (Utilities.isLinux()) {
                platform = "linux";
            } else {
                platform = "";
            }
            GetMethod versionGetMethod = new GetMethod(Constants.VERSION_URL+platform);
            try {
                versionGetMethod.addRequestHeader(Constants.MAX_AGE_HEADER);
                httpClient.executeMethod(versionGetMethod);
                if (versionGetMethod.getStatusCode() != 200) {
                    throw new IOException("Invalid status code: " + versionGetMethod.getStatusCode());
                }
                String remoteVersionJson = versionGetMethod.getResponseBodyAsString();

                if (remoteVersionJson != null) {
                    Gson gson = new Gson();
                    RemoteVersion remoteVersion = gson.fromJson(remoteVersionJson, RemoteVersion.class);

                    if (remoteVersion.currentVersion.compareTo(Version.BUILD_VERSION) <= 0) {
                        label.setText("No update is available. You already have the latest version.");
                        pack();
                    } else {
                        label.setText("New version available!");
                        downloadUri = remoteVersion.downloadUrl;
                        downloadButton.setVisible(true);
                        if (automatic) {
                            setVisible(true);
                        }
                    }
                }

            } catch (IOException | RuntimeException e) {
                logger.warn("Update failure", e);
                label.setText("Could not connect to the download site. Visit the SongScribe website and download the latest version.");
            } finally {
                versionGetMethod.releaseConnection();
            }
        }
    }

    public static class RemoteVersion {
        private String currentVersion;
        private String downloadUrl;
    }
}
