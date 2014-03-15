package songscribe.data;

/**
 * Created by Kávai on 2014.03.15..
 */
public class TupletIntervalData {

    public static final String SEPARATOR = ",";

    public static int getGrade(Interval tupletInterval) {
        String[] datas = tupletInterval.getData().split(SEPARATOR);
        return Integer.parseInt(datas[0]);
    }

    public static void setGrade(Interval tupletInterval, int grade) {
        String data = tupletInterval.getData();
        String[] datas = data != null ? data.split(SEPARATOR) : new String[]{};
        if (datas.length < 2) {
            tupletInterval.setData(Integer.toString(grade));
        } else {
            tupletInterval.setData(grade + SEPARATOR + datas[1]);
        }
    }

    public static boolean isVerticalAdjusted(Interval tupletInterval) {
        String[] datas = tupletInterval.getData().split(SEPARATOR);
        return datas.length > 1;
    }

    public static int getVerticalPosition(Interval tupletInterval) {
        String[] datas = tupletInterval.getData().split(SEPARATOR);
        return isVerticalAdjusted(tupletInterval) ? Integer.parseInt(datas[1]) : 0;
    }

    public static void setVerticalPosition(Interval tupletInterval, int position) {
        String[] datas = tupletInterval.getData().split(SEPARATOR);
        tupletInterval.setData(datas[0] + SEPARATOR + position);
    }

}
