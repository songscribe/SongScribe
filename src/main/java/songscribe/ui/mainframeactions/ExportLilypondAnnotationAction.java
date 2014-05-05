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

Created on Aug 6, 2006
*/
package songscribe.ui.mainframeactions;

import org.apache.log4j.Logger;
import songscribe.data.*;
import songscribe.music.*;
import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/** The following features are not supported in abc 2.1
 * 1. Accidentals in parenthesis. I opened a forum for this topic (with no answer yet): http://abcnotation.com/forums/viewtopic.php?f=7&t=260
 * 2. Beat change.
 * 3. Glissandos. I substituted them with slurs for now.
 * 4. On syllabified lyrics no long hyphen between compound words (like God-Realisation)
 * 5. Syllables under grace notes. Solution: put together with the syllable of next note with \
 * 6. Forcing syllable under a rest (new SongScribe feature)
 * @author Csaba Kávai
 */
public class ExportLilypondAnnotationAction extends AbstractAction {
    private Logger logger = Logger.getLogger(ExportLilypondAnnotationAction.class);
    private PlatformFileDialog pfd;
    private MainFrame mainFrame;

    public ExportLilypondAnnotationAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        putValue(Action.NAME, "Export as LilyPond Notation...");
        pfd = new PlatformFileDialog(mainFrame, "Export as LilyPond Notation", false, new MyAcceptFilter("LilyPond Files", "ly"));
    }

    public void actionPerformed(ActionEvent e) {
        pfd.setFile(Utilities.getSongTitleFileNameForFileChooser(mainFrame.getMusicSheet()));
        if(pfd.showDialog()){
            File saveFile = pfd.getFile();
            if(!saveFile.getName().toLowerCase().endsWith(".ly")){
                saveFile = new File(saveFile.getAbsolutePath()+".ly");
            }
            if(saveFile.exists()){
                int answ = JOptionPane.showConfirmDialog(mainFrame, "The file "+saveFile.getName()+" already exists. Do you want to overwrite it?",
                        mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION);
                if(answ==JOptionPane.NO_OPTION){
                    return;
                }
            }
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(saveFile);
                writeABC(writer);
            } catch (IOException e1) {
                mainFrame.showErrorMessage(MainFrame.COULDNOTSAVEMESSAGE);
                logger.error("Saving LilyPond", e1);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
    
    public void writeABC(PrintWriter writer) {
        Composition composition = mainFrame.getMusicSheet().getComposition();
        writer.println("\\score {");
        writer.println("}");

    }


    String translatePitch(int yPos) {
        StringBuilder sb = new StringBuilder();
        char letter = (char)((getPitchType(yPos) + 1 ) % 7 + 'a');
        sb.append(letter);
        for (int y = yPos;y < 7; y+=7) {
            sb.append('\'');
        }
        return sb.toString();
    }

    /**
     * @return 0 for B, 1 for C, 2 for D, ..., 6 for A
     */
    int getPitchType(int yPos){
        return yPos<=0 ? -yPos % 7 : (7 - yPos % 7) % 7;
    }



}
