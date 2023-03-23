package songscribe.ui.mainframeactions;

import org.junit.Before;
import org.junit.Test;
import songscribe.data.Fraction;
import songscribe.music.Composition;
import songscribe.music.Crotchet;
import songscribe.music.Demisemiquaver;
import songscribe.music.KeyType;
import songscribe.music.Minim;
import songscribe.music.Note;
import songscribe.music.Quaver;
import songscribe.music.Semibreve;
import songscribe.music.Semiquaver;
import songscribe.music.Tempo;

import static junit.framework.Assert.assertEquals;

public class ExportABCAnnotationActionTest {
    ExportABCAnnotationAction action;

    @Before
    public void init() {
        action = new ExportABCAnnotationAction(null);
    }

    @Test
    public void testTranslateKey() {
        assertEquals("C# major", action.translateKey(KeyType.SHARPS, 7));
        assertEquals("F# major", action.translateKey(KeyType.SHARPS, 6));
        assertEquals("B major", action.translateKey(KeyType.SHARPS, 5));
        assertEquals("E major", action.translateKey(KeyType.SHARPS, 4));
        assertEquals("A major", action.translateKey(KeyType.SHARPS, 3));
        assertEquals("D major", action.translateKey(KeyType.SHARPS, 2));
        assertEquals("G major", action.translateKey(KeyType.SHARPS, 1));
        assertEquals("C major", action.translateKey(KeyType.SHARPS, 0));
        assertEquals("C major", action.translateKey(KeyType.FLATS, 0));
        assertEquals("F major", action.translateKey(KeyType.FLATS, 1));
        assertEquals("Bb major", action.translateKey(KeyType.FLATS, 2));
        assertEquals("Eb major", action.translateKey(KeyType.FLATS, 3));
        assertEquals("Ab major", action.translateKey(KeyType.FLATS, 4));
        assertEquals("Db major", action.translateKey(KeyType.FLATS, 5));
        assertEquals("Gb major", action.translateKey(KeyType.FLATS, 6));
        assertEquals("Cb major", action.translateKey(KeyType.FLATS, 7));
    }

    @Test
    public void testTranslateTempo() {
        assertEquals("\"Moderato\"", action.translateTempo(new Tempo(120, Tempo.Type.CROTCHET, "Moderato", false)));
        assertEquals("1/4=120 \"Moderato\"", action.translateTempo(new Tempo(120, Tempo.Type.CROTCHET, "Moderato", true)));
        assertEquals("3/8=140 \"Fast\"", action.translateTempo(new Tempo(140, Tempo.Type.CROTCHET_DOTTED, "Fast", true)));
        assertEquals("1/2=60 \"Slow\"", action.translateTempo(new Tempo(60, Tempo.Type.MINIM, "Slow", true)));
        assertEquals("3/4=70 \"Slow\"", action.translateTempo(new Tempo(70, Tempo.Type.MINIM_DOTTED, "Slow", true)));
        assertEquals("1/8=80 \"Very slow\"", action.translateTempo(new Tempo(80, Tempo.Type.QUAVER, "Very slow", true)));
        assertEquals("3/16=70 \"Very slow\"", action.translateTempo(new Tempo(70, Tempo.Type.QUAVER_DOTTED, "Very slow", true)));
        assertEquals("1/1=50 \"Even slower\"", action.translateTempo(new Tempo(50, Tempo.Type.SEMI_BREVE, "Even slower", true)));
    }

    @Test
    public void testTranslateUnitLength() {

        assertEquals(new Fraction(4,1), action.translateUnitLength(Tempo.Type.SEMI_BREVE.getNote().getDuration(), Composition.PPQ));
        assertEquals(new Fraction(2,1), action.translateUnitLength(Tempo.Type.MINIM.getNote().getDuration(), Composition.PPQ));
        assertEquals(new Fraction(3,1), action.translateUnitLength(Tempo.Type.MINIM_DOTTED.getNote().getDuration(), Composition.PPQ));
        assertEquals(new Fraction(1,1), action.translateUnitLength(Tempo.Type.CROTCHET.getNote().getDuration(), Composition.PPQ));
        assertEquals(new Fraction(3,2), action.translateUnitLength(Tempo.Type.CROTCHET_DOTTED.getNote().getDuration(), Composition.PPQ));
        assertEquals(new Fraction(1,2), action.translateUnitLength(Tempo.Type.QUAVER.getNote().getDuration(), Composition.PPQ));
        assertEquals(new Fraction(3,4), action.translateUnitLength(Tempo.Type.QUAVER_DOTTED.getNote().getDuration(), Composition.PPQ));
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
        assertEquals(expected, action.translatePitch(crotchet.getYPos()));
    }

