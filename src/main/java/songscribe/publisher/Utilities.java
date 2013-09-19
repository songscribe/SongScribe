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

Created on May 6, 2007
*/
package songscribe.publisher;

import songscribe.ui.MusicSheet;

/**
 * @author Csaba Kávai
 */
public class Utilities {

    /**
     * Takes pixel and returns inch or mm.
     * @param pixel
     * @param mode 0 or 1
     * @return if mode 0, returns in inch, otherwise in mm
     */
    public static double convertFromPixels(int pixel, int mode){
        return mode==0 ? Math.round((double)pixel/MusicSheet.RESOLUTION*100.0)/100.0 : Math.round((double)pixel/MusicSheet.RESOLUTION*25.4);
    }

    /**
     * Takes inch or mm and returns pixel
     * @param length if mode 0, this is inch, otherwise this is mm
     * @param mode 0 or 1
     * @return pixel
     */
    public static int convertToPixel(double length, int mode){
        return mode==0 ? (int)Math.round(length*MusicSheet.RESOLUTION) : (int)Math.round(length/25.4*MusicSheet.RESOLUTION);
    }


}
