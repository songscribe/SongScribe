package songscribe.converter;

import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.data.MyBorder;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.*;

public class ImageConverter {

    @ArgumentDescribe("The type of resulting image. Values: [ GIF | JPG | PNG | BMP ]")
    public String type="GIF";

    @ArgumentDescribe("Resolution in DPI")
    public int resolution=100;

    @ArgumentDescribe("Export image without lyrics under the song")
    public boolean withoutLyrics;

    @ArgumentDescribe("Export image without song title")
    public boolean withoutSongTitle;

    @ArgumentDescribe("Margin around the image in pixels.")
    public int margin=10;

    @ArgumentDescribe("Top margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int topmargin=-1;

    @ArgumentDescribe("Left margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int leftmargin=-1;

    @ArgumentDescribe("Bottom margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int bottommargin=-1;
    
    @ArgumentDescribe("Right margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int rightmargin=-1;

    @FileArgument
    public File[] files;

    public static void main(String[] args) {
        ArgumentumReader ar = new ArgumentumReader(args, ImageConverter.class);
        ((ImageConverter) ar.getObj()).convert();
    }

    private void convert(){
        MainFrame mf = new MainFrame(){
            @Override
            public void showErrorMessage(String message) {
                System.out.println(message);
            }
        };
        mf.setMusicSheet(new MusicSheet(mf));
        MyBorder myBorder = new MyBorder(margin);
        if(topmargin>-1)myBorder.setTop(topmargin);
        if(leftmargin>-1)myBorder.setLeft(leftmargin);
        if(bottommargin>-1)myBorder.setBottom(bottommargin);
        if(rightmargin>-1)myBorder.setRight(rightmargin);
        for(File file:files){
            mf.getMusicSheet().setComposition(null);
            mf.openMusicSheet(file, false);
            if(withoutLyrics){
                mf.getMusicSheet().getComposition().setUnderLyrics("");
                mf.getMusicSheet().getComposition().setTranslatedLyrics("");
            }
            if(withoutSongTitle){
                mf.getMusicSheet().getComposition().setSongTitle("");
            }
            BufferedImage image = mf.getMusicSheet().createMusicSheetImageForExport(Color.WHITE, (double)resolution/MusicSheet.RESOLUTION, myBorder);
            String fileName = file.getName();
            int dotPos = fileName.indexOf('.');
            if(dotPos>0)fileName=fileName.substring(0, dotPos);
            try {
                ImageIO.write(image, type, new File(fileName+"."+type.toLowerCase()));
            } catch (IOException e) {
                System.out.println("Could not convert "+file.getName());
            }
        }
    }
}
