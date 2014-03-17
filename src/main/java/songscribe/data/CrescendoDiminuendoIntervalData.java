package songscribe.data;

/**
 * Created by Kávai on 2014.03.17..
 */
public class CrescendoDiminuendoIntervalData {
    public static final String SEPARATOR = ",";

    public static int getX1Shift(Interval interval) {
        if (interval.getData() == null) {
            return 0;
        } else {
            return Integer.parseInt(interval.getData().split(SEPARATOR)[0]);
        }
    }

    public static int getX2Shift(Interval interval) {
        if (interval.getData() == null) {
            return 0;
        } else {
            return Integer.parseInt(interval.getData().split(SEPARATOR)[1]);
        }
    }

    public static int getYShift(Interval interval) {
        if (interval.getData() == null) {
            return 0;
        } else {
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
