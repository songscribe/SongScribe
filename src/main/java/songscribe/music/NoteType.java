/*
    SongScribe song notation program
    Copyright (C) 2006 Csaba Kavai

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

    Created on 2005.01.13., 21:32:45
*/

package songscribe.music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Csaba Kávai
 */
public enum NoteType {
    SEMIBREVE(new Semibreve(), "Whole", KeyStroke.getKeyStroke(KeyEvent.VK_6, 0)),
    MINIM(new Minim(), "Half", KeyStroke.getKeyStroke(KeyEvent.VK_5, 0)),
    CROTCHET(new Crotchet(), "Quarter", KeyStroke.getKeyStroke(KeyEvent.VK_4, 0)),
    QUAVER(new Quaver(), "Eighth", KeyStroke.getKeyStroke(KeyEvent.VK_3, 0)),
    SEMIQUAVER(new Semiquaver(), "Sixteenth", KeyStroke.getKeyStroke(KeyEvent.VK_2, 0)),
    DEMI_SEMIQUAVER(new Demisemiquaver(), "Thirtysecond", KeyStroke.getKeyStroke(KeyEvent.VK_1, 0)),

    SEMIBREVE_REST(new SemibreveRest(), "Whole rest", KeyStroke.getKeyStroke(KeyEvent.VK_6, getMenuShortcutKeyMask())),
    MINIM_REST(new MinimRest(), "Half rest", KeyStroke.getKeyStroke(KeyEvent.VK_5, getMenuShortcutKeyMask())),
    CROTCHET_REST(new CrotchetRest(), "Quarter rest", KeyStroke.getKeyStroke(KeyEvent.VK_4, getMenuShortcutKeyMask())),
    QUAVER_REST(new QuaverRest(), "Eighth rest", KeyStroke.getKeyStroke(KeyEvent.VK_3, getMenuShortcutKeyMask())),
    SEMIQUAVER_REST(new SemiquaverRest(), "Sixteenth rest", KeyStroke.getKeyStroke(KeyEvent.VK_2, getMenuShortcutKeyMask())),
    DEMI_SEMIQUAVER_REST(new DemisemiquaverRest(), "Thirtysecond rest", KeyStroke.getKeyStroke(KeyEvent.VK_1, getMenuShortcutKeyMask())),

