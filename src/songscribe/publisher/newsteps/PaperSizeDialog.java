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

Created on Jul 19, 2007
*/
package songscribe.publisher.newsteps;

import songscribe.ui.MyDialog;
import songscribe.ui.MainFrame;
import songscribe.publisher.Publisher;
import songscribe.publisher.Book;
import songscribe.data.DoNotShowException;

import java.awt.*;


/**
 * @author Csaba Kávai
 */
public class PaperSizeDialog extends MyDialog {

    private PaperSizeStep paperSizePanel;
    private Data paperSizeData;

    public PaperSizeDialog(MainFrame publisher) {
        super(publisher, "Paper size");
        paperSizeData = new Data();
        paperSizeData.publisher = (Publisher)publisher;
        paperSizePanel = new PaperSizeStep(paperSizeData);
        dialogPanel.add(BorderLayout.CENTER, paperSizePanel);
        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData() throws DoNotShowException{
        if(paperSizeData.publisher.isBookNull())throw new DoNotShowException();
        Book book = paperSizeData.publisher.getBook();
        paperSizePanel.setValues(book.getPageSize().width, book.getPageSize().height, book.getLeftInnerMargin(),
                book.getRightOuterMargin(), book.getTopMargin(), book.getBottomMargin(), book.isMirroredMargin());
    }

    protected void setData() {
        paperSizePanel.end();
        paperSizeData.publisher.getBook().setPageSize(paperSizeData.paperWidth, paperSizeData.paperHeight, paperSizeData.leftInnerMargin,
                paperSizeData.rightOuterMargin, paperSizeData.topMargin, paperSizeData.bottomMargin, paperSizeData.mirrored);
        paperSizeData.publisher.modifiedDocument();
        paperSizeData.publisher.getBook().repaintWhole();
    }
}
