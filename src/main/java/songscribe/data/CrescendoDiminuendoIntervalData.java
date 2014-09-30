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

    Created by Himadri on 2014.03.17
*/
package songscribe.data;

public class CrescendoDiminuendoIntervalData {
    public static final String SEPARATOR = ",";

    public static int getX1Shift(Interval interval) {
        if (interval.getData() == null) {
            return 0;
        }
        else {
            return Integer.parseInt(interval.getData().split(SEPARATOR)[0]);
        }
    }

    public static int getX2Shift(Interval interval) {
        if (interval.getData() == null) {
            return 0;
        }
        else {
            return Integer.parseInt(interval.getData().split(SEPARATOR)[1]);
        }
    }

    public static int getYShift(Interval interval) {
        if (interval.getData() == null) {
            return 0;
        }
        else {
            return Integer.parseInt(interval.getData().split(SEPARATOR)[2]);
        }
    }

    public static void setX1Shift(Interval interval, int x1Shift) {
        interval.setData(createDataString(x1Shift, getX2Shift(interval), getYShift(interval)));
    }

    public static void setX2Shift(Interval interval, int x2Shift) {
        interval.setData(createDataString(getX1Shift(interval), x2Shift, getYShift(interval)));
    }

    public static void setYShift(Interval interval, int yShift) {
        interval.setData(createDataString(getX1Shift(interval), getX2Shift(interval), yShift));
    }

    private static String createDataString(int x1Shift, int x2Shift, int yShift) {
        return x1Shift + SEPARATOR + x2Shift + SEPARATOR + yShift;
    }
}
