package songscribe.converter;

import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;
import songscribe.ui.mainframeactions.ExportABCAnnotationAction;

import java.io.File;
import java.io.PrintWriter;

public class AbcConverter {
    @FileArgument
    public File file;

    public static void main(String[] args) {
        ArgumentumReader am = new ArgumentumReader(args, AbcConverter.class);
        ((AbcConverter) am.getObj()).convert();
    }

    public void convert() {
        MainFrame mf = new MainFrame(){
            @Override
            public void showErrorMessage(String message) {
                System.out.println(message);
            }
        };
        mf.setMusicSheet(new MusicSheet(mf));
        ExportABCAnnotationAction exportABCAnnotation = new ExportABCAnnotationAction(mf);

        mf.getMusicSheet().setComposition(null);
        mf.openMusicSheet(file, false);

        PrintWriter writer = new PrintWriter(System.out);
        exportABCAnnotation.writeABC(writer);
        writer.close();
        
    }
}
