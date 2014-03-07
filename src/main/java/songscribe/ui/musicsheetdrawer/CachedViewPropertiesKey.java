package songscribe.ui.musicsheetdrawer;

/**
 * Created by Himadri on 2014.03.07..
 */
public class CachedViewPropertiesKey {
    public static enum Property {ONE_DASH_CENTER};
    private final int lineIndex;
    private final int noteIndex;
    private final Property property;

    public CachedViewPropertiesKey(int lineIndex, int noteIndex, Property property) {
        this.lineIndex = lineIndex;
        this.noteIndex = noteIndex;
        this.property = property;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public int getNoteIndex() {
        return noteIndex;
    }

    public Property getProperty() {
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedViewPropertiesKey that = (CachedViewPropertiesKey) o;

        if (lineIndex != that.lineIndex) return false;
        if (noteIndex != that.noteIndex) return false;
        if (property != that.property) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lineIndex;
        result = 31 * result + noteIndex;
        result = 31 * result + (property != null ? property.hashCode() : 0);
        return result;
    }
}
