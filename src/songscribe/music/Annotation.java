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

Created on Jul 23, 2006
*/
package songscribe.music;

import songscribe.ui.MusicSheet;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class Annotation {
    public static final int ABOVE = -4*MusicSheet.LINEDIST;
    public static final int BELOW = 8*MusicSheet.LINEDIST;
    private String annotation;
    private float xalignment = Component.LEFT_ALIGNMENT;
    private int yPos = ABOVE; 

    public Annotation(String annotation) {
        this.annotation = annotation;
    }

    public Annotation(String annotation, float xalignment) {
        this.annotation = annotation;
        this.xalignment = xalignment;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    public float getXalignment() {
        return xalignment;
    }

    public void setXalignment(float xalignment) {
        this.xalignment = xalignment;
    }

    public int getyPos() {
        return yPos;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
    }
}
