/*
SongScribe song notation program
Copyright (C) 2006-2007 Csaba Kavai

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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public enum NoteType {
    SEMIBREVE(new Semibreve(), "Whole", KeyStroke.getKeyStroke(KeyEvent.VK_6, 0)),
    MINIM(new Minim(), "Half", KeyStroke.getKeyStroke(KeyEvent.VK_5, 0)),
    CROTCHET(new Crotchet(), "Quarter", KeyStroke.getKeyStroke(KeyEvent.VK_4, 0)),
    QUAVER(new Quaver(), "Eighth", KeyStroke.getKeyStroke(KeyEvent.VK_3, 0)),
    SEMIQUAVER(new Semiquaver(), "Sixteenth", KeyStroke.getKeyStroke(KeyEvent.VK_2, 0)),
    DEMISEMIQUAVER(new Demisemiquaver(), "Thirtysecond", KeyStroke.getKeyStroke(KeyEvent.VK_1, 0)),

    SEMIBREVEREST(new SemibreveRest(), "Whole rest", KeyStroke.getKeyStroke(KeyEvent.VK_6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
    MINIMREST(new MinimRest(), "Half rest", KeyStroke.getKeyStroke(KeyEvent.VK_5, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
    CROTCHETREST(new CrotchetRest(), "Quarter rest", KeyStroke.getKeyStroke(KeyEvent.VK_4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
    QUAVERREST(new QuaverRest(), "Eighth rest", KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
    SEMIQUAVERREST(new SemiquaverRest(), "Sixteenth rest", KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),
    DEMISEMIQUAVERREST(new DemisemiquaverRest(), "Thirtysecond rest", KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),

    GRACEQUAVER(new GraceQuaver(), "Grace note", KeyStroke.getKeyStroke(KeyEvent.VK_G, 0)),
    GLISSANDO(Note.GLISSANDONOTE, "Glissando", KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_MASK)),
    REPEATLEFT(new RepeatLeft(), "Repeat left", KeyStroke.getKeyStroke(KeyEvent.VK_L, 0)),
    REPEATRIGHT(new RepeatRight(), "Repeat right", KeyStroke.getKeyStroke(KeyEvent.VK_R, 0)),
    REPEATLEFTRIGHT(new RepeatLeftRight(), null, null),
    BREATHMARK(new BreathMark(), "Breath mark", null),
    SINGLEBARLINE(new SingleBarLine(), "Single barline", null),
    DOUBLEBARLINE(new DoubleBarLine(), "Double barline", null),
    FINALDOUBLEBARLINE(new FinalDoubleBarLine(), "Final double barline", null),

    PASTE(Note.PASTENOTE, null, null);

    private Note instance;
    private String name;
    private KeyStroke acceleratorKey;

    NoteType(Note instance, String name, KeyStroke acceleratorKey) {
        this.instance = instance;
        this.name = name;
        this.acceleratorKey = acceleratorKey;
    }

    public Note getInstance() {
        return instance;
    }

    public Note newInstance() {
        return instance.clone();
    }

    public String getName(){
        return name;
    }

    public String getCompoundName(){
        return getCompoundName(name, acceleratorKey);
    }

    public static String getCompoundName(String name, KeyStroke acceleratorKey) {
        StringBuilder sb = new StringBuilder(20);
        sb.append(name);
        if(acceleratorKey!=null){
            sb.append(" (");
            if((acceleratorKey.getModifiers() & InputEvent.CTRL_MASK)!=0){
                sb.append("CTRL+");
            }
            if((acceleratorKey.getModifiers() & InputEvent.META_MASK)!=0){
                sb.append("FUNCTION+");
            }
            if((acceleratorKey.getModifiers() & InputEvent.ALT_MASK)!=0){
                sb.append("ALT+");
            }
            if((acceleratorKey.getModifiers() & InputEvent.SHIFT_MASK)!=0){
                sb.append("SHIFT+");
            }
            if(acceleratorKey.getKeyCode()==KeyEvent.VK_ENTER){
                sb.append("ENTER");
            }else{
                sb.append((char)acceleratorKey.getKeyCode());
            }
            sb.append(')');
        }
        return sb.toString();
    }

    public KeyStroke getAcceleratorKey() {
        return acceleratorKey;
    }

    public boolean isNote() {
        return ordinal()>=SEMIBREVE.ordinal() && ordinal()<=DEMISEMIQUAVER.ordinal() || this==GRACEQUAVER;
    }

    public boolean isRest() {
        return ordinal()>=SEMIBREVEREST.ordinal() && ordinal()<=DEMISEMIQUAVERREST.ordinal();
    }

    public boolean isBeamable(){
        return this==QUAVER || this==SEMIQUAVER || this==DEMISEMIQUAVER;
    }

    public boolean isRepeat(){
        return this==REPEATLEFT || this==REPEATRIGHT || this==REPEATLEFTRIGHT;
    }

    public boolean drawStaveLongitude(){
        return this!=BREATHMARK;
    }

    public boolean snapToEnd(){
        return this==REPEATRIGHT || this==SINGLEBARLINE || this==DOUBLEBARLINE || this==FINALDOUBLEBARLINE;
    }
}
