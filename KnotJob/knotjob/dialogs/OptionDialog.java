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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class OptionDialog extends JDialog {
    
    private final JFrame frame;
    private final JButton cancelButton;
    private final JButton okayButton;
    private final JCheckBox totGirthBox;
    private final JSpinner subdivSpinner;
    private final JComboBox detailBox;
    private final JCheckBox primTorBox;
    private final JCheckBox calcTimeBox;
    private final JComboBox redOptions;
    private final JCheckBox changeNames;
    private final JSpinner saveMax;
    private final Options options;
    
    public OptionDialog(JFrame fram, String title, boolean bo, Options opts) {
        super(fram,title,bo);
        frame = fram;
        options = opts;
        cancelButton = new JButton("Cancel");
        okayButton = new JButton("OK");
        totGirthBox = new JCheckBox("total");
        totGirthBox.setSelected(options.getTotGirth());
        subdivSpinner = new JSpinner(new SpinnerNumberModel(opts.getDivFactor(), 2.5, 1000.0, 0.5));
        String [] details = {"low", "medium", "high"};
        detailBox = new JComboBox<String>(details);
        detailBox.setSelectedIndex(options.getGirthInfo());
        primTorBox = new JCheckBox("primary");
        primTorBox.setSelected(options.getPrimary());
        calcTimeBox = new JCheckBox("show");
        calcTimeBox.setSelected(options.getTimeInfo());
        String [] items = {"both", "reduced", "unreduced"};
        redOptions = new JComboBox<String>(items);
        changeNames = new JCheckBox("change Numbers");
        changeNames.setSelected(options.getChangeOfNumbers());
        saveMax = new JSpinner(new SpinnerNumberModel(opts.getMaxSaveCount(), 1000, 50000, 1000));
    }
    
    public void setUpStuff() {
        this.setSize(400,300);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        JTabbedPane theTab = new JTabbedPane();
        JPanel khovanov = new JPanel();
        JPanel files = new JPanel();
        JPanel diagram = new JPanel();
        JPanel calculations = new JPanel();
        theTab.addTab("Calculations", calculations);
        theTab.addTab("Diagrams", diagram);
        theTab.addTab("Files", files);
        theTab.addTab("Khovanov Cohomology", khovanov);
        JPanel optPanel = new JPanel();
        JPanel butPanel = new JPanel();
        butPanel.add(okayButton);
        butPanel.add(cancelButton);
        optPanel.add(theTab);
        diagram.setPreferredSize(new Dimension(380,195));
        this.setLayout(new BorderLayout());
        this.add(butPanel, BorderLayout.SOUTH);
        this.add(optPanel, BorderLayout.CENTER);
        addToCalculationOptions(calculations);
        addToDiagramOptions(diagram);
        addToFilesOptions(files);
        addToKhovanovOptions(khovanov);
        setUpButtons();
        this.setVisible(true);
    }

    private void setUpButtons() {
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newTotGirth = totGirthBox.isSelected();
                options.setTotGirth(newTotGirth);
                double factor = (double) subdivSpinner.getValue();
                options.setDivFactor(factor);
                boolean newPrimary = primTorBox.isSelected();
                options.setPrimary(newPrimary);
                options.setGirthInfo(detailBox.getSelectedIndex());
                options.setTimeInfo(calcTimeBox.isSelected());
                options.setChangeOfNumbers(changeNames.isSelected());
                options.setMaxSaveCount((int) saveMax.getValue());
                int n = redOptions.getSelectedIndex();
                boolean r = true;
                boolean u = true;
                if (n == 1) u = false;
                if (n == 2) r = false;
                options.setKhovRed(r);
                options.setKhovUnred(u);
                dispose();
            }
        });
    }
    
    private void addToDiagramOptions(JPanel diagram) {
        JLabel girthLabel = new JLabel("Girth minimizing : ");
        JPanel girthPanel = new JPanel();
        girthPanel.add(girthLabel);
        girthPanel.setPreferredSize(new Dimension(190,30));
        JPanel totalPanel = new JPanel();
        totalPanel.add(totGirthBox);
        totalPanel.setPreferredSize(new Dimension(190,30));
        JPanel girTotPanel = new JPanel();
        girTotPanel.setLayout(new GridLayout(1,2));
        girTotPanel.add(girthPanel);
        girTotPanel.add(totalPanel);
        JLabel divLabel = new JLabel("Subdivision Factor : ");
        JPanel divPanel = new JPanel();
        divPanel.add(divLabel);
        divPanel.setPreferredSize(new Dimension(190, 30));
        JPanel subPanel = new JPanel();
        subPanel.add(subdivSpinner);
        subPanel.setPreferredSize(new Dimension(190, 30));
        JPanel subdivPanel = new JPanel();
        subdivPanel.setLayout(new GridLayout(1,2));
        subdivPanel.add(divPanel);
        subdivPanel.add(subPanel);
        diagram.setLayout(new GridLayout(2,1));
        diagram.add(girTotPanel);
        diagram.add(subdivPanel);
    }

    private void addToKhovanovOptions(JPanel khovanov) {
        JLabel torsionLabel = new JLabel("Torsion : ");
        JPanel torsionPanel = new JPanel();
        torsionPanel.add(torsionLabel);
        torsionPanel.setPreferredSize(new Dimension(190,30));
        JPanel primaryPanel = new JPanel();
        primaryPanel.add(primTorBox);
        primaryPanel.setPreferredSize(new Dimension(190,30));
        JPanel torPrimPanel = new JPanel();
        torPrimPanel.setLayout(new GridLayout(1,2));
        torPrimPanel.add(torsionPanel);
        torPrimPanel.add(primaryPanel);
        JLabel reductionLabel = new JLabel("Reduced/Unreduced : ");
        JPanel reductLabPanel = new JPanel();
        reductLabPanel.add(reductionLabel);
        reductLabPanel.setPreferredSize(new Dimension(190,30));
        JPanel reductBoxPanel = new JPanel();
        reductBoxPanel.add(redOptions);
        reductBoxPanel.setPreferredSize(new Dimension(190,30));
        JPanel reductionPanel = new JPanel();
        reductionPanel.setLayout(new GridLayout(1,2));
        reductionPanel.add(reductLabPanel);
        reductionPanel.add(reductBoxPanel);
        int n = 0;
        if (options.getKhovRed() & !options.getKhovUnred()) n = 1;
        if (!options.getKhovRed() & options.getKhovUnred()) n = 2;
        redOptions.setSelectedIndex(n);
        khovanov.setLayout(new GridLayout(2,1));
        khovanov.add(torPrimPanel);
        khovanov.add(reductionPanel);
    }

    private void addToCalculationOptions(JPanel calculations) {
        JLabel girthLabel = new JLabel("Information detail : ");
        JPanel girthPanel = new JPanel();
        girthPanel.add(girthLabel);
        girthPanel.setPreferredSize(new Dimension(190,30));
        JPanel detailPanel = new JPanel();
        detailPanel.add(detailBox);
        detailPanel.setPreferredSize(new Dimension(190,30));
        JPanel detGirthPanel = new JPanel();
        detGirthPanel.setLayout(new GridLayout(1,2));
        detGirthPanel.add(girthPanel);
        detGirthPanel.add(detailPanel);
        JLabel showLabel = new JLabel("Calculation Time : ");
        JPanel showPanel = new JPanel();
        showPanel.add(showLabel);
        JPanel calcPanel = new JPanel();
        calcPanel.add(calcTimeBox);
        JPanel detShowPanel = new JPanel();
        detShowPanel.setLayout(new GridLayout(1,2));
        detShowPanel.add(showPanel);
        detShowPanel.add(calcPanel);
        calculations.setLayout(new GridLayout(2,1));
        calculations.add(detGirthPanel);
        calculations.add(detShowPanel);
    }
    
    private void addToFilesOptions(JPanel files) {
        JLabel maxLinksLabel = new JLabel("Maximal links in file :");
        JPanel maxLinksPanel = new JPanel();
        maxLinksPanel.add(maxLinksLabel);
        maxLinksPanel.setPreferredSize(new Dimension(190, 30));
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.add(saveMax);
        spinnerPanel.setPreferredSize(new Dimension(190, 30));
        JPanel maxPanel = new JPanel();
        maxPanel.setLayout(new GridLayout(1, 2));
        maxPanel.add(maxLinksPanel);
        maxPanel.add(spinnerPanel);
        JLabel letterLabel = new JLabel("Export Names :");
        JPanel letterPanel = new JPanel();
        letterPanel.add(letterLabel);
        letterPanel.setPreferredSize(new Dimension(190, 30));
        JPanel changePanel = new JPanel();
        changePanel.add(changeNames);
        changePanel.setPreferredSize(new Dimension(190, 30));
        JPanel expPanel = new JPanel();
        expPanel.setLayout(new GridLayout(1, 2));
        expPanel.add(letterPanel);
        expPanel.add(changePanel);
        files.setLayout(new GridLayout(2, 1));
        files.add(maxPanel);
        files.add(expPanel);
    }
    
}
