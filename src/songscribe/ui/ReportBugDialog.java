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

Created on Dec 24, 2006
*/
package songscribe.ui;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;

import songscribe.data.MyDesktop;

/**
 * @author Csaba Kávai
 */
public class ReportBugDialog extends MyDialog {
    private static final Logger logger = Logger.getLogger(ReportBugDialog.class);
    public static final String BUGEMAIL = "songscribe@vasudevaserver.org";

    public ReportBugDialog(MainFrame mainFrame) {
        super(mainFrame, "Bug report");
        final File logFile = new File(MainFrame.SSHOME, "log");
        JEditorPane area = new JEditorPane("text/html", "<html><h1>Bug report</h1><p>If you encounter a program error, "+
                "a bad behavior or just have a wish to improve the program, you are most welcome to write a bug report to the following e-mail address:</p><p color=\"blue\"><u>"+
                BUGEMAIL+"</u></p><p>If you want to write a report, just click the button below and it will create an e-mail message at your default e-mail client."+
                "If you do not use e-mail client, you can write the mail by yourself, but do it in this way: please write \"SongScribe bug\" or \"SongScribe wish\" as subject, "+
                "write the operation system, the version number (this: "+Utilities.getVersion()+") and attach the log file which can be found here:.</p>"+
                "<p color=\"blue\"><u>"+logFile.getAbsolutePath()+"</u></p>"+
                "<p>Thank you for helping improve SongScribe.</p><p>Csaba Kavai<br>The author</p></html>");
        area.setEditable(false);
        area.setBackground(dialogPanel.getBackground());
        area.setPreferredSize(new Dimension(400, 450));
        dialogPanel.add(area);
        southPanel.remove(applyButton);
        southPanel.remove(cancelButton);
        final MainFrame mf = mainFrame;
        if(MyDesktop.isDesktopSupported()){
            JButton sendBug = new JButton("Send a report", new ImageIcon(MainFrame.getImage("mail_generic.png")));
            sendBug.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    int answ = JOptionPane.showOptionDialog(dialogPanel, "What would you like to send?", mf.PROGNAME, JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Bug", "Wish", "Cancel"}, null);
                    if(answ==2 || answ == JOptionPane.CLOSED_OPTION) return;
                    StringBuilder sb = new StringBuilder();
                    sb.append(BUGEMAIL);
                    sb.append("?SUBJECT=SongScribe ");
                    sb.append(answ==0 ? "bug" : "wish");
                    if(answ==0){
                        sb.append("&ATTACHMENT=\"");
                        sb.append(logFile.getAbsolutePath());
                        sb.append("\"");
                    }
                    sb.append("&BODY=Version: ");
                    sb.append(Utilities.getVersion());
                    sb.append("\nOperation system: ");
                    sb.append(System.getProperty("os.name"));
                    sb.append("\nJVM version: ");
                    sb.append(System.getProperty("java.vm.version"));
                    sb.append("\nDescription:\n-------------Write your report here---------------");
                    try {
                        Utilities.openEmail(mf, sb.toString());
                    } catch (Exception e1) {
                        mf.showErrorMessage("Cannot open the e-mail client. Please make your report manually as described above.");
                        logger.error("Report mail send", e1);
                    }
                }
            });
            southPanel.add(sendBug);
        }
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }


    protected void getData() {
    }

    protected void setData() {
    }
}
