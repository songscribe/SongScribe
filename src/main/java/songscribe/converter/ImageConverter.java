/*
    SongScribe song notation program
    Copyright (C) 2014 Csaba Kavai

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
*/
package songscribe.converter;

import songscribe.data.MyBorder;
import songscribe.ui.MusicSheet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageConverter {

    @ArgumentDescribe("The type of resulting image. Values: [ gif | jpg | png | bmp ]")
    public String type = "png";

    @ArgumentDescribe("Resolution in DPI")
    public int resolution = 100;

    @ArgumentDescribe("Suffix to add to filename")
    public String suffix = "";

    @ArgumentDescribe("Export image without lyrics under the song")
    public boolean withoutLyrics;

    @ArgumentDescribe("Export image without song title")
    public boolean withoutSongTitle;

    @ArgumentDescribe("Export image without song copyright info and date")
    public boolean withoutCopyright;

    @ArgumentDescribe("Margin around the image in pixels")
    public int margin = 10;

    @ArgumentDescribe("Top margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int topmargin = -1;

    @ArgumentDescribe("Left margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int leftmargin = -1;

    @ArgumentDescribe("Bottom margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int bottommargin = -1;

    @ArgumentDescribe("Right margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int rightmargin = -1;

    @FileArgument
    public File[] files;

    public static void main(String[] args) {
        ArgumentReader ar = new ArgumentReader(args, ImageConverter.class);
        ImageConverter converter = (ImageConverter) ar.getObj();
        converter.convert();
    }

    private void convert() {
        ConverterMainFrame mf = new ConverterMainFrame();
        mf.setMusicSheet(new MusicSheet(mf));
        MyBorder myBorder = new MyBorder(margin);

        if (topmargin > -1) {
            myBorder.setTop(topmargin);
        }

        if (leftmargin > -1) {
            myBorder.setLeft(leftmargin);
        }

        if (bottommargin > -1) {
            myBorder.setBottom(bottommargin);
        }

        if (rightmargin > -1) {
            myBorder.setRight(rightmargin);
        }

        for (File file : files) {
            mf.getMusicSheet().setComposition(null);
            mf.getMusicSheet().openMusicSheet(mf, file, false);

            if (withoutLyrics) {
                mf.getMusicSheet().getComposition().setUnderLyrics("");
                mf.getMusicSheet().getComposition().setTranslatedLyrics("");
            }

            if (withoutSongTitle) {
                mf.getMusicSheet().getComposition().setSongTitle("");
            }

            if (withoutCopyright) {
                mf.getMusicSheet().getComposition().setRightInfo("");
            }

            BufferedImage image = mf.getMusicSheet().createMusicSheetImageForExport(Color.WHITE,
                    (double) resolution / MusicSheet.RESOLUTION, myBorder);

            try {
                String path = file.getCanonicalPath();
                int dotPos = path.lastIndexOf('.');

                if (dotPos > 0) {
                    path = path.substring(0, dotPos);
                }

                path += suffix;
                ImageIO.write(image, type.toUpperCase(), new File(path + "." + type.toLowerCase()));
            }
            catch (IOException e) {
                System.out.println("Could not convert " + file.getName());
            }
        }
    }
}
