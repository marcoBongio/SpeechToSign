package com.theavengers.speechtosign;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.theavengers.speechtosign.Model.SliderItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;
    private ArrayList<String> data;
    private ArrayList<String> tokens;
    private List<String> stopwords;
    private SliderView sliderView;
    private CardView cardView;
    private SliderAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    public void loadStopwords() throws IOException {
        stopwords = new BufferedReader(new InputStreamReader(getAssets().open("stopwords.txt"),
                StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
    }

    public void renewItems(View view, ArrayList<String> tokens){
        List<SliderItem> sliderItemList = new ArrayList<>();
        //dummy data
        Iterator<String>tokens_iterator = tokens.iterator();
        while (tokens_iterator.hasNext()) {
            String token=tokens_iterator.next();
            try {
                if(token=="please") {
                    String token2=tokens_iterator.next();
                    if (token2 == "repeat") {
                        SliderItem sliderItem = new SliderItem();
                        sliderItem.setImageUrl("file:///android_asset/please_repeat.gif");
                        sliderItemList.add(sliderItem);
                    }
                    else {
                        if (Arrays.asList(getResources().getAssets().list("")).contains(token + ".gif")) {
                            SliderItem sliderItem = new SliderItem();
                            sliderItem.setImageUrl("file:///android_asset/" + token + ".gif");
                            sliderItemList.add(sliderItem);
                        }
                        if (Arrays.asList(getResources().getAssets().list("")).contains(token2 + ".gif")) {
                            SliderItem sliderItem = new SliderItem();
                            sliderItem.setImageUrl("file:///android_asset/" + token2 + ".gif");
                            sliderItemList.add(sliderItem);
                        }
                    }
                }
                else {
                    if (Arrays.asList(getResources().getAssets().list("")).contains(token + ".gif")) {
                        SliderItem sliderItem = new SliderItem();
                        sliderItem.setImageUrl("file:///android_asset/" + token + ".gif");
                        sliderItemList.add(sliderItem);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(sliderItemList.size() == 0){
            SliderItem sliderItem = new SliderItem();
            sliderItem.setImageUrl("file:///android_asset/_error.gif");
            sliderItemList.add(sliderItem);
        }
        adapter.renewItems(sliderItemList);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        cardView = findViewById(R.id.cW);
        sliderView = findViewById(R.id.imageSlider);

        adapter = new SliderAdapter(this);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();

        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        try {
            loadStopwords();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String sentence=data.get(0);
                editText.setText(sentence);
                //tokenization
                String[] words= sentence.toLowerCase().split(" ");
                tokens = new ArrayList<>();
                for (String word : words) {
                    if (!stopwords.contains(word)) {
                        tokens.add(word);
                    }
                }
                cardView.setVisibility(View.VISIBLE);
                renewItems(sliderView, tokens);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView tv;
        switch (item.getItemId()) {
            case R.id.menu_clear:
                editText.setText("");
                editText.setHint("Tap to Speak");
                cardView.setVisibility(View.INVISIBLE);
                tokens = new ArrayList<>();
                renewItems(sliderView, tokens);
                return true;
            case R.id.menu_about:
                Intent iA = new Intent(this, AboutActivity.class);
                startActivity(iA);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save tokens value
        outState.putStringArrayList("tokens", tokens);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore tokens value
        tokens = savedInstanceState.getStringArrayList("tokens");

        if(tokens != null) {
            cardView.setVisibility(View.VISIBLE);
            renewItems(sliderView, tokens);
        }
    }

}