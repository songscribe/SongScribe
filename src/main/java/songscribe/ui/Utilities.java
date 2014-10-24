/*
    SongScribe song notation program
    Copyright (C) 2006 Csaba Kavai

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
import sun.font.Font2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Csaba Kávai
 */
public class Utilities {
    public static String[] fontFamilyNames;
    private static Logger logger = Logger.getLogger(Utilities.class);
    private static boolean isMac;
    private static boolean isWindows;
    private static boolean isLinux;
    // An array of every actual font in the system, plus those included with SongScribe,
    // with all stylistic variations.
    private static Font[] systemFonts;
    // An array of base font names for each font in systemFonts.
    // For example, if the systemFont name is "MyriadPro-It", the base name is "MyriadPro".
    // This saves a lot of time when trying to find font names.
    private static String[] systemFontBaseNames;
    // For each font style, there are several possible font name suffix components
    // that might appear in the full font name.
    private static String[] plainFontSuffixNames = { "", "Regular", "Medium" };
    private static String[] italicFontSuffixNames = { "It", "Italic", "Oblique", "ItalicMT" };
    private static String[] boldFontSuffixNames = { "Bold", "Semibold", "Demibold", "BoldMT" };

    static {
        String os = System.getProperty("os.name").toLowerCase();
        isMac = os.contains("mac");
        isWindows = os.contains("windows");
        isLinux = os.contains("linux");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        systemFonts = ge.getAllFonts();

        List<Font> sortedFonts = new ArrayList<>(systemFonts.length);
        sortedFonts.addAll(Arrays.asList(systemFonts));

        // Remove any system occurrences of Source Sans Pro
        sortedFonts.removeIf(new Predicate<Font>() {
            @Override
            public boolean test(Font font) {
                return font.getFamily().equals("Source Sans Pro");
            }
        });

        // Alphabetical order, the fonts will be inserted into sortedFonts
        String suffixes[] = {
                "Regular", "Italic", "Bold", "BoldItalic"
        };

        int styles[] = {
                Font.PLAIN, Font.ITALIC, Font.BOLD, Font.BOLD + Font.ITALIC
        };

        for (int i = 0; i < styles.length; ++i) {
            String name = "";

            try {
                name = "SourceSansPro-" + suffixes[i];
                Font font = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/" + name + ".ttf"));
                sortedFonts.add(font);
            }
            catch (Exception e) {
                throw new RuntimeException("Cannot load font " + name, e);
            }
        }

        systemFonts = sortedFonts.toArray(new Font[sortedFonts.size()]);
        Arrays.sort(systemFonts, new Comparator<Font>() {
            @Override
            public int compare(Font font1, Font font2) {
                return font1.getPSName().compareTo(font2.getPSName());
            }
        });
        systemFontBaseNames = new String[systemFonts.length];
        List<String> familyNames = new ArrayList<>();

        for (int i = 0; i < systemFonts.length; ++i) {
            Font font = systemFonts[i];
            String name = font.getPSName();
            String[] parts = name.split("-");
            systemFontBaseNames[i] = parts[0];
            String familyName = font.getFamily();

            if (!familyNames.contains(familyName)) {
                familyNames.add(familyName);
            }
        }

        fontFamilyNames = familyNames.toArray(new String[familyNames.size()]);
        Arrays.sort(fontFamilyNames, new Comparator<String>() {
            public int compare(String name1, String name2) {
                return name1.compareTo(name2);
            }
        });
    }

