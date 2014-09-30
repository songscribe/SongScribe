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
import songscribe.ChecksumMaker;
import songscribe.Version;
import songscribe.data.CannotUpdateException;
import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author Csaba Kávai
 */
public class UpdateDialog extends MyDialog {
    private static final Logger logger = Logger.getLogger(UpdateDialog.class);
    private static final String VERSION_FILE = "version";
    DecimalFormat df = new DecimalFormat("0.##");
    private JComboBox updateMode = new JComboBox(new String[] { "Update from the Internet", "Have an update file" });
    private boolean downloadCancelled;

    public UpdateDialog(MainFrame mainFrame) {
        super(mainFrame, "Update", true);
        southPanel.remove(applyButton);
        okButton.setText("Update");
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
        JPanel center = new JPanel();
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("How do you want to update " + MainFrame.PACKAGE_NAME + "?");
        label.setAlignmentX(0f);
        center.add(label);
        center.add(Box.createVerticalStrut(10));
        updateMode.setAlignmentX(0f);
        center.add(updateMode);
        dialogPanel.add(BorderLayout.CENTER, center);
    }


    protected void getData() {
    }

    protected void setData() {
        if (updateMode.getSelectedIndex() == 0) {
            new UpdateInternetThread(false).start();
        }
        else {
            PlatformFileDialog pfd = new PlatformFileDialog(mainFrame, "Open", true, new MyAcceptFilter("SongScribe Update files", "msu"));

            if (pfd.showDialog()) {
                byte buf[] = new byte[1024];

                try {
                    ZipFile zipFile = new ZipFile(pfd.getFile());

                    for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
                        ZipEntry ze = (ZipEntry) e.nextElement();

                        if (ze.getName().equals(VERSION_FILE)) {
                            continue;
                        }

                        if (!ze.isDirectory()) {
                            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(ze));
                            FileOutputStream fos = new FileOutputStream(ze.getName());
                            int read;

                            while ((read = bis.read(buf)) > 0) {
                                fos.write(buf, 0, read);
                            }

                            fos.close();
                            bis.close();
                        }
                        else {
                            File file = new File(ze.getName());

                            if (!file.exists()) {
                                file.mkdirs();
                            }
                        }
                    }

                    showSuccessfulDialog();
                }
                catch (ZipException e) {
                    mainFrame.showErrorMessage("The update file is damaged. Cannot update.");
                    logger.error(e);
                }
                catch (IOException e) {
                    mainFrame.showErrorMessage("The program cannot be updated. Some I/O error occured. Please report a bug.");
                    logger.error(e);
                }
            }
        }
    }

    private void showSuccessfulDialog() {
        int answer = JOptionPane.showConfirmDialog(dialogPanel,
                "You have successfully updated " + MainFrame.PACKAGE_NAME +
                "!\nYou must restart the program. Do you want to quit now?", MainFrame.PACKAGE_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.YES_OPTION) {
            mainFrame.exitAction.actionPerformed(null);
        }
    }

    private String formatBytes(int bytes) {
        if (bytes < 1024) {
            return Integer.toString(bytes) + "bytes";
        }
        else if (bytes < 1048576) {
            return df.format(bytes / 1024.0) + "kB";
        }
        else {
            return df.format(bytes / 1048576.0) + "MB";
        }
    }

    private class TempFilePair {
        File originalFile;
        File tempFile;

        public TempFilePair(File originalFile, File tempFile) {
            this.originalFile = originalFile;
            this.tempFile = tempFile;
        }
    }

    public class UpdateInternetThread extends Thread {
        private boolean automatic;

        public UpdateInternetThread(boolean automatic) {
            this.automatic = automatic;
        }

        public void run() {
            if (!automatic && !isWriteEnabledForUpdate()) {
                mainFrame.showErrorMessage("In order to update you need to quit " + MainFrame.PACKAGE_NAME +
                                           " and launch it in administrator mode.\n" + "Right click on " +
                                           mainFrame.PROG_NAME +
                                           " and click on 'Run as administrator', then try the update again.");
                return;
            }

            byte buf[] = new byte[1024];

            String[] updateBaseURLs = {
                    mainFrame.getProperties().getProperty(Constants.UPDATE_URL1),
                    mainFrame.getProperties().getProperty(Constants.UPDATE_URL2)
            };
            UpdateProcessDialog upd = null;
            ArrayList<TempFilePair> tempFilePairs = new ArrayList<TempFilePair>();
            int fileSum = 0;
            HttpClient httpClient = new HttpClient();

            try {
                int updateNumber = 0;

                // determine the files to update and calculating the the size
                try {
                    // download the version
                    for (; updateNumber < updateBaseURLs.length; updateNumber++) {
                        GetMethod versionGetMethod = new GetMethod(
                                updateBaseURLs[updateNumber] + Constants.VERSION_FILENAME);
                        versionGetMethod.addRequestHeader(Constants.MAX_AGE_HEADER);
                        httpClient.executeMethod(versionGetMethod);
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

                                return;
                            }
                        }

                        if (remoteVersion == null) {
                            versionGetMethod.releaseConnection();
                        }
                        else {
                            break;
                        }
                    }

                    // download the checksums
                    BufferedReader checksumBufferedReader = null;
                    GetMethod getChecksum = new GetMethod(updateBaseURLs[updateNumber] + Constants.CHECKSUMS_FILENAME);
                    getChecksum.addRequestHeader(Constants.MAX_AGE_HEADER);
                    httpClient.executeMethod(getChecksum);
                    InputStream checksumStream = getChecksum.getResponseBodyAsStream();

                    if (checksumStream != null) {
                        checksumBufferedReader = new BufferedReader(new InputStreamReader(checksumStream));

                        if (!checksumBufferedReader.readLine().equals(ChecksumMaker.HEADER)) {
                            checksumStream.close();
                            checksumStream = null;
                        }
                    }

                    if (checksumStream == null) {
                        getChecksum.releaseConnection();
                        throw new IOException("Cannot download checksums");
                    }

                    String line;

                    while ((line = checksumBufferedReader.readLine()) != null) {
                        int spacePos1 = line.lastIndexOf(' ');
                        int spacePos2 = line.lastIndexOf(' ', spacePos1 - 1);
                        File file = new File(line.substring(0, spacePos2));
                        long remoteChecksum = Long.parseLong(line.substring(spacePos1 + 1));
                        long localChecksum = file.exists() ? ChecksumMaker.getChecksum(file) : -1;

                        if (localChecksum != remoteChecksum) {
                            tempFilePairs.add(new TempFilePair(file, File.createTempFile("gss", "upd")));
                            long size = Long.parseLong(line.substring(spacePos2 + 1, spacePos1));
                            fileSum += size;

                            if (localChecksum == -1) {
                                System.out.println("New file: " + file.getName());
                            }
                            else {
                                System.out.println(
                                        "Update file (" + file.length() + ", " + size + "): " + file.getName());
                            }
                        }
                    }

                    checksumBufferedReader.close();
                    getChecksum.releaseConnection();
                }
                catch (IOException e) {
                    logger.error("Internet update checksums", e);

                    if (!automatic) {
                        mainFrame.showErrorMessage("Cannot connect to the Internet or the update server failed.");
                        throw new CannotUpdateException();
                    }
                    else {
                        return;
                    }
                }
                catch (NumberFormatException e) {
                    logger.error("Internet update checksums number", e);

                    if (!automatic) {
                        mainFrame.showErrorMessage("The update server failed.");
                        throw new CannotUpdateException();
                    }
                    else {
                        return;
                    }
                }

                if (tempFilePairs.isEmpty()) {
                    if (!automatic) {
                        JOptionPane.showMessageDialog(dialogPanel, "No update is available. You already have the lastest version.", mainFrame.PROG_NAME, JOptionPane.INFORMATION_MESSAGE);
                    }

                    return;
                }
                else if (automatic) {
                    if (isWriteEnabledForUpdate()) {
                        int answer = JOptionPane.showConfirmDialog(dialogPanel,
                                "New update is available.\nThe size to download is " + formatBytes(fileSum) +
                                "\nDo you want to update?", mainFrame.PROG_NAME, JOptionPane.YES_NO_OPTION);

                        if (answer == JOptionPane.NO_OPTION) {
                            return;
                        }
                    }
                    else {
                        mainFrame.showErrorMessage("New update is available, however you cannot update, because " +
                                                   MainFrame.PACKAGE_NAME + " is not running in administrator mode.\n" +
                                                   "Quit " + MainFrame.PACKAGE_NAME + " now, right click on " +
                                                   mainFrame.PROG_NAME + " icon and click on 'Run as administrator'");
                        mainFrame.properties.setProperty(Constants.LAST_AUTO_UPDATE, "0");
                        return;
                    }
                }

                downloadCancelled = false;
                upd = new UpdateProcessDialog(mainFrame, fileSum);
                upd.setVisible(true);

                // download the files into temp files
                try {
                    for (TempFilePair tfp : tempFilePairs) {
                        FileOutputStream fos = new FileOutputStream(tfp.tempFile);
                        GetMethod getFile = new GetMethod(updateBaseURLs[updateNumber] +
                                                          tfp.originalFile.getPath().replace('\\', '/').replace(" ", "%20"));
                        getFile.addRequestHeader(Constants.MAX_AGE_HEADER);
                        httpClient.executeMethod(getFile);
                        InputStream urlIs = getFile.getResponseBodyAsStream();

                        if (urlIs == null) {
                            throw new IOException("Cannot download file: " + tfp.originalFile.getPath());
                        }

                        int read;

                        while ((read = urlIs.read(buf)) > 0) {
                            upd.nextValue(read);
                            fos.write(buf, 0, read);

                            if (downloadCancelled) {
                                fos.close();
                                urlIs.close();
                                upd.setVisible(false);
                                JOptionPane.showMessageDialog(mainFrame, "Update is cancelled.", mainFrame.PROG_NAME, JOptionPane.INFORMATION_MESSAGE);
                                return;
                            }
                        }

                        fos.close();
                        urlIs.close();
                        getFile.releaseConnection();
                        System.out.println("Downloaded: " + tfp.originalFile.getName());
                    }
                }
                catch (IOException e) {
                    mainFrame.showErrorMessage("Some error occured during download.");
                    logger.error("Internet update download", e);
                    throw new CannotUpdateException();
                }

                // copy
                try {
                    for (TempFilePair tfp : tempFilePairs) {
                        int read;
                        File parentDir = tfp.originalFile.getParentFile();

                        if (parentDir != null && !parentDir.exists()) {
                            parentDir.mkdirs();
                        }

                        FileInputStream fis = new FileInputStream(tfp.tempFile);
                        FileOutputStream fos = new FileOutputStream(tfp.originalFile);

                        while ((read = fis.read(buf)) > 0) {
                            fos.write(buf, 0, read);
                        }

                        fis.close();
                        fos.close();
                        System.out.println("Copied: " + tfp.originalFile.getName());
                    }
                }
                catch (IOException e) {
                    mainFrame.showErrorMessage("You don't have permission to overwrite the application files.\n" +
                                               "If you are using Windows, quit SongScribe and start it with \"Run as administrator\", and again click on Help | Check for Updates");
                    logger.error("Internet update copy", e);
                    throw new CannotUpdateException();
                }

                upd.setVisible(false);
                showSuccessfulDialog();
            }
            catch (CannotUpdateException e) {
                if (upd != null) {
                    upd.setVisible(false);
                }

                mainFrame.showErrorMessage("Updating failed.");
            }
        }

        public boolean isWriteEnabledForUpdate() {
            File file = new File("probeupdate" + new Random().nextInt());

            try {
                return file.createNewFile();
            }
            catch (IOException e) {
                return false;
            }
            finally {
                file.delete();
            }
        }
    }

    private class UpdateProcessDialog extends ProcessDialog {
        JLabel readLabel = new JLabel();
        JLabel dashLabel = new JLabel("/");
        JLabel sumLabel = new JLabel();

        public UpdateProcessDialog(MainFrame mainFrame, int total) throws HeadlessException {
            super(mainFrame, "Update progress", total);
            setModal(false);

            JPanel south = new JPanel(new BorderLayout());

            JPanel progressLabelPane = new JPanel();
            progressLabelPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            progressLabelPane.setLayout(new BoxLayout(progressLabelPane, BoxLayout.X_AXIS));
            progressLabelPane.add(readLabel);
            progressLabelPane.add(dashLabel);
            sumLabel.setText(formatBytes(total));
            progressLabelPane.add(sumLabel);
            south.add(progressLabelPane);

            JPanel cancelPanel = new JPanel();
            final JButton cancelButton = new JButton("Cancel", new ImageIcon(MainFrame.getImage("cancel.png")));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    downloadCancelled = true;
                    cancelButton.setEnabled(false);
                }
            });
            cancelPanel.add(cancelButton);
            south.add(BorderLayout.SOUTH, cancelPanel);

            getContentPane().add(BorderLayout.SOUTH, south);

            packAndPos();
        }

        public void nextValue(int value) {
            super.nextValue(value);
            readLabel.setText(formatBytes(progressBar.getValue()));
        }
    }
}
