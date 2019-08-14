package hu.bme.yjzygk.speech1.network;

import hu.bme.yjzygk.speech1.model.LuisResponseData;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {

    private static final String ENDPOINT_ADDRESS = "https://westus.api.cognitive.microsoft.com";
    //private static final String APP_ID = "bd53c3c5e0c17fa5f49543b1e5714a2c";
    private static final String SUBSCRIPTION_ID = "8451b52236be4972ac1b0aa4681f0f09";


    private static NetworkManager instance;

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    private Retrofit retrofit;
    private LuisApi luisApi;

    private NetworkManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl(ENDPOINT_ADDRESS)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        luisApi = retrofit.create(LuisApi.class);
    }

    public Call<LuisResponseData> getLuisResponse(String utterance) {
        return luisApi.getLuisResponse(SUBSCRIPTION_ID, utterance, "0", "true", "false", "false");
    }
}
