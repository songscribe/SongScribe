package songscribe.converter;

import songscribe.ui.Constants;
import songscribe.ui.MainFrame;
import songscribe.ui.MusicSheet;

import javax.sound.midi.MidiSystem;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class MidiConverter {
    @ArgumentDescribe("MIDI Instrument (0-127)")
    public int instrument = 0;    

    @ArgumentDescribe("Export with repeats")
    public boolean withRepeat = false;

    @ArgumentDescribe("Tempo change (in percentage)")
    public int tempoChange = 100;

    @FileArgument
    public File[] files;

    public static void main(String[] args) {
        ArgumentumReader am = new ArgumentumReader(args, MidiConverter.class);
        ((MidiConverter) am.getObj()).convert();
    }

    public void convert() {
        if (instrument < 0 || instrument > 127) {
            System.out.println("The instrument must be in range of 0-127");
            return;
        }
        if (tempoChange <=0 || tempoChange > 200) {
            System.out.println("The tempo change must be in range of 1-200");
            return;
        }

        MainFrame mf = new MainFrame(){
            @Override
            public void showErrorMessage(String message) {
                System.out.println(message);
            }
        };
        mf.setMusicSheet(new MusicSheet(mf));

        Properties props = new Properties(mf.getProperties());
        props.setProperty(Constants.WITHREPEATPROP, withRepeat ? Constants.TRUEVALUE : Constants.FALSEVALUE);
        props.setProperty(Constants.INSTRUMENTPROP, Integer.toString(instrument));
        props.setProperty(Constants.TEMPOCHANGEPROP, Integer.toString(tempoChange));
        
        for(File file:files){
            try {
                mf.getMusicSheet().setComposition(null);
                mf.openMusicSheet(file, false);
                mf.getMusicSheet().getComposition().musicChanged(props);
                String fileName = file.getName();
                int dotPos = fileName.indexOf('.');
                if(dotPos>0)fileName=fileName.substring(0, dotPos);
                MidiSystem.write(mf.getMusicSheet().getComposition().getSequence(), 1, new File(fileName+".midi"));
            } catch (IOException e) {
                System.out.println("Could not convert "+file.getName());
            }
        }
    }
}
