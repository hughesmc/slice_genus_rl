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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Dirk
 */
public class SquareDialog extends JDialog {
    
    private final JFrame frame;
    private int square;
    
    public SquareDialog(JFrame fram, String title, boolean bo) {
        super(fram,title,bo);
        frame = fram;
        square = 0;
        
    }
    
    public int getSquare() {
        setSize(500,125);
        setResizable(false);
        setLayout(new BorderLayout());
        setLocationRelativeTo(frame);
        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new GridLayout(1,2));
        JButton sq1Button = new JButton("Sq^1 even");
        JButton sqoButton = new JButton("Sq^1 odd");
        JPanel sqoPanel = new JPanel();
        JPanel sq1Panel = new JPanel();
        sqoPanel.add(sqoButton);
        sq1Panel.add(sq1Button);
        choicePanel.add(sq1Panel);
        choicePanel.add(sqoPanel);
        JPanel cancelPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelPanel.add(cancelButton);
        sq1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = 1;
                setVisible(false);
            }
        });
        sqoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = -1;
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });
        //sq2Button.setEnabled(false);
        add(choicePanel,BorderLayout.NORTH);
        add(cancelPanel,BorderLayout.SOUTH);
        setVisible(true);
        return square;
    }
    
}
