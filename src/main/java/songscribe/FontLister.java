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

Created on Apr 22, 2006
*/
package songscribe;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Csaba Kávai
 */
public class FontLister {
    public static void main(String[] args) {
        final Font[] fonts = {new Font("Maestro", 0, 60), new Font("Fughetta", 0, 60)};
        JComponent c = new FontComponent(fonts);
        JFrame frame = new JFrame("FontLister");
        frame.getContentPane().add(new JScrollPane(c));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static class FontComponent extends JComponent{
        BufferedImage image;
        Font labelFont = new Font("Arial", 0, 10);

        public FontComponent(Font[] font) {
            setDisplayFont(font);
        }

        public void setDisplayFont(Font[] font){
            int width = 60;
            int height = 360;
            image = new BufferedImage(256*width+1, 2*height+1, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2 = image.createGraphics();
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics labelMetrics = g2.getFontMetrics(labelFont);
            g2.setPaint(Color.black);
            char[] ch = new char[1];
            Rectangle2D rec;
            for(int i=0;i<font.length;i++){
                FontMetrics metrics = g2.getFontMetrics(font[i]);
            //int i=0;
                g2.drawLine(0, i*height, image.getWidth(), i*height);
                g2.drawLine(i*width, 0, i*width, image.getHeight());
                for(int j=0;j<256;j++){
                    ch[0] = (char)(0xf0*256+j);
                    String str = new String(ch);
                    g2.setFont(font[i]);
                    rec = metrics.getStringBounds(str, 0, 1, g2);
                    g2.drawString(str, j*width+(width-(int)rec.getWidth())/2-(int)rec.getX(), (i+0.5f)*height);
                    String hex = Integer.toHexString(ch[0]).toUpperCase();
                    g2.setFont(labelFont);
                    rec = labelMetrics.getStringBounds(hex, g2);
                    g2.drawString(hex, j*width+(width-(int)rec.getWidth())/2, (i+1)*height);
                }
            }
            g2.drawLine(0, image.getHeight()-1, image.getWidth(), image.getHeight()-1);
            g2.drawLine(image.getWidth()-1, 0, image.getWidth()-1, image.getHeight());
            g2.dispose();

            try {
                ImageIO.write(image, "png", new File("font.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
    }
}
