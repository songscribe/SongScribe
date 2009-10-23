package songscribe.music;

public class ParsonsCodeGenerator {
    public static final char STARTER_SYMBOL = '*';
    public static final char REPEAT_SYMBOL = 'r';
    public static final char UP_SYMBOL = 'u';
    public static final char DOWN_SYMBOL = 'd';

    public static String getParsonsCode(Composition composition, boolean improved) {
        int lastNotePitch = 0;
        StringBuilder sb = new StringBuilder();
        for (int l = 0; l < composition.lineCount(); l++) {
            Line line = composition.getLine(l);
            for (int n = 0; n < line.noteCount(); n++) {
                Note note = line.getNote(n);
                if (note.getNoteType().isRealNote()) {
                    int pitch = note.getPitch();
                    if (lastNotePitch == 0) {
                        sb.append(STARTER_SYMBOL);
                    }
                    else {
                        int diff = pitch - lastNotePitch;
                        if (diff == 0) {
                            sb.append(REPEAT_SYMBOL);
                        }
                        else {
                            sb.append(diff > 0 ? UP_SYMBOL : DOWN_SYMBOL);
                            if (improved) {
                                sb.append(Math.abs(diff));
                            }
                        }
                    }
                    lastNotePitch = pitch;
                }
            }
        }
        return sb.toString();
    }
}
