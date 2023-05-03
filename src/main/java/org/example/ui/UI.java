package org.example.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.managers.*;
import org.example.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class UI {
    private List<Film> films;

    public UI(List<Film> films){
        this.films = films;
    }

    public void printMenu(){
        Utils.print("");
        Utils.print("1. pridat | 2. upravit | 3. smazat | 4. vsechny filmy | 5. info o filmu | 6. pridani hodnoceni\n" +
                "7. filmy podle umelce | 8. umelci s vice filmy | 9. film do souboru | 10. film ze souboru\n" +
                "0. konec");
    }

    private void printFilm(Film film){
        String stat = film instanceof Played ? "Hvezdicek" : "Bodu";
        Utils.print("\n");
        Utils.print(film.getName());
        Utils.print("\tRok vydani: " + film.getYearOfRelease());
        Utils.print("\tReziser: " + film.getDirector().getName() + " " + film.getDirector().getSurname());
        List<Actor> actors = film.getActors();
        for (Actor act : actors){
            Utils.print(act.getName() + " " + act.getSurname());
        }
        List<Review> reviews = film.getReviews();
        if (film.getReviews() != null){
            for (Review rev : reviews) {
                Utils.print("\t" + stat + ": " + rev.getPoints());
                Utils.print("\tHodnoceni: " + rev.getReview());
            }
        }
    }
    public void printAllFilms(){
        if (films.size() != 0){
            for (Film dat : films) {
                printFilm(dat);
            }
        } else {
            Utils.print("Zadne filmy v databazi!");
        }
    }

    public void createFilm(DirectorManager dirmng, ActorManager actmng) throws Exception {
        Film film = null;
        Utils.print("Vyberte typ filmu: 1. animovany | 2. hrany");
        int in = Utils.intInput();
        if (in == 1){
            film = new Animated();
        } else if (in == 2) {
            film = new Played();
        } else {
            Utils.print("Neplatny vyber, opakujte akci!");
            createFilm(dirmng, actmng);
        }
        Utils.print("Nazev filmu:");
        film.setName(Utils.stringInput());
        Utils.print("Rok vydani filmu: ");
        film.setYearOfRelease(Utils.intInput());
        Utils.print("Jmeno rezisera: ");
        String[] names = Utils.stringInput().split(" ");
        film.setDirector(dirmng.addDirector(names[0], names[1]));
        Utils.print("Zadejte umelce (q pro ukonceni zadavani umelcu): ");
        boolean ter = false;
        while (!ter){
            Actor actor = new Actor();
            String name = Utils.stringInput();
            if(!name.equals("q")){
                names = name.split(" ");
                actor.setName(names[0]);
                actor.setSurname(names[1]);
                film.addActor(actmng.addActor(names[0],names[1],film));
            } else {
                ter = true;
            }
        }
        if (film instanceof Animated){
            Utils.print("Zadejte doporuceny vek: ");
            ((Animated) film).setRecommendedAge(Utils.intInput());
        }
        film.setStatus(DBBase.BaseStatus.created);
         films.add(film);
    }

    public void editFilm (DirectorManager dirmng, ActorManager actmng) throws Exception {
        Utils.print("Zadejte nazev filmu");
        Film film = returnFilmByName(Utils.stringInput());
        if (film == null){
            Utils.print("Film neni v databazi!");
            return;
        }
        printFilm(film);
        Played played = null;
        Animated animated = null;

        System.out.print("\nVyberte co chcete zmenit:\n" +
                "1. nazev filmu | " +
                "2. rok vydani | " +
                "3. rezisera | " +
                "4. herce");
        if (film instanceof Animated){Utils.print(" | 5. doporuceny vek");}
        Utils.print("6. konec uprav");
        boolean ter = false;
        while(!ter) {
            int choice = Utils.intInput();
            switch (choice){
                case 1:
                    Utils.print("Novy nazev filmu:");
                    film.setName(Utils.stringInput());
                    break;
                case 2:
                    Utils.print("Novy rok vydani:");
                    film.setYearOfRelease(Utils.intInput());
                    break;
                case 3:
                    Utils.print("Nove jmeno rezisera:");
                    String[] names = Utils.stringInput().split(" ");
                    film.setDirector(dirmng.addDirector(names[0], names[1]));
                    break;
                case 4:
                    Utils.print("Vyberte herce k uprave nebo smazani:");
                    List<Actor> actors = film.getActors();
                    for (int i = 0; i <= actors.size(); i++){
                        Utils.print((i+1) + ". " + actors.get(i).getName() + " " + actors.get(i).getSurname());
                    }
                    Utils.print("\nCo chcete s hercem udelat?\n" +
                            "1. pridat\n" +
                            "2. upravit\n" +
                            "3. odebrat");
                    int in = Utils.intInput();
                    switch (in){
                        case 1:
                            Utils.print("Zadejte jmeno herce:");
                            Actor actor = new Actor();
                            String[] name = Utils.stringInput().split(" ");
                            actor.setName(name[0]);
                            actor.setSurname(name[1]);
                            actors.add(actor);
                            break;
                        case 2:
                            Utils.print("Zadejte cislo herce k uprave:");
                            int idx = Utils.intInput() - 1;
                            Actor actorToChange = actors.get(idx);
                            Utils.print("Zadejte nove jmeno herce:");
                            String[] nm = Utils.stringInput().split(" ");
                            actmng.editActor(actorToChange, film, nm[0], nm[1]);
                            break;
                        case 3:
                            Utils.print("Zadejte cislo herce ke smazani:");
                            int index = Utils.intInput() - 1;
                            Actor actorToDelete = actors.get(index);
                            actmng.deleteActor(actorToDelete, film);
                            break;
                        default:
                            Utils.print("Neplatny vyber!");
                            break;
                    }
                case 5:
                    if (film instanceof Played){
                        Utils.print("Neplatny vyber!");
                        break;
                    } else {
                        animated = (Animated) film;
                        Utils.print("Zadejte novy doporuceny vek:");
                        animated.setRecommendedAge(Utils.intInput());
                    }
                    break;
                case 6:
                    ter = true;
                    break;
                default:
                    Utils.print("Neplatny vyber!");
                    break;
            }
        }
        film.setStatus(DBBase.BaseStatus.edited);
    }

    public void deleteFilm() throws Exception {
        Utils.print("Zadejte nazev filmu ke smazani:");
        String name = Utils.stringInput();
        for (Film dat : films){
            if (name.equals(dat.getName())){
                dat.setStatus(DBBase.BaseStatus.deleted);
                return;
            } else {
                Utils.print("Zadany film neni v databazi!");
            }
        }
    }

    public void addReview() throws Exception {
        Review review = null;
        Utils.print("Zadejte nazev filmu:");
        Film film = returnFilmByName(Utils.stringInput());
        if (film == null) {
            Utils.print("Film neni v databazi!");
        } else{
            if (film instanceof Played) {
                review = new PlayedReview();
            } else {
                review = new AnimatedReview();
            }
            Utils.print("Bodove hodnoceni:");
            review.setPoints(Utils.intInput());
            Utils.print("Textove hodnoceni:");
            review.setReview(Utils.stringInput());
            film.addReview(review);
        }
    }

    public void showFilmInfoByFilm(FilmManager filmng) throws Exception {
        Utils.print("Zadejte nazev filmu:");
        Film film = filmng.getFilmByName(Utils.stringInput());
        if (film == null){
            Utils.print("Film neni v databazi!");
        } else {
            printFilm(film);
        }
    }


    public void showFilmsByActor(FilmManager filmng, ActorManager actmng) throws Exception {
        Utils.print("Zadejte jmeno umelce:");
        String[] names = Utils.stringInput().split(" ");
        Actor actor = actmng.getActorByName(names[0], names[1]);
        printFilmsByActor(filmng, actmng, actor);
    }

    private void printFilmsByActor(FilmManager filmng, ActorManager actmng, Actor actor) {
        if(actor == null){
            Utils.print("Herec neni v databazi!");
        } else {
            List<Integer> ids = actmng.returnFilmIDsByActor(actor);
            if (ids.size() == 0){
                Utils.print("Umelec nema zadne filmy!");
            } else {
                for (Integer dat : ids) {
                    Film film = filmng.getFilmByID(dat);
                    if (film != null){
                        Utils.print(film.getName());
                    }
                }
            }
        }
    }

    public void showActorsWithMultipleFilms(FilmManager filmng, ActorManager actmng){
        HashMap<Actor, Integer> actors = actmng.filterActorsWithMultiFilms();
        for (Actor dat : actors.keySet()){
            if(actors.get(dat) > 1){
                Utils.print(dat.getName() + " " + dat.getSurname() + ":");
                printFilmsByActor(filmng, actmng, dat);
                Utils.print("\n");
            }
        }
    }

    private Film returnFilmByName(String name){
        for (Film dat : films){
            if (name.equals(dat.getName())){
                return dat;
            }
        }
        return null;
    }

    public void saveFilmToFile() throws Exception {
        Utils.print("Zadejte nazev filmu k ulozeni:");
        Film film = returnFilmByName(Utils.stringInput());
        if (film != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new FileOutputStream(film.getName() + ".json"), film);
        } else {
            Utils.print("Zadany film neni v databazi!");
        }
    }

    public void addFilmFromFile() throws Exception {
        Film film;
        Utils.print("Nazev filmu pro nahrati:");
        String name = Utils.stringInput();
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(name + ".json");
        if (file.exists()) {
            JsonNode node = objectMapper.readTree(file);
            JsonNode type = node.get("isAnimated");
            if (type != null && type.intValue() == 0) {
                film = objectMapper.readValue(file, Played.class);
            } else {
                film = objectMapper.readValue(file, Animated.class);
            }
            printFilm(film);
        } else {
            Utils.print("Soubor filmu neexistuje!");
        }
    }
}
