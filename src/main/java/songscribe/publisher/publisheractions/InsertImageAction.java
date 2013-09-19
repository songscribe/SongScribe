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

Created on Oct 3, 2006
*/
package songscribe.publisher.publisheractions;

import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.PImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class InsertImageAction extends AbstractAction{
    private Publisher publisher;
    private PlatformFileDialog pfd;

    public InsertImageAction(Publisher publisher) {
        this.publisher = publisher;
        putValue(NAME, "Image");
        putValue(SMALL_ICON, new ImageIcon(Publisher.getImage("insertimage.png")));
        pfd = new PlatformFileDialog(publisher, "Open", true, new MyAcceptFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
    }

    public void actionPerformed(ActionEvent e) {
        if(publisher.isBookNull())return;
        if(pfd.showDialog()){
            File openFile = pfd.getFile();
            Image image = Publisher.getImage(openFile);
            if(image!=null){
                publisher.getBook().insertPageComponent(new PImage(image, 0, 0, publisher.getBook().getNotGreaterResolution(image.getWidth(null), image.getHeight(null)), openFile));
            }
        }
    }
}
