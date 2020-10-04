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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import songscribe.Version;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class UpdateDialog extends MyDialog {
    private static final Logger logger = Logger.getLogger(UpdateDialog.class);

    public UpdateDialog(MainFrame mainFrame) {
        super(mainFrame, "Update", true);
        southPanel.remove(applyButton);
        southPanel.remove(okButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
        JPanel center = new JPanel();
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Checking for updates...");
        label.setAlignmentX(0f);
        center.add(label);
        center.add(Box.createVerticalStrut(10));
        dialogPanel.add(BorderLayout.CENTER, center);
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

            try {
                GetMethod versionGetMethod = new GetMethod(Constants.VERSION_URL);
                versionGetMethod.addRequestHeader(Constants.MAX_AGE_HEADER);
                httpClient.executeMethod(versionGetMethod);
                if (versionGetMethod.getStatusCode() != 200) {
                    throw new IOException("Invalid status code: " + versionGetMethod.getStatusCode());
                }
                String remoteVersion = versionGetMethod.getResponseBodyAsString();

                if (remoteVersion != null) {
                    remoteVersion = remoteVersion.trim();
                    String localVersion = Version.BUILD_VERSION;
                    System.out.format("remote version: %s; local version: %s", remoteVersion, localVersion);

                    if (remoteVersion.compareTo(localVersion) <= 0) {
                        versionGetMethod.releaseConnection();

                        if (!automatic) {
                            JOptionPane.showMessageDialog(dialogPanel, "No update is available. You already have the lastest version.", mainFrame.PROG_NAME, JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialogPanel,
                            "New version available! Please go to https://songscribe.himadri.eu to download the latest version", mainFrame.PROG_NAME, JOptionPane.INFORMATION_MESSAGE);
                    }
                    if (!automatic) {
                        setVisible(false);
                    }
                }

                versionGetMethod.releaseConnection();
            } catch (IOException e) {
                logger.warn("Update failure", e);
                if (!automatic) {
                    JOptionPane.showMessageDialog(dialogPanel,
                        "Could not connect to the download site. Visit the SongScribe website and download the latest version.", mainFrame.PROG_NAME, JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                }
            }
        }
    }
}
