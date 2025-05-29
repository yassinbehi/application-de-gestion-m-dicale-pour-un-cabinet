package connexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connexiondb {
    private static final String URL = "jdbc:mysql://localhost:3306/healthtruck";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    
    // Method to get a new database connection each time
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver (optional for newer versions of JDBC)
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Providing new connection: " + conn);
            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Erreur : Driver JDBC non trouvé.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données : " + e.getMessage(), e);
        }
    }

    // No need for closeConnection since try-with-resources handles it
}