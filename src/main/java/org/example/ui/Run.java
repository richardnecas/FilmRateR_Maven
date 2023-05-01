package org.example.ui;

import org.example.managers.ActorManager;
import org.example.managers.DirectorManager;
import org.example.managers.FilmManager;
import org.example.model.Film;

import java.util.List;
import java.util.Scanner;

public class Run {
    private UI ui;
    private FilmManager filmng;
    private ActorManager actmng;
    private DirectorManager dirmng;
    public Run(List<Film> films, FilmManager filmng, ActorManager actmng, DirectorManager dirmng){
        ui = new UI(films);
        this.filmng = filmng;
        this.actmng = actmng;
        this.dirmng = dirmng;
    }
    public void runApp() throws Exception {
        boolean ter = false;
        while (!ter){
            ui.printMenu();
            Utils.print("Vyberte co chcete delat:");
            switch(Utils.intInput()){
                case 1:
                    ui.createFilm();
                    break;
                case 2:
                    ui.editFilm(dirmng, actmng);
                    break;
                case 3:
                    ui.deleteFilm();
                    break;
                case 4:
                    ui.printAllFilms();
                    break;
                case 5:
                    ui.showFilmInfoByFilm(filmng);
                    break;
                case 6:
                    ui.addReview();
                    break;
                case 7:
                    ui.showFilmsByActor(filmng, actmng);
                    break;
                case 8:
                    ui.showActorsWithMultipleFilms(filmng, actmng);
                    break;
                case 9:
                    ui.saveFilmToFile();
                    break;
                case 10:
                    ui.addFilmFromFile();
                    break;
                case 0:
                    ter = true;
                    break;
                default:
                    Utils.print("Neplatny vyber!");
                    break;
            }
        }
    }
}