    @Test
    public void testTranslateAccidental() {
        assertEquals("", action.translateAccidental(Note.Accidental.NONE));
        assertEquals("=", action.translateAccidental(Note.Accidental.NATURAL));
        assertEquals("_", action.translateAccidental(Note.Accidental.FLAT));
        assertEquals("^", action.translateAccidental(Note.Accidental.SHARP));
        assertEquals("==", action.translateAccidental(Note.Accidental.DOUBLE_NATURAL));
        assertEquals("__", action.translateAccidental(Note.Accidental.DOUBLE_FLAT));
        assertEquals("^^", action.translateAccidental(Note.Accidental.DOUBLE_SHARP));
        assertEquals("=_", action.translateAccidental(Note.Accidental.NATURAL_FLAT));
        assertEquals("=^", action.translateAccidental(Note.Accidental.NATURAL_SHARP));
    }

    @Test
    public void testTranslateNoteLength() {
        int compositionUnitLength = Composition.PPQ * 4;
        assertEquals("/32", action.translateNoteLength(new Demisemiquaver().getDuration(), compositionUnitLength));
        assertEquals("3/64", action.translateNoteLength(makeDotted(new Demisemiquaver(), 1).getDuration(), compositionUnitLength));
        assertEquals("7/128", action.translateNoteLength(makeDotted(new Demisemiquaver(), 2).getDuration(), compositionUnitLength));

        assertEquals("/16", action.translateNoteLength(new Semiquaver().getDuration(), compositionUnitLength));
        assertEquals("3/32", action.translateNoteLength(makeDotted(new Semiquaver(), 1).getDuration(), compositionUnitLength));
        assertEquals("7/64", action.translateNoteLength(makeDotted(new Semiquaver(), 2).getDuration(), compositionUnitLength));

        assertEquals("/8", action.translateNoteLength(new Quaver().getDuration(), compositionUnitLength));
        assertEquals("3/16", action.translateNoteLength(makeDotted(new Quaver(), 1).getDuration(), compositionUnitLength));
        assertEquals("7/32", action.translateNoteLength(makeDotted(new Quaver(), 2).getDuration(), compositionUnitLength));

        assertEquals("/4", action.translateNoteLength(new Crotchet().getDuration(), compositionUnitLength));
        assertEquals("3/8", action.translateNoteLength(makeDotted(new Crotchet(), 1).getDuration(), compositionUnitLength));
        assertEquals("7/16", action.translateNoteLength(makeDotted(new Crotchet(), 2).getDuration(), compositionUnitLength));

        assertEquals("/2", action.translateNoteLength(new Minim().getDuration(), compositionUnitLength));
        assertEquals("3/4", action.translateNoteLength(makeDotted(new Minim(), 1).getDuration(), compositionUnitLength));
        assertEquals("7/8", action.translateNoteLength(makeDotted(new Minim(), 2).getDuration(), compositionUnitLength));

        assertEquals("", action.translateNoteLength(new Semibreve().getDuration(), compositionUnitLength));
        assertEquals("3/2", action.translateNoteLength(makeDotted(new Semibreve(), 1).getDuration(), compositionUnitLength));
        assertEquals("7/4", action.translateNoteLength(makeDotted(new Semibreve(), 2).getDuration(), compositionUnitLength));

        compositionUnitLength = Composition.PPQ;
        assertEquals("4", action.translateNoteLength(new Semibreve().getDuration(), compositionUnitLength));
        assertEquals("2", action.translateNoteLength(new Minim().getDuration(), compositionUnitLength));
        assertEquals("", action.translateNoteLength(new Crotchet().getDuration(), compositionUnitLength));
        assertEquals("/2", action.translateNoteLength(new Quaver().getDuration(), compositionUnitLength));
    }

    private Note makeDotted(Note note, int dotted) {
        note.setDotted(dotted);
        return note;
    }

}
