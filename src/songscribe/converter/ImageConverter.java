package songscribe.converter;

import java.util.Vector;
import java.io.File;

public class ImageConverter {

    static class A{
        @ArgumentDescribe("The type of resulting image. Values: [ GIF | JPG | PNG | BMP ]")
        public String type="GIF";

        @ArgumentDescribe("Resolution in DPI")
        public int resolution=100;

        @ArgumentDescribe("Export image without lyrics under the song")
        public boolean withoutLyrics;
        
        public Vector<File> files;
    }

    public static void main(String[] args) {
        ArgumentumReader ar = new ArgumentumReader(args, A.class);
        Object o = ar.getObj();
        A a = (A) o;
        System.out.println(a.type);
        System.out.println(a.resolution);
        System.out.println(a.withoutLyrics);
        for(File f:a.files){
            System.out.println(f.getAbsolutePath());
        }


    }
}
