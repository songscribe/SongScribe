package songscribe.ui;

import songscribe.data.MyBorder;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BorderPanel extends JPanel{
    private JPanel expertBorderPanel;
    private JSpinner topSpinner;
    private JSpinner leftSpinner;
    private JSpinner bottomSpinner;
    private JSpinner rightSpinner;
    private JButton expertBorderButton;
    private JPanel simpleBorderPanel;
    private JSpinner borderSpinner;
    private JPanel borderPanel;
    private boolean exportBorder;
    private ActionListener packListener;

    public BorderPanel() {
        borderSpinner.setValue(10);
        expertBorderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setExpertBorder(!exportBorder);
                if(packListener!=null)packListener.actionPerformed(null);
            }
        });
        add(borderPanel);
    }

    public void setPackListener(ActionListener packListener) {
        this.packListener = packListener;
    }

    public void addChangeListener(ChangeListener changeListener){
        borderSpinner.addChangeListener(changeListener);
        topSpinner.addChangeListener(changeListener);
        leftSpinner.addChangeListener(changeListener);
        bottomSpinner.addChangeListener(changeListener);
        rightSpinner.addChangeListener(changeListener);
    }

    public void setExpertBorder(boolean expert){
        exportBorder = expert;
        expertBorderButton.setText(expert ? "<< Simple" : "Expert >>");
        expertBorderPanel.setVisible(expert);
        simpleBorderPanel.setVisible(!expert);
        if(expert){
            topSpinner.setValue(borderSpinner.getValue());
            leftSpinner.setValue(borderSpinner.getValue());
            bottomSpinner.setValue(borderSpinner.getValue());
            rightSpinner.setValue(borderSpinner.getValue());
        }
    }

    public MyBorder getMyBorder(){
        return !exportBorder ? new MyBorder((Integer) borderSpinner.getValue()) :
                new MyBorder((Integer) topSpinner.getValue(), (Integer) bottomSpinner.getValue(), (Integer) leftSpinner.getValue(), (Integer) rightSpinner.getValue());
    }
}
