package tresa.simulator.tresa_indexer;

import javafx.scene.control.Button;

public class Articles {
    private Button title;
    private String places;
    private String people;
    private String body;
    private Float score;

    public Articles(Button title,String places,String people,String body) {
        this.title = title;
        this.places = places;
        this.people = people;
        this.body = body;
    }

    public Articles(Button title,String places,Float score) {
        this.title = title;
        this.places = places;
        this.score = score;
    }

    public Articles(Button title, String places) {
        this.title = title;
        this.places = places;
    }

    public Articles(Button title) {
        this.title = title;

    }


    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Button getTitle() {
        return title;
    }

    public void setTitle(Button title) {
        this.title = title;
    }

    public String getPlaces() {
        return places;
    }

    public void setPlaces(String places) {
        this.places = places;
    }

    public String getPeople() {
        return people;
    }

    public void setPeople(String people) {
        this.people = people;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
