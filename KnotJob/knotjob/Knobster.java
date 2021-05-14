/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import knotjob.diagrams.ShowDiagram;
import knotjob.links.*;
import knotjob.dialogs.*;
import knotjob.filters.AndFilter;
import knotjob.filters.CompFilter;
import knotjob.filters.Filter;
import knotjob.filters.KhovFilter;
import knotjob.filters.NotFilter;
import knotjob.filters.OrFilter;
import knotjob.filters.SInvFilter;
import knotjob.homology.HomologyInfo;
import knotjob.frames.ViewCohomology;
import knotjob.frames.ViewDocumentation;
import knotjob.frames.ViewOddHomology;
import knotjob.frames.ViewSInvariants;
import knotjob.homology.oddkhov.OddKhovCalculator;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.homology.evenkhov.sinv.SInvariantCalculator;
import knotjob.homology.evenkhov.sinv.SqOneCalculator;
import knotjob.homology.oddkhov.sinv.SqOneOddCalculator;

/**
 *
 * @author Dirk
 */
public class Knobster extends JFrame {
    
    private final DefaultListModel<String> listModelAll;
    private final DefaultListModel<String> listModelFiltered;
    private final ArrayList<LinkData> allLinks;
    private final ArrayList<LinkData> filteredLinks;
    private final JList<String> list;
    private int[] choices;
    private int choice; 
    private final JPanel panelLinks;
    private final JPanel panelKnotInfo;
    private final JPanel panelButtons;
    private JLabel labelLinkNumber;
    private boolean filtered;
    public final Options options;
    private final Comparer comparer;
    private final Image img;
    private final ArrayList<Filter> existingFilters;
    private Filter activeFilter;
    
    public Knobster(String title, Options optns) {
        super(title);
        options = optns;
        img = options.getImage();
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        listModelAll = new DefaultListModel<String>();
        listModelFiltered = new DefaultListModel<String>();
        allLinks = new ArrayList<LinkData>();
        filteredLinks = new ArrayList<LinkData>();
        list = new JList<String>(listModelAll);
        choices = null;
        choice = -1;
        panelLinks = new JPanel();
        panelKnotInfo = new JPanel();
        panelButtons = new JPanel();
        filtered = false;
        existingFilters = new ArrayList<Filter>();
        comparer = new Comparer(0);
        arrangeStuff();
    }

