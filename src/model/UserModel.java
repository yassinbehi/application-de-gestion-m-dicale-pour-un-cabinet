package model;

import java.util.HashMap;
import java.util.Map;

public class UserModel {

    private Map<String, String> users;

    public UserModel() {
        // Simule une base de données avec des utilisateurs
        users = new HashMap<>();
        users.put("doctor", "doc123"); // Rôle: doctor, Mot de passe: doc123
        users.put("secretary", "sec456"); // Rôle: secretary, Mot de passe: sec456
    }

    /**
     * Valide les informations de connexion.
     * @param role Le rôle de l'utilisateur ("doctor" ou "secretary")
     * @param password Le mot de passe entré
     * @return true si les informations sont correctes, false sinon
     */
    public boolean validateLogin(String role, String password) {
        if (users.containsKey(role)) {
            String storedPassword = users.get(role);
            return storedPassword.equals(password);
        }
        return false;
    }
}