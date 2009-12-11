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

Created on Jul 17, 2006
*/
package songscribe;

import songscribe.ui.MainFrame;
import songscribe.ui.SlideFrame;
import songscribe.ui.Utilities;
import songscribe.publisher.Publisher;
import songscribe.converter.ImageConverter;
import songscribe.converter.MidiConverter;
import songscribe.uiconverter.UIConverter;

import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.io.File;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

import org.apache.log4j.Logger;

import javax.swing.*;

/**
 * @author Csaba Kávai
 */
public class Runner {
    public static void main(String[] args) {
        //adding the classpath dinamically
        for(File lib:new File("libs").listFiles()) if(lib.isFile())addURL(lib);

        Logger LOG = Logger.getLogger(Runner.class);

        String ss = System.getProperty("songscribe");

        if ("version".equals(ss)) {
            System.out.println(Utilities.getVersion());
            return;
        }

        //look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOG.warn("Could not set system default look&feel", e);
        }

        //launching ss
        if(ss == null)startFrame(args);
        else if(ss.equals("sw"))MainFrame.main(args);
        else if(ss.equals("ss"))SlideFrame.main(args);
        else if(ss.equals("sb"))Publisher.main(args);
        else if(ss.equals("image_converter")) ImageConverter.main(args);
        else if(ss.equals("midi_converter")) MidiConverter.main(args);
        else if(ss.equals("ui_converter")) UIConverter.main(args);
        else startFrame(args);
    }

    public static void addURL(File file) {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;     
        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            //noinspection deprecation
            method.invoke(sysloader, file.toURL());
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, "The program could not start because of an unexpected error.\n Please contact the developer.", "Fatal", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Runner.class).fatal("Error, could not add URL to system classloader", t);
            System.exit(0);
        }
    }

    private static void startFrame(final String[] args){
        final JFrame frame = new JFrame(MainFrame.PACKAGENAME);
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
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                if(sw.isSelected())MainFrame.main(args);
                else if(ss.isSelected())SlideFrame.main(args);
                else if(sb.isSelected())Publisher.main(args);                
            }
        });
        JPanel south = new JPanel();
        south.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        south.add(start);
        frame.getContentPane().add(BorderLayout.SOUTH, south);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation(MainFrame.CENTERPOINT.x-frame.getWidth()/2, MainFrame.CENTERPOINT.y-frame.getHeight()/2);
        frame.setVisible(true);
    }
}