    public void setAbout() {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(AboutEvent e) {
                showInfo();
            }
        });
        desktop.setQuitHandler(new QuitHandler() {
            @Override
            public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
                if (yesNoDialog("Quit KnotJob", Color.CYAN)) System.exit(0);
            }
        });// */
    }
    
    public void setClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                if (yesNoDialog("Quit KnotJob", Color.ORANGE)) System.exit(0);
            }
        });
    }
    
    public void setIcon() {
        if (img != null) setIconImage(img);
        Taskbar taskbar = Taskbar.getTaskbar();
        if (img != null) taskbar.setIconImage(img);
    }
    
    private void arrangeStuff() {
        addMenus();
        addLinkList();
        addFilters();
        setKnotInfoEmpty();
    }

    private void addFilters() {
        CompFilter compFilter = new CompFilter("2-Component Links",true,2,2);
        SInvFilter sinvFilter = new SInvFilter("Non const s-Invariant",true,false,true,false,false,0,0);
        SInvFilter sqnvFilter = new SInvFilter("Non const Sq^1-even",false,true,true,false,false,0,0);
        SInvFilter sqovFilter = new SInvFilter("Non const Sq^1-odd",false,true,false,false,false,0,0);
        KhovFilter khovFilter = new KhovFilter("3<=Tor unred Kh", false, false, false, 3, 0,options.getPrimes());
        KhovFilter khorFilter = new KhovFilter("3<=Tor red Kh", false, true, false, 3, 0,options.getPrimes());
        KhovFilter khmiFilter = new KhovFilter("Mirror inv unr Kh", false, false, false, options.getPrimes());
        KhovFilter khmrFilter = new KhovFilter("Mirror inv red rat Kh", false, true, true, options.getPrimes());
        KhovFilter widtFilter = new KhovFilter("3<=Width unred Kh",options.getPrimes(), false, false, false, 3, 0);
        existingFilters.add(compFilter);
        existingFilters.add(sinvFilter);
        existingFilters.add(sqnvFilter);
        existingFilters.add(sqovFilter);
        existingFilters.add(khovFilter);
        existingFilters.add(khorFilter);
        existingFilters.add(widtFilter);
        existingFilters.add(khmiFilter);
        existingFilters.add(khmrFilter);
        activeFilter = null;
    }
    
    private void addMenus() {
        JMenuBar menubar = new JMenuBar();
        addFiletoMenu(menubar);
        addEdittoMenu(menubar);
        addFilttoMenu(menubar);
        addDiagramtoMenu(menubar);
        addHelptoMenu(menubar);
        this.setJMenuBar(menubar);
    }

    private void addFiletoMenu(JMenuBar menubar) {
        JMenu file = new JMenu("File");
        JMenuItem newlink = new JMenuItem("New Link");
        JMenuItem lodlink = new JMenuItem("Open Link(s)");
        JMenuItem savlink = new JMenuItem("Save Link(s)");
        JMenuItem explink = new JMenuItem("Export Link(s)");
        JMenuItem quilink = new JMenuItem("Quit");
        if (options.getOperatingSystem() != 2) {
            quilink.setMnemonic(KeyEvent.VK_Q);
            quilink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        }
        quilink.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (yesNoDialog("Quit KnotJob", Color.GREEN)) System.exit(0);
            }
        });
        lodlink.setMnemonic(KeyEvent.VK_O);
        lodlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        lodlink.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<LinkData> links = loadLinks();
                for (LinkData ink : links) {
                    allLinks.add(ink);
                    listModelAll.addElement(ink.name);
                }
                if (!filtered) labelLinkNumber.setText("Links : "+allLinks.size());// */
                else labelLinkNumber.setText("Links : "+filteredLinks.size());
            }
        });// */
        newlink.setMnemonic(KeyEvent.VK_N);
        newlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newlink.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<LinkData> inks = getNewLink();
                for (LinkData ink : inks) {
                    if (ink != null) {
                        allLinks.add(ink);
                        listModelAll.addElement(ink.name);
                    }
                }
                if (!filtered) labelLinkNumber.setText("Links : "+allLinks.size());// */
                else labelLinkNumber.setText("Links : "+filteredLinks.size());
            }
        });// */
        savlink.setMnemonic(KeyEvent.VK_S);
        savlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        savlink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (choices != null) saveLinks();
            }
        });// */
        explink.setMnemonic(KeyEvent.VK_E);
        explink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        explink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (choices != null) exportLinks();
            }
        });
        file.add(newlink);
        file.add(lodlink);
        file.add(savlink);
        file.addSeparator();
        file.add(explink);
        file.addSeparator();
        file.add(quilink);
        menubar.add(file);
    }

    private void addEdittoMenu(JMenuBar menubar) {
        JMenu edit = new JMenu("Edit");
        JMenuItem editlink = new JMenuItem("Edit Link");
        JMenuItem deletelink = new JMenuItem("Remove Link(s)");
        JMenuItem chosallink = new JMenuItem("Select All");
        JMenuItem optionlink = new JMenuItem("Options");
        editlink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choice >= 0) editLink();
            }
        });
        deletelink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choice >= 0 && yesNoDialog("Remove selected links", Color.BLUE)) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    int i = choices.length-1;
                    while (i >= 0) {
                        LinkData toRemove = linkList.get(choices[i]);
                        listModelAll.removeElementAt(allLinks.indexOf(toRemove));
                        allLinks.remove(toRemove);
                        if (filteredLinks.contains(toRemove)) {
                            listModelFiltered.removeElementAt(filteredLinks.indexOf(toRemove));
                            filteredLinks.remove(toRemove);
                        }
                        i--;
                    }
                    if (filtered) list.setModel(listModelFiltered);
                    else list.setModel(listModelAll);
                    setKnotInfoEmpty();
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        chosallink.setMnemonic(KeyEvent.VK_A);
        chosallink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        chosallink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                int n = allLinks.size();
                if (filtered) n = filteredLinks.size();
                int[] all = new int[n];
                for (int i = 0; i < all.length; i++) all[i]=i;
                list.setSelectedIndices(all);
                choices = all;
                if (choices.length > 0) choice = 0;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        optionlink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOptions();
            }
        });
        edit.add(editlink);
        edit.add(deletelink);
        edit.add(chosallink);
        edit.addSeparator();
        edit.add(optionlink);
        menubar.add(edit);
    }

    private void addFilttoMenu(JMenuBar menubar) {
        JMenu sorter = new JMenu("Filters");
        JMenuItem sortlinks = new JMenuItem("Sort Links");
        JMenuItem creafilter = new JMenuItem("Create Filter");
        JMenuItem selfilter = new JMenuItem("Select Filter");
        JMenuItem recfilter = new JMenuItem("Recalculate Filter");
        JMenuItem editfilter = new JMenuItem("Edit Filter Name");
        JMenuItem remfilter = new JMenuItem("Remove Filter");
        selfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFilters();
            }
        });
        creafilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createFilter();
            }
        });
        recfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFilter();
            }
        });
        editfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editFilter();
            }
        });
        remfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeFilter();
            }
        });
        sortlinks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sortLinks();
            }
        });
        recfilter.setMnemonic(KeyEvent.VK_R);
        recfilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        selfilter.setMnemonic(KeyEvent.VK_F);
        selfilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        sorter.add(selfilter);
        sorter.add(creafilter);
        sorter.add(recfilter);
        sorter.add(editfilter);
        sorter.add(remfilter);
        sorter.addSeparator();
        sorter.add(sortlinks);
        menubar.add(sorter);
    }
    
    private void addDiagramtoMenu(JMenuBar menubar) {
        JMenu diagram = new JMenu("Diagram");
        JMenuItem girthdiag = new JMenuItem("Minimize Girth");
        JMenuItem setdiag = new JMenuItem("Choose Girth-minimized Diagram");
        setdiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> theLinks = allLinks;
                    if (filtered) theLinks = filteredLinks;
                    for (int i : choices) {
                        LinkData lData = theLinks.get(i);
                        if (lData.links.size()> 1) {
                            int mingirth = lData.links.get(0).maxGirth();
                            int mintgirth = lData.links.get(0).totalGirth();
                            int choose = 0;
                            for (int j = 1; j < lData.links.size(); j++) {
                                Link link = lData.links.get(j);
                                int girth = link.maxGirth();
                                int tgirth = link.totalGirth();
                                if (girth < mingirth | (girth == mingirth & tgirth < mintgirth)) {
                                    mingirth = girth;
                                    mintgirth = tgirth;
                                    choose = j;
                                }
                            }
                            lData.setChosen(choose);
                        }
                    }
                    if (choices.length > 0) setLinkInfo(theLinks.get(choice));
                }
            }
        });
        girthdiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    linkList = chosenLinks(linkList,choices);
                    minimizeGirth(linkList);
                    if (choices.length > 0) setLinkInfo(linkList.get(0));
                }
            }
        });
        girthdiag.setMnemonic(KeyEvent.VK_G);
        girthdiag.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        diagram.add(girthdiag);
        diagram.add(setdiag);
        menubar.add(diagram);
    }
    
    private void addHelptoMenu(JMenuBar menubar) {
        JMenu help = new JMenu("Help");
        JMenuItem helpItem = new JMenuItem("KnotJob Documentation");
        JMenuItem infoItem = new JMenuItem("About KnotJob");
        helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpInfo();
            }
        });
        infoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInfo();
            }
        });
        help.add(helpItem);
        help.addSeparator();
        help.add(infoItem);
        menubar.add(help);
    }
    
    private void helpInfo() {
        ViewDocumentation viewer = new ViewDocumentation(this, "KnotJob Documentation", options);
        viewer.setUpStuff();
    }
    
    protected void showInfo() {
        InfoDialog dial = new InfoDialog(null, "About", true, options);
        dial.showInfo();
    }
    
    protected boolean yesNoDialog(String title, Color col) {
        YesNoDialog dial = new YesNoDialog(this, title, true, options, col);
        return dial.showDialog();
    }
    
    private void addLinkList() {
        Container box = Box.createVerticalBox();
        JScrollPane scroller = new JScrollPane(list);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        box.setPreferredSize(new Dimension(200,470));
        box.add(scroller);
        panelLinks.setLayout(new BorderLayout());
        panelLinks.add(box, BorderLayout.CENTER);
        labelLinkNumber = new JLabel("Links : 0");
        JCheckBox filteredOk = new JCheckBox("Filter");
        JPanel filterPanel = new JPanel();
        filterPanel.add(labelLinkNumber);
        filterPanel.add(filteredOk);
        filteredOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JCheckBox theBox = (JCheckBox) ae.getSource();
                if (theBox.isSelected()) {
                    filtered = true;
                    list.setModel(listModelFiltered);
                    labelLinkNumber.setText("Links : "+filteredLinks.size());
                }
                else {
                    filtered = false;
                    list.setModel(listModelAll);
                    labelLinkNumber.setText("Links : "+allLinks.size());
                }
                setKnotInfoEmpty();
            }
        });
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent le) {
                @SuppressWarnings("unchecked")
                JList<String> list = (JList<String>) le.getSource();
                choices = list.getSelectedIndices();
                int u = list.getSelectedIndex();
                if ( u >= 0 & u != choice) {
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    setLinkInfo(linkList.get(u));
                    choice = u;
                }
            }
        });
        panelLinks.add(filterPanel, BorderLayout.SOUTH);
        setUpPanelButtons();
        setLayout(new BorderLayout());
        add(panelLinks, BorderLayout.WEST);
        add(panelKnotInfo, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);
        
    }

    private void setUpPanelButtons() {
        panelButtons.setPreferredSize(new Dimension(200, 200));
        panelButtons.setBackground(options.getColor());
        JButton calcSInvariant = new JButton("s-Invariants");
        calcSInvariant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calcSInvariant(chosenLinks(linkList,choices));
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JButton lipSarInvariant = new JButton("Lipshitz-Sarkar Invariants");
        lipSarInvariant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calculateLipSar(chosenLinks(linkList,choices));
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JButton calcModKhovHom = new JButton("Modular Khovanov Cohomology");
        calcModKhovHom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calculateKhovHom(chosenLinks(linkList,choices),true);
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JButton calcModOddHom = new JButton("Modular Odd Khovanov Homology");
        calcModOddHom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calculateOddKhovHom(chosenLinks(linkList,choices),true);
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JButton calcChaKhovHom = new JButton("Char 0 Khovanov Cohomology");
        calcChaKhovHom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calculateKhovHom(chosenLinks(linkList,choices),false);
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JButton calcChaOddHom = new JButton("Char 0 Odd Khovanov Homology");
        calcChaOddHom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calculateOddKhovHom(chosenLinks(linkList,choices),false);
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JButton jonesPolynomial = new JButton("Jones Polynomial");
        jonesPolynomial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calculateJones(chosenLinks(linkList,choices));
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JButton whiteTorsion = new JButton("Whitehead Torsion");
        whiteTorsion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    if (choices.length > 0) {
                        ArrayList<LinkData> linkList = allLinks;
                        if (filtered) linkList = filteredLinks;
                        calculateTorsion(chosenLinks(linkList,choices));
                        setLinkInfo(linkList.get(choice));
                    }
                }
            }
        });
        JPanel calcSPanel = buttonPanel(calcSInvariant);
        JPanel lipSarPanel = buttonPanel(lipSarInvariant);
        JPanel modevKhPanel = buttonPanel(calcModKhovHom);
        JPanel charevKhPanel = buttonPanel(calcChaKhovHom);
        JPanel mododKhPanel = buttonPanel(calcModOddHom);
        JPanel charodKhPanel = buttonPanel(calcChaOddHom);
        JPanel torsionPanel = buttonPanel(whiteTorsion);
        panelButtons.setLayout(new GridLayout(5,3));
        for (int i = 0; i < 3; i++) panelButtons.add(buttonPanel(null));
        panelButtons.add(calcSPanel);
        panelButtons.add(charevKhPanel);
        panelButtons.add(charodKhPanel);
        panelButtons.add(lipSarPanel);
        panelButtons.add(modevKhPanel);
        panelButtons.add(mododKhPanel);
        //panelButtons.add(torsionPanel);
        for (int i = 0; i < 5; i++) panelButtons.add(buttonPanel(null));
        
        //panelButtons.add(jonesPolynomial);
    }
    
    private JPanel buttonPanel(JButton theButton) {
        JPanel thePanel = new JPanel();
        thePanel.setBackground(options.getColor());
        if (theButton != null) thePanel.add(theButton);
        return thePanel;
    }
    
    private ArrayList<LinkData> getNewLink() {
        JDialog fram = new JDialog(new JFrame(),"New Link", true);
        fram.setSize(400,300);
        fram.setLocationRelativeTo(this);
        fram.setResizable(false);
        fram.setLayout(new GridLayout(7,2));
        JButton torLink = new JButton("Torus Link");
        JButton preLink = new JButton("Pretzel Link");
        JButton dtKnot  = new JButton("DT Code");
        JButton pdLink  = new JButton("PD Code"); 
        JButton gaLink  = new JButton("Gauss Code");
        JButton adtLink = new JButton("alphabetical DT Code");
        JButton braLink = new JButton("Braid Code");
        JButton conLink = new JButton("Concatenate Links");
        JButton splLink = new JButton("Split Union");
        JButton mirLink = new JButton("Mirror Links");
        JButton unLink  = new JButton("Unlink");
        JButton orLink  = new JButton("Change Orientation");
        JButton cancelB = new JButton("Cancel");
        torLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                torLink.setEnabled(false);
            }
        });
        preLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                preLink.setEnabled(false);
            }
        });
        dtKnot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                dtKnot.setEnabled(false);
            }
        });
        pdLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                pdLink.setEnabled(false);
            }
        });
        adtLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                adtLink.setEnabled(false);
            }
        });
        braLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                braLink.setEnabled(false);
            }
        });
        conLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                conLink.setEnabled(false);
            }
        });
        splLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                splLink.setEnabled(false);
            }
        });
        mirLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                mirLink.setEnabled(false);
            }
        });
        cancelB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
            }
        });
        gaLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                gaLink.setEnabled(false);
            }
        });
        unLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                unLink.setEnabled(false);
            }
        });
        orLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                orLink.setEnabled(false);
            }
        });
        JPanel torPanel = new JPanel();
        JPanel prePanel = new JPanel();
        JPanel dtcPanel = new JPanel();
        JPanel pdcPanel = new JPanel();
        JPanel gauPanel = new JPanel();
        JPanel adtPanel = new JPanel();
        JPanel braPanel = new JPanel();
        JPanel conPanel = new JPanel();
        JPanel splPanel = new JPanel();
        JPanel mirPanel = new JPanel();
        JPanel unPanel  = new JPanel();
        JPanel orPanel  = new JPanel();
        JPanel canPanel = new JPanel();
        torPanel.add(torLink);
        prePanel.add(preLink);
        dtcPanel.add(dtKnot);
        pdcPanel.add(pdLink);
        gauPanel.add(gaLink);
        adtPanel.add(adtLink);
        braPanel.add(braLink);
        conPanel.add(conLink);
        splPanel.add(splLink);
        mirPanel.add(mirLink);
        unPanel.add(unLink);
        orPanel.add(orLink);
        canPanel.add(cancelB);
        fram.add(torPanel);
        fram.add(prePanel);
        fram.add(braPanel);
        fram.add(dtcPanel);
        fram.add(pdcPanel);
        fram.add(gauPanel);
        fram.add(adtPanel);
        fram.add(conPanel);
        fram.add(splPanel);
        fram.add(mirPanel);
        fram.add(unPanel);
        fram.add(orPanel);
        fram.add(canPanel);
        fram.setVisible(true);
        ArrayList<LinkData> links = new ArrayList<LinkData>();
        if (!torLink.isEnabled()) links.add(enterTorusLink());
        if (!preLink.isEnabled()) links.add(enterPretzelLink());
        if (!dtKnot.isEnabled())  links.add(LinkCreator.enterDTCode(null, null, true, this, comparer));
        if (!pdLink.isEnabled())  links.add(LinkCreator.enterPDCode(null, null, true, this, comparer));
        if (!gaLink.isEnabled())  links.add(LinkCreator.enterGaussCode(this, comparer));
        if (!adtLink.isEnabled()) links.add(LinkCreator.enterADTCode(null, null, true, this, comparer));
        if (!braLink.isEnabled()) links.add(LinkCreator.enterBraidCode(null, null, this, true, comparer));
        if (!conLink.isEnabled()) links.add(concatenateLinks());
        if (!splLink.isEnabled()) links.add(disjointUnion());
        if (!mirLink.isEnabled()) links = mirrorLink();
        if (!unLink.isEnabled())  links.add(unLink());
        if (!orLink.isEnabled())  links.add(orLink());
        return links;
    }

    private LinkData concatenateLinks() {
        ArrayList<LinkData> theLinks = allLinks;
        DefaultListModel<String> listModel = listModelAll;
        if (filtered) {
            theLinks = filteredLinks;
            listModel = listModelFiltered;
        }
        if (theLinks.isEmpty()) return null;
        UnionKnot uKnot = new UnionKnot(this, "Concatenate Links", theLinks, listModel, true);
        if (uKnot.knot == null) return null;
        String name = uKnot.name;
        return new LinkData(name,uKnot.knot.girthMinimize(),comparer);
    }
    
    private LinkData disjointUnion() {
        ArrayList<LinkData> theLinks = allLinks;
        DefaultListModel<String> listModel = listModelAll;
        if (filtered) {
            theLinks = filteredLinks;
            listModel = listModelFiltered;
        }
        if (theLinks.isEmpty()) return null;
        UnionKnot uKnot = new UnionKnot(this, "Split Union", theLinks, listModel, false);
        if (uKnot.knot == null) return null;
        String name = uKnot.name;
        return new LinkData(name,uKnot.knot.girthMinimize(),comparer);
    }
    
    private LinkData enterTorusLink() {
        JDialog fram = new JDialog(new JFrame(), "Torus Link", true);
        fram.setSize(400, 150);
        fram.setLocationRelativeTo(this);
        fram.setLayout(new BorderLayout());
        fram.setResizable(false);
        SpinnerNumberModel modelq = new SpinnerNumberModel(5,2,60,1);
        JSpinner spinnerq = new JSpinner(modelq);
        JLabel labep = new JLabel("Enter a p-value :");
        SpinnerNumberModel modelp = new SpinnerNumberModel(3,2,10,1);
        JSpinner spinnerp = new JSpinner(modelp);
        JLabel labeq = new JLabel("Enter a q-value :");
        JPanel panep = new JPanel();
        panep.add(labep);
        panep.add(spinnerp);
        JPanel paneq = new JPanel();
        paneq.add(labeq);
        paneq.add(spinnerq);
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JPanel paneb = new JPanel();
        paneb.add(ok);
        paneb.add(cancel);
        fram.add(panep,BorderLayout.NORTH);
        fram.add(paneq,BorderLayout.CENTER);
        fram.add(paneb,BorderLayout.SOUTH);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ok.setEnabled(false);
                fram.dispose();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
            }
        });
        fram.setVisible(true);
        if (ok.isEnabled()) return null;
        int p = modelp.getNumber().intValue();
        int q = modelq.getNumber().intValue();
        Link tLink = torusKnot(q,p);
        LinkData tKnot = new LinkData("T("+p+","+q+")", tLink,comparer);
        return tKnot;
    }
    
    private Link torusKnot(int p, int q) {
        int prod = p * (q-1);
        int [] crossings = new int[prod];
        for (int k = 0 ; k < prod; k++) {
            crossings[k] = 1;
        }
        int [][] paths = new int[prod][4];
        for (int i = 0; i < p; i++) {
            int u = i * (q-1);
            paths[u][0] = (2*i*(q-1)) + 2;
            paths[u][1] = ((2*i+1)*(q-1)) + 2;
            paths[u][2] = (2*(i+1)*(q-1)) + 1;
            paths[u][3] = (2*i*(q-1)) + 1;
        }
        paths[(p-1)*(q-1)][2] = 1;
        for (int j = 1; j < q-2; j++) {
            for (int i = 0; i < p ; i++) {
                int u = i * (q-1) + j;
                paths[u][0] = (2*i*(q-1)) + j + 2;
                paths[u][1] = ((2*i)+1)*(q-1) + j + 2;
                paths[u][2] = ((2*i)+2)*(q-1) + j + 1;
                paths[u][3] = ((2*i)+1)*(q-1) + j + 1;
            }
            paths[(p-1)*(q-1)+j][2] = j + 1;
        }
        for (int i = 0; i < p; i++) {
            int u = i * (q-1) + (q - 2);
            paths[u][0] = (2*i*(q-1)) + q;
            paths[u][1] = (2*(i+1)*(q-1)) + q;
            paths[u][2] = ((2*i+2)*(q-1)) + q - 1;
            if (q > 2) paths[u][3] = ((2*i+1)*(q-1)) + q - 1;
            else paths[u][3] = ((2*i+1)*(q-1));
        }
        paths[p*(q-1)-1][1] = q;
        paths[p*(q-1)-1][2] = q-1;
        Link tLink = new Link(crossings,paths);
        return tLink;
    }

    private ArrayList<LinkData> mirrorLink() {
        if (allLinks.isEmpty()) return new ArrayList<LinkData>(0);
        MirrorLink frame = new MirrorLink(this, "Mirror Link", true, false, listModelAll);
        frame.setUpStuff();
        if (frame.getChosen() == -1) return new ArrayList<LinkData>(0);
        ArrayList<LinkData> theLinks = new ArrayList<LinkData>();
        for (int i : frame.getAllChosen()) {
            Link theMirror = allLinks.get(i).chosenLink().mirror();
            theLinks.add(mirrorData("-"+allLinks.get(i).name, theMirror, allLinks.get(i)));
        }
        return theLinks;
    }
    
    private LinkData mirrorData(String name, Link theMirror, LinkData origLink) {
        LinkData mirrorData = new LinkData(name, theMirror, comparer);
        if (origLink.sqEven != null) {
            int[] sqeven = origLink.getSqOne(true); // at the moment we don't mirror the odd sq1.
            int[] mqeven = new int[4];
            mqeven[0] = -sqeven[2];
            mqeven[1] = -sqeven[3];
            mqeven[2] = -sqeven[0];
            mqeven[3] = -sqeven[1];
            mirrorData.setSqOne(mqeven);
        }
        if (origLink.sinvariant != null) {
            int[][] sinv = origLink.sInvariants();
            for (int i = 0; i < sinv.length; i++) {
                mirrorData.setSInvariant(sinv[i][0], -sinv[i][1]);
            }
        }
        if (origLink.khovInfo == null) return mirrorData;
        mirrorData.khovInfo = new ArrayList<String>();
        long[][] data = origLink.getStartInfo(origLink.khovInfo, options.getPrimes());
        for (int i = 0; i < origLink.khovInfo.size(); i++) {
            String info = origLink.khovInfo.get(i);
            boolean reduced = false;
            if (info.charAt(0) == 'r') {
                reduced = true;
                if (mirrorData.redKhovHom == null) mirrorData.redKhovHom = new ArrayList<String>();
            }
            else if (mirrorData.unredKhovHom == null) mirrorData.unredKhovHom = new ArrayList<String>();
            ArrayList<String> homStrings = origLink.unredKhovHom;
            if (reduced) homStrings = origLink.redKhovHom;
            HomologyInfo homInfo = origLink.theHomology(data[i], homStrings).mirror();
            ArrayList<String> mirrorKhovs = mirrorData.unredKhovHom;
            if (reduced) mirrorKhovs = mirrorData.redKhovHom;
            for (QuantumCohomology qCoh : homInfo.getHomologies()) {
                mirrorKhovs.add(qCoh.toString());
            }
            mirrorData.khovInfo.add(info);
        }
        return mirrorData;
    }
    
    private LinkData orLink() {
        if (allLinks.isEmpty()) return null;
        OrientLink frame = new OrientLink(this, "Change Orientation Link", true, listModelAll, allLinks);
        frame.setUpStuff();
        if (frame.chosen == -1) return null;
        Link theLink = allLinks.get(frame.chosen).chosenLink().componentChoice(frame.comps,frame.orient);
        return new LinkData("V"+allLinks.get(frame.chosen).name, theLink,comparer);
    }
    
    private LinkData unLink() {
        JDialog fram = new JDialog(new JFrame(), "Unlink", true);
        fram.setSize(300, 130);
        fram.setLocationRelativeTo(this);
        fram.setLayout(new BorderLayout());
        fram.setResizable(false);
        JPanel choicePanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(1,1,10,1);
        JSpinner spinner = new JSpinner(model);
        JLabel label = new JLabel("Number of components :");
        choicePanel.add(label);
        choicePanel.add(spinner);
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        fram.add(choicePanel, BorderLayout.CENTER);
        fram.add(buttonPanel, BorderLayout.SOUTH);
        fram.add(new JPanel(), BorderLayout.NORTH);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                fram.dispose();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fram.dispose();
            }
        });
        fram.setVisible(true);
        if (ok.isEnabled()) return null;
        int comp = model.getNumber().intValue();
        Link newLink = new Link(comp);
        String name = "U_"+comp;
        return new LinkData(name,newLink,comparer);
    }
    
    private void setKnotInfoEmpty() {
        panelKnotInfo.removeAll();
        JLabel label = new JLabel(" ");
        label.setPreferredSize(new Dimension(790,600));
        panelKnotInfo.add(label);
        panelKnotInfo.revalidate();
        choice = -1;
    }
    
    private void setLinkInfo(LinkData theLink) {
        JLabel label = new JLabel(theLink.name, JLabel.CENTER);
        label.setFont(new Font("Sans Serif",Font.BOLD, 16));
        panelKnotInfo.removeAll();
        JPanel topInfo = new JPanel();
        String commStr = " ";
        if (theLink.comment != null) commStr = "Comment : "+theLink.comment;
        JLabel comment = new JLabel(commStr, JLabel.CENTER);
        topInfo.setLayout(new GridLayout(2,1));
        topInfo.add(label);
        topInfo.add(comment);
        topInfo.setPreferredSize(new Dimension(500,50));
        JPanel crossingInfo = new JPanel();
        setUpCrossingInfo(crossingInfo,theLink);
        JPanel diagInfo = new JPanel();
        diagInfo.setLayout(new GridLayout(1,5));
        JLabel diagLabel = new JLabel("Diagrams", JLabel.CENTER);
        JPanel diagChoice = new JPanel();
        int n = theLink.links.size();
        diagChoice.setLayout(new GridLayout(n,1));
        ButtonGroup diagramButtons = new ButtonGroup();
        ActionListener listener;
        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JRadioButton btn = (JRadioButton) ae.getSource();
                int i = 0;
                try {
                    i = Integer.parseInt(btn.getName());
                }
                catch(NumberFormatException e) {

                }
                theLink.setChosen(i);
                setUpCrossingInfo(crossingInfo,theLink);
            }
        };
        for (int i = 0; i < n; i++) {
            int j = i+1;
            JRadioButton rButton = new JRadioButton("Diagram "+j);
            if (i == theLink.chosen()) rButton.setSelected(true);
            diagramButtons.add(rButton);
            diagChoice.add(rButton);
            rButton.setName(String.valueOf(i));
            rButton.addActionListener(listener);
        }
        JButton diagShow = new JButton("Show Diagram");
        JPanel diagShowP = new JPanel();
        diagShowP.add(diagShow, BorderLayout.CENTER);
        JButton diagDTCode = new JButton("DT-Code");
        JPanel diagDTCodeP = new JPanel();
        if (theLink.links.get(0).components() > 1) diagDTCode.setEnabled(false);
        diagShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTheDiagram(theLink);
            }
        });
        diagDTCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String code = theLink.chosenLink().dowkerThistle();
                showString("DT Code", code, theLink.name);
            }
        });
        diagDTCodeP.add(diagDTCode,BorderLayout.SOUTH);
        JButton diagGauss = new JButton("Gauss-Code");
        JPanel diagGaussP = new JPanel();
        diagGauss.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String code = theLink.chosenLink().gaussCode();
                showString("Gauss Code", code, theLink.name);
            }
        });
        diagGaussP.add(diagGauss, BorderLayout.CENTER);
        JScrollPane diagPane = new JScrollPane(diagChoice);
        diagPane.setPreferredSize(new Dimension(80,80));
        diagInfo.add(diagLabel);
        diagInfo.add(diagPane);
        diagInfo.add(diagShowP);
        diagInfo.add(diagDTCodeP);
        diagInfo.add(diagGaussP);
        JPanel invPanel = new JPanel();
        JPanel rasmusPanel = new JPanel();
        fillRasmussen(rasmusPanel,theLink);
        invPanel.add(rasmusPanel);
        JPanel lipsarPanel = new JPanel();
        fillLipSar(lipsarPanel,theLink);
        invPanel.add(lipsarPanel);
        JPanel khovanovPanel = new JPanel();
        fillKhovanov(khovanovPanel,theLink);
        invPanel.add(khovanovPanel);
        JPanel oddKhovPanel = new JPanel();
        fillOddKhov(oddKhovPanel, theLink);
        invPanel.add(oddKhovPanel);
        invPanel.setPreferredSize(new Dimension(700,300));
        panelKnotInfo.add(topInfo);
        panelKnotInfo.add(diagInfo);
        panelKnotInfo.add(crossingInfo);
        panelKnotInfo.add(invPanel);
        panelKnotInfo.revalidate();
    }

    private void fillRasmussen(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1,3));
        int[][] data = theLink.sInvariants();
        Integer ras = getValue(data,0);
        String rasmus = "     Rasmussen invariant : ";
        if (ras != null) rasmus = rasmus+ras;
        JLabel rasmusLabel = new JLabel(rasmus);
        ras = getValue(data,2);
        String sinv = "     s-Invariant mod 2 : ";
        if (ras != null) sinv = sinv+ras;
        JLabel sinvmodtwoLabel = new JLabel(sinv);
        JButton otherInvButton = new JButton("Other s-Invariants");
        otherInvButton.setEnabled(theLink.otherSInvariants());
        otherInvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayOtherSInvariants(theLink);
            }
        });
        panel.add(rasmusLabel);
        panel.add(sinvmodtwoLabel);
        panel.add(otherInvButton);
    }
    
    private void fillLipSar(JPanel panel, LinkData Link) {
        panel.setLayout(new GridLayout(1,3));
        JLabel LpSkLabel = new JLabel("Lipshitz-Sarkar Invariants");
        JPanel LpSkPanel = new JPanel();
        LpSkPanel.add(LpSkLabel);
        panel.add(LpSkPanel);
        String sq1Inv = "Sq^1-even : ";
        if (Link.sqEven != null) sq1Inv = sq1Inv+Link.sqEven;
        JLabel sq1Label = new JLabel(sq1Inv);
        JPanel sq1Panel = new JPanel();
        sq1Panel.add(sq1Label);
        panel.add(sq1Panel);
        String sq1oInv = "Sq^1-odd : ";
        if (Link.sqOdd != null) sq1oInv = sq1oInv+Link.sqOdd;
        JLabel sq1oLabel = new JLabel(sq1oInv);
        JPanel sq1oPanel = new JPanel();
        sq1oPanel.add(sq1oLabel);
        panel.add(sq1oPanel);
    }
    
    private void displayOtherSInvariants(LinkData theLink) {
        ViewSInvariants viewer = new ViewSInvariants(theLink,options);
        viewer.setupFrame();
        viewer.setLocationRelativeTo(this);
        viewer.setVisible(true);
    }
    
    private Integer getValue(int[][] data, int field) {
        boolean found = false;
        int i = 0;
        while (!found && i < data.length) {
            if (data[i][0] == field) found = true;
            else i++;
        }
        if (found) return data[i][1];
        return null;
    }
    
    private void fillOddKhov(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1,2));
        String khovHom = "Odd Khovanov Homology";
        JLabel akhovHomLabel = new JLabel(khovHom);
        JPanel akhovHomPanel = new JPanel();
        akhovHomPanel.add(akhovHomLabel);
        panel.add(akhovHomPanel);
        JButton aunredButton = new JButton("unreduced");
        aunredButton.setEnabled(theLink.showOddKhovButton());
        JPanel aunredPanel = new JPanel();
        aunredPanel.add(aunredButton);
        JButton aredButton = new JButton("reduced");
        aredButton.setEnabled(theLink.showOddKhovButton());
        aunredPanel.add(aredButton);
        panel.add(aunredPanel);
        aunredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOddHomology(theLink,false);
            }
        });
        aredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOddHomology(theLink,true);
            }
        });
    }
    
    private void fillKhovanov(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1,2));
        String khovHom = "Khovanov Cohomology";
        JLabel akhovHomLabel = new JLabel(khovHom);
        JPanel akhovHomPanel = new JPanel();
        akhovHomPanel.add(akhovHomLabel);
        panel.add(akhovHomPanel);
        JButton aunredButton = new JButton("unreduced");
        aunredButton.setEnabled(theLink.showKhovHomButton(false));
        JPanel aunredPanel = new JPanel();
        aunredPanel.add(aunredButton);
        JButton aredButton = new JButton("reduced");
        aredButton.setEnabled(theLink.showKhovHomButton(true));
        aunredPanel.add(aredButton);
        panel.add(aunredPanel);
        aunredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomology(theLink,false);
            }
        });
        aredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomology(theLink,true);
            }
        });
    }
    
    private void showOddHomology(LinkData theLink, boolean reduced) {
        ViewOddHomology viewer = new ViewOddHomology(theLink, reduced, options);
        String title;
        if (reduced) title = "Reduced Odd Khovanov Homology of "+theLink.name;
        else title = "Unreduced Odd Khovanov Homology of "+theLink.name;
        viewer.setUpStuff(title);
    }
    
    private void showHomology(LinkData theLink, boolean reduced) {
        ViewCohomology viewer = new ViewCohomology(theLink,reduced,options);
        String title;
        if (reduced) title = "Reduced Khovanov Cohomology of "+theLink.name;
        else title = "Unreduced Khovanov Cohomology of "+theLink.name;
        viewer.setUpStuff(title);
    }
    
    private void showString(String title, String code, String name) {
        JFrame fram = new JFrame(title+" of "+name);
        fram.setLayout(new GridLayout(2,1));
        fram.setSize(680,140);
        fram.setLocationRelativeTo(this);
        JLabel laba = new JLabel(title+" : ", SwingConstants.RIGHT);
        JLabel labb = new JLabel(code, SwingConstants.LEFT);
        JScrollPane scroller = new JScrollPane(labb);
        scroller.setPreferredSize(new Dimension(560,40));
        JPanel pane = new JPanel();
        pane.add(laba);
        pane.add(scroller);
        JButton copyDT = new JButton("Copy");
        JPanel panf = new JPanel();
        panf.add(copyDT);
        JButton close = new JButton("Close");
        panf.add(close);
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
            }
        });
        copyDT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                StringSelection stringSelection = new StringSelection(code);
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });
        fram.add(pane);
        fram.add(panf);
        fram.setResizable(false);
        fram.setVisible(true);
    }

    private void setUpCrossingInfo(JPanel panel, LinkData theLink) {
        panel.removeAll();
        panel.setLayout(new GridLayout(1,4));
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();
        int bc = 1 + theLink.chosenLink().basecomponent();
        JLabel compLabel = new JLabel("Components : "+theLink.chosenLink().components()+" ("+bc+")");
        int[] crsigns = theLink.chosenLink().crossingSigns();
        int writhe = crsigns[0]-crsigns[1];
        JLabel writLabel = new JLabel("Writhe : "+writhe);
        int crossngs = crsigns[0]+crsigns[1];
        int crossngz = theLink.chosenLink().crossingLength();
        JLabel crosLabel = new JLabel("Crossings : "+crossngs+"/"+crossngz);
        int girth = theLink.chosenLink().maxGirth();
        int tgirth = theLink.chosenLink().totalGirth();
        JLabel girtLabel = new JLabel("Girth : "+girth+"/"+tgirth);
        panel1.add(compLabel);
        panel2.add(writLabel);
        panel3.add(crosLabel);
        panel4.add(girtLabel);
        panel.add(panel1);
        panel.add(panel2);
        panel.add(panel3);
        panel.add(panel4);
        panel.revalidate();
    }
    
    private void showTheDiagram(LinkData theLink) {
        int number = theLink.chosen()+1;
        DiagramFrame frame = new DiagramFrame(theLink.name+" - Diagram "+number,options);
        frame.setSize(524,600);
        frame.setMinimumSize(new Dimension(524,600));
        frame.setLocationRelativeTo(this);
        frame.addBasics();
        frame.setVisible(true);
        ShowDiagram showDiagram = new ShowDiagram(theLink, 12, frame, options.getDivFactor());
        showDiagram.start();
    }

    private LinkData enterPretzelLink() {
        JDialog fram = new JDialog(new JFrame(), "Pretzel Link", true);
        fram.setSize(260,250);
        fram.setLocationRelativeTo(this);
        fram.setLayout(new BorderLayout());
        fram.setResizable(false);
        SpinnerNumberModel modelp = new SpinnerNumberModel(3,2,12,1);
        JSpinner spinnerp = new JSpinner(modelp);
        JLabel PreLabel = new JLabel("No. of Pretzels");
        JPanel PrePanel = new JPanel();
        PrePanel.add(PreLabel);
        PrePanel.add(spinnerp);
        fram.add(PrePanel, BorderLayout.WEST);
        SpinnerNumberModel[] models = new SpinnerNumberModel[12];
        JSpinner[] spinners = new JSpinner[12];
        JPanel SpinPanel = new JPanel();
        for (int i = 0; i < 12; i++) {
            models[i] = new SpinnerNumberModel(2,-999,999,1);
            spinners[i] = new JSpinner(models[i]);
            spinners[i].setPreferredSize(new Dimension(50,24));
        }
        addSpinners(SpinPanel, spinners,3);
        JScrollPane SpinPane = new JScrollPane(SpinPanel);
        spinnerp.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int k = modelp.getNumber().intValue();
                addSpinners(SpinPanel,spinners,k);
            }
        });
        SpinPane.setPreferredSize(new Dimension(100,150));
        JPanel ButtonPanel = new JPanel();
        JButton OKButton = new JButton("OK");
        JButton CancelButton = new JButton("Cancel");
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                spinnerp.setEnabled(false);
            }
        });
        CancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
            }
        });
        ButtonPanel.add(OKButton);
        ButtonPanel.add(CancelButton);
        fram.add(SpinPane, BorderLayout.EAST);
        fram.add(ButtonPanel, BorderLayout.SOUTH);
        fram.setVisible(true);
        LinkData preLink = null;
        if (!spinnerp.isEnabled()) {
            int k = modelp.getNumber().intValue();
            int[] crossings = new int[k];
            int[][] paths = new int[k][4];
            for (int u = 0; u < k; u++) crossings[u] = models[u].getNumber().intValue();
            String name = "P("+crossings[0];
            for (int u = 1; u < k; u++) name = name+","+crossings[u];
            name = name +")";
            for (int u = 0; u < k; u++) {
                paths[u][0] = 2*u + 1;
                paths[u][1] = 2*u + 2;
                paths[u][2] = 2*u;
                paths[u][3] = 2*u - 1;
            }
            paths[0][2] = 2*k;
            paths[0][3] = 2*k - 1;
            Link link = new Link(crossings,paths);
            preLink = new LinkData(name,link,comparer);
        }
        return preLink;
    }
    
    private void addSpinners(JPanel SpinPanel, JSpinner[] spinners, int k) {
        SpinPanel.removeAll();
        SpinPanel.setLayout(new GridLayout(k,1));
        for (int i = 0; i < k; i++) {
            JPanel panel = new JPanel();
            panel.add(spinners[i]);
            SpinPanel.add(panel);
        }
        SpinPanel.revalidate();
    }
    
    private void exportLinks() {
        ArrayList<LinkData> theLinks = allLinks;
        if (filtered) theLinks = filteredLinks;
        LinkSaver exporter = new LinkSaver(theLinks, choices, options, this);
        exporter.export();
        
    }
    
    
    
    private void saveLinks() {
        ArrayList<LinkData> theLinks = allLinks;
        if (filtered) theLinks = filteredLinks;
        LinkSaver saver = new LinkSaver(theLinks, choices, options, this);
        saver.save();
        
        /*int i = 0;
        boolean keepgoing = true;
        ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
        ArrayList<String> filenames = new ArrayList<String>();
        ArrayList<LinkData> theLinks = allLinks;
        if (filtered) theLinks = filteredLinks;
        while (keepgoing) {
            ArrayList<String> theData = new ArrayList<String>();
            int counter = 0;
            String filename = theLinks.get(choices[i]).name;
            while (i < choices.length && counter < 1000) {
                LinkData ink = theLinks.get(choices[i]);
                theData.add("0:"+ink.name);
                if (ink.comment != null) theData.add("1:"+ink.comment);
                for (Link link : ink.links) {
                    theData.add("2:"+link.crossingsToString());
                    theData.add("3:"+link.pathToString());
                    theData.add("9:"+link.orientToString());
                }
                if (ink.unredKhovHom != null) for (String ukhov : ink.unredKhovHom) theData.add("4:"+ukhov);
                if (ink.redKhovHom != null) for (String rkhov : ink.redKhovHom) theData.add("5:"+rkhov);
                if (ink.khovInfo != null) for (String rinf : ink.khovInfo) theData.add("6:"+rinf);
                if (ink.oddKhovHom != null) for (String okhov : ink.oddKhovHom) theData.add("10:"+okhov);
                if (ink.okhovInfo != null) for (String oinf : ink.okhovInfo) theData.add("11:"+oinf);
                if (ink.sinvariant != null) theData.add("8:"+ink.sinvariant);
                if (ink.smod4 != null) theData.add("7:"+ink.smod4);
                if (ink.sqOdd != null) theData.add("12:"+ink.sqOdd);
                i++;
                counter++;
            }
            if (counter > 1) filename = filename+"-"+theLinks.get(choices[i-1]).name;
            filenames.add(filename);
            allData.add(theData);
            if (i >= choices.length) keepgoing = false;
        }
        for (int j = 0; j < filenames.size(); j++) {
            JFileChooser chooser = new JFileChooser();
            if (options.getLoadLinksFrom() != null) chooser.setCurrentDirectory(options.getLoadLinksFrom());
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Knots and Links (*.kjb)", "kjb");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(new File(filenames.get(j)+".kjb"));
            int val = chooser.showSaveDialog(this);
            if (val == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    options.setLoadLinksFrom(chooser.getCurrentDirectory());
                    String fname = file.getAbsolutePath();
                    if(!fname.endsWith(".kjb") ) {
                        file = new File(fname + ".kjb");
                    }
                    try (FileWriter fw = new FileWriter(file)) {
                        PrintWriter pw = new PrintWriter(fw);
                        for (String data : allData.get(j)) pw.println(data);
                        pw.close();
                        fw.close();
                    }
                }
                catch (IOException e) {

                }
            }
        }// */
    }
    
    private ArrayList<LinkData> loadLinks() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ArrayList<LinkData> theLinks = new ArrayList<LinkData>();
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if (options.getLoadLinksFrom() != null) chooser.setCurrentDirectory(options.getLoadLinksFrom());
        FileNameExtensionFilter filtek = new FileNameExtensionFilter("KnotJob Links (*.kjb)", "kjb");
        FileNameExtensionFilter filtet = new FileNameExtensionFilter("TKnotJob Links (*.tkj)", "tkj");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SKnotJob Links (*.kts)", "kts");
        FileNameExtensionFilter filtex = new FileNameExtensionFilter("XKnotJob Links (*.gld)", "gld");
        FileNameExtensionFilter filtes = new FileNameExtensionFilter("KnotScape Knots (*.dtc)", "dtc");
        FileNameExtensionFilter filtey = new FileNameExtensionFilter("Planar Diagrams (*.txt)", "txt");
        FileNameExtensionFilter filteb = new FileNameExtensionFilter("Braid Diagrams (*.brd)", "brd");
        FileNameExtensionFilter filtea = new FileNameExtensionFilter("Alphabetical DT-Code (*.adc)", "adc");
        chooser.setFileFilter(filtek);
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(filtet);
        chooser.addChoosableFileFilter(filtex);
        chooser.addChoosableFileFilter(filtes);
        chooser.addChoosableFileFilter(filtey);
        chooser.addChoosableFileFilter(filteb);
        chooser.addChoosableFileFilter(filtea);
        int val = chooser.showOpenDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            LoadDialog fram = new LoadDialog(this, "Loading Links", true);
            LinkLoader loading = new LinkLoader(chooser,fram,this);
            loading.start();
            fram.setUpStuff();
            theLinks = loading.getLinks();
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return theLinks;
    }
    
    void delay(int k) {
        try {
            Thread.sleep(k);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private ArrayList<LinkData> chosenLinks(ArrayList<LinkData> linkList, int[] choices) {
        ArrayList<LinkData> theList = new ArrayList<LinkData>(choices.length);
        for (int c : choices) theList.add(linkList.get(c));
        return theList;
    }
    
    private void calculateTorsion(ArrayList<LinkData> linkList) {
        CalculationDialog frame = new CalculationDialog(this, "RBN-Torsion", true, 3);
        //WhTorsionCalculator calculator = new WhTorsionCalculator(linkList,options,frame);
        //calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateKhovHom(ArrayList<LinkData> linkList, boolean mod) {
        KhovDialog diagFrame = new KhovDialog(this,mod);
        long val = diagFrame.getValue();
        diagFrame.dispose();
        if (diagFrame.isOkay()) calculateKnotHom(linkList,val);
    }
    
    private void calculateOddKhovHom(ArrayList<LinkData> linkList, boolean mod) {
        KhovDialog diagFrame = new KhovDialog(this,mod);
        diagFrame.setTitle("Odd Khovanov Homology");
        long val = diagFrame.getValue();
        diagFrame.dispose();
        if (diagFrame.isOkay()) calculateOddKnotHom(linkList,val);
    }
    
    private void calculateKnotHom(ArrayList<LinkData> linkList, long val) {
        String khovTitle = "Integral Khovanov Cohomology";
        if (val == 1) khovTitle = "Rational Khovanov Cohomology";
        if (val < 0) khovTitle = "Local Khovanov Cohomology";
        if (val > 1) khovTitle = "Khovanov Cohomology mod "+val;
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(this, khovTitle, true, lev);
        EvenKhovCalculator calculator = new EvenKhovCalculator(linkList, val, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateOddKnotHom(ArrayList<LinkData> linkList, long val) {
        String khovTitle = "Integral Odd Khovanov Homology";
        if (val == 1) khovTitle = "Rational Odd Khovanov Homology";
        if (val < 0) khovTitle = "Local Odd Khovanov Homology";
        if (val > 1) khovTitle = "Odd Khovanov Homology mod "+val;
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(this, khovTitle, true, lev);
        OddKhovCalculator calculator = new OddKhovCalculator(linkList, val, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calcSInvariant(ArrayList<LinkData> linkList) {
        CharDialog charFrame = new CharDialog(this,"Choose a Characteristic",true,options);
        int p = charFrame.getChar();
        charFrame.dispose();
        if (p<0) return;
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(this,"Calculate s-Invariant mod "+p, true, lev);
        SInvariantCalculator calculator = new SInvariantCalculator(linkList, p, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSar(ArrayList<LinkData> linkList) {
        SquareDialog squaFrame = new SquareDialog(this,"Lipshitz-Sarkar Invariants",true);
        int dig = squaFrame.getSquare();
        squaFrame.dispose();
        if (dig == 1) calculateLipSarEvenSqOne(linkList);
        if (dig == -1) calculateLipSarOddSqOne(linkList);
    }
    
    private void calculateLipSarOddSqOne(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(this,"Calculate odd Sq^1 s-Invariant",true, lev);
        SqOneOddCalculator calculator = new SqOneOddCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSarEvenSqOne(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(this,"Calculate even Sq^1 s-Invariant",true, lev);
        SqOneCalculator calculator = new SqOneCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateJones(ArrayList<LinkData> linkList) {
        CalculationDialog frame = new CalculationDialog(this, "Calculate Jones Polynomial", true, 2);
        //JonesCalculator calculator = new JonesCalculator(linkList,options,frame);
        //calculator.start();
        frame.setUpStuff();
    }
    
    private void minimizeGirth(ArrayList<LinkData> theLinks) {
        JDialog fram = new JDialog(new JFrame(), "Minimize Girth", true);
        fram.setSize(300,250);
        fram.setLocationRelativeTo(this);
        fram.setLayout(new BorderLayout());
        fram.setResizable(false);
        JButton abortButton = new JButton("Abort");
        JButton cancelButton = new JButton("Cancel");
        JPanel abortPanel = new JPanel();
        JPanel cancelPanel = new JPanel();
        abortPanel.add(abortButton);
        cancelPanel.add(cancelButton);
        JPanel btnPanel = new JPanel();
        btnPanel.add(abortPanel);
        btnPanel.add(cancelPanel);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4,1));
        JLabel knotLabel = new JLabel("Knot",JLabel.CENTER);
        JPanel startPanel = new JPanel();
        startPanel.setLayout(new GridLayout(1,3));
        JLabel startLabel = new JLabel("Start : ", JLabel.RIGHT);
        JLabel posLabel = new JLabel("0", JLabel.LEFT);
        JButton skipButton = new JButton("Skip");
        JPanel skipPanel = new JPanel();
        skipPanel.add(skipButton);
        JPanel optPanel = new JPanel();
        optPanel.setLayout(new GridLayout(1,2));
        JLabel optLabel = new JLabel("Minimal Girth : ", JLabel.RIGHT);
        JLabel optnumLabel = new JLabel(" 0/0", JLabel.LEFT);
        optPanel.add(optLabel);
        optPanel.add(optnumLabel);
        startPanel.add(startLabel);
        startPanel.add(posLabel);
        startPanel.add(skipPanel);
        infoPanel.add(knotLabel);
        infoPanel.add(startPanel);
        infoPanel.add(optPanel);
        infoPanel.add(btnPanel);
        fram.add(infoPanel);
        GirthMinimizer giMi = new GirthMinimizer(theLinks,knotLabel,posLabel,optnumLabel,options.getTotGirth(),fram);
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                giMi.setSkipped(true);
            }
        });
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                giMi.setSkipped(true);
                giMi.setAborted(true);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                giMi.setSkipped(true);
                giMi.setAborted(true);
                giMi.setCancelled(true);
                fram.dispose();
            }
        });
        giMi.start();
        fram.setVisible(true);
        giMi.setCancelled(true);
    }
    
    private void setOptions() {
        OptionDialog opts = new OptionDialog(this, "Options", true, options);
        opts.setUpStuff();
    }
    
    public Options getOptions() {
        return options;
    }
    
    private void editLink() {
        ArrayList<LinkData> theLinks = allLinks;
        if (filtered) theLinks = filteredLinks;
        LinkData theData = theLinks.get(choice);
        EditDialog editor = new EditDialog(this,"Edit "+theData.name,true,theData);
        editor.setupDialog();
        if (editor.isOkay()) {
            theData.name = editor.getNewName();
            theData.comment = editor.getComment();
            setLinkInfo(theData);
            DefaultListModel<String> listModel = listModelAll;
            if (filtered) listModel = listModelFiltered;
            listModel.setElementAt(theData.name, choice);
        }
    }
    
    private void selectFilters() {
        FilterSelectDialog dial = new FilterSelectDialog(this, "Select Filter", true,existingFilters);
        int sel = dial.getSelection();
        if (sel < 0) return;
        activeFilter = existingFilters.get(sel);
        setFilter();
    }
    
    private void setFilter() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        filteredLinks.clear();
        listModelFiltered.removeAllElements();
        for (LinkData link : allLinks) {
            if (activeFilter.linkIsFiltered(link)) {
                filteredLinks.add(link);
                listModelFiltered.addElement(link.name);
            }
        }
        if (filtered) labelLinkNumber.setText("Links : "+filteredLinks.size());
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void createFilter() {
        FilterCreateDialog create = new FilterCreateDialog(this, "Create Filter", true);
        int sel = create.getSelection();
        if (sel < 0) return;
        if (sel == 0) createCompFilter();
        if (sel == 1) createKhovFilter();
        if (sel == 2) createSInvFilter();
        if (sel == 3) createAndFilter();
        if (sel == 4) createOrFilter();
        if (sel == 5) createNotFilter();
    }
    
    private void createCompFilter() {
        CompFilterDialog dial = new CompFilterDialog(this, "Component Filter", true);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        boolean bddAb = dial.getBoundedAbove();
        int lb = dial.getLowerBound();
        int ub = dial.getUpperBound();
        String name = "-Component Links";
        if (bddAb) {
            name = String.valueOf(ub)+name;
            if (lb < ub) name = String.valueOf(lb)+"-"+name;
        }
        else {
            name = ">="+lb+name;
        }
        existingFilters.add(new CompFilter(name,bddAb,lb,ub));
    }
    
    private void createKhovFilter() {
        ArrayList<String> opts = new ArrayList<String>(3);
        opts.add("Betti Number Filter");
        opts.add("Torsion Filter");
        opts.add("Width Filter");
        opts.add("Comparison Filter");
        FilterTypeDialog dial = new FilterTypeDialog(this, "Khovanov Filter", true, opts);
        int typ = dial.getFilterType();
        if (typ == 0) createKhBettiFilter();
        if (typ == 1) createKhTorsionFilter();
        if (typ == 2) createKhWidthFilter();
        if (typ == 3) createKhComparFilter();
    }
    
    private void createKhBettiFilter() {
        BettiFilterDialog dial = new BettiFilterDialog(this, "Khovanov Betti Number Filter", true, 0, 65536, 0);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        String name = dial.getHomString()+" Betti";
        existingFilters.add(new KhovFilter(getKhName(dial, name), options.getPrimes(), dial.isOdd(), dial.isReduced(),
                dial.getBoundedAbove(), dial.getLowerBound(), dial.getUpperBound(), dial.getHom()));
    }
    
    private void createKhTorsionFilter() {
        CompFilterDialog dial = new CompFilterDialog(this, "Khovanov Torsion Filter", true, 2, 65536);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        existingFilters.add(new KhovFilter(getKhName(dial,"Tor"), dial.isOdd(), dial.isReduced(),dial.getBoundedAbove(), 
                dial.getLowerBound(), dial.getUpperBound(), options.getPrimes()));
    }
    
    private void createKhWidthFilter() {
        CompFilterDialog dial = new CompFilterDialog(this, "Khovanov Width Filter", true, 1, 15);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        existingFilters.add(new KhovFilter(getKhName(dial,"Width"),options.getPrimes(), dial.isOdd(), dial.isReduced(),
                dial.getBoundedAbove(), dial.getLowerBound(), dial.getUpperBound()));
    }
    
    private String getKhName(CompFilterDialog dial, String add) {
        boolean bab = dial.getBoundedAbove();
        int lb = dial.getLowerBound();
        int ub = dial.getUpperBound();
        boolean red = dial.isReduced();
        boolean odd = dial.isOdd();
        String name = String.valueOf(lb)+"<="+add;
        if (bab && lb == ub) name = String.valueOf(lb)+"-"+add;
        if (bab && lb < ub) name = name+"<="+ub;
        String extra = " unred Kh";
        if (red) extra = " red Kh";
        if (odd) extra = " OKh";
        return name + extra;
    }
    
    private void createKhComparFilter() {
        if (allLinks.isEmpty()) return;
        MirrorLink dial = new MirrorLink(this, "Same Khovanov Cohomology as", true, true, listModelAll);
        dial.setUpStuff();
        int chosen = dial.getChosen();
        if (chosen < 0) return;
        boolean rat = dial.isRational();
        boolean red = dial.isReduced();
        boolean odd = dial.isOdd();
        String name = getSameNameAs(chosen, odd, rat, red);
        existingFilters.add(new KhovFilter(name, odd, rat, red, allLinks.get(chosen),options.getPrimes()));
    }
    
    private String getSameNameAs(int chosen, boolean odd, boolean rat, boolean red) {
        String name = "Same ";
        if (rat) name = name+"rat ";
        if (odd) name = name+"odd ";
        else {
            if (red) name = name+"red ";
            else name = name+"unred ";
        }
        name = name +"Kh as "+allLinks.get(chosen).name;
        return name;
    }
    
    private void createSInvFilter() {
        SFilterDialog dial = new SFilterDialog(this, "S-Invariant Filter", true, -1000, 1000, false);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        String name = sInvName(dial);
        existingFilters.add(new SInvFilter(name, false,  false, true, dial.getBoundedBelow(), dial.getBoundedAbove(), 
                dial.getLowerBound(), dial.getUpperBound()));
    }
    
    private String sInvName(SFilterDialog dial) {
        boolean bb = dial.getBoundedBelow();
        boolean ba = dial.getBoundedAbove();
        int lb = dial.getLowerBound();
        int ub = dial.getUpperBound();
        String name = "s-Inv";
        if (bb) name = String.valueOf(lb)+"<="+name;
        if (ba) name = name+"<="+String.valueOf(ub);
        return name;
    }
    
    private void createAndFilter() {
        FilterAndOrDialog diag = new FilterAndOrDialog(this, "Logical AND Filter", true, true, existingFilters);
        ArrayList<Filter> andFilters = diag.getFilters();
        if (andFilters != null) {
            String name = getAndOrName(andFilters,true);
            AndFilter andFilter = new AndFilter(name,andFilters);
            existingFilters.add(andFilter);
        }
    }
    
    private void createOrFilter() {
        FilterAndOrDialog diag = new FilterAndOrDialog(this, "Logical OR Filter", true, false, existingFilters);
        ArrayList<Filter> orFilters = diag.getFilters();
        if (orFilters != null) {
            String name = getAndOrName(orFilters,false);
            OrFilter orFilter = new OrFilter(name,orFilters);
            existingFilters.add(orFilter);
        }
    }
    
    private String getAndOrName(ArrayList<Filter> filters, boolean and) {
        String extra = "OR";
        if (and) extra = "AND";
        String name = "("+filters.get(0).getName()+")";
        for (int i = 1; i < filters.size(); i++) name = name + extra + "("+filters.get(i).getName()+")";
        return name;
    }
    
    /*private String clip(String name) {
        String clipped = name;
        if (clipped.length() > 20) clipped = clipped.substring(0,20);
        return clipped;
    }// */
    
    private void createNotFilter() {
        FilterSelectDialog diag = new FilterSelectDialog(this, "Logical NOT Filter", true,existingFilters);
        int sel = diag.getSelection();
        if (sel < 0) return;
        Filter filt = existingFilters.get(sel);
        String name = "NOT("+filt.getName()+")";
        existingFilters.add(new NotFilter(name,filt));
    }
    
    public Comparer getComparer() {
        return comparer;
    }
    
    private void sortLinks() {
        SortDialog sorter = new SortDialog(this, "Sort Links by", true);
        int sort = sorter.getSelected();
        if (sort < 0) return;
        comparer.setType(sort);
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Collections.sort(filteredLinks);
        listModelFiltered.removeAllElements();
        for (LinkData link : filteredLinks) listModelFiltered.addElement(link.name);
        Collections.sort(allLinks);
        listModelAll.removeAllElements();
        for (LinkData link : allLinks) listModelAll.addElement(link.name);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setKnotInfoEmpty();
    }
    
    private void editFilter() {
        FilterSelectDialog diag = new FilterSelectDialog(this, "Choose Filter to Edit", true,existingFilters);
        int sel = diag.getSelection();
        if (sel < 0) return;
        EditDialog dial = new EditDialog(this, "Edit Name", true, existingFilters.get(sel));
        dial.setupDialog();
        if (dial.isOkay()) existingFilters.get(sel).setName(dial.getNewName());
    }
    
    private void removeFilter() {
        FilterSelectDialog diag = new FilterSelectDialog(this, "Choose Filter to Remove", true,existingFilters);
        int sel = diag.getSelection();
        if (sel < 0) return;
        if (JOptionPane.showConfirmDialog(null, "Are you sure?", "Remove selected filter", JOptionPane.YES_NO_OPTION) 
                        == JOptionPane.OK_OPTION) {
            existingFilters.remove(sel);
        }
    }
    
}
