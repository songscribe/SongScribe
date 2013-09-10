package songscribe.ui.mainframeactions;

import org.junit.Before;
import org.junit.Test;
import songscribe.data.Fraction;
import songscribe.music.*;

import static junit.framework.Assert.assertEquals;

public class ExportABCAnnotationActionTest {
    ExportABCAnnotationAction action;

    @Before
    public void init() {
        action = new ExportABCAnnotationAction(null);
    }

    @Test
    public void testTranslateUnitLength() {

        assertEquals(new Fraction(1, 1), action.translateUnitLength(Tempo.Type.SEMIBREVE.getNote().getDuration()));
        assertEquals(new Fraction(1,2), action.translateUnitLength(Tempo.Type.MINIM.getNote().getDuration()));
        assertEquals(new Fraction(3,4), action.translateUnitLength(Tempo.Type.MINIMDOTTED.getNote().getDuration()));
        assertEquals(new Fraction(1,4), action.translateUnitLength(Tempo.Type.CROTCHET.getNote().getDuration()));
        assertEquals(new Fraction(3,8), action.translateUnitLength(Tempo.Type.CROTCHETDOTTED.getNote().getDuration()));
        assertEquals(new Fraction(1,8), action.translateUnitLength(Tempo.Type.QUAVER.getNote().getDuration()));
        assertEquals(new Fraction(3,16), action.translateUnitLength(Tempo.Type.QUAVERDOTTED.getNote().getDuration()));
    }

    @Test
    public void testTranslatePitch() {
        assertPitch("c''", -15);
        assertPitch("b'", -14);
        assertPitch("c'", -8);
        assertPitch("b", -7);
        assertPitch("c", -1);
        assertPitch("B", 0);
        assertPitch("C", 6);
        assertPitch("B,", 7);
        assertPitch("E,", 11);
        assertPitch("C,", 13);
        assertPitch("B,,", 14);
    }

    private void assertPitch(String expected, int yPos) {
        Crotchet crotchet = new Crotchet();
        crotchet.setYPos(yPos);
        assertEquals(expected, action.translatePitch(crotchet));
    }

    @Test
    public void testTranslateAccidental() {
        assertEquals("", action.translateAccidental(Note.Accidental.NONE));
        assertEquals("=", action.translateAccidental(Note.Accidental.NATURAL));
        assertEquals("_", action.translateAccidental(Note.Accidental.FLAT));
        assertEquals("^", action.translateAccidental(Note.Accidental.SHARP));
        assertEquals("==", action.translateAccidental(Note.Accidental.DOUBLENATURAL));
        assertEquals("__", action.translateAccidental(Note.Accidental.DOUBLEFLAT));
        assertEquals("^^", action.translateAccidental(Note.Accidental.DOUBLESHARP));
        assertEquals("=_", action.translateAccidental(Note.Accidental.NATURALFLAT));
        assertEquals("=^", action.translateAccidental(Note.Accidental.NATURALSHARP));
    }

    @Test
    public void testTranslateNoteLength() {
        assertEquals("/32", action.translateNoteLength(new Demisemiquaver().getDuration()));
        assertEquals("3/64", action.translateNoteLength(makeDotted(new Demisemiquaver(), 1).getDuration()));
        assertEquals("7/128", action.translateNoteLength(makeDotted(new Demisemiquaver(), 2).getDuration()));

        assertEquals("/16", action.translateNoteLength(new Semiquaver().getDuration()));
        assertEquals("3/32", action.translateNoteLength(makeDotted(new Semiquaver(), 1).getDuration()));
        assertEquals("7/64", action.translateNoteLength(makeDotted(new Semiquaver(), 2).getDuration()));

        assertEquals("/8", action.translateNoteLength(new Quaver().getDuration()));
        assertEquals("3/16", action.translateNoteLength(makeDotted(new Quaver(), 1).getDuration()));
        assertEquals("7/32", action.translateNoteLength(makeDotted(new Quaver(), 2).getDuration()));

        assertEquals("/4", action.translateNoteLength(new Crotchet().getDuration()));
        assertEquals("3/8", action.translateNoteLength(makeDotted(new Crotchet(), 1).getDuration()));
        assertEquals("7/16", action.translateNoteLength(makeDotted(new Crotchet(), 2).getDuration()));

        assertEquals("/2", action.translateNoteLength(new Minim().getDuration()));
        assertEquals("3/4", action.translateNoteLength(makeDotted(new Minim(), 1).getDuration()));
        assertEquals("7/8", action.translateNoteLength(makeDotted(new Minim(), 2).getDuration()));

        assertEquals("", action.translateNoteLength(new Semibreve().getDuration()));
        assertEquals("3/2", action.translateNoteLength(makeDotted(new Semibreve(), 1).getDuration()));
        assertEquals("7/4", action.translateNoteLength(makeDotted(new Semibreve(), 2).getDuration()));
    }

    private Note makeDotted(Note note, int dotted) {
        note.setDotted(dotted);
        return note;
    }

}
