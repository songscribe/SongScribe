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

Created on Jan 10, 2007
*/
package songscribe;

import songscribe.ui.Constants;

import java.io.*;
import java.util.zip.Adler32;

/**
 * @author Csaba Kávai
 */
public class ChecksumMaker {

    private static byte[] buf = new byte[1024];
    public static final String HEADER = "SongScribe update";

    public static void main(String[] args) throws IOException {
        PrintWriter pw = new PrintWriter(Constants.CHECKSUMSFILENAME);
        pw.println(HEADER);
        writeCheckSum(pw, new File("."));
        pw.close();
    }

    private static void writeCheckSum(PrintWriter pw, File file) throws IOException {
        if(file.isDirectory()){
            for(String df:file.list()){
                if(!df.equals(Constants.CHECKSUMSFILENAME))writeCheckSum(pw, new File(file, df));
            }
        }else{
            String path = file.getPath().replace('\\', '/');
            if(path.startsWith("./"))path = path.substring(2);
            pw.print(path);
            pw.print(' ');
            pw.print(file.length());
            pw.print(' ');
            pw.println(getChecksum(file));
        }
    }

    public static long getChecksum(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        Adler32 adler32 = new Adler32();
        int read;
        while((read=fis.read(buf))>0){
            adler32.update(buf, 0, read);
        }
        fis.close();
        return adler32.getValue();
    }
}
