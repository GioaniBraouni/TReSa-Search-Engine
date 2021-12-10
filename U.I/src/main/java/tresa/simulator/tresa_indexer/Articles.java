package tresa.simulator.tresa_indexer;

public class Articles {
    private String title;
    private String places;
    private String people;
    private String body;

    public Articles(String title,String places,String people,String body) {
        this.title = title;
        this.places = places;
        this.people = people;
        this.body = body;
    }

    public Articles(String title, String places) {
        this.title = title;
        this.places = places;
    }

    public Articles(String title) {
        this.title = title;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
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
