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

    Created on 2006.01.01.
*/
package songscribe.ui;

import org.apache.commons.httpclient.Header;

/**
 * @author Csaba Kávai
 */
public class Constants {
    public static final String WITH_REPEAT_PROP = "withrepeat";
    public static final String PLAY_CONTINUOUSLY_PROP = "playcontinuously";
    public static final String INSTRUMENT_PROP = "instrument";
    public static final String TEMPO_CHANGE_PROP = "tempochange";
    public static final String DURATION_SHORTEN_PROP = "durationshortitude";
    public static final String CONTROL_PROP = "control";
    public static final String DEFAULT_PROFILE_PROP = "defaultprofile";
    public static final String IMAGE_EXPORT_FILTER_PROP = "imageexportfilter";
    public static final String DPI_PROP = "dpi";
    public static final String PREVIOUS_DIRECTORY = "previousdirectory";
    public static final String PLAY_INSERTING_NOTE = "playinsertingnote";
    public static final String COLORIZE_NOTE = "colorizenote";
    public static final String TIP_INDEX = "tipindex";
    public static final String SHOW_TIP = "showtip";
    public static final String SHOW_WHATS_NEW = "showwhatsnew";
    public static final String TRUE_VALUE = "true";
    public static final String FALSE_VALUE = "false";
    public static final String ACCELERATOR_KEYS = "AcceleratorKeys";
    public static final String UNDERSCORE = "_";
    public static final String HYPHEN = "-";
    public static final String SHOW_MEM_USAGE = "showmemusage";
    public static final String SHOW_PUBLISHER_NEW_INFO = "showpublishernewinfo";
    public static final String UPDATE_URL = "updateurl";
    public static final String LAST_AUTO_UPDATE = "lastautoupdate";
    public static final String AUTO_UPDATE_PERIOD = "autoupdateperiod";
    public static final String METRIC = "metric";
    public static final String FIRST_RUN = "firstrun";
    public static final String CHECKSUMS_FILENAME = "checksums";
    public static final String VERSION_URL = "https://songscribe.himadri.eu/download/version";
    public static final Header MAX_AGE_HEADER = new Header("Cache-Control", "max-age=0");
    public static final String NON_BREAKING_HYPHEN = Character.toString('\u00AD');
    public static final String PACKAGE_NAME = "SongScribe";
    public static final String SONG_SCRIBE_JAR = PACKAGE_NAME + ".jar";
}
