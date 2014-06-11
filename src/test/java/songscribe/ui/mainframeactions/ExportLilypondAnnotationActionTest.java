package songscribe.ui.mainframeactions;

import org.junit.Before;
import org.junit.Test;
import songscribe.music.BreathMark;
import songscribe.music.Crotchet;
import songscribe.music.CrotchetRest;
import songscribe.music.Demisemiquaver;
import songscribe.music.DemisemiquaverRest;
import songscribe.music.DoubleBarLine;
import songscribe.music.DurationArticulation;
import songscribe.music.FinalDoubleBarLine;
import songscribe.music.ForceArticulation;
import songscribe.music.GraceQuaver;
import songscribe.music.GraceSemiQuaver;
import songscribe.music.Minim;
import songscribe.music.MinimRest;
import songscribe.music.Note;
import songscribe.music.Quaver;
import songscribe.music.QuaverRest;
import songscribe.music.RepeatLeft;
import songscribe.music.RepeatLeftRight;
import songscribe.music.RepeatRight;
import songscribe.music.Semibreve;
import songscribe.music.SemibreveRest;
import songscribe.music.Semiquaver;
import songscribe.music.SemiquaverRest;
import songscribe.music.SingleBarLine;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Kávai on 2014.05.03..
 */
public class ExportLilypondAnnotationActionTest
{
    private ExportLilypondAnnotationAction action;

    @Before public void setUp() throws Exception
    {
        action = new ExportLilypondAnnotationAction(null);
    }

    @Test public void testTranslateArticulation() throws Exception
    {
        Note note = new Crotchet();

        for (DurationArticulation durationArticulation : DurationArticulation.values())
        {
            note.setDurationArticulation(durationArticulation);
            assertTrue(action.translateArticulation(note).startsWith("-"));
        }

        for (ForceArticulation forceArticulation : ForceArticulation.values())
        {
            note.setForceArticulation(forceArticulation);
            assertTrue(action.translateArticulation(note).startsWith("-"));
        }
    }

    @Test public void testTranslateDuration()
    {
        assertEquals("1", action.translateDuration(new Semibreve()));
        assertEquals("2", action.translateDuration(new Minim()));
        assertEquals("4", action.translateDuration(new Crotchet()));
        assertEquals("8", action.translateDuration(new Quaver()));
        assertEquals("16", action.translateDuration(new Semiquaver()));
        assertEquals("32", action.translateDuration(new Demisemiquaver()));
        assertEquals("1", action.translateDuration(new SemibreveRest()));
        assertEquals("2", action.translateDuration(new MinimRest()));
        assertEquals("4", action.translateDuration(new CrotchetRest()));
        assertEquals("8", action.translateDuration(new QuaverRest()));
        assertEquals("16", action.translateDuration(new SemiquaverRest()));
        assertEquals("32", action.translateDuration(new DemisemiquaverRest()));
        assertEquals("8", action.translateDuration(new GraceQuaver()));
        assertEquals("16", action.translateDuration(new GraceSemiQuaver()));
        assertEquals("", action.translateDuration(new RepeatLeft()));
        assertEquals("", action.translateDuration(new RepeatRight()));
        assertEquals("", action.translateDuration(new RepeatLeftRight()));
        assertEquals("", action.translateDuration(new BreathMark()));
        assertEquals("", action.translateDuration(new SingleBarLine()));
        assertEquals("", action.translateDuration(new DoubleBarLine()));
        assertEquals("", action.translateDuration(new FinalDoubleBarLine()));
    }
}
