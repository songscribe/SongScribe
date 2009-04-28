package songscribe.data;

import javax.swing.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.*;
import java.util.Properties;
import java.io.*;

public class FileGeneralPath {
    private static final char breathMarkChar = '\uf02c';
    private static final char fermataChar = '\uf055';

    private static Properties valueNum = new Properties();

    static{
        valueNum.put(PathIterator.SEG_MOVETO, 2);
        valueNum.put(PathIterator.SEG_LINETO, 2);
        valueNum.put(PathIterator.SEG_QUADTO, 4);
        valueNum.put(PathIterator.SEG_CUBICTO, 6);
        valueNum.put(PathIterator.SEG_CLOSE, 0);
    }

    public static void writeGeneralPath(char ch, File file) throws IOException {
        float[] ret = new float[6];
        Shape outline = getShape(ch);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        for(PathIterator pi=outline.getPathIterator(null);!pi.isDone();pi.next()){
            int seg = pi.currentSegment(ret);
            int parNum = (Integer)valueNum.get(seg);
            oos.writeInt(seg);
            for(int i=0;i<parNum;i++)oos.writeFloat(ret[i]);
        }
        oos.close();
    }

    public static void printGeneralPath(char ch){
        Shape outline = getShape(ch);
        printGeneralPath(outline);
    }

    private static void printGeneralPath(Shape outline) {
        double[] ret = new double[6];
        for(PathIterator pi=outline.getPathIterator(null);!pi.isDone();pi.next()){
            int i = pi.currentSegment(ret);
            switch(i){
                case PathIterator.SEG_MOVETO:
                    System.out.printf("MOVETO: (%f, %f)\n", ret[0], ret[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    System.out.printf("LINETO: (%f, %f)\n", ret[0], ret[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    System.out.printf("QUADTO: (%f, %f); (%f, %f)\n", ret[0], ret[1], ret[2], ret[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    System.out.printf("CUBICTO: (%f, %f); (%f, %f); (%f, %f)\n", ret[0], ret[1], ret[2], ret[3], ret[4], ret[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    System.out.println("CLOSE");
            }
        }
    }

    public static GeneralPath readGeneralPath(File file) throws IOException {
        GeneralPath gm = new GeneralPath(1);
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        while(ois.available()>0){
            int seg = ois.readInt();
            switch(seg){
                case PathIterator.SEG_MOVETO:
                    gm.moveTo(ois.readFloat(), ois.readFloat());
                    break;
                case PathIterator.SEG_LINETO:
                    gm.lineTo(ois.readFloat(), ois.readFloat());
                    break;
                case PathIterator.SEG_QUADTO:
                    gm.quadTo(ois.readFloat(), ois.readFloat(), ois.readFloat(), ois.readFloat());
                    break;
                case PathIterator.SEG_CUBICTO:
                    gm.curveTo(ois.readFloat(), ois.readFloat(), ois.readFloat(), ois.readFloat(), ois.readFloat(), ois.readFloat());
                    break;
                case PathIterator.SEG_CLOSE:
                    gm.closePath();
            }
        }
        return gm;
    }

    private static Shape getShape(char ch) {
        Font maestro = new Font("Maestro", 0, 512);
        JFrame f = new JFrame();
        f.pack();
        Graphics2D g2 = (Graphics2D) f.getGraphics();
        Shape outline = maestro.createGlyphVector(g2.getFontRenderContext(), Character.toString(ch)).getOutline();
        return outline;
    }

    public static void main(String[] args) throws IOException {
        writeGeneralPath(fermataChar, new File("fonts/fm"));
        //final GeneralPath gm = readGeneralPath(new File("fm"));
        //showInWindow(FughettaDrawer.breathMark);
    }

    private static void showInWindow(final GeneralPath gm) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent comp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.translate(50, 50);
                g2.fill(gm);
                //g2.translate(50, 0);
                g2.setPaint(Color.RED);
                g2.fill(getShape('\uf02c'));
            }
        };
        comp.setPreferredSize(new Dimension(500, 500));
        f.add(comp);
        f.pack();
        f.setVisible(true);
    }
}