    GRACE_QUAVER(new GraceQuaver(), "Grace Eighth", KeyStroke.getKeyStroke(KeyEvent.VK_G, 0)),
    GRACE_SEMIQUAVER(new GraceSemiQuaver(), "Grace Sixteenth", null),
    GLISSANDO(Note.GLISSANDO_NOTE, "GlissandoNote", KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_MASK)),
    REPEAT_LEFT(new RepeatLeft(), "Repeat left", KeyStroke.getKeyStroke(KeyEvent.VK_L, 0)),
    REPEAT_RIGHT(new RepeatRight(), "Repeat right", KeyStroke.getKeyStroke(KeyEvent.VK_R, 0)),
    REPEAT_LEFT_RIGHT(new RepeatLeftRight(), null, null),
    BREATH_MARK(new BreathMark(), "Breath mark", null),
    SINGLE_BARLINE(new SingleBarLine(), "Single barline", null),
    DOUBLE_BARLINE(new DoubleBarLine(), "Double barline", null),
    FINAL_DOUBLE_BARLINE(new FinalDoubleBarLine(), "Final double barline", null),

    PASTE(Note.PASTE_NOTE, null, null),
    GRACE_SEMIQUAVER_EDIT_STEP1(new GraceSemiQuaverEditStep1(), null, null),

    // IO values
    SEMIBREVEREST(NoteType.SEMIBREVE_REST),
    MINIMREST(NoteType.MINIM_REST),
    CROTCHETREST(NoteType.CROTCHET_REST),
    QUAVERREST(NoteType.QUAVER_REST),
    SEMIQUAVERREST(NoteType.SEMIQUAVER_REST),
    DEMISEMIQUAVERREST(NoteType.DEMI_SEMIQUAVER_REST),
    GRACEQUAVER(NoteType.GRACE_QUAVER),
    GRACESEMIQUAVER(NoteType.GRACE_SEMIQUAVER),
    REPEATLEFT(NoteType.REPEAT_LEFT),
    REPEATRIGHT(NoteType.REPEAT_RIGHT),
    REPEATLEFTRIGHT(NoteType.REPEAT_LEFT_RIGHT),
    BREATHMARK(NoteType.BREATH_MARK),
    SINGLEBARLINE(NoteType.SINGLE_BARLINE),
    DOUBLEBARLINE(NoteType.DOUBLE_BARLINE),
    FINALDOUBLEBARLINE(NoteType.FINAL_DOUBLE_BARLINE);

    public static int getMenuShortcutKeyMask() {
        return !GraphicsEnvironment.isHeadless()
                ? Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                : 0;
    }

    private Note instance;
    private String name;
    private KeyStroke acceleratorKey;

    NoteType(Note instance, String name, KeyStroke acceleratorKey) {
        this.instance = instance;
        this.name = name;
        this.acceleratorKey = acceleratorKey;
    }

    NoteType(NoteType note) {
        this.instance = note.instance;
        this.name = note.name;
        this.acceleratorKey = note.acceleratorKey;
    }

    public static String getCompoundName(String name, KeyStroke acceleratorKey) {
        StringBuilder sb = new StringBuilder(20);
        sb.append(name);

        if (acceleratorKey != null) {
            sb.append(" (");

            if ((acceleratorKey.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                sb.append("CTRL+");
            }

            if ((acceleratorKey.getModifiers() & InputEvent.META_MASK) != 0) {
                sb.append("FUNCTION+");
            }

            if ((acceleratorKey.getModifiers() & InputEvent.ALT_MASK) != 0) {
                sb.append("ALT+");
            }

            if ((acceleratorKey.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                sb.append("SHIFT+");
            }

            if (acceleratorKey.getKeyCode() == KeyEvent.VK_ENTER) {
                sb.append("ENTER");
            }
            else {
                sb.append((char) acceleratorKey.getKeyCode());
            }

            sb.append(')');
        }
        return sb.toString();
    }

    public Note getInstance() {
        return instance;
    }

    public Note newInstance() {
        return instance.clone();
    }

    public String getName() {
        return name;
    }

    public String getCompoundName() {
        return getCompoundName(name, acceleratorKey);
    }

    public KeyStroke getAcceleratorKey() {
        return acceleratorKey;
    }

    public boolean isRealNote() {
        return ordinal() >= SEMIBREVE.ordinal() && ordinal() <= DEMI_SEMIQUAVER.ordinal();
    }

    public boolean isNote() {
        return isRealNote() || isGraceNote();
    }

    public boolean isNoteWithStem() {
        return isNote() && this != SEMIBREVE;
    }

    public boolean isRest() {
        return ordinal() >= SEMIBREVE_REST.ordinal() && ordinal() <= DEMI_SEMIQUAVER_REST.ordinal();
    }

    public boolean isBeamable() {
        return this == QUAVER || this == SEMIQUAVER || this == DEMI_SEMIQUAVER;
    }

    public boolean isRepeat() {
        return this == REPEAT_LEFT || this == REPEAT_RIGHT || this == REPEAT_LEFT_RIGHT;
    }

    public boolean isBarLine() {
        return ordinal() >= SINGLE_BARLINE.ordinal() && ordinal() <= FINAL_DOUBLE_BARLINE.ordinal();
    }

    public boolean isGraceNote() {
        return this == GRACE_QUAVER || this == GRACE_SEMIQUAVER;
    }

    public boolean drawStaveLongitude() {
        return this != BREATH_MARK && this != GRACE_SEMIQUAVER;
    }

    public boolean snapToEnd() {
        return this == REPEAT_RIGHT || this == SINGLE_BARLINE || this == DOUBLE_BARLINE || this == FINAL_DOUBLE_BARLINE;
    }
}
