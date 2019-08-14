package hu.bme.yjzygk.speech1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.bme.yjzygk.speech1.location.GPSTracker;
import hu.bme.yjzygk.speech1.model.LuisEntity;
import hu.bme.yjzygk.speech1.model.LuisIntent;
import hu.bme.yjzygk.speech1.model.LuisResponseData;
import hu.bme.yjzygk.speech1.network.NetworkManager;
import hu.bme.yjzygk.speech1.tts.TextToSpeechSpeaker;
import hu.bme.yjzygk.speech1.ui.LuisResponseDataHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static hu.bme.yjzygk.speech1.tts.TextToSpeechSpeaker.HUNGARIAN;

public class MainActivity extends AppCompatActivity implements RecognitionListener, LuisResponseDataHolder {
    public static int LANGUAGE = TextToSpeechSpeaker.ENGLISH;
    public static boolean PLUS_INFO_NEEDED = true;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    private String recognizedSpeech;

    private TextView visualizerText;
    private ToggleButton languageSwitch;
    private Button startButton;
    private TextView luisResultTextView;

    private LuisResponseData luisResponseData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }

        visualizerText = findViewById(R.id.VisualizerText);
        languageSwitch = findViewById(R.id.LanguageSwitch);
        languageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LANGUAGE = isChecked ? TextToSpeechSpeaker.HUNGARIAN : TextToSpeechSpeaker.ENGLISH;
                if (PLUS_INFO_NEEDED) {
                    speakAppInfo(LANGUAGE);
                    speakAppInfoPlus(LANGUAGE);
                    PLUS_INFO_NEEDED = false;
                } else {
                    speakWaitingForInput(LANGUAGE);
                }
            }
        });
        startButton = findViewById(R.id.StartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deploySpeechRecognizer();
                start(startButton);
                /*GPSTracker gps = new GPSTracker(MainActivity.this);
                double coordinates[] = gps.getGPSCoordinates();
                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + coordinates[0] + "\nLong: " + coordinates[1], Toast.LENGTH_LONG).show();
                gps.stopUsingGPS();*/
            }
        });
        startButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                languageSwitch.setChecked(!languageSwitch.isChecked());
                return true;
            }
        });
        luisResultTextView = findViewById(R.id.luisResult);
        luisResultTextView.setText("You can say things like:\n" +
                                    "   \"Where am I?\" / \"Hol vagyok?\"\n" +
                                    "       or\n" +
                                    "   \"Where is the university?\" /\n" +
                                    "       \"Merre van az egyetem?\"");
        luisResultTextView.canScrollVertically(1);
        speakAppInfo(LANGUAGE);
        speakAppInfoPlus(LANGUAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void deploySpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, LANGUAGE == HUNGARIAN ? "hu-HU" : "en");
        if (LANGUAGE == HUNGARIAN)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hu-HU");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, LANGUAGE == HUNGARIAN ? RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH : RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        startButton.setText("Listening...");
    }

    @Override
    public void onBeginningOfSpeech() {
        startButton.setText("Go on...");
    }

    @Override
    public void onRmsChanged(float v) {
        visualizerText = findViewById(R.id.VisualizerText);
        visualizerText.setText(String.format("%.2f", v+2.12));
    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        startButton.setText("...");
    }

    @Override
    public void onError(int i) {
        String message;
        switch (i) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = getString(R.string.error_audio_error);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = getString(R.string.error_client);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = getString(R.string.error_permission);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = getString(R.string.error_network);
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = getString(R.string.error_timeout);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = getString(R.string.error_no_match);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = getString(R.string.error_busy);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = getString(R.string.error_server);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = getString(R.string.error_timeout);
                break;
            default:
                message = getString(R.string.error_understand);
                break;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        startButton.setText("Listen!");
    }

    @Override
    public void onResults(Bundle bundle) {
        startButton.setText("Listen!");
        ArrayList<String> matches = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        recognizedSpeech = matches.get(0);
        Toast.makeText(this, "Got it: " + matches.get(0), Toast.LENGTH_LONG).show();
        if(recognizedSpeech.length() != 0) {
            loadLuisResponseData();
        }

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    public void start(View view) {
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    public LuisResponseData getLuisResponseData() {
        return luisResponseData;
    }

    public void showLuisResponse() {
        LuisResponseData luisResponseData = this.getLuisResponseData(); // for future reference
        StringBuilder luisResultStringBuilder = new StringBuilder();
        if("Places.GetRoute".equals(luisResponseData.topScoringIntent.intent)) {
            //Uri gmmIntentUri = Uri.parse("google.navigation:q=Taronga+Zoo,+Sydney+Australia");

            /*int i;
            for(i = 0; i < luisResponseData.entities.size(); ++i) {
                if (luisResponseData.entities.get(i).entity.toLowerCase().equals("az"))
                    break;
            }
            if (i < luisResponseData.entities.size()) { luisResponseData.entities.remove(i); }*/

            Collections.sort(luisResponseData.entities);
            if (!luisResponseData.entities.isEmpty()) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + luisResponseData.entities.get(0).entity.replace(' ', '+') + "&mode=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        } else if ("LocateSelf".equals(luisResponseData.topScoringIntent.intent)) {
            GPSTracker gps = new GPSTracker(MainActivity.this);
            String address = gps.getReverseGeocodedAddress();
            gps.stopUsingGPS();
            new TextToSpeechSpeaker(this, address, LANGUAGE);

            luisResultStringBuilder.append("Address: \n" + address + "\n");
        } else {
            Toast.makeText(MainActivity.this, "Intent invalid or currently unavailable.", Toast.LENGTH_LONG).show();
            new TextToSpeechSpeaker(this, LANGUAGE==0?"Sorry, could you repeat that?":"Kérlek ismételd meg.", LANGUAGE);
        }

        luisResultStringBuilder.append(
                "---LUIS Results:---\n" +
                "Query: " + luisResponseData.query + "\n" +
                "TopIntent:\n" +
                "       intent: " + luisResponseData.topScoringIntent.intent + "\n" +
                "       score: " + luisResponseData.topScoringIntent.score + "\n");
        luisResultStringBuilder.append("Intents:\n");
        for(int i = 0; i < luisResponseData.intents.size(); ++i) {
            LuisIntent intent = luisResponseData.intents.get(i);
            luisResultStringBuilder.append(
                    "       Intent_" + i + ": " + "\n" +
                    "           intent: " + intent.intent + "\n" +
                    "           score: " + intent.score + "\n");
        }
        luisResultStringBuilder.append("Entities sorted by score:\n");
        for(int i = 0; i < luisResponseData.entities.size(); ++i) {
            LuisEntity entity = luisResponseData.entities.get(i);
            luisResultStringBuilder.append(
                    "       Entity_" + i + ": " + "\n" +
                    "           entity: " + entity.entity + "\n" +
                    "           type: " + entity.type + "\n" +
                    "           startIndex: " + entity.startIndex + "\n" +
                    "           endIndex: " + entity.endIndex + "\n" +
                    "           score: " + entity.score + "\n");
        }
        luisResultStringBuilder.append("--------------\n");
        String luisResultString = luisResultStringBuilder.toString();
        luisResultTextView.setText(luisResultString);

    }

    private void loadLuisResponseData() {
        NetworkManager.getInstance().getLuisResponse(recognizedSpeech).enqueue(new Callback<LuisResponseData>() {
            @Override
            public void onResponse(Call<LuisResponseData> call,
                                   Response<LuisResponseData> response) {
                //Log.d(TAG, "onResponse: " + response.code());
                if (response.isSuccessful()) {
                    displayLuisResponseData(response.body());
                } else {
                    Toast.makeText(MainActivity.this,
                            "Error: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LuisResponseData> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MainActivity.this,
                        "Error in network request, check LOG\n"+t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayLuisResponseData(LuisResponseData receivedLuisResponseData) {
        luisResponseData = receivedLuisResponseData;
        showLuisResponse();
    }

    private void speakAppInfo(int language) {
        if (language == 0) {
            new TextToSpeechSpeaker(this, getString(R.string.infoTextEng), 0);
        } else {
            new TextToSpeechSpeaker(this, getString(R.string.infoTextHu), 1);
        }
    }
    private void speakAppInfoPlus(int language) {
        if (language == 0) {
            new TextToSpeechSpeaker(this, getString(R.string.infoPlusTextEng), 0);
        } else {
            new TextToSpeechSpeaker(this, getString(R.string.infoPlusTextHu), 1);
        }
    }
    private void speakWaitingForInput(int language) {
        if (language == 0) {
            new TextToSpeechSpeaker(this, getString(R.string.waitInputEng), 0);
        } else {
            new TextToSpeechSpeaker(this, getString(R.string.waitInputHu), 1);
        }
    }

}
