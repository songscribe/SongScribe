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

    Created on Aug 4, 2006
*/
package songscribe.publisher.newsteps;

import songscribe.publisher.Book;
import songscribe.publisher.Page;
import songscribe.publisher.Publisher;
import songscribe.publisher.pagecomponents.Song;
import songscribe.ui.Constants;
import songscribe.ui.MusicSheet;
import songscribe.ui.ProcessDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Csaba Kávai
 */
public class NewDialog extends JDialog {
    private Data data = new Data();

    private JLabel infoLabel = new JLabel();
    private CardLayout cardLayout = new CardLayout();
    private JPanel center = new JPanel(cardLayout);

    private PreviousAction previousAction = new PreviousAction();
    private JButton nextButton = new JButton();

    private Step[] steps;
    private int currentStep;

    private Publisher publisher;

    public NewDialog(Publisher publisher) {
        super(publisher, "New document");
        this.publisher = publisher;
        data.mainFrame = publisher;
        steps = new Step[] {
                new InfoStep(data),
                new SongSelectStep(data),
                new PaperSizeStep(data),
                new LayoutStep(data)
        };
        setLayout(new BorderLayout());
        JPanel northCenter = new JPanel();
        northCenter.setLayout(new BoxLayout(northCenter, BoxLayout.Y_AXIS));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        infoLabel.setAlignmentX(0f);
        northCenter.add(infoLabel);
        center.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        center.setAlignmentX(0f);
        northCenter.add(center);
        getContentPane().add(BorderLayout.CENTER, northCenter);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        south.add(new JButton(previousAction));
        south.add(nextButton);
        getContentPane().add(BorderLayout.SOUTH, south);

        for (int i = 0; i < steps.length; i++) {
            center.add(steps[i], Integer.toString(i));
        }

        getContentPane().setPreferredSize(new Dimension(470, 340));
        pack();
        setLocation(Publisher.CENTER_POINT.x - getWidth() / 2, Publisher.CENTER_POINT.y - getHeight() / 2);
        setVisible(true);
        setStep(publisher.getProperties().getProperty(Constants.SHOW_PUBLISHER_NEW_INFO).equals(Constants.TRUE_VALUE) ? 0 : 1);
    }

    private void setStep(int step) {
        steps[currentStep].end();
        currentStep = step;
        cardLayout.show(center, Integer.toString(step));
        infoLabel.setText(steps[step].getInfo());
        previousAction.setEnabled(step > 0);
        nextButton.setAction(step == steps.length - 1 || step == steps.length - 2 && data.files.size() ==
                                                                                     0 ? new GenerateAction() : new NextAction());
        steps[step].start();
    }

    private class PreviousAction extends AbstractAction {
        public PreviousAction() {
            putValue(Action.NAME, "Previous");
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("back16.png")));
        }

        public void actionPerformed(ActionEvent e) {
            setStep(currentStep - 1);
        }
    }

    private class NextAction extends AbstractAction {
        public NextAction() {
            putValue(Action.NAME, "Next");
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("forward16.png")));
        }

        public void actionPerformed(ActionEvent e) {
            setStep(currentStep + 1);
        }
    }

    private class GenerateAction extends AbstractAction implements Runnable {
        private ProcessDialog processDialog;

        public GenerateAction() {
            putValue(Action.NAME, "Generate");
            putValue(Action.SMALL_ICON, new ImageIcon(Publisher.getImage("apply.png")));
        }

        public void actionPerformed(ActionEvent e) {
            processDialog = new ProcessDialog(NewDialog.this, "Creating the document...", steps.length * 2);
            processDialog.packAndPos();
            new Thread(this).start();
            processDialog.setVisible(true);
        }

        public void run() {
            steps[currentStep].end();
            Book book = new Book(publisher, data.paperWidth, data.paperHeight, data.leftInnerMargin, data.rightOuterMargin, data.topMargin, data.bottomMargin, data.mirrored);
            Page page = book.addPage();
            int drawableWidth = data.paperWidth - data.leftInnerMargin - data.rightOuterMargin;
            int drawableHeight = data.paperHeight - data.topMargin - data.bottomMargin;
            int currentHeight = 0;
            int firstInThisPage = 0;
            MusicSheet[] musicSheets = new MusicSheet[data.files.size()];
            double[] resolutions = new double[data.files.size()];
            int[] heights = new int[data.files.size()];

            for (int i = 0; i < data.files.size(); i++) {
                musicSheets[i] = publisher.openMusicSheet(data.files.get(i));
                resolutions[i] = book.getNotGreaterResolution(musicSheets[i].getSheetWidth(), musicSheets[i].getSheetHeight());
                heights[i] = (int) Math.round(musicSheets[i].getSheetHeight() * resolutions[i]);
                processDialog.nextValue();
            }

            for (int i = 0; i < data.files.size(); i++) {
                currentHeight += heights[i];

                if (i == data.files.size() - 1 || currentHeight + heights[i + 1] > drawableHeight ||
                    i - firstInThisPage + 1 == data.songsPerPage) {//page break
                    int space = (drawableHeight - currentHeight) / (i == firstInThisPage ? 1 : i - firstInThisPage);
                    int currentY = 0;

                    for (int j = firstInThisPage; j <= i; j++) {
                        int xPos = (int) Math.round(
                                (drawableWidth - musicSheets[j].getSheetWidth() * resolutions[j]) / 2.0);
                        page.addPageComponent(new Song(musicSheets[j], xPos, currentY, resolutions[j], data.files.get(j)));
                        currentY += heights[j] + space;
                    }

                    firstInThisPage = i + 1;
                    currentHeight = 0;

                    if (i < data.files.size() - 1) {
                        page = book.addPage();
                    }
                }

                processDialog.nextValue();
            }

            processDialog.dispose();
            publisher.setBook(book);
            dispose();
            publisher.modifiedDocument();
        }
    }
}
