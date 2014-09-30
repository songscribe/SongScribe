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
*/
package songscribe.ui;

import songscribe.data.DoNotShowException;
import songscribe.publisher.newsteps.Data;
import songscribe.publisher.newsteps.PaperSizeStep;

import java.awt.*;

public class ExportPDFDialog extends MyDialog {
    private PaperSizeStep paperSizePanel;
    private Data paperSizeDataPrivate, paperSizeData;

    public ExportPDFDialog(MainFrame mainFrame) {
        super(mainFrame, "PDF properties");
        paperSizeDataPrivate = new Data();
        paperSizeDataPrivate.mainFrame = mainFrame;
        paperSizePanel = new PaperSizeStep(paperSizeDataPrivate);
        paperSizePanel.setMirroredCheckInvisible();
        dialogPanel.add(BorderLayout.CENTER, paperSizePanel);
        southPanel.remove(applyButton);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData() throws DoNotShowException {
        paperSizePanel.start();
    }

    protected void setData() {
        paperSizePanel.end();
        paperSizeData = paperSizeDataPrivate;
    }

    public Data getPaperSizeData() {
        return paperSizeData;
    }
}
