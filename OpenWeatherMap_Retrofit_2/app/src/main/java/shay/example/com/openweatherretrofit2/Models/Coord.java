package shay.example.com.openweatherretrofit2.Models;

import com.google.gson.annotations.Expose;

/**
 * http://www.jsonschema2pojo.org/
 */
public class Coord {

    @Expose
    private Double lon;
    @Expose
    private Double lat;

    /**
     *
     * @return
     * The lon
     */
    public Double getLon() {
        return lon;
    }

    /**
     *
     * @param lon
     * The lon
     */
    public void setLon(Double lon) {
        this.lon = lon;
    }

    /**
     *
     * @return
     * The lat
     */
    public Double getLat() {
        return lat;
    }

    /**
     *
     * @param lat
     * The lat
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }
}


