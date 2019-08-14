package hu.bme.yjzygk.speech1.tts;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class TextToSpeechSpeaker extends UtteranceProgressListener implements TextToSpeech.OnInitListener  {

    private Context mContext;
    private String utteranceString;
    private TextToSpeech tts;
    public static final int ENGLISH = 0;
    public static final int HUNGARIAN = 1;
    public int LANGUAGE = 0;

    public TextToSpeechSpeaker(Context mContext, String utteranceString, int language) {
        this.mContext = mContext;
        this.utteranceString = utteranceString;
        this.LANGUAGE = language;
        this.tts = new TextToSpeech(mContext, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (LANGUAGE == HUNGARIAN) {
                    result = tts.setLanguage(Locale.forLanguageTag("hu"));
                } else {
                    result = tts.setLanguage(Locale.US);
                }
            } else {
                Toast.makeText(mContext, "Hungarian TTS not available", Toast.LENGTH_LONG).show();
                result = tts.setLanguage(Locale.US);
            }

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut(utteranceString);
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    @Override
    public void onStart(String utteranceId) {
    }

    @Override
    public void onDone(String utteranceId) {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onError(String utteranceId) {

    }

    public void speakOut(String what) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(what, TextToSpeech.QUEUE_ADD, null,"id1");
        }
    }
}
