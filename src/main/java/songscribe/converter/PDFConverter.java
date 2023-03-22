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

import songscribe.publisher.newsteps.Data;
import songscribe.ui.MusicSheet;
import songscribe.ui.mainframeactions.ExportPDFAction;

import java.io.File;
import java.io.IOException;

public class PDFConverter {

    @ArgumentDescribe("Paper size: a4, letter, legal, custom")
    @NoDefault
    public String paperSize;

    @ArgumentDescribe("Paper width in 100ths of an inch, ignored if paperSize is not 'custom'")
    @NoDefault
    public int paperWidth = -1;

    @ArgumentDescribe("Paper height in 100ths of an inch, ignored if paperSize is not 'custom'")
    @NoDefault
    public int paperHeight = -1;

    @ArgumentDescribe("Export PDF without lyrics under the song")
    public boolean withoutLyrics;

    @ArgumentDescribe("Export PDF without song title")
    public boolean withoutSongTitle;

    @ArgumentDescribe("Margin around the PDF in pixels")
    public int margin = 0;

    @ArgumentDescribe("Top margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int topMargin = -1;

    @ArgumentDescribe("Left margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int leftMargin = -1;

    @ArgumentDescribe("Bottom margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int bottomMargin = -1;

    @ArgumentDescribe("Right margin. If not present, the size of margin parameter is applied.")
    @NoDefault
    public int rightMargin = -1;

    @FileArgument
    public File[] files;

    public static void main(String[] args) {
        ArgumentReader ar = new ArgumentReader(args, PDFConverter.class);
        ((PDFConverter) ar.getObj()).convert();
    }

    public void convert() {
        paperSize = paperSize.toLowerCase();

        if (paperSize.equals("a4")) {
            paperWidth = 827;
            paperHeight = 1169;
        }
        else if (paperSize.equals("letter")) {
            paperWidth = 850;
            paperHeight = 1100;
        }
        else if (paperSize.equals("legal")) {
            paperWidth = 850;
            paperHeight = 1400;
        }
        else if (paperSize.equals("custom")) {
            if (paperWidth <= 0 || paperHeight <= 0) {
                System.out.println("paperWidth and paperHeight must be specified for custom paperSize");
                return;
            }
        }
        else {
            System.out.println("invalid paperSize");
            return;
        }

        ConverterMainFrame mf = new ConverterMainFrame();
        mf.setMusicSheet(new MusicSheet(mf));

        Data data = new Data();
        data.paperWidth = paperWidth;
        data.paperHeight = paperHeight;
        data.topMargin = data.bottomMargin = data.leftInnerMargin = data.rightOuterMargin = 75;
        data.mainFrame = mf;

        if (topMargin > -1) {
            data.topMargin = topMargin;
        }

        if (leftMargin > -1) {
            data.leftInnerMargin = leftMargin;
        }

        if (bottomMargin > -1) {
            data.bottomMargin = bottomMargin;
        }

        if (rightMargin > -1) {
            data.rightOuterMargin = rightMargin;
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

            try {
                String path = file.getCanonicalPath();
                int dotPos = path.lastIndexOf(".");

                if (dotPos > 0) {
                    path = path.substring(0, dotPos);
                }

                path += ".pdf";
                ExportPDFAction.createPDF(data, new File(path), false);
            }
            catch (IOException e) {
                System.out.println("Could not convert " + file.getName());
            }
        }
    }
}
