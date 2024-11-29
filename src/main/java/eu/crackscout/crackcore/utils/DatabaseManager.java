/**
 * @author rzepk
 *
 *  Nov 29, 2024 5:46:47 AM
 */
package eu.crackscout.crackcore.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
	private Connection connection;

    /**
     * Initialisiert die Verbindung zur MariaDB-Datenbank.
     *
     * @param host     Die IP-Adresse oder der Hostname der Datenbank.
     * @param port     Der Port der Datenbank (Standard: 3306).
     * @param database Der Name der Datenbank.
     * @param username Der Benutzername für die Verbindung.
     * @param password Das Passwort für die Verbindung.
     * @throws SQLException    Falls ein Fehler beim Aufbau der Verbindung auftritt.
     * @throws ClassNotFoundException Falls der JDBC-Treiber nicht gefunden wird.
     */
    public void connect(String host, int port, String database, String username, String password) throws SQLException, ClassNotFoundException {
        // MariaDB/MySQL JDBC-Treiber laden
        Class.forName("org.mariadb.jdbc.Driver");

        // Verbindung zur Datenbank aufbauen
        String url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
        connection = DriverManager.getConnection(url, username, password);
        System.out.println("Verbindung zur Datenbank erfolgreich hergestellt!");
    }

    /**
     * Führt eine SELECT-Abfrage aus.
     *
     * @param query  Die SQL-Abfrage.
     * @param params Parameter für die Abfrage (falls benötigt).
     * @return Das ResultSet mit den Ergebnissen.
     * @throws SQLException Falls ein Fehler bei der Abfrage auftritt.
     */
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        setParameters(statement, params);
        return statement.executeQuery();
    }

    /**
     * Führt eine INSERT, UPDATE oder DELETE-Abfrage aus.
     *
     * @param query  Die SQL-Abfrage.
     * @param params Parameter für die Abfrage (falls benötigt).
     * @throws SQLException Falls ein Fehler bei der Abfrage auftritt.
     */
    public void executeUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            setParameters(statement, params);
            statement.executeUpdate();
        }
    }

    /**
     * Beendet die Verbindung zur Datenbank.
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Verbindung zur Datenbank geschlossen.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Setzt Parameter für eine vorbereitete Abfrage.
     *
     * @param statement Die vorbereitete SQL-Anweisung.
     * @param params    Die Parameter für die Abfrage.
     * @throws SQLException Falls ein Fehler beim Setzen der Parameter auftritt.
     */
    private void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
