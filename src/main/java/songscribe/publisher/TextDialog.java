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

    Created on Feb 3, 2007
*/
package songscribe.publisher;

import songscribe.data.MyJTextArea;
import songscribe.publisher.pagecomponents.PageComponent;
import songscribe.publisher.pagecomponents.Text;
import songscribe.ui.MyDialog;
import songscribe.ui.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Csaba Kávai
 */
public class TextDialog extends MyDialog {
    JComboBox fontCombo;
    SpinnerNumberModel fontSizeSpinner;
    JToggleButton boldToggle, italicToggle;
    JToggleButton leftAlign, centerAlign, rightAlign;
    JTextArea textArea = new MyJTextArea(15, 35);

    public TextDialog(Publisher publisher, boolean isInsert) {
        super(publisher, isInsert ? "Insert text" : "Text properties");
        fontCombo = new JComboBox(Utilities.fontFamilyNames);
        fontSizeSpinner = new SpinnerNumberModel(12, 1, 256, 1);
        boldToggle = new JToggleButton("<html><b>B</b></html>");
        italicToggle = new JToggleButton("<html><i>I</i></html>");

        JPanel fontCenter = new JPanel();
        fontCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fontCenter.setLayout(new BoxLayout(fontCenter, BoxLayout.Y_AXIS));
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        tempPanel.add(fontCombo);
        tempPanel.add(new JSpinner(fontSizeSpinner));
        tempPanel.add(boldToggle);
        tempPanel.add(italicToggle);
        tempPanel.setAlignmentX(0f);
        fontCenter.add(tempPanel);
        fontCenter.add(Box.createVerticalStrut(3));
        tempPanel = new JPanel();
        leftAlign = new JToggleButton(new ImageIcon(Publisher.getImage("leftAlignment.png")));
        centerAlign = new JToggleButton(new ImageIcon(Publisher.getImage("centerAlignment.png")));
        rightAlign = new JToggleButton(new ImageIcon(Publisher.getImage("rightAlignment.png")));
        leftAlign.setToolTipText("Left alignment");
        centerAlign.setToolTipText("Center alignment");
        rightAlign.setToolTipText("Right alignment");
        tempPanel.add(leftAlign);
        tempPanel.add(centerAlign);
        tempPanel.add(rightAlign);
        ButtonGroup bg = new ButtonGroup();
        bg.add(leftAlign);
        bg.add(centerAlign);
        bg.add(rightAlign);
        tempPanel.setAlignmentX(0f);
        fontCenter.add(tempPanel);
        fontCenter.add(Box.createVerticalStrut(5));
        tempPanel = new JPanel();
        tempPanel.add(new JScrollPane(textArea));
        tempPanel.setAlignmentX(0f);
        fontCenter.add(tempPanel);
        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT));
        center.add(fontCenter);
        dialogPanel.add(BorderLayout.CENTER, center);

        if (isInsert) {
            southPanel.remove(applyButton);
        }

        dialogPanel.add(BorderLayout.SOUTH, southPanel);
    }

    protected void getData() {
        Book book = ((Publisher) mainFrame).getBook();
        PageComponent pc = book.getSelectedComponent();

        if (pc != null && pc instanceof Text) {
            Text text = (Text) pc;
            textArea.setText(text.getText());

            switch (text.getAlignment()) {
                case LEFT:
                    leftAlign.setSelected(true);
                    break;

                case CENTER:
                    centerAlign.setSelected(true);
                    break;

                case RIGHT:
                    rightAlign.setSelected(true);
            }

            fontCombo.setSelectedItem(text.getFont().getFamily());
            fontSizeSpinner.setValue(text.getFont().getSize());
            boldToggle.setSelected(text.getFont().isBold());
            italicToggle.setSelected(text.getFont().isItalic());
        }
        else {
            textArea.setText("");
        }
    }

    protected void setData() {
        Book book = ((Publisher) mainFrame).getBook();
        PageComponent pc = book.getSelectedComponent();

        if (pc != null && pc instanceof Text) {
            ((Text) pc).setAlignment(getSelectedAlignment());
            ((Text) pc).setTextAndFont(textArea.getText(), getSelectedFont());
            book.repaintWhole();
        }
        else {
            Text text = new Text(textArea.getText(), getSelectedFont(), getSelectedAlignment(), 0, 0);
            book.insertPageComponent(text);
        }
    }

    private Font getSelectedFont() {
        return songscribe.ui.Utilities.createFont(fontCombo.getSelectedItem().toString(),
                (boldToggle.isSelected() ? Font.BOLD : Font.PLAIN) |
                (italicToggle.isSelected() ? Font.ITALIC : Font.PLAIN), (Integer) fontSizeSpinner.getValue());
    }

    private Text.Alignment getSelectedAlignment() {
        Text.Alignment alignment = Text.Alignment.LEFT;

        if (centerAlign.isSelected()) {
            alignment = Text.Alignment.CENTER;
        }
        else if (rightAlign.isSelected()) {
            alignment = Text.Alignment.RIGHT;
        }

        return alignment;
    }
}
