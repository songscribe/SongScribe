package songscribe.converter;

import java.lang.reflect.*;
import java.io.File;
import java.util.Vector;

public class ArgumentumReader {
    private String[] args;
    private Class clazz;
    private Object obj;

    public ArgumentumReader(String[] args, Class clazz) {
        this.args = args;
        this.clazz = clazz;
    }

    public Object getObj() {
        if(obj==null)parseArguments();
        return obj;
    }

    public String infoBuilder(){
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: [options] file1 [file2] ...\n");
        for(Field f:clazz.getFields()){
            if(f.isAnnotationPresent(ArgumentDescribe.class)){
                sb.append('-').append(f.getName()).append(' ').append(f.getAnnotation(ArgumentDescribe.class).value()).append('\n');
            }
        }
        return sb.toString();
    }

    public void parseArguments(){
        try {
            obj = clazz.newInstance();
            //reading the parameters
            for(String arg:args){
                if(arg.charAt(0)!='-')break;
                if(arg.equals("-?") || arg.equals("-help")){
                    System.out.println(infoBuilder());
                    System.exit(-1);
                }
                int equalPos = arg.indexOf('=');
                if(equalPos==-1)equalPos=arg.length();
                Field f = findField(arg.substring(1, equalPos));
                if(f==null){
                    System.out.println("Bad argument: "+arg);
                    System.exit(-1);
                }else{
                    setField(f, equalPos<arg.length() ? arg.substring(equalPos+1) : null);
                }
            }

            //reading the files
            Field filesField = findField("files");
            if(filesField!=null && filesField.getType().getSimpleName().equals("Vector")){
                Vector<File> files = new Vector<File>();
                for(String arg:args){
                    if(arg.charAt(0)=='-')continue;
                    File file = new File(arg);
                    if(file.exists())files.add(file);
                    else{
                        System.out.println("File not found: "+arg);
                        System.exit(-1);
                    }
                }
                filesField.set(obj, files);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Field findField(String name){
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private void setField(Field field, String value){
        String t = field.getType().getSimpleName();
        try{
            if(t.equals("int")){
                field.setInt(obj, Integer.parseInt(value));
            }else if(t.equals("boolean")){
                field.setBoolean(obj, value == null || Boolean.parseBoolean(value));
            }else{
                field.set(obj, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (NumberFormatException e){
            System.out.println("Not a number given for parameter "+field.getName());
            return;
        }
    }
}
