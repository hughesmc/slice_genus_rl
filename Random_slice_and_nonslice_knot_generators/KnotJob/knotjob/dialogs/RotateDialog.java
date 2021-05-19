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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

/**
 *
 * @author Dirk
 */
public class RotateDialog extends JDialog {
    
    public int angle;
    private final JFrame frame; 
    
    public RotateDialog(JFrame fram, String name, boolean bo) {
        super(fram,name,bo);
        angle = 0;
        frame = fram;
    }
    
    public void setUpStuff() {
        this.setSize(250,130);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        JLabel topLabel = new JLabel("Rotate by 0 degrees");
        topPanel.add(topLabel);
        JScrollBar rotateSlider = new JScrollBar(JScrollBar.HORIZONTAL, 0, 180, -180, 360);
        rotateSlider.setPreferredSize(new Dimension(150,20));
        rotateSlider.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                topLabel.setText("Rotate by "+rotateSlider.getValue()+" degrees");
            }
        });
        JPanel centerPanel = new JPanel();
        centerPanel.add(rotateSlider);
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                angle = rotateSlider.getValue();
                setVisible(false);
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel botPanel = new JPanel();
        botPanel.add(okayButton);
        botPanel.add(cancButton);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(botPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }
    
}
