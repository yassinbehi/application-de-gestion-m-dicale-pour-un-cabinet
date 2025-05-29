package controller;

import vue.LoginView;
import vue.Accueil;
import model.UserModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {

    private LoginView view;
    private UserModel model;
    private static final String DOCTOR_PASSWORD = "medecin123";    // Mot de passe médecin
    private static final String SECRETARY_PASSWORD = "sec123";    // Mot de passe secrétaire

    public LoginController(LoginView view, UserModel model) {
        this.view = view;
        this.model = model;
        initializeListeners();
    }

    private void initializeListeners() {
        view.getLoginButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!view.isDoctorSelected() && !view.isSecretarySelected()) {
                    view.showErrorMessage("Il faut sélectionner un rôle.");
                    return;
                }

                String password = view.getPassword();
                String role = view.isDoctorSelected() ? "doctor" : "secretary";

                if (password.isEmpty()) {
                    view.showErrorMessage("Veuillez entrer un mot de passe.");
                    return;
                }

                boolean isValid = false;
                if (role.equals("doctor") && password.equals(DOCTOR_PASSWORD)) {
                    isValid = true;
                } else if (role.equals("secretary") && password.equals(SECRETARY_PASSWORD)) {
                    isValid = true;
                }

                if (isValid) {
                    view.dispose();
                    Accueil acMedcin = new Accueil(role); // Passer le rôle à Accueil
                    acMedcin.setVisible(true);
                } else {
                    view.showErrorMessage("Mot de passe incorrect pour ce rôle.");
                }
            }
        });
    }
}