package shay.example.com.openweatherretrofit2.Models;

import com.google.gson.annotations.Expose;

/**
 * http://www.jsonschema2pojo.org/
 */
public class Clouds {

    @Expose
    private Integer all;

    /**
     *
     * @return
     * The all
     */
    public Integer getAll() {
        return all;
    }

    /**
     *
     * @param all
     * The all
     */
    public void setAll(Integer all) {
        this.all = all;
    }
}
