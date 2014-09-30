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

import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.mainframeactions.ExportABCAnnotationAction;

import java.io.File;
import java.io.PrintWriter;

public class AbcConverter {
    @FileArgument
    public File file;

    public static void main(String[] args) {
        ArgumentReader am = new ArgumentReader(args, AbcConverter.class);
        ((AbcConverter) am.getObj()).convert();
    }

    public void convert() {
        MainFrame mf = new MainFrame() {
            @Override
            public void showErrorMessage(String message) {
                System.out.println(message);
            }
        };
        mf.setMusicSheet(new MusicSheet(mf));
        ExportABCAnnotationAction exportABCAnnotation = new ExportABCAnnotationAction(mf);

        mf.getMusicSheet().setComposition(null);
        mf.openMusicSheet(file, false);

        PrintWriter writer = new PrintWriter(System.out);
        exportABCAnnotation.writeABC(writer);
        writer.close();
    }
}
