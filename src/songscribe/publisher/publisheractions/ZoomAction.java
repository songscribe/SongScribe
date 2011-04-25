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

Created on Sep 26, 2006
*/
package songscribe.publisher.publisheractions;

import songscribe.publisher.Publisher;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class ZoomAction implements ActionListener{
    private Publisher publisher;

    public ZoomAction(Publisher publisher) {
        this.publisher = publisher;
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        String zoomStr = (String)publisher.getZoomCombo().getSelectedItem();
        if(zoomStr.charAt(zoomStr.length()-1)=='%'){
            zoomStr = zoomStr.substring(0, zoomStr.length()-1);
        }
        int zoomInt;
        try{
            zoomInt = Integer.parseInt(zoomStr);
        }catch(NumberFormatException ex){
            zoomInt = 100;
        }
        zoomInt = Math.max(zoomInt, 20);
        zoomInt = Math.min(zoomInt, 1600);
        publisher.getZoomCombo().setSelectedItem(Integer.toString(zoomInt)+"%");
        publisher.getBook().setScale(zoomInt/100.0);
    }
}
