package breakthroughPP.gui;

import breakthroughPP.Game;
import breakthroughPP.SplinterTheOmniscientRat;
import breakthroughPP.preset.Position;
import breakthroughPP.preset.PresetException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

public class MenuGUI extends JFrame implements WindowListener, ActionListener, DocumentListener {
    private final static String PLAYER = "Player";
    private final static String AI_RANDOM = "AI - Random";
    private final static String AI_EASY = "AI - Easy";
    private final static String AI_HARD = "AI - Hard";
    private final JComboBox<String> playerOneBox, playerTwoBox, rmiMode;
    private final DefaultIntField width, height, aiTurnDelay;
    private final JTextField rmiHost, rmiOne, rmiTwo;
    private final JLabel rmiOneLabel, rmiTwoLabel;
    private final JCheckBox rmiBox;
    private final JButton launchButton;
    private final MenuGUI thisgui;
    /**
     * Create a graphical menu for AGoS
     */
    public MenuGUI() {
        super("A Game of Stones");
        thisgui = this;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel main = new JPanel();
        main.setBorder(new EmptyBorder(4, 4, 4, 4));
        main.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel headerImg;
        URL AGOs = SplinterTheOmniscientRat.get("agos.png");
        if(AGOs != null) headerImg = new JLabel(new ImageIcon(AGOs));
        else headerImg = new JLabel();
        JPanel headerPanel = new JPanel();
        headerPanel.add(headerImg);
        main.add(headerPanel, gbc);

        //RMI PANEL
        JPanel rmiPanel = new JPanel();
        rmiPanel.setBorder(new EmptyBorder(4, 22, 4, 22));
        rmiBox = new JCheckBox("Network", false);
        rmiBox.addActionListener(this);
        rmiPanel.add(rmiBox);
        rmiHost = new JTextField("localhost:12345", 20);
        rmiHost.getDocument().addDocumentListener(this);
        rmiHost.setEnabled(false);
        rmiPanel.add(rmiHost);
        rmiMode = new JComboBox<String>();
        rmiMode.addItem("host Red");
        rmiMode.addItem("host Blue");
        rmiMode.setEnabled(false);
        rmiMode.addActionListener(this);
        rmiPanel.add(rmiMode);

        gbc.gridy++;
        JPanel playerOnePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerOneBox = new JComboBox<String>();
        playerOnePanel.add(comboBox(playerOneBox));
        rmiOneLabel = new JLabel("rmi://" + rmiHost.getText() + "/");
        //rmiOneLabel.setPreferredSize(new Dimension((int)rmiHost.getPreferredSize().getWidth(), (int)rmiOneLabel.getPreferredSize().getHeight()));
        rmiOneLabel.setForeground(Color.GRAY);
        playerOnePanel.add(rmiOneLabel);
        rmiOne = new JTextField("red", 7);
        rmiOne.setEnabled(false);
        playerOnePanel.add(rmiOne);
        main.add(playerOnePanel, gbc);

        gbc.gridy++;
        JPanel playerTwoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerTwoBox = new JComboBox<String>();
        playerTwoPanel.add(comboBox(playerTwoBox));
        rmiTwoLabel = new JLabel("rmi://" + rmiHost.getText() + "/");
        rmiTwoLabel.setForeground(Color.GRAY);
        //rmiTwoLabel.setPreferredSize(new Dimension((int)rmiHost.getPreferredSize().getWidth(), (int)rmiTwoLabel.getPreferredSize().getHeight()));
        rmiTwoLabel.setForeground(Color.GRAY);
        playerTwoPanel.add(rmiTwoLabel);
        rmiTwo = new JTextField("blue", 7);
        rmiTwo.setEnabled(false);
        playerTwoPanel.add(rmiTwo);
        main.add(playerTwoPanel, gbc);

        //RMIPANEL
        gbc.gridy++;
        main.add(rmiPanel, gbc);

        gbc.gridy++;
        JPanel sizePanel = new JPanel();
        int maxSize = Position.getAlphabet().length();
        sizePanel.add(new JLabel("Board size: "));
        width = new DefaultIntField(" Width ", 1, maxSize);
        sizePanel.add(width);
        sizePanel.add(new JLabel("x"));
        height = new DefaultIntField(" Height ", 1, maxSize);
        sizePanel.add(height);
        sizePanel.add(new JLabel(" (2x6 to 26x26)"));
        main.add(sizePanel, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JPanel launcherPanel = new JPanel();
        aiTurnDelay = new DefaultIntField(" Turn delay ms ", 0, 5000);
        launcherPanel.add(aiTurnDelay, gbc);
        launchButton = new JButton("Launch game");
        launchButton.addActionListener(this);
        launcherPanel.add(launchButton);
        main.add(launcherPanel, gbc);
        add(main);
    }

    /**
     * Action event listener for user interface items
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == rmiBox) {
            boolean enable = rmiBox.isSelected();
            rmiHost.setEnabled(enable);
            rmiOneLabel.setForeground(enable ? Color.BLACK : Color.GRAY);
            rmiTwoLabel.setForeground(enable ? Color.BLACK : Color.GRAY);
            rmiMode.setEnabled(enable);
            rmiOne.setEnabled(enable);
            rmiTwo.setEnabled(enable);
            updateNetFieldEnable();
        } else if (source == rmiMode) {
            updateNetFieldEnable();
        } else if (source == launchButton) {
            launchButton.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                Game game = null;
                boolean host = (!rmiBox.isSelected()) || rmiMode.getSelectedIndex() == 0;
                try {
                    game = new Game(true, width.getValue(), height.getValue(), comboBoxToPlayerConstant(playerOneBox), comboBoxToPlayerConstant(playerTwoBox),
                            rmiBox.isSelected() ? rmiHost.getText() : null, rmiOne.getText(), rmiTwo.getText(), host);
                    width.setText(String.valueOf(game.width));
                    height.setText(String.valueOf(game.height));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(thisgui, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    launchButton.setEnabled(true);
                    return;
                }
                if (host) {
                    game.setAiTurnDelay(aiTurnDelay.getValue());
                    final Game gamefinal = game;
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (gamefinal.play(-1) == Game.PLAY_ERROR)
                                    System.err.println("game.play encountered an error");
                                gamefinal.shutdown();
                            } catch (PresetException ex) {
                                System.err.println("game.play error: " + ex.getMessage());
                            }
                            launchButton.setEnabled(true);
                        }
                    }.start();
                } else launchButton.setEnabled(true);
            });
        }
    }

    /**
     * Adds player item Strings to the comboBox
     *
     * @param playerBox Combobox to extend
     */
    private JPanel comboBox(JComboBox<String> playerBox) {
        JPanel player = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerBox.addItem(PLAYER);
        playerBox.addItem(AI_RANDOM);
        playerBox.addItem(AI_EASY);
        playerBox.addItem(AI_HARD);
        player.add(playerBox);
        return player;
    }

    /**
     * Retrieve matching Game.PLAYER_* constant for selected comboBox item
     *
     * @param box Combobox to check
     */
    private int comboBoxToPlayerConstant(JComboBox<String> box) {
        switch (box.getSelectedItem().toString()) {
            case PLAYER:
                return Game.PLAYER_GRAPHIC;
            case AI_RANDOM:
                return Game.PLAYER_AIRANDOM;
            case AI_EASY:
                return Game.PLAYER_AIEASY;
            case AI_HARD:
                return Game.PLAYER_AIHARD;
            default:
                return Game.PLAYER_ISNOPLAYER;
        }
    }

    /**
     * Updates item status depending on selected network items
     */
    private void updateNetFieldEnable() {
        boolean enableAll = !rmiBox.isSelected();
        if (rmiMode.getSelectedIndex() == 0) {
            playerOneBox.setEnabled(true);
            playerTwoBox.setEnabled(enableAll);
            width.setEnabled(true);
            height.setEnabled(true);
            aiTurnDelay.setEnabled(true);
        } else {
            playerOneBox.setEnabled(enableAll);
            playerTwoBox.setEnabled(true);
            width.setEnabled(enableAll);
            height.setEnabled(enableAll);
            aiTurnDelay.setEnabled(enableAll);
        }
    }

    /**
     * Close window
     */
    public void windowClosing(WindowEvent e) {
        if (!launchButton.isEnabled())
            if (JOptionPane.showConfirmDialog(this, "There is still a game instance running.\nTerminate?",
                    "Close AGoS", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
                return;
        System.exit(0);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    //Event for host textfield change
    public void insertUpdate(DocumentEvent e) {
        updateLabels();
    }

    public void removeUpdate(DocumentEvent e) {
        updateLabels();
    }

    public void changedUpdate(DocumentEvent e) {
        updateLabels();
    }

    private void updateLabels() {
        rmiOneLabel.setText("rmi://" + rmiHost.getText() + "/");
        rmiTwoLabel.setText("rmi://" + rmiHost.getText() + "/");
    }
}

