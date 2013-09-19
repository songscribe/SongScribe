/* 
SongScribe song notation program
Copyright (C) 2006-2007 Csaba Kavai

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

Created on: 2006.03.18.
*/
package songscribe.ui;

import org.apache.log4j.Logger;
import songscribe.data.MyDesktop;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Csaba Kávai
 */
public class AboutDialog extends MyDialog{
    private static Logger logger = Logger.getLogger(AboutDialog.class);
    public static final String WEB = "http://www.songscribe.org";
    public static final String LICENSE = "GPL (General Public License)";

    public AboutDialog(MainFrame mainFrame) {
        super(mainFrame, "About");

        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("About", makeAboutPanel());
        try {
            tabPane.add("Read me", createTextPane("file:readme.html"));
        } catch (IOException e) {
            logger.error("Could not load readme file.", e);
        }
        try {
            tabPane.add("License agreement", createTextPane("file:license.txt"));
        } catch (IOException e) {
            logger.error("Could not load license file.", e);
        }
        try {
            tabPane.add("Acknowledgements", createTextPane("file:help/Acknowledgements.html"));
        } catch (IOException e) {
            logger.error("Could not load acknowledgement file.", e);
        }

        JPanel southPanel = new JPanel();
        southPanel.add(okButton);
        dialogPanel.add(BorderLayout.CENTER, tabPane);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    private Component createTextPane(String url) throws IOException{
        JTextPane textPane = new JTextPane();
        textPane.setPage(url);
        textPane.setEditable(false);
        JScrollPane textScroll = new JScrollPane(textPane);
        textScroll.setPreferredSize(new Dimension(400, 200));
        return textScroll;
    }

    private JPanel makeAboutPanel() {
        javax.swing.JLabel copyRight1;
        javax.swing.JLabel copyRight2;
        javax.swing.JLabel copyrightLabel;
        javax.swing.JLabel email;
        javax.swing.JLabel emailLabel;
        javax.swing.JLabel iconLabel;
        javax.swing.JLabel license;
        javax.swing.JLabel licenseLabel;
        javax.swing.JLabel progNameLabel;
        javax.swing.JLabel version;
        javax.swing.JLabel versionLabel;
        javax.swing.JLabel web;
        javax.swing.JLabel webLabel;

        iconLabel = new javax.swing.JLabel();
        progNameLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        version = new javax.swing.JLabel();
        copyrightLabel = new javax.swing.JLabel();
        copyRight1 = new javax.swing.JLabel();
        copyRight2 = new javax.swing.JLabel();
        licenseLabel = new javax.swing.JLabel();
        license = new javax.swing.JLabel();
        webLabel = new javax.swing.JLabel();
        web = new javax.swing.JLabel();
        emailLabel = new javax.swing.JLabel();
        email = new javax.swing.JLabel();

        iconLabel.setIcon(new javax.swing.ImageIcon(mainFrame.getIconImage()));

        progNameLabel.setFont(new java.awt.Font("Arial", 0, 30));
        progNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        progNameLabel.setText(mainFrame.PROGNAME);

        versionLabel.setFont(new java.awt.Font("Arial", 1, 14));
        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        versionLabel.setText("Version:");

        version.setFont(new java.awt.Font("Arial", 0, 14));
        version.setText(Utilities.getPublicVersion());

        copyrightLabel.setFont(new java.awt.Font("Arial", 1, 14));
        copyrightLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        copyrightLabel.setText("Copyright:");

        copyRight1.setFont(new java.awt.Font("Arial", 0, 14));
        copyRight1.setText("\u00a9 2006-" + new GregorianCalendar().get(Calendar.YEAR) + " Csaba Kavai");

        copyRight2.setFont(new java.awt.Font("Arial", 0, 14));
        copyRight2.setText("All rights reserved.");

        licenseLabel.setFont(new java.awt.Font("Arial", 1, 14));
        licenseLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        licenseLabel.setText("License:");

        license.setFont(new java.awt.Font("Arial", 0, 14));
        license.setText(LICENSE);

        webLabel.setFont(new java.awt.Font("Arial", 1, 14));
        webLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        webLabel.setText("Web:");

        web.setFont(new java.awt.Font("Arial", 0, 14));
        web.setText(WEB);
        if(MyDesktop.isDesktopSupported()){
            web.setForeground(new java.awt.Color(0, 0, 204));
            web.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    webMouseClicked();
                }
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    webMouseEntered();
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    webMouseExited();
                }
            });
        }

        emailLabel.setFont(new java.awt.Font("Arial", 1, 14));
        emailLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        emailLabel.setText("E-mail:");

        email.setFont(new java.awt.Font("Arial", 0, 12));
        email.setText(ReportBugDialog.BUGEMAIL);
        if(MyDesktop.isDesktopSupported()){
            email.setForeground(new java.awt.Color(0, 0, 204));
            email.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    emailMouseClicked();
                }
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    webMouseEntered();
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    webMouseExited();
                }
            });
        }

        JPanel aboutPanel = new JPanel();
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(aboutPanel);
        aboutPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(iconLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(versionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(copyrightLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .add(licenseLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(webLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(emailLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(version, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                            .add(copyRight1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                            .add(license, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                            .add(copyRight2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                            .add(web, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                            .add(email, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)))
                    .add(progNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(iconLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(progNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(versionLabel)
                            .add(version))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(copyrightLabel)
                            .add(copyRight1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(copyRight2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(licenseLabel)
                            .add(license))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(webLabel)
                            .add(web))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(emailLabel)
                            .add(email))))
                .addContainerGap())
        );
        return aboutPanel;
    }


    private void webMouseExited() {
        dialogPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void webMouseEntered() {
        dialogPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void webMouseClicked() {
        Utilities.openWebPage(mainFrame, WEB);
    }

    private void emailMouseClicked() {
        Utilities.openEmail(mainFrame,ReportBugDialog.BUGEMAIL+"?SUBJECT=SongScribe comment");
    }

    protected void getData() {
    }

    protected void setData() {
    }
}
