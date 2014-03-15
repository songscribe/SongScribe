package songscribe.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Kávai on 2014.03.15..
 */
public class TupletIntervalDataTest {
    @Test
    public void testGetGrade() throws Exception {
        Interval interval = new Interval(0, 0, "3");
        assertEquals(3, TupletIntervalData.getGrade(interval));
    }

    @Test
    public void testGetGrade2() throws Exception {
        Interval interval = new Interval(0, 0, "3,2");
        assertEquals(3, TupletIntervalData.getGrade(interval));
    }

    @Test
    public void testSetGrade() throws Exception {
        Interval interval = new Interval(0, 0, null);
        TupletIntervalData.setGrade(interval, 5);
        assertEquals(5, TupletIntervalData.getGrade(interval));
    }

    @Test
    public void testSetGrade2() throws Exception {
        Interval interval = new Interval(0, 0, "3,2");
        TupletIntervalData.setGrade(interval, 5);
        assertEquals(5, TupletIntervalData.getGrade(interval));
        assertEquals("5,2", interval.getData());
    }

    @Test
    public void testIsVerticalAdjusted() throws Exception {
        Interval interval = new Interval(0, 0, "3");
        assertFalse(TupletIntervalData.isVerticalAdjusted(interval));
    }

    @Test
    public void testIsVerticalAdjusted2() throws Exception {
        Interval interval = new Interval(0, 0, "3,2");
        assertTrue(TupletIntervalData.isVerticalAdjusted(interval));
    }

    @Test
    public void testGetVerticalPosition() throws Exception {
        Interval interval = new Interval(0, 0, "3,2");
        assertEquals(2, TupletIntervalData.getVerticalPosition(interval));
    }

    @Test
    public void testSetVerticalPosition() throws Exception {
        Interval interval = new Interval(0, 0, "3");
        TupletIntervalData.setVerticalPosition(interval, 5);
        assertEquals(5, TupletIntervalData.getVerticalPosition(interval));
        assertEquals("3,5", interval.getData());
    }

    @Test
    public void testSetVerticalPosition2() throws Exception {
        Interval interval = new Interval(0, 0, "3,4");
        TupletIntervalData.setVerticalPosition(interval, 5);
        assertEquals(5, TupletIntervalData.getVerticalPosition(interval));
        assertEquals("3,5", interval.getData());
    }
}
