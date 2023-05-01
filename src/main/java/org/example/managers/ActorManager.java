package org.example.managers;


import org.example.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActorManager {
    private List<Actor> actors = new ArrayList<>();
    private List<Cross> crossList = new ArrayList<>();

    public List<Cross> getCrossList() {
        return crossList;
    }

    public void setCrossList(List<Cross> crossList) {
        this.crossList = crossList;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public void loadFromDB(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectQuery);
        while (rs.next()) {
            Actor actor = new Actor();
            actor.setId(rs.getInt("id"));
            actor.setName(rs.getString("name"));
            actor.setSurname(rs.getString("surname"));
            actors.add(actor);
        }
        rs = stmt.executeQuery(selectCrossQuery);
        while (rs.next()) {
            Cross cross = new Cross();
            cross.setId(rs.getInt("id"));
            cross.setFilm_id(rs.getInt("film_id"));
            cross.setActor(getActorByID(rs.getInt("artists_id")));
            crossList.add(cross);
        }
    }

    public ArrayList<Actor> getActorsByFilmID(int id) {
        ArrayList<Actor> actualActors = new ArrayList<>();
        for (Cross dat : crossList){
            if (dat.getFilm_id() == id && dat.getStatus() != DBBase.BaseStatus.deleted){
                actualActors.add(dat.getActor());
            }
        }
        return actualActors;
    }

    private Actor getActorByID(int id){
        for (Actor dat : actors){
            if (dat.getId() == id){
                return dat;
            }
        }
        return null;
    }

    public Actor getActorByName(String name, String surname){
        Actor act = actors.stream().filter(director -> director.getName().equals(name) && director.getSurname().equals(surname)).findFirst().orElse(null);
        return act;
    }

    public List<Integer> returnFilmIDsByActor(Actor actor){
        List<Integer> ids = new ArrayList<>();
        for(Cross dat : crossList){
            if (dat.getActor().equals(actor)){
                ids.add(dat.getFilm_id());
            }
        }
        return ids;
    }

    public Actor addActor(String name, String surname, Film film){
        Actor act = addOrCreateActor(name, surname);
        addCrossList(act, film.getId());
        return act;
    }

    private Actor addOrCreateActor(String name, String surname){
        Actor act = getActorByName(name, surname);
        if (act == null){
            act = new Actor();
            act.setName(name);
            act.setSurname(surname);
            act.setStatus(DBBase.BaseStatus.created);
            actors.add(act);
        }
        return act;
    }



    public Actor editActor(Actor oldActor, Film film, String name, String surname){
        Cross cross = findActorComb(oldActor, film);
        Actor newActor = addActor(name, surname, film);
        if (cross != null){
            cross.setActor(newActor);
            cross.setStatus(DBBase.BaseStatus.edited);
        } else {
            addCrossList(newActor, film.getId());
        }
        return newActor;
    }

    private Cross findActorComb(Actor act, Film film){
        for(Cross dat : crossList){
            if(dat.getActor() == act && film.getId() == dat.getFilm_id()){
               return dat;
            }
        }
        return null;
    }

    public void deleteActor(Actor actor, Film film){
        Cross cross = findActorComb(actor, film);
        if (cross != null) {
            cross.setStatus(DBBase.BaseStatus.deleted);
        }
    }

    private void addCrossList(Actor actor, int id){
        Cross cross = new Cross();
        cross.setFilm_id(id);
        cross.setActor(actor);
        cross.setStatus(DBBase.BaseStatus.created);
        crossList.add(cross);
    }

    public void saveToDB(Connection conn) throws SQLException {
        for(Actor dat : actors){
            if (dat.getStatus() == DBBase.BaseStatus.created){
                PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, dat.getName());
                stmt.setString(2, dat.getSurname());
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        dat.setId(generatedKeys.getInt(1));
                    }
                    else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
        }
        for (Cross cross : crossList){
            if (cross.getStatus() == DBBase.BaseStatus.deleted){
                PreparedStatement stmt = conn.prepareStatement(deleteCrossQuery);
                stmt.setInt(1, cross.getId());
                stmt.executeUpdate();
            } else if (cross.getStatus() == DBBase.BaseStatus.created) {
                PreparedStatement stmt = conn.prepareStatement(insertCrossQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, cross.getFilm_id());
                stmt.setInt(2, cross.getActor().getId());
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cross.setId(generatedKeys.getInt(1));
                    }
                    else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            } else if (cross.getStatus() == DBBase.BaseStatus.edited) {
                PreparedStatement stmt = conn.prepareStatement(editCrossQuery);
                stmt.setInt(1, cross.getActor().getId());
                stmt.setInt(2, cross.getId());
                stmt.executeUpdate();
            }
        }
    }

    public HashMap<Actor, Integer> filterActorsWithMultiFilms(){
        HashMap<Actor, Integer> actors = new HashMap<>();
        for(Cross dat : crossList){
            if(actors.containsKey(dat.getActor())){
                actors.put(dat.getActor(), actors.get(dat.getActor()) + 1);
            } else {
                actors.put(dat.getActor(), 1);
            }
        }
        return actors;
    }

    private static class Cross extends DBBase{
        private int film_id;
        private Actor actor;

        public int getFilm_id() {
            return film_id;
        }

        public void setFilm_id(int film_id) {
            this.film_id = film_id;
        }

        public Actor getActor() {
            return actor;
        }

        public void setActor(Actor actor) {
            this.actor = actor;
        }
    }

    private final String selectQuery = "SELECT * FROM artists";
    private final String selectCrossQuery = "SELECT * FROM crossArtists WHERE isDeleted = 0";
    private final String insertQuery = "INSERT INTO artists(name, surname) VALUES(?, ?)";
    //private final String editQuery = "UPDATE artists SET name = ?, surname = ? WHERE id = ?";
    private static final String insertCrossQuery = "INSERT INTO crossArtists(film_id, artist_id) VALUES (?, ?)";
    private static final String editCrossQuery = "UPDATE crossArtists SET artist_id = ? WHERE id = ?";
    private static final String deleteCrossQuery = "UPDATE crossArtists SET isDeleted = 1 WHERE id = ?";
}
