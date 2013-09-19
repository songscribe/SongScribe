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

Created on Nov 5, 2007
*/
package songscribe.publisher;

import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class PageNumber {
    public enum Alignment{
        LEFT("Left"),
        CENTER("Center"),
        RIGHT("Right"),
        BOOKOUTER("Book - outer"),
        BOOKINNER("Book - inner");

        private String description;

        private Alignment(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
    
    public enum Placement{
        TOP("Top"),
        BOTTOM("Bottom");

        private String description;

        private Placement(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private Font font;
    private Alignment alignment;
    private Placement placement;
    private int fromPage;
    private int spaceFromMargin;
    
    public Alignment getAlignment() {
        return alignment;
    }

    public Font getFont() {
        return font;
    }

    public int getFromPage() {
        return fromPage;
    }

    public Placement getPlacement() {
        return placement;
    }

    public int getSpaceFromMargin() {
        return spaceFromMargin;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setFromPage(int fromPage) {
        this.fromPage = fromPage;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public void setSpaceFromMargin(int spaceFromMargin) {
        this.spaceFromMargin = spaceFromMargin;
    }
}
