package songscribe.ui;

import songscribe.data.DoNotShowException;
import songscribe.publisher.newsteps.PaperSizeStep;
import songscribe.publisher.newsteps.Data;

import java.awt.*;

public class ExportPDFDialog extends MyDialog{
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
