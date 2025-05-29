package vue;

import model.GestionnairePatients;
import model.GestionnaireRendezVous;
import model.Patient;
import model.RendezVous;
import model.FicheMedicale;
import controller.ControleurPatients;
import controller.ControleurRendezVous;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class Accueil extends JFrame {
    private final GestionnaireRendezVous gestionRdv;
    private final GestionnairePatients gestionPatients;
    private JLabel labelNbPatients;
    private JLabel labelRdvAujourdhui;
    private JLabel labelRdvSemaine;
    private JLabel labelRdvMois;
    private JPanel accueilPanel;
    private VuePatients vuePatients;
    private ControleurPatients controleurPatients;
    private VueRendezVous vueRendezVous;
    private ControleurRendezVous controleurRendezVous;
    private String userRole; // Ajouté pour stocker le rôle

    public Accueil(String role) {
        this.userRole = role; // Recevoir le rôle depuis LoginController
        setTitle("Health Track - " + (role.equals("doctor") ? "Médecin" : "Secrétaire"));
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialisation des gestionnaires
        gestionRdv = new GestionnaireRendezVous();
        gestionPatients = new GestionnairePatients();

        // Initialisation des données de test
        //initialiserDonneesTest();

        // Layout principal
        setLayout(new BorderLayout());

        // Barre de navigation
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.NORTH);

        // Contenu avec CardLayout
        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);

        // Création du tableau de bord (Accueil)
        accueilPanel = createAccueilPanel();
        contentPanel.add(accueilPanel, "Accueil");

        // Initialisation des vues et contrôleurs avec isDoctor basé sur le rôle
        boolean isDoctor = role.equals("doctor");
        try {
            vueRendezVous = new VueRendezVous(gestionRdv, gestionPatients, this, null);
            controleurRendezVous = new ControleurRendezVous(vueRendezVous, gestionRdv, gestionPatients, this);
            vueRendezVous.controleur = controleurRendezVous;

            vuePatients = new VuePatients(gestionPatients, gestionRdv, this, null, isDoctor); // Passer isDoctor
            controleurPatients = new ControleurPatients(vuePatients, gestionPatients, gestionRdv, this);
            vuePatients.controleur = controleurPatients;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'initialisation: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ajouter les vues au CardLayout
        contentPanel.add(vueRendezVous.getPanel(), "Rendez-vous");
        contentPanel.add(vuePatients.getPanel(), "Patients");
        add(contentPanel, BorderLayout.CENTER);

        // Actions des boutons
        setupNavigationButtons(cardLayout, contentPanel);

        // Mettre à jour le tableau de bord au démarrage
        mettreAJourTableauDeBord();
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        navPanel.setBackground(new Color(245, 245, 245));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton homeButton = createNavButton("Accueil");
        JButton rdvButton = createNavButton("Rendez-vous");
        JButton patientsButton = createNavButton("Patients");


        navPanel.add(homeButton);
        navPanel.add(rdvButton);
        navPanel.add(patientsButton);
        
        return navPanel;
    }

    private void setupNavigationButtons(CardLayout cardLayout, JPanel contentPanel) {
        JButton homeButton = (JButton) ((JPanel) getContentPane().getComponent(0)).getComponent(0);
        JButton rdvButton = (JButton) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        JButton patientsButton = (JButton) ((JPanel) getContentPane().getComponent(0)).getComponent(2);

        homeButton.addActionListener(e -> {
            cardLayout.show(contentPanel, "Accueil");
            mettreAJourTableauDeBord();
        });
        rdvButton.addActionListener(e -> {
            cardLayout.show(contentPanel, "Rendez-vous");
            controleurRendezVous.mettreAJourVue();
        });
        patientsButton.addActionListener(e -> {
            cardLayout.show(contentPanel, "Patients");
            controleurPatients.mettreAJourVue();
        });
    }

    private class ShadowPanel extends JPanel {
        private final int shadowSize = 5;
        private final Color shadowColor = new Color(150, 150, 150, 50);

        public ShadowPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(shadowSize, shadowSize, shadowSize, shadowSize));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int width = getWidth() - shadowSize * 2;
            int height = getHeight() - shadowSize * 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < shadowSize; i++) {
                g2.setColor(new Color(150, 150, 150, 50 - (i * 10)));
                g2.drawRoundRect(i, i, width + (shadowSize - i) * 2 - 1, height + (shadowSize - i) * 2 - 1, 15, 15);
            }

            g2.setColor(new Color(240, 240, 240));
            g2.fillRoundRect(shadowSize, shadowSize, width, height, 15, 15);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel createAccueilPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(255, 255, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Bienvenue dans Health Track");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        Color titleColor = new Color(70, 130, 180);

        ShadowPanel panelPatients = new ShadowPanel();
        panelPatients.setLayout(new BorderLayout(0, 5));
        panelPatients.setPreferredSize(new Dimension(180, 100));
        JLabel labelPatientsTitle = new JLabel("Nombre total de patients", SwingConstants.CENTER);
        labelPatientsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelPatientsTitle.setForeground(titleColor);
        labelNbPatients = new JLabel("0", SwingConstants.CENTER);
        labelNbPatients.setFont(new Font("Segoe UI", Font.BOLD, 24));
        labelNbPatients.setForeground(Color.BLACK);
        panelPatients.add(labelPatientsTitle, BorderLayout.NORTH);
        panelPatients.add(labelNbPatients, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(panelPatients, gbc);

        ShadowPanel panelRdvAujourdhui = new ShadowPanel();
        panelRdvAujourdhui.setLayout(new BorderLayout(0, 5));
        panelRdvAujourdhui.setPreferredSize(new Dimension(180, 100));
        JLabel labelRdvAujourdhuiTitle = new JLabel("Rendez-vous aujourd'hui", SwingConstants.CENTER);
        labelRdvAujourdhuiTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelRdvAujourdhuiTitle.setForeground(titleColor);
        labelRdvAujourdhui = new JLabel("0", SwingConstants.CENTER);
        labelRdvAujourdhui.setFont(new Font("Segoe UI", Font.BOLD, 24));
        labelRdvAujourdhui.setForeground(Color.BLACK);
        panelRdvAujourdhui.add(labelRdvAujourdhuiTitle, BorderLayout.NORTH);
        panelRdvAujourdhui.add(labelRdvAujourdhui, BorderLayout.CENTER);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(panelRdvAujourdhui, gbc);

        ShadowPanel panelRdvSemaine = new ShadowPanel();
        panelRdvSemaine.setLayout(new BorderLayout(0, 5));
        panelRdvSemaine.setPreferredSize(new Dimension(180, 100));
        JLabel labelRdvSemaineTitle = new JLabel("Rendez-vous Cette Semaine", SwingConstants.CENTER);
        labelRdvSemaineTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelRdvSemaineTitle.setForeground(titleColor);
        labelRdvSemaine = new JLabel("0", SwingConstants.CENTER);
        labelRdvSemaine.setFont(new Font("Segoe UI", Font.BOLD, 24));
        labelRdvSemaine.setForeground(Color.BLACK);
        panelRdvSemaine.add(labelRdvSemaineTitle, BorderLayout.NORTH);
        panelRdvSemaine.add(labelRdvSemaine, BorderLayout.CENTER);
        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(panelRdvSemaine, gbc);

        ShadowPanel panelRdvMois = new ShadowPanel();
        panelRdvMois.setLayout(new BorderLayout(0, 5));
        panelRdvMois.setPreferredSize(new Dimension(180, 100));
        JLabel labelRdvMoisTitle = new JLabel("Rendez-vous Ce Mois", SwingConstants.CENTER);
        labelRdvMoisTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelRdvMoisTitle.setForeground(titleColor);
        labelRdvMois = new JLabel("0", SwingConstants.CENTER);
        labelRdvMois.setFont(new Font("Segoe UI", Font.BOLD, 24));
        labelRdvMois.setForeground(Color.BLACK);
        panelRdvMois.add(labelRdvMoisTitle, BorderLayout.NORTH);
        panelRdvMois.add(labelRdvMois, BorderLayout.CENTER);
        gbc.gridx = 3;
        gbc.gridy = 1;
        panel.add(panelRdvMois, gbc);

        return panel;
    }

    public void mettreAJourTableauDeBord() {
    try {
        // Mettre à jour le nombre total de patients
        labelNbPatients.setText(String.valueOf(gestionPatients.getTous().size()));

        // Utiliser la date actuelle (11 avril 2025)
        LocalDate today = LocalDate.of(2025, 4, 20);

        // Récupérer tous les rendez-vous une seule fois
        java.util.List<RendezVous> tousLesRdv = gestionRdv.getTous();

        // Compter les rendez-vous d'aujourd'hui
        long rdvAujourdhui = tousLesRdv.stream()
                .filter(rv -> rv.getDate().equals(today))
                .count();
        labelRdvAujourdhui.setText(String.valueOf(rdvAujourdhui));

        // Compter les rendez-vous de la semaine
        LocalDate startOfWeek = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        long rdvSemaine = tousLesRdv.stream()
                .filter(rv -> !rv.getDate().isBefore(startOfWeek) && !rv.getDate().isAfter(endOfWeek))
                .count();
        labelRdvSemaine.setText(String.valueOf(rdvSemaine));

        // Compter les rendez-vous du mois
        long rdvMois = tousLesRdv.stream()
                .filter(rv -> rv.getDate().getMonthValue() == today.getMonthValue() &&
                              rv.getDate().getYear() == today.getYear())
                .count();
        labelRdvMois.setText(String.valueOf(rdvMois));
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour du tableau de bord: " +
            e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 150, 200));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });

        return button;
    }

    // Getter pour permettre l'accès depuis LoginController
    public JPanel getContentPanel() {
        return (JPanel) getContentPane().getComponent(1);
    }

    // Getter pour VuePatients (utilisé par le contrôleur)
    public VuePatients getVuePatients() {
        return vuePatients;
    }

  
}