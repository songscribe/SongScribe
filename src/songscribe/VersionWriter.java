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

Created on Jan 30, 2007
*/
package songscribe;

import songscribe.ui.Constants;
import songscribe.ui.Utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Csaba Kávai
 */
public class VersionWriter {
    public static void main(String[] args) throws IOException {
        if(args[0].equals("full")){
            System.out.println(Utilities.getFileVersion());
        } else if(args[0].equals("normal")) {
            System.out.println(Utilities.getPublicVersion());
        } else if(args[0].equals("checksum")) {
            Properties versionProperties = new Properties();
            versionProperties.load(new FileInputStream(Utilities.VERSION_PROPERTIES));
            versionProperties.setProperty(Constants.CHECKSUM_VERSION_PROP,
                    Integer.toString(Utilities.getVersionChecksum(versionProperties)));
            versionProperties.store(new FileWriter(Utilities.VERSION_PROPERTIES), null);
        }
    }
}
