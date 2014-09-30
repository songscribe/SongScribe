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

    Created on Oct 3, 2006
*/
package songscribe.publisher.pagecomponents;

import songscribe.publisher.PImageDialog;
import songscribe.publisher.Publisher;
import songscribe.publisher.publisheractions.PImageReloadAction;
import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;

/**
 * @author Csaba Kávai
 */
public class PImage extends PageComponent {
    private static PImageDialog imageDialog;
    private static JPopupMenu popupMenu;
    private Image image;
    private File imageFile;

    public PImage(Image image, int xPos, int yPos, double resolution, File imageFile) {
        super(new Rectangle(xPos, yPos, image.getWidth(null), image.getHeight(null)), resolution);
        this.image = image;
        this.imageFile = imageFile;
    }

    public void paintComponent(Graphics2D g2) {
        AffineTransform at = g2.getTransform();
        g2.translate(pos.x, pos.y);
        g2.scale(resolution, resolution);
        g2.drawImage(image, 0, 0, null);
        g2.setTransform(at);
    }

    public Image getImage() {
        return image;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public void reload() {
        image = Publisher.getImage(imageFile);
        pos.width = (int) Math.round(image.getWidth(null) * resolution);
        pos.height = (int) Math.round(image.getHeight(null) * resolution);
    }

    public MyDialog getPropertiesDialog(Publisher publisher) {
        if (imageDialog == null) {
            imageDialog = new PImageDialog(publisher);
        }

        return imageDialog;
    }

    public JPopupMenu getPopupMenu(Publisher publisher) {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu("Image");
            popupMenu.add(new PImageReloadAction(publisher));
            addCommonPopups(publisher, popupMenu);
        }

        return popupMenu;
    }
}
