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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * @author Csaba Kávai
 */
public class UpdateMaker {
    public static void main(String[] args) throws IOException {
        if(args.length!=1)return;
        File base = new File(args[0]);        
        Properties props = new Properties();
        props.load(new FileInputStream("conf/defprops"));
        String updateBaseURL = props.getProperty(Constants.UPDATEURL1);
        HttpClient httpClient = new HttpClient();
        GetMethod getChecksum = new GetMethod(updateBaseURL+ Constants.CHECKSUMSFILENAME);
        getChecksum.addRequestHeader(Constants.MAXAGEHEADER);
        httpClient.executeMethod(getChecksum);
        BufferedReader br = new BufferedReader(new InputStreamReader(getChecksum.getResponseBodyAsStream()));
        if(!br.readLine().equals(ChecksumMaker.HEADER)){
            System.out.println("Hiba");
            return;
        }
        String line;
        while((line=br.readLine())!=null){
            int spacePos1 = line.lastIndexOf(' ');
            int spacePos2 = line.lastIndexOf(' ', spacePos1-1);
            File file = new File(base, line.substring(0, spacePos2));
            long remoteChecksum = Long.parseLong(line.substring(spacePos1+1));
            long localChecksum = file.exists() ? ChecksumMaker.getChecksum(file) : -1;
            if(localChecksum==remoteChecksum){
                file.delete();
            }
            System.out.println(file.getPath()+", "+file.length()+", "+line.substring(spacePos2+1, spacePos1));
        }
        br.close();
        getChecksum.releaseConnection();        
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
