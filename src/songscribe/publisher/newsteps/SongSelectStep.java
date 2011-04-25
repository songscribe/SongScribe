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

Created on Aug 4, 2006
*/
package songscribe.publisher.newsteps;

import songscribe.data.MyAcceptFilter;
import songscribe.data.PlatformFileDialog;
import songscribe.data.FileExtensions;
import songscribe.publisher.Publisher;
import songscribe.ui.MyDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Vector;

/**
 * @author Csaba Kávai
 */
class SongSelectStep extends Step{
    private static final String INFO = "<html><ul><li>Select the songs you want to publish.<li>Use the add button the add one or more songs to the list.<li>Use the arrows to change the order.</ul></html>";

    private DefaultListModel listModel = new DefaultListModel();
    private JList list = new JList(listModel);
    private Vector<File> files = new Vector<File>(20, 10);

    public SongSelectStep(Data data) {
        super(data);
        data.files = files;
        setLayout(new BorderLayout());

        //CENTER
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        MyDialog.addLabelToBox(center, "Song list:", 5);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setMaximumSize(new Dimension(300, 150));
        scrollPane.setAlignmentX(0f);
        center.add(scrollPane);
        add(BorderLayout.CENTER, center);

        //EAST
        JButton addButton = new JButton(new AddAction());
        JButton addFolderButton = new JButton(new AddFolderAction());
        JButton removeButton = new JButton(new RemoveAction());
        Dimension butPrefSize = new Dimension(Math.max(addFolderButton.getPreferredSize().width, removeButton.getPreferredSize().width),
                Math.max(addFolderButton.getPreferredSize().height, removeButton.getPreferredSize().height));
        addButton.setMinimumSize(butPrefSize);
        addButton.setPreferredSize(butPrefSize);
        addButton.setMaximumSize(butPrefSize);
        addFolderButton.setMinimumSize(butPrefSize);
        addFolderButton.setPreferredSize(butPrefSize);
        addFolderButton.setMaximumSize(butPrefSize);
        removeButton.setMinimumSize(butPrefSize);
        removeButton.setPreferredSize(butPrefSize);
        removeButton.setMaximumSize(butPrefSize);
        JButton upButton = new JButton(new UpAction());
        JButton downButton = new JButton(new DownAction());
        Dimension upDownPrefSize = new Dimension(32, 32);
        upButton.setMinimumSize(upDownPrefSize);
        upButton.setPreferredSize(upDownPrefSize);
        upButton.setMaximumSize(upDownPrefSize);
        downButton.setMinimumSize(upDownPrefSize);
        downButton.setPreferredSize(upDownPrefSize);
        downButton.setMaximumSize(upDownPrefSize);
        JPanel eastPanel = new JPanel();
        eastPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        eastPanel.add(Box.createVerticalGlue());
        eastPanel.add(addButton);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(addFolderButton);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(removeButton);
        eastPanel.add(Box.createVerticalStrut(10));
        JPanel upDownPanel = new JPanel();
        upDownPanel.setAlignmentX(0f);
        upDownPanel.setLayout(new BoxLayout(upDownPanel, BoxLayout.X_AXIS));
        upDownPanel.add(Box.createHorizontalGlue());
        upDownPanel.add(upButton);
        upDownPanel.add(Box.createHorizontalGlue());
        upDownPanel.add(downButton);
        upDownPanel.add(Box.createHorizontalGlue());
        eastPanel.add(upDownPanel);
        eastPanel.add(Box.createVerticalGlue());
        add(BorderLayout.EAST, eastPanel);
    }

    public String getInfo() {
        return INFO;
    }

    public void start() {
    }

    public void end() {
    }

    private class AddAction extends AbstractAction{
        private PlatformFileDialog pfd;

