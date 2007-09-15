/*  
Music of The Supreme song notation program
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

Created on Sep 12, 2007
*/
package songscribe;

import songscribe.ui.Constants;

import java.util.Properties;
import java.io.*;
import java.net.URL;

/**
 * @author Csaba Kávai
 */
public class UpdateMaker {
    public static void main(String[] args) throws IOException {
        if(args.length==0)return;
        File base = new File(args[0]);
        Properties props = new Properties();
        props.load(new FileInputStream("conf/defprops"));
        String updateBaseURL = props.getProperty(Constants.UPDATEURL);
        if(updateBaseURL.charAt(updateBaseURL.length()-1)!='/')updateBaseURL+="/";
        BufferedReader br = new BufferedReader(new InputStreamReader(new URL(updateBaseURL+"checksums").openStream()));
        String line;
        while((line=br.readLine())!=null){
            int spacePos1 = line.lastIndexOf(' ');
            int spacePos2 = line.lastIndexOf(' ', spacePos1-1);
            File file = new File(base, line.substring(0, spacePos2));
            long remoteCS = Long.parseLong(line.substring(spacePos1+1));
            long localCS = file.exists() ? ChecksumMaker.getChecksum(file) : remoteCS-1;
            if(localCS==remoteCS){
                file.delete();
            }
        }
        br.close();
        deleteIfEmpty(base);
    }

    private static void deleteIfEmpty(File file){
        for(File f:file.listFiles()){
            if(f.isDirectory())deleteIfEmpty(f);
        }
        if(file.listFiles().length==0){
            file.delete();
        }
    }
}
