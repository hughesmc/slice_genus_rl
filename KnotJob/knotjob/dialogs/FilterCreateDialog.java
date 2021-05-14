/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

This file is part of KnotJob.

KnotJob is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

KnotJob is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTIBILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org.licenses/>.

 */

package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Dirk
 */
public class FilterCreateDialog extends JDialog {
    
    private final JFrame frame;
    private int choice;
    private final JButton createButton;
    private final JButton cancelButton;
    private final JRadioButton compButton;
    private final JRadioButton khovButton;
    private final JRadioButton sinvButton;
    private final JRadioButton andButton;
    private final JRadioButton orButton;
    private final JRadioButton notButton;
    private final ButtonGroup groupOfButtons;
    
    public FilterCreateDialog(JFrame frm, String title, boolean bo) {
        super(frm, title, bo);
        frame = frm;
        createButton = new JButton("Create Filter");
        cancelButton = new JButton("Cancel");
        groupOfButtons = new ButtonGroup();
        choice = -1;
        compButton = new JRadioButton("Component Filter");
        khovButton = new JRadioButton("Khovanov Filter");
        sinvButton = new JRadioButton("S-Invariant Filter");
        andButton = new JRadioButton("Logical AND Filter");
        orButton = new JRadioButton("Logical OR Filter");
        notButton = new JRadioButton("Logical NOT Filter");
        andButton.setSelected(true);
        Dimension dim = new Dimension(160,40);
        compButton.setPreferredSize(dim);
        khovButton.setPreferredSize(dim);
        sinvButton.setPreferredSize(dim);
        andButton.setPreferredSize(dim);
        orButton.setPreferredSize(dim);
        notButton.setPreferredSize(dim);
        groupOfButtons.add(compButton);
        groupOfButtons.add(khovButton);
        groupOfButtons.add(sinvButton);
        groupOfButtons.add(andButton);
        groupOfButtons.add(orButton);
        groupOfButtons.add(notButton);
    }
    
    public int getSelection() {
        this.setSize(360,360);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        JPanel mainPanel = new JPanel();
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(6,1));
        radioPanel.add(andButton);
        radioPanel.add(orButton);
        radioPanel.add(notButton);
        radioPanel.add(compButton);
        radioPanel.add(khovButton);
        radioPanel.add(sinvButton);
        mainPanel.add(radioPanel);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getChoice();
                dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        this.setVisible(true);
        return choice;
    }
    
    private void getChoice() {
        if (compButton.isSelected()) choice = 0;
        if (khovButton.isSelected()) choice = 1;
        if (sinvButton.isSelected()) choice = 2;
        if (andButton.isSelected()) choice = 3;
        if (orButton.isSelected()) choice = 4;
        if (notButton.isSelected()) choice = 5;
    }
    
}
