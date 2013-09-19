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
import songscribe.Version;
import songscribe.data.GifEncoder;
import songscribe.data.MyDesktop;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Csaba Kávai
 */
public class Utilities {
    private static Logger logger = Logger.getLogger(Utilities.class);

    public static String getSongTitleFileNameForFileChooser(MusicSheet musicSheet){
        StringBuilder sb = new StringBuilder(musicSheet.getComposition().getSongTitle().length()+10);
        try{
            int number = Integer.parseInt(musicSheet.getComposition().getNumber());
            sb.append(String.format("%03d", number));
        }catch(NumberFormatException nfe){
            sb.append(musicSheet.getComposition().getNumber());
        }
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
    private static boolean isWindows;
    private static boolean isLinux;
    static{
        isMac = System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1;
        isWindows = System.getProperty("os.name").toLowerCase().indexOf("windows")!=-1;
        isLinux = System.getProperty("os.name").toLowerCase().indexOf("linux")!=-1;
    }

    public static boolean isMac() {        
        return isMac;
    }

    public static boolean isWindows() {
        return isWindows;
    }

    public static boolean isLinux() {
        return isLinux;
    }

    public static String getPublicVersion() {
        return Version.MAJOR_VERSION + "." + Version.MINOR_VERSION;
    }

    public static String getFullVersion() {
        return Version.MAJOR_VERSION + "." + Version.MINOR_VERSION + "." + Version.BUILD_VERSION;
    }

    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static void copyFile(File in, File out) throws IOException {
        FileInputStream fis  = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i;
        while((i=fis.read(buf))!=-1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }

    public static void openExportFile(MainFrame mainFrame, File file){
        if(MyDesktop.isDesktopSupported()){
            MyDesktop desktop = MyDesktop.getDesktop();
            if(JOptionPane.showConfirmDialog(mainFrame, "Do you want to open the file?", mainFrame.PROGNAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
                try {
                    desktop.open(file);
                } catch (Exception e) {
                    mainFrame.showErrorMessage("Could not open the file.");
                }
            }
        }
    }

    public static void openWebPage(MainFrame mainFrame, String webPage){
        if(MyDesktop.isDesktopSupported()){
            MyDesktop desktop = MyDesktop.getDesktop();
            try {
                desktop.browse(new URI(webPage));
            } catch (Exception e) {
                mainFrame.showErrorMessage("Could not open the webpage.");
            }
        }
    }

    public static void openEmail(MainFrame mainFrame, String email){
        if(MyDesktop.isDesktopSupported()){
            MyDesktop desktop = MyDesktop.getDesktop();
            try {
                desktop.mail(new URI("mailto", email, null));
            } catch (Exception e) {
                mainFrame.showErrorMessage("Could not open the webpage.");
            }
        }
    }

    public static boolean writeImage(BufferedImage image, String extension, File file) throws IOException, AWTException {
        boolean successful = ImageIO.write(image, extension, file);
        if (!successful && extension.equalsIgnoreCase("gif")) {
            GifEncoder.writeFile(image, file);
            successful = true;
        }
        return successful;
    }

    public static String zipFile(ZipOutputStream zos, File file, String requestName, byte[] buf) throws IOException {
        String fileName = requestName == null ? file.getName() : requestName;
        zos.putNextEntry(new ZipEntry(fileName));
        FileInputStream fis = new FileInputStream(file);
        int read;
        while((read=fis.read(buf))>0){
            zos.write(buf, 0, read);
        }
        fis.close();
        return fileName;
    }


    public static String removeSyllablifyMarkings(String lyrics) {
        char[] lyricsChars = lyrics.toCharArray();
        boolean inParanthesis = false;
        StringBuilder sb = new StringBuilder(lyrics.length());
        for(int i=0;i<lyricsChars.length;i++){
            char c = lyricsChars[i];
            if(c=='(')inParanthesis=true;
            if(!inParanthesis){
                if(c!='-' && c!='_'){
                    sb.append(c);
                }else if(c=='-' && i<lyricsChars.length-1 && lyricsChars[i+1]=='-'){
                    sb.append('-');
                }
            }
            if(c==')')inParanthesis=false;
        }
        return sb.toString();
    }
}
