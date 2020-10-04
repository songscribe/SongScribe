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

    Created on Jul 17, 2006
*/
package songscribe;

import org.apache.log4j.PropertyConfigurator;
import songscribe.converter.AbcConverter;
import songscribe.converter.ImageConverter;
import songscribe.converter.MidiConverter;
import songscribe.converter.PDFConverter;
import songscribe.publisher.Publisher;
import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.SlideFrame;
import songscribe.uiconverter.UIConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class SongScribe {
    public static String basePath = ".";

    public static void main(String[] args) {
        for (String entry: System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!entry.equals(Constants.SONG_SCRIBE_JAR) && entry.endsWith(Constants.SONG_SCRIBE_JAR)) {
                basePath = entry.substring(0, entry.length() - Constants.SONG_SCRIBE_JAR.length() - 1);
            }
        }


        PropertyConfigurator.configure(basePath + "/conf/logger.properties");
        String ss = System.getProperty("songscribe");

        // look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            // pass
        }

        if ("sw".equals(ss)) {
            MainFrame.main(args);
        }
        else if ("ss".equals(ss)) {
            SlideFrame.main(args);
        }
        else if ("sb".equals(ss)) {
            Publisher.main(args);
        }
        else if ("image_converter".equals(ss)) {
            ImageConverter.main(args);
        }
        else if ("midi_converter".equals(ss)) {
            MidiConverter.main(args);
        }
        else if ("pdf_converter".equals(ss)) {
            PDFConverter.main(args);
        }
        else if ("ui_converter".equals(ss)) {
            UIConverter.main(args);
        }
        else if ("abc_converter".equals(ss)) {
            AbcConverter.main(args);
        }
        else {
            //startFrame(args);
            MainFrame.main(args);
        }
    }

    private static void startFrame(final String[] args) {
        final JFrame frame = new JFrame(Constants.PACKAGE_NAME);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Which application do you want to start?");
        label.setAlignmentX(0f);
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        ButtonGroup bg = new ButtonGroup();
        MainFrame.setMediaTracker(new MediaTracker(frame));
        final JRadioButton sw = new JRadioButton("Song Writer", new ImageIcon(MainFrame.getImage("swicon.png")), true);
        bg.add(sw);
        panel.add(sw);
        panel.add(Box.createVerticalStrut(5));
        final JRadioButton ss = new JRadioButton("Song Show", new ImageIcon(MainFrame.getImage("ssicon.png")));
        bg.add(ss);
        panel.add(ss);
        panel.add(Box.createVerticalStrut(5));
        final JRadioButton sb = new JRadioButton("Song Book", new ImageIcon(MainFrame.getImage("sbicon.png")));
        bg.add(sb);
        panel.add(sb);
        frame.getContentPane().add(panel);
        JButton start = new JButton("Start");
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);

                if (sw.isSelected()) {
                    MainFrame.main(args);
                }
                else if (ss.isSelected()) {
                    SlideFrame.main(args);
                }
                else if (sb.isSelected()) {
                    Publisher.main(args);
                }
            }
        });
        JPanel south = new JPanel();
        south.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        south.add(start);
        frame.getContentPane().add(BorderLayout.SOUTH, south);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation(
                MainFrame.CENTER_POINT.x - frame.getWidth() / 2, MainFrame.CENTER_POINT.y - frame.getHeight() / 2);
        frame.setVisible(true);
    }
}
