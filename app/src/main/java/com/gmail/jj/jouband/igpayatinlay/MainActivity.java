package com.gmail.jj.jouband.igpayatinlay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Activity mActivity;
    private EditText inputBox;
    private TextToSpeech textToSpeech;
    private static final int TTS_REQUEST = 1;
    private long lastMicClickTime;
    private static long clickInterval = 1000;   // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        inputBox = findViewById(R.id.inputTextBox);
        ConstraintLayout masterContainer = findViewById(R.id.constraintLayout);
        setupUI(masterContainer);
        initTextToSpeech();
        initInputBox();
    }

    /**
     * Should only be called once on startup.
     * Initializes the TTS object.
     * Creates a toast informing user of failure in case of initialization failure.
     * Reference: https://javapapers.com/android/android-text-to-speech-tutorial/
     */
    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
//                    int ttsLang = textToSpeech.setLanguage(Locale.US);
//
//                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
//                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Log.e("TTS", "The language is not supported!");
//                    } else {
//                        Log.i("TTS", "Language Supported.");
//                    }
//                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(),
                            "TTS Initialization failed!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Should only be called once on startup.
     * Initialize all non EditText views with an onTouchListener that hides the keyboard.
     * Only works for our purposes as the UI only has one EditText view.
     * Credit: Navneeth G, StackOverflow
     * Reference: https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
     * @param view Parent container view
     */
    private void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.performClick();
                    hideSoftKeyboard(mActivity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    /**
     * Hides the keyboard.
     * Credit: Navneeth G, StackOverflow
     * Reference: https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
     * @param activity The current activity
     */
    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus == null) return;
        inputMethodManager.hideSoftInputFromWindow(
                currentFocus.getWindowToken(), 0);
    }

    /**
     * Should only be called once on startup.
     * Initializes a TextChangedListener for the input field.
     */
    private void initInputBox() {
        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            /**
             * Convert contents of input field to pig latin and display in output box.
             * @param s Contents of the input field
             */
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    setOutputButton("");
                } else {
                    setOutputButton(convertToPigLatin(s.toString()));
                }
            }
        });
    }

    /**
     * Sets the content of the output button to that of the given string.
     * @param input String to set contents of button to.
     */
    private void setOutputButton(String input) {
        Button outputButton = findViewById(R.id.buttonOutput);
        outputButton.setText(input);
    }

    /**
     * Method to execute when the output field is clicked.
     * Uses TextToSpeech to dictate button text.
     * Error message is logged on TextToSpeech error.
     * @param view Necessary argument for button click methods.
     */
    @SuppressWarnings("unused")
    public void outputButtonClick(View view) {
        Button outputButton = findViewById(R.id.buttonOutput);
        String outputString = outputButton.getText().toString();
        textToSpeech.speak(outputString, TextToSpeech.QUEUE_FLUSH, null);
//        int speechStatus = textToSpeech.speak(outputString, TextToSpeech.QUEUE_FLUSH, null);
//
//        if (speechStatus == TextToSpeech.ERROR) {
//            Log.e("TTS", "Error in converting Text to Speech!");
//        }
    }

    /**
     * Method to execute when mic button is clicked.
     * Start the SpeechRecognizer for speech to text recognition.
     * Voice recognition activity is started on success, else toast error message is printed.
     * Result from the RecognizerIntent is received from onActivityResult().
     * Reference: https://www.tutorialspoint.com/how-to-integrate-android-speech-to-text
     * @param view Necessary argument for button click methods.
     */
    @SuppressWarnings("unused")
    public void speakButtonClick(View view) {
        // Prevent double clicking mic button
        if (SystemClock.elapsedRealtime() - lastMicClickTime < clickInterval) {
            return;
        }
        lastMicClickTime = SystemClock.elapsedRealtime();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, TTS_REQUEST);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry your device is not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receive the text converted from speech from the SpeechRecognizer activity.
     * @param requestCode Identifies the request. Was passed to activity when started.
     * @param resultCode Specifies if operation was successful or not.
     * @param data Carries the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TTS_REQUEST) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result == null) return;
                inputBox.setText(result.get(0).toString());
            }
        }
    }

    /**
     * Convert a string to pig latin
     * If a word starts with a vowel, simply append "ay" to the word.
     * If a word starts with a consonant, move starting consonants to end of word and append "way".
     * @param input Input string that needs to be converted to pig latin
     * @return String containing the input converted to pig latin
     */
    private String convertToPigLatin(String input) {
        if (input.length() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String[] words = input.split(" ");
        for (String word : words) {
            if (word.length() > 0) {
                if (isAVowel(word.charAt(0))) {
                    result.append(word);
                    result.append("way ");
                } else {
                    result.append(moveConsonants(word));
                    result.append("ay ");
                }
            }
        }
        return result.toString();
    }

    /**
     * Check if a character is a vowel.
     * @param c Character to check.
     * @return True if c is a vowel, False otherwise.
     */
    private boolean isAVowel(char c) {
        return "aeiouy".indexOf(c) != -1;
    }

    /**
     * Moves first consonants to end of word.
     * If word starts with a vowel, does nothing.
     * @param word String containing a single word. No spaces.
     * @return String containing word with first consonants moved to the end.
     */
    private String moveConsonants(String word) {
        StringBuilder consonants = new StringBuilder();
        int i = 0;
        while (i < word.length() && !isAVowel(word.charAt(i))) {
            consonants.append(word.charAt(i));
            i++;
        }
        return word.substring(i) + consonants.toString();
    }

}
