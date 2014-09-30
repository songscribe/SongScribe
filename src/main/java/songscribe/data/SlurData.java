/*
    SongScribe song notation program
    Copyright (C) 2014 Csaba Kavai

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
*/
package songscribe.data;

public class SlurData {
    private int xPos1, xPos2, yPos1, yPos2, ctrlY;

    public SlurData(int xPos1, int xPos2, int yPos1, int yPos2, int ctrlY) {
        this.xPos1 = xPos1;
        this.xPos2 = xPos2;
        this.yPos1 = yPos1;
        this.yPos2 = yPos2;
        this.ctrlY = ctrlY;
    }

    public SlurData(String data) {
        if (data != null) {
            String[] split = data.split(",");
            xPos1 = Integer.parseInt(split[0]);
            yPos1 = Integer.parseInt(split[1]);
            xPos2 = Integer.parseInt(split[2]);
            yPos2 = Integer.parseInt(split[3]);
            ctrlY = Integer.parseInt(split[4]);
        }
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d,%d,%d", xPos1, yPos1, xPos2, yPos2, ctrlY);
    }

    public int getXPos1() {
        return xPos1;
    }

    public int getXPos2() {
        return xPos2;
    }

    public int getYPos1() {
        return yPos1;
    }

    public int getYPos2() {
        return yPos2;
    }

    public int getCtrlY() {
        return ctrlY;
    }

    public void setXPos1(int xPos1) {
        this.xPos1 = xPos1;
    }

    public void setXPos2(int xPos2) {
        this.xPos2 = xPos2;
    }

    public void setYPos1(int yPos1) {
        this.yPos1 = yPos1;
    }

    public void setYPos2(int yPos2) {
        this.yPos2 = yPos2;
    }

    public void setCtrlY(int ctrlY) {
        this.ctrlY = ctrlY;
    }
}
