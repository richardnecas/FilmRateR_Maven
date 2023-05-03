package org.example.managers;


import org.example.model.Animated;
import org.example.model.DBBase;
import org.example.model.Film;
import org.example.model.Played;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilmManager {
    private ReviewManager revmng = new ReviewManager();
    private List<Film> films = new ArrayList<>();

    public List<Film> getFilms() {
        return films;
    }

    public void setFilms(List<Film> films) {
        this.films = films;
    }

    public void loadFromDB(Connection conn, DirectorManager dirmng, ActorManager actmng) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectQuery);
        while (rs.next()) {
            Film film;
            if (rs.getInt("isAnimated") == 1) {
                Animated filmA = new Animated();
                filmA.setRecommendedAge(rs.getInt("recomendedAge"));
                film = filmA;
            } else{
                film = new Played();
            }
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("filmName"));
            film.setDirector(dirmng.getDirByID(rs.getInt("director_id")));
            film.setYearOfRelease(rs.getInt("yearOfRelease"));
            film.setActors(actmng.getActorsByFilmID(rs.getInt("id")));
            film.setReviews(revmng.loadReviewsByFilmID(conn,film));
            films.add(film);
        }
    }

    public void saveToDB(Connection conn) throws SQLException{
        for (Film dat : films){
            if (dat.getStatus() == DBBase.BaseStatus.deleted){
                PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                stmt.setInt(1, dat.getId());
                stmt.executeUpdate();
            } else if (dat.getStatus() == DBBase.BaseStatus.edited) {
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, dat.getName());
                stmt.setInt(2, dat.getDirector().getId());
                stmt.setInt(3, dat instanceof Animated ? 1 : 0);
                stmt.setInt(4, dat instanceof Animated ? ((Animated) dat).getRecommendedAge() : 0);
                stmt.setInt(5, dat.getYearOfRelease());
                stmt.setInt(6, dat.getId());
                stmt.executeUpdate();
            } else if (dat.getStatus() == DBBase.BaseStatus.created) {
                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, dat.getName());
                stmt.setInt(2, dat.getDirector().getId());
                stmt.setInt(3, dat instanceof Animated ? 1 : 0);
                stmt.setInt(4, dat instanceof Animated ? ((Animated) dat).getRecommendedAge() : 0);
                stmt.setInt(5, dat.getYearOfRelease());
                stmt.executeUpdate();
            }
            if (dat.getStatus() != DBBase.BaseStatus.deleted){
                revmng.reviewToDB(conn, dat.getReviews());
            }
        }
    }

    public Film getFilmByID(int id){
        for (Film dat : films){
            if (dat.getId() == id){
                return dat;
            }
        }
        return null;
    }

    public Film getFilmByName(String name){
        for (Film dat : films){
            if (name.equals(dat.getName())){
                return dat;
            }
        }
        return null;
    }

    private static final String selectQuery = "SELECT * FROM films WHERE isDeleted = 0 ORDER BY filmName ASC";
    private static final String insertQuery = "INSERT INTO films (filmName, director_id, isAnimated, recomendedAge, yearOfRelease) VALUES (?,?,?,?,?)";
    private static final String updateQuery = "UPDATE films SET filmName = ?, director_id = ?, isAnimated = ? recomendedAge = ? yearOfRelease = ? WHERE id = ?";
    private static final String deleteQuery = "UPDATE films SET isDeleted = 1 WHERE id = ?";
}
