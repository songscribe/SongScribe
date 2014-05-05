package songscribe.ui.mainframeactions;

import org.junit.Before;
import org.junit.Test;
import songscribe.music.Crotchet;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Kávai on 2014.05.03..
 */
public class ExportLilypondAnnotationActionTest {
    private ExportLilypondAnnotationAction action;

    @Before
    public void setUp() throws Exception {
        action = new ExportLilypondAnnotationAction(null);

    }

    @Test
    public void testTranslatePitch() {
        assertEquals("d'''", action.translatePitch(-9));
        assertEquals("c'''", action.translatePitch(-8));
        assertEquals("b''", action.translatePitch(-7));
        assertEquals("c''", action.translatePitch(-1));
        assertEquals("b'", action.translatePitch(0));
        assertEquals("c'", action.translatePitch(6));
        assertEquals("b", action.translatePitch(7));
        assertEquals("e", action.translatePitch(11));
    }
}
