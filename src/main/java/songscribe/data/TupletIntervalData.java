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

    Created by Himadri on 2014.03.15
*/
package songscribe.data;

public class TupletIntervalData {

    public static final String SEPARATOR = ",";

    public static int getGrade(Interval tupletInterval) {
        String[] data = tupletInterval.getData().split(SEPARATOR);
        return Integer.parseInt(data[0]);
    }

    public static void setGrade(Interval tupletInterval, int grade) {
        String data = tupletInterval.getData();
        String[] datas = data != null ? data.split(SEPARATOR) : new String[] { };

        if (datas.length < 2) {
            tupletInterval.setData(Integer.toString(grade));
        }
        else {
            tupletInterval.setData(grade + SEPARATOR + datas[1]);
        }
    }

    public static boolean isVerticalAdjusted(Interval tupletInterval) {
        String[] data = tupletInterval.getData().split(SEPARATOR);
        return data.length > 1;
    }

    public static int getVerticalPosition(Interval tupletInterval) {
        String[] data = tupletInterval.getData().split(SEPARATOR);
        return isVerticalAdjusted(tupletInterval) ? Integer.parseInt(data[1]) : 0;
    }

    public static void setVerticalPosition(Interval tupletInterval, int position) {
        String[] data = tupletInterval.getData().split(SEPARATOR);
        tupletInterval.setData(data[0] + SEPARATOR + position);
    }
}
