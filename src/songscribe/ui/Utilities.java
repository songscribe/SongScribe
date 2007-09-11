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
package songscribe.ui;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.*;

/**
 * @author Csaba Kávai
 */
public class Utilities {
    private static Logger logger = Logger.getLogger(Utilities.class);
    public static String getSongTitleFileNameForFileChooser(MusicSheet musicSheet){
        StringBuilder sb = new StringBuilder(musicSheet.getComposition().getSongTitle().length()+10);
        sb.append(musicSheet.getComposition().getNumber());
        if(musicSheet.getComposition().getNumber().length()>0)sb.append(' ');
        outer:
        for(char c : musicSheet.getComposition().getSongTitle().toCharArray()){
            for(int i=0;i<LyricsDialog.specChars.length;i++){
                for(int j=0;j<LyricsDialog.specChars[i].length;j++){
                    if(c==LyricsDialog.specChars[i][j]){
                        sb.append(LyricsDialog.specCharsMap[i][j]);
                        continue outer;
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static int arrayIndexOf(Object[] array, Object element){
        for(int i=0;i<array.length;i++){
            if(element.equals(array[i])){
                return i;
            }
        }
        return -1;
    }

    public static int lineCount(String str){
        if(str.length()==0)return 0;
        int found = 0;
        for(char ch:str.toCharArray()){
            if(ch=='\n')found++;
        }
        if(str.charAt(str.length()-1)!='\n')found++;
        return found;
    }

    public static void readComboValuesFromFile(JComboBox combo, File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line=br.readLine())!=null){
                combo.addItem(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Could not open a necessary file. Please reinstall the software.", MainFrame.PACKAGENAME, JOptionPane.ERROR_MESSAGE);
            logger.error("readComboValuesFromFile open", e);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not open a necessary file. Please reinstall the software.", MainFrame.PACKAGENAME, JOptionPane.ERROR_MESSAGE);
            logger.error("readComboValuesFromFile open", e);
        }
    }

    private static boolean isMac;
    static{
        isMac = System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1;
    }

    public static boolean isMac() {        
        return isMac;
    }

    public static int getFileVersion(){
        return MainFrame.MAJORVERSION*10000+MainFrame.MINORVERSION*100;
    }

    public static String getVersion() {
        return MainFrame.MAJORVERSION +"."+ MainFrame.MINORVERSION;
    }

    public static void copyFile(File in, File out) throws IOException {
        FileInputStream fis  = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while((i=fis.read(buf))!=-1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }
}
