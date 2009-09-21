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

Created on 2005.10.18. 
*/
package songscribe;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;


/**
 * @author Csaba Kávai
 */
public class Proba {
    public static void main(String[] args) throws Exception {

        /*Calendar c = Calendar.getInstance();
        c.set(2007, 11, 31, 23, 59, 59);
        System.out.println(c.getTimeInMillis());*/        
        /*MyComponent c = new MyComponent();
        JFrame frame = new JFrame("Proba");
        frame.addKeyListener(c);
        frame.getContentPane().add(new JScrollPane(c));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);*/

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyComponent comp = new MyComponent();
        frame.add(comp);
        frame.addKeyListener(comp);
        frame.pack();
        frame.setVisible(true);
    }

    private static final String[] accidentals = {"", "\uf06e", "\uf062", "\uf023", "\uf06e\uf06e", "\uf0ba", "\uf0dc", "\uf06e\uf062", "\uf06e\uf023"};
    private static final String[] accidentalParenthesis = {"\uf028\uf06e\uf06e\uf029", "\uf028\uf06e\uf062\uf029", "\uf028\uf06e\uf023\uf029"};

    private static class MyComponent extends JComponent implements KeyListener{
        int s = 320;

        final Font maestro = new Font("Maestro", 0, s);
        Font fughetta;
        final Font toccata = new Font("Toccata", 0, s);

        int s8 = (int)Math.ceil(s/8.0);
        int x = 300;
        int y = 400;

        int xv = x;
        int yv = y;

        public MyComponent() {
            setPreferredSize(new Dimension(600, 600));
            try {
                fughetta = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/fughetta.ttf")).deriveFont(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawString(Integer.toString(s), 0, 20);

            g2.setFont(fughetta.deriveFont((float)s));

            //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            //for(int i=-2;i<=2;i++){
            //    g2.drawLine(0, y+i*s/4, 400, y+i*s/4);
            //}
            //g2.setStroke(new BasicStroke(0.836f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            //g2.draw(new Line2D.Float(x+s/3.6056337f, 0, x+s/3.6056337f, 500));
            //String s1 = accidentals[5];
            //g2.drawString(s1, xv, yv);
            //g2.drawRect(xv, yv-s, g2.getFontMetrics().stringWidth(s1), s);
            g2.drawLine(x-2, 0, x-2, yv);
            g2.drawString("\uf06a", x, y-s/1.6623377f);
            g2.drawString("\uf0fb", xv, y-s/1.1851852f);
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()){
                case KeyEvent.VK_LEFT:xv--;repaint();break;
                case KeyEvent.VK_RIGHT:xv++;repaint();break;
                case KeyEvent.VK_UP:yv--;repaint();break;
                case KeyEvent.VK_DOWN:yv++;repaint();break;
                case KeyEvent.VK_PAGE_UP:s+=1;repaint();break;
                case KeyEvent.VK_PAGE_DOWN:s-=1;repaint();break;
                case KeyEvent.VK_ENTER: System.out.println((float)s/(xv-x)+", "+(float)s/(yv-y)+"; "+(xv-x)+", "+(yv-y));
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }
}
