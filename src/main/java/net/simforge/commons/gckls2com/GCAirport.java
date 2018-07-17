package net.simforge.commons.gckls2com;

public class GCAirport {
    private String icao;
    private String iata;
    private String city;
    private String name;
    private double lat;
    private double lon;
    private String timezone1;
    private String timezone2;

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTimezone1() {
        return timezone1;
    }

    public void setTimezone1(String timezone1) {
        this.timezone1 = timezone1;
    }

    public String getTimezone2() {
        return timezone2;
    }

    public void setTimezone2(String timezone2) {
        this.timezone2 = timezone2;
    }
}