        public AddAction() {
            putValue(Action.NAME, "Add");
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("add.png")));
            pfd = new PlatformFileDialog(data.mainFrame, "Open song", true, new MyAcceptFilter("SongScribe song files", FileExtensions.SONGWRITER.substring(1)));
            pfd.setMultiSelectionEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
            if(pfd.showDialog()){
                File[] openFile = pfd.getFiles();
                for(File of:openFile){
                    files.add(of);
                    listModel.addElement(getListName(of.getName()));
                }
            }
        }
    }

    private class AddFolderAction extends AbstractAction{
        private PlatformFileDialog pfd;

        public AddFolderAction() {
            putValue(Action.NAME, "Add Folder");
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("addfolder.png")));
            pfd = new PlatformFileDialog(data.mainFrame, "Open folder", true, new MyAcceptFilter("Folders"), true);
        }

        public void actionPerformed(ActionEvent e) {
            if(pfd.showDialog()){
                boolean isThereDir = false;
                for(File file:pfd.getFile().listFiles()){
                    if(file.isDirectory()){
                        isThereDir=true;
                        break;
                    }
                }
                int answ=JOptionPane.NO_OPTION;
                if(isThereDir){
                    answ = JOptionPane.showConfirmDialog(data.mainFrame, "All song files will be added from the selected folder.\nDo you want to add songs from its subfolders, too?",
                            data.mainFrame.PROGNAME, JOptionPane.YES_NO_CANCEL_OPTION);
                    if(answ==JOptionPane.CANCEL_OPTION)return;
                }
                addSongFiles(pfd.getFile(), answ==JOptionPane.YES_OPTION);
            }
        }

        private void addSongFiles(File dir, boolean descend){
            for(File file:dir.listFiles()){
                if(file.isDirectory() && descend){
                    addSongFiles(file, descend);
                }else if(file.isFile() && file.getName().endsWith(FileExtensions.SONGWRITER)){
                    files.add(file);
                    listModel.addElement(getListName(file.getName()));
                }
            }
        }
    }


    private class RemoveAction extends AbstractAction{
        public RemoveAction() {
            putValue(Action.NAME, "Remove");
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("remove.png")));
        }

        public void actionPerformed(ActionEvent e) {
            int[] sel = list.getSelectedIndices();
            for(int i=sel.length-1;i>=0;i--){
                files.remove(sel[i]);
                listModel.remove(sel[i]);
            }
        }
    }

    private class UpAction extends AbstractAction{
        public UpAction() {
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("1uparrow32.png")));
            putValue(Action.SHORT_DESCRIPTION, "Move the selected item up in the list");
        }

        public void actionPerformed(ActionEvent e) {
            int[] sels = list.getSelectedIndices();
            if(sels.length>0 && sels[0]>0){
                for(int sel:sels){
                    File f = files.get(sel);
                    files.set(sel, files.get(sel-1));
                    files.set(sel-1, f);
                    Object o = listModel.get(sel);
                    listModel.set(sel, listModel.get(sel-1));
                    listModel.set(sel-1, o);
                }
                for(int i=0;i<sels.length;i++){
                    sels[i]=sels[i]-1;
                }
                list.setSelectedIndices(sels);
            }
        }
    }

    private class DownAction extends AbstractAction{
        public DownAction() {
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("1downarrow32.png")));
            putValue(Action.SHORT_DESCRIPTION, "Move the selected item down in the list");
        }

        public void actionPerformed(ActionEvent e) {
            int[] sels = list.getSelectedIndices();
            if(sels.length>0 && sels[sels.length-1]<listModel.size()-1){
                for (int i=sels.length-1;i>=0;i--) {
                    int sel = sels[i];
                    File f = files.get(sel);
                    files.set(sel, files.get(sel + 1));
                    files.set(sel + 1, f);
                    Object o = listModel.get(sel);
                    listModel.set(sel, listModel.get(sel + 1));
                    listModel.set(sel + 1, o);
                }
                for(int i=0;i<sels.length;i++){
                    sels[i]=sels[i]+1;
                }
                list.setSelectedIndices(sels);
            }
        }
    }

    private String getListName(String fileName){
        if(fileName.endsWith(FileExtensions.SONGWRITER)){
            return fileName.substring(0, fileName.length()-5);
        }else{
            return fileName;
        }
    }
}
