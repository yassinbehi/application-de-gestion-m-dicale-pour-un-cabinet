package vue;

import controller.*;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoginView extends JFrame {

    private JPasswordField passwordField;
    private JButton loginButton;
    private JRadioButton doctorRadio;
    private JRadioButton secretaryRadio;

    public LoginView() {
        // Configuration de la fenêtre principale
        setTitle("Health Track - Login");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panneau principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Partie gauche (logo et nom)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(70, 130, 180));
        leftPanel.setPreferredSize(new Dimension(300, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel computerIcon = new JLabel("\uD83D\uDCBB");
        computerIcon.setFont(new Font("Arial", Font.PLAIN, 50));
        computerIcon.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(computerIcon, gbc);

        JLabel appNameLabel = new JLabel("HEALTHTRACK");
        appNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appNameLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        leftPanel.add(appNameLabel, gbc);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Partie droite (formulaire)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.insets = new Insets(10, 10, 10, 10);
        gbcRight.anchor = GridBagConstraints.CENTER;

        // Titre "LOGIN" avec icône
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Color.WHITE);

        JLabel userIcon;
        System.out.println(getClass().getResource("teamwork.png"));
        
        URL imageUrl = getClass().getResource("teamwork.png");
        if (imageUrl != null) {
            ImageIcon userIconImage = new ImageIcon(imageUrl);
            Image scaledImage = userIconImage.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            userIconImage = new ImageIcon(scaledImage);
            userIcon = new JLabel(userIconImage);
        } else {
            userIcon = new JLabel("\uD83D\uDC65");
            userIcon.setFont(new Font("Arial", Font.PLAIN, 30));
            System.err.println("Erreur : Impossible de charger l'image /vue.imgs/teamwork.png");
        }
        userIcon.setForeground(new Color(70, 130, 180));

        JLabel loginLabel = new JLabel("LOGIN");
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loginLabel.setForeground(Color.BLACK);

        titlePanel.add(userIcon);
        titlePanel.add(loginLabel);

        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        rightPanel.add(titlePanel, gbcRight);

        // Boutons radio pour "médecin" et "secrétaire"
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        radioPanel.setBackground(Color.WHITE);

        ButtonGroup roleGroup = new ButtonGroup();
        doctorRadio = new JRadioButton("médecin");
        doctorRadio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        doctorRadio.setBackground(Color.WHITE);

        secretaryRadio = new JRadioButton("secrétaire");
        secretaryRadio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        secretaryRadio.setBackground(Color.WHITE);

        roleGroup.add(doctorRadio);
        roleGroup.add(secretaryRadio);
        radioPanel.add(doctorRadio);
        radioPanel.add(secretaryRadio);

        gbcRight.gridy = 1;
        rightPanel.add(radioPanel, gbcRight);

        // Champ de mot de passe
        JLabel passwordLabel = new JLabel("Mot de passe :");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(139, 69, 19));
        gbcRight.gridy = 2;
        rightPanel.add(passwordLabel, gbcRight);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbcRight.gridy = 3;
        rightPanel.add(passwordField, gbcRight);

        // Bouton "login"
        loginButton = new JButton("login");
        loginButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(new Color(70, 130, 180));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        loginButton.setPreferredSize(new Dimension(100, 40));

        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(70, 130, 180));
                loginButton.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Color.WHITE);
                loginButton.setForeground(new Color(70, 130, 180));
            }
        });

        gbcRight.gridy = 4;
        rightPanel.add(loginButton, gbcRight);

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    // Getters pour le Controller
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public boolean isDoctorSelected() {
        return doctorRadio.isSelected();
    }

    public boolean isSecretarySelected() {
        return secretaryRadio.isSelected();
    }

    // Méthode pour afficher les messages d'erreur
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
             Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
             e.printStackTrace();
            }

            LoginView view = new LoginView();
            model.UserModel model = new model.UserModel();
            new LoginController(view, model);
            view.setVisible(true);
        });
    }
}