package hu.bme.yjzygk.speech1.network;

import hu.bme.yjzygk.speech1.model.LuisResponseData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LuisApi {

    //@Headers("8451b52236be4972ac1b0aa4681f0f09") // subscriptionid

    @GET("/luis/v2.0/apps/b252355d-b777-45c0-8a10-7cad6a8d9357") // appid
    Call<LuisResponseData> getLuisResponse(
            @Query("subscription-key") String subscriptionKey,
            @Query("q") String utterance,
            @Query("timezoneOffset") String timezoneOffset,
            @Query("verbose") String verbose,
            @Query("spellCheck") String spellCheck,
            @Query("staging") String staging
    );
}
