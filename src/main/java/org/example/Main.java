package org.example;

import org.example.managers.ActorManager;
import org.example.managers.DBConnection;
import org.example.managers.DirectorManager;
import org.example.managers.FilmManager;
import org.example.ui.Run;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception {
        DirectorManager dirmng = new DirectorManager();
        FilmManager filmng = new FilmManager();
        ActorManager actmng = new ActorManager();
        DBConnection.connect();
        try {
            dirmng.loadFromDB(DBConnection.getConn());
            actmng.loadFromDB(DBConnection.getConn());
            filmng.loadFromDB(DBConnection.getConn(), dirmng, actmng);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            DBConnection.disconnectConn();
        }

        Run run = new Run(filmng.getFilms(), filmng, actmng, dirmng);
        run.runApp();

        DBConnection.connect();
        try {
            dirmng.saveToDB(DBConnection.getConn());
            filmng.saveToDB(DBConnection.getConn());
            actmng.saveToDB(DBConnection.getConn());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            DBConnection.disconnectConn();
        }
    }
}