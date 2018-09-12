package shay.example.com.openweatherretrofit2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import shay.example.com.openweatherretrofit2.Models.Model;

/**
 * Created by Shay de Barra 03/05/2018
 */

public interface WeatherClient {
    // example query being produced with Query params below
 //   @GET("weather?lat=53.349805&lon=-6.260310&appid=ab76296461f49ff0bbe06aef0933e929&units=metric")

    @GET("weather/")
    Call<Model> getWeatherData(@Query("lat") String query_lat,@Query("lon") String query_lon,@Query("APPID") String api_str,@Query("units") String units );

}