    public static String getSongTitleFileNameForFileChooser(MusicSheet musicSheet) {
        StringBuilder sb = new StringBuilder(musicSheet.getComposition().getSongTitle().length() + 10);

        try {
            int number = Integer.parseInt(musicSheet.getComposition().getNumber());
            sb.append(String.format("%03d", number));
        }
        catch (NumberFormatException nfe) {
            sb.append(musicSheet.getComposition().getNumber());
        }

        if (musicSheet.getComposition().getNumber().length() > 0) {
            sb.append(' ');
        }

        for (char c : musicSheet.getComposition().getSongTitle().toCharArray()) {
            Character specialCharMapped = mapSpecialChar(c);

            if (specialCharMapped != null) {
                sb.append(specialCharMapped);
            }
            else if (Character.isLetterOrDigit(c) || c == ' ') {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static Character mapSpecialChar(char c) {
        for (int i = 0; i < LyricsDialog.specChars.length; i++) {
            for (int j = 0; j < LyricsDialog.specChars[i].length; j++) {
                if (c == LyricsDialog.specChars[i][j]) {
                    return LyricsDialog.specCharsMap[i][j];
                }
            }
        }

        return null;
    }

    public static int arrayIndexOf(Object[] array, Object element) {
        for (int i = 0; i < array.length; i++) {
            if (element.equals(array[i])) {
                return i;
            }
        }

        return -1;
    }

    public static int lineCount(String str) {
        if (str.length() == 0) {
            return 0;
        }

        int found = 0;

        for (char ch : str.toCharArray()) {
            if (ch == '\n') {
                found++;
            }
        }

        if (str.charAt(str.length() - 1) != '\n') {
            found++;
        }

        return found;
    }

    public static void readComboValuesFromFile(JComboBox combo, File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                combo.addItem(line);
            }

            br.close();
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Could not open a necessary file. Please reinstall the software.", MainFrame.PACKAGE_NAME, JOptionPane.ERROR_MESSAGE);
            logger.error("readComboValuesFromFile open", e);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not open a necessary file. Please reinstall the software.", MainFrame.PACKAGE_NAME, JOptionPane.ERROR_MESSAGE);
            logger.error("readComboValuesFromFile open", e);
        }
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

    /*
        Given a font, base font name, and set of suffixes to add to the base name,
        return the matching base+suffix. If exactMatch is false, it's a match if
        the font name starts with base+suffix. If there is no match, return null.
    */
    private static String matchFontName(Font font, String baseName, String[] suffixes, boolean exactMatch) {
        String fontName = font.getPSName();
        boolean hasSuffix = baseName.contains("-");

        for (String suffix : suffixes) {
            if (!hasSuffix && suffix.length() > 0) {
                suffix = "-" + suffix;
            }

            String name = baseName + suffix;

            if (exactMatch) {
                if (fontName.equals(name)) {
                    return name;
                }
            }
            else if (fontName.startsWith(name)) {
                return name;
            }
        }

        return null;
    }

    /*
        Major hack alert!

        There is a bug in Oracle Java's Font2D.setStyle method
        that does not set the style of the Font2D correctly for
        some fonts, because the style matching algorithm is broken.
        This method forces the Font2D style to be the requested style.
    */
    public static void fixFont2DStyle(Font font, int style) {
        Map<String, Object> result = get2DStyleFieldForFont(font);

        if (result != null) {
            try {
                Field styleField = (Field) result.get("field");
                Font2D font2d = (Font2D) result.get("font");
                styleField.setInt(font2d, style);
            }
            catch (Exception ex) {
                // Oh well, we tried
            }
        }
    }

    public static Map<String, Object> get2DStyleFieldForFont(Font font) {
        try {
            Class<?>[] params = new Class[0];
            Method method = font.getClass().getDeclaredMethod("getFont2D", params);
            method.setAccessible(true);
            Font2D font2d = (Font2D) method.invoke(font);

            if (font2d != null) {
                // The style field we want to set is in the Font2D class, get that class
                Field styleField = Font2D.class.getDeclaredField("style");
                styleField.setAccessible(true);
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("font", font2d);
                result.put("field", styleField);
                return result;
            }
        }
        catch (Exception ex) {
            // Oh well, we tried
        }

        return null;
    }

    /*
        The built in Java font matching algorithms are pretty useless.
        This method will return the closest matching font for a given style.
        If bold+italic is requested, it will fall back to bold if available.
        Any style other than plain if not available will fall back to plain.
        If plain is not available, null is returned.

        The fixFont2D hack is applied if a matching font is found.
    */
    public static Font createFont(String familyName, int style, int size) {
        Font foundFont = null;

        for (int i = 0; i < systemFonts.length; ++i) {
            Font font = systemFonts[i];

            if (font.getFamily().equals(familyName)) {
                String baseName = systemFontBaseNames[i];

                if (style == Font.PLAIN) {
                    String name = matchFontName(font, baseName, plainFontSuffixNames, true);

                    // If we can't find the plain font, fail
                    if (name != null) {
                        foundFont = font;
                        break;
                    }
                }
                else if ((style & Font.BOLD) != 0) {
                    boolean wantItalic = (style & Font.ITALIC) != 0;
                    String name = matchFontName(font, baseName, boldFontSuffixNames, !wantItalic);

                    // If italic is also desired, try adding italic suffixes
                    if (name != null && wantItalic) {
                        name = matchFontName(font, name, italicFontSuffixNames, true);
                    }

                    if (name != null) {
                        foundFont = font;
                        break;
                    }
                }
                else if (style == Font.ITALIC) {
                    String name = matchFontName(font, baseName, italicFontSuffixNames, true);

                    if (name != null) {
                        foundFont = font;
                        break;
                    }
                }
            }
        }

        if (foundFont == null) {
            if (style == (Font.BOLD | Font.ITALIC)) {
                // If we can't find bold+italic, fall back to bold
                foundFont = createFont(familyName, Font.BOLD, size);
            }
            else if (style != Font.PLAIN) {
                // If we get here and no styled font is found, fall back to plain.
                foundFont = createFont(familyName, Font.PLAIN, size);
            }
        }

        // If all attempts fail, use Java's built in search
        if (foundFont == null) {
            foundFont = new Font(familyName, style, size);
        }
        else {
            // Always use plain for the style, if we get here we have
            // a styled font variant already.
            foundFont = foundFont.deriveFont(Font.PLAIN, (float) size);
            fixFont2DStyle(foundFont, style);
        }

        return foundFont;
    }

    public static boolean fontHasStyle(Font font, int style) {
        boolean isBold = false, isItalic = false;
        Map<String, Object> result = get2DStyleFieldForFont(font);

        if (result != null) {
            try {
                Font2D font2d = (Font2D) result.get("font");
                Field styleField = (Field) result.get("field");
                int style2d = styleField.getInt(font2d);
                isBold = (style2d & Font.BOLD) != 0;
                isItalic = (style2d & Font.ITALIC) != 0;
            }
            catch (Exception ex) {
                result = null;
            }
        }

        if (result == null) {
            String fontName = font.getPSName();
            boolean hasDash = fontName.indexOf("-") > 0;

            for (String suffix : boldFontSuffixNames) {
                if (isBold) {
                    break;
                }

                if (hasDash) {
                    suffix = "-" + suffix;
                }

                if (fontName.endsWith(suffix)) {
                    isBold = true;
                    break;
                }
                else {
                    for (String italicSuffix : italicFontSuffixNames) {
                        if (fontName.endsWith(suffix + italicSuffix)) {
                            isBold = true;
                            isItalic = true;
                            break;
                        }
                    }
                }
            }

            if (!isBold) {
                for (String suffix : italicFontSuffixNames) {
                    if (hasDash) {
                        suffix = "-" + suffix;
                    }

                    if (fontName.endsWith(suffix)) {
                        isItalic = true;
                        break;
                    }
                }
            }
        }

        if (style == Font.PLAIN) {
            return (!isBold && !isItalic);
        }
        else if ((style & Font.BOLD) != 0) {
            return isBold;
        }
        else if ((style & Font.ITALIC) != 0) {
            return isItalic;
        }

        return false;
    }

    public static boolean isBold(Font font) {
        return fontHasStyle(font, Font.BOLD);
    }

    public static boolean isItalic(Font font) {
        return fontHasStyle(font, Font.ITALIC);
    }

    public static Font deriveFont(Font font, int style, int size) {
        return createFont(font.getFamily(), style, size);
    }

    public static String getPublicVersion() {
        return Version.PUBLIC_VERSION;
    }

    public static String getFullVersion() {
        return Version.PUBLIC_VERSION + "." + Version.BUILD_VERSION;
    }

    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static void copyFile(File in, File out) throws IOException {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i;

        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }

        fis.close();
        fos.close();
    }

    public static void openExportFile(MainFrame mainFrame, File file) {
        if (MyDesktop.isDesktopSupported()) {
            MyDesktop desktop = MyDesktop.getDesktop();

            if (JOptionPane.showConfirmDialog(mainFrame, "Do you want to open the file?", mainFrame.PROG_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
                JOptionPane.YES_OPTION) {
                try {
                    desktop.open(file);
                }
                catch (Exception e) {
                    mainFrame.showErrorMessage("Could not open the file.");
                }
            }
        }
    }

    public static void openWebPage(MainFrame mainFrame, String webPage) {
        if (MyDesktop.isDesktopSupported()) {
            MyDesktop desktop = MyDesktop.getDesktop();

            try {
                desktop.browse(new URI(webPage));
            }
            catch (Exception e) {
                mainFrame.showErrorMessage("Could not open the webpage.");
            }
        }
    }

    public static void openEmail(MainFrame mainFrame, String email) {
        if (MyDesktop.isDesktopSupported()) {
            MyDesktop desktop = MyDesktop.getDesktop();

            try {
                desktop.mail(new URI("mailto", email, null));
            }
            catch (Exception e) {
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

        while ((read = fis.read(buf)) > 0) {
            zos.write(buf, 0, read);
        }

        fis.close();
        return fileName;
    }


    public static String removeSyllabifyMarkings(String lyrics) {
        char[] lyricsChars = lyrics.toCharArray();
        boolean inParanthesis = false;
        StringBuilder sb = new StringBuilder(lyrics.length());

        for (int i = 0; i < lyricsChars.length; i++) {
            char c = lyricsChars[i];

            if (c == '(') {
                inParanthesis = true;
            }

            if (!inParanthesis) {
                if (c != '-' && c != '_') {
                    sb.append(c);
                }
                else if (c == '-' && i < lyricsChars.length - 1 && lyricsChars[i + 1] == '-') {
                    sb.append('-');
                }
            }

            if (c == ')') {
                inParanthesis = false;
            }
        }

        return sb.toString();
    }
}
