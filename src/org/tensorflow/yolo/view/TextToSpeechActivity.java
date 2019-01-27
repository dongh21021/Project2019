package org.tensorflow.yolo.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.vr.sdk.audio.GvrAudioEngine;

import org.tensorflow.yolo.Config;
import org.tensorflow.yolo.FirstActivity;
import org.tensorflow.yolo.model.BoxPosition;
import org.tensorflow.yolo.model.Recognition;
import org.tensorflow.yolo.view.CameraActivity;
import java.util.List;
import java.util.Locale;

import static org.tensorflow.yolo.Config.LOGGING_TAG;

/**
 * Created by Zoltan Szabo on 4/25/18.
 */

public abstract class TextToSpeechActivity extends CameraActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private float resultsViewHeight=336;
    private String lastRecognizedClass = "";
    private int i=0;
    private int j=0;
    private float sum = 0;
    /////AudioEngine
    private GvrAudioEngine gvrAudioEngine;
    private volatile int sourceId = GvrAudioEngine.INVALID_ID;
    private volatile int successSourceId = GvrAudioEngine.INVALID_ID;
    private static final String OBJECT_SOUND_FILE = "ping_mono.wav";
    private static final String SUCCESS_SOUND_FILE = "success.wav";
    private float[] modelPosition;
    private float[] headRotation;
    private Thread alarm;

    private int lock=-1;
    private MusicIntentReceiver myReceiver;
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.KOREA);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(LOGGING_TAG, "Text to speech error: This Language is not supported");
            }
        } else {
            Log.e(LOGGING_TAG, "Text to speech: Initilization Failed!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textToSpeech = new TextToSpeech(this, this);

        modelPosition = new float[] {0.0f,0.0f,1.0f};
        gvrAudioEngine = new GvrAudioEngine(this,GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        headRotation = new float[4];
        gvrAudioEngine.setHeadPosition(-0,-0,-0);
        myReceiver = new MusicIntentReceiver();
    }

    protected void speak(List<Recognition> results) {
        if(jong){
            if(!(results.isEmpty())){
                i++;
                if(i==2) {
                    RectF box = reCalcSize(results.get(0).getLocation());
                    lastRecognizedClass = results.get(0).getTitle();
                    double stride = 0.4;
                    String dis = "";
                    if (box.centerX() < 500 && (270 <= angle && angle <= 360)) {
                        // 화면상 왼쪽, 실제로 9시~11시방향
                        textToSpeech.speak("횡단보도의 길이는 " + c_length + "m 대략" + (int) (c_length / stride) + "걸음이며 " + CameraActivity.direction, TextToSpeech.QUEUE_ADD, null, null);
                        i=0;
                    } else if (box.centerX() > 600 && (30 <= angle && angle <= 120)) {
                        // 화면상 오른쪽, 실제로 1시~3시방향
                        textToSpeech.speak("횡단보도의 길이는 " + c_length + "m 대략" + c_length / stride + "걸음이며 " + CameraActivity.direction, TextToSpeech.QUEUE_ADD, null, null);
                        i=0;
                    } else if ((500 <= box.centerX() && box.centerX() <= 600) && (0 <= angle && angle <= 60) && (330 < angle && angle <= 360)) {
                        // 화면상 중앙, 실제로 11시~1시방향
                        textToSpeech.speak("횡단보도의 길이는 " + c_length + "m 대략" + c_length / stride + "걸음이며 " + CameraActivity.direction, TextToSpeech.QUEUE_ADD, null, null);
                        i=0;
                    } else {
                        if (box.centerX() < 500) {
                            textToSpeech.speak("횡단보도" + " 왼쪽 " + CameraActivity.direction, TextToSpeech.QUEUE_ADD, null, null);
                            i=0;
                        } else if (box.centerX() > 600) {
                            textToSpeech.speak("횡단보도" + " 오른쪽" + CameraActivity.direction, TextToSpeech.QUEUE_ADD, null, null);
                            i=0;
                        } else {
                            textToSpeech.speak("횡단보도" + " 중앙" + CameraActivity.direction, TextToSpeech.QUEUE_ADD, null, null);
                            i=0;
                        }
                    }
                }
                else{
                    RectF box= reCalcSize(results.get(0).getLocation());
                    lastRecognizedClass = results.get(0).getTitle();
           }
            }
        }else {
            //jong 아닐때
            if(lock==1){
               // Log.d("tts","plugged");
                //Toast.makeText(this,"plugged",Toast.LENGTH_LONG).show();
                if (!(results.isEmpty())) {
                    RectF box = reCalcSize(results.get(0).getLocation());
                    lastRecognizedClass = results.get(0).getTitle();
                    if(lastRecognizedClass.equals("crosswalk")){
                        alarm = new Thread(new playSound());
                        alarm.start();

                        modelPosition[0] = (box.centerX() - 540) / 100.0f;
                        modelPosition[1] = 0.0f;
                        modelPosition[2] = 0.0f;
                    }else{}

                }
            }else {
                if (!(results.isEmpty())){
                    j++;
                    RectF box= reCalcSize(results.get(0).getLocation());
                    sum += box.centerX();
                    if(j==3){
                        //RectF box= reCalcSize(results.get(0).getLocation());
                        float average = sum/3.0f;
                        lastRecognizedClass = results.get(0).getTitle();
                        if(lastRecognizedClass.equals("crosswalk")){
                            if (average < 500) {
                                textToSpeech.speak("횡단보도" + " 왼쪽 " , TextToSpeech.QUEUE_ADD, null, null);
                                sum=0;
                                j=0;
                            } else if (box.centerX() > 600) {
                                textToSpeech.speak("횡단보도" + " 오른쪽" , TextToSpeech.QUEUE_ADD, null, null);
                                sum=0;
                                j=0;
                            } else {
                                textToSpeech.speak("횡단보도" + " 중앙", TextToSpeech.QUEUE_ADD, null, null);
                                sum=0;
                                j=0;
                            }
//                            if (box.centerX() < 500) {
//                                textToSpeech.speak("횡단보도" + " 왼쪽 " , TextToSpeech.QUEUE_ADD, null, null);
//                                j=0;
//                            } else if (box.centerX() > 600) {
//                                textToSpeech.speak("횡단보도" + " 오른쪽" , TextToSpeech.QUEUE_ADD, null, null);
//                                j=0;
//                            } else {
//                                textToSpeech.speak("횡단보도" + " 중앙", TextToSpeech.QUEUE_ADD, null, null);
//                                j=0;
//                            }
                        }
                        else{
                            textToSpeech.speak("더미",TextToSpeech.QUEUE_ADD,null,null);
                        }
                    }else{
                        box= reCalcSize(results.get(0).getLocation());
                        //RectF box= reCalcSize(results.get(0).getLocation());
                        lastRecognizedClass = results.get(0).getTitle();
                    }

                }
                //Log.d("tts","unplugged");
                //Toast.makeText(this,"unplugged",Toast.LENGTH_LONG).show();
            }

        }
    }

    class playSound implements Runnable{
        @Override
        public void run() {
            gvrAudioEngine.preloadSoundFile(OBJECT_SOUND_FILE);
            sourceId = gvrAudioEngine.createSoundObject(OBJECT_SOUND_FILE);
            gvrAudioEngine.setSoundObjectPosition(
                    sourceId, modelPosition[0], modelPosition[1], modelPosition[2]);
            //loop 없애줌
            gvrAudioEngine.playSound(sourceId, false /* looped playback */);
            // Preload an unspatialized sound to be played on a successful trigger on the cube.
            gvrAudioEngine.preloadSoundFile(SUCCESS_SOUND_FILE);
        }
    }

    private RectF reCalcSize(BoxPosition rect) {
        int height=1848;
        int width=1080;
        int padding = 5;
        float overlayViewHeight = height - resultsViewHeight;
        float sizeMultiplier = Math.min((float) width / (float) Config.INPUT_SIZE,
                overlayViewHeight / (float) Config.INPUT_SIZE);

        float offsetX = (width - Config.INPUT_SIZE * sizeMultiplier) / 2;
        float offsetY = (overlayViewHeight - Config.INPUT_SIZE * sizeMultiplier) / 2 + resultsViewHeight;

        float left = Math.max(padding,sizeMultiplier * rect.getLeft() + offsetX);
        float top = Math.max(offsetY + padding, sizeMultiplier * rect.getTop() + offsetY);

        float right = Math.min(rect.getRight() * sizeMultiplier, width - padding);
        float bottom = Math.min(rect.getBottom() * sizeMultiplier + offsetY, height - padding);
        return new RectF(left, top, right, bottom);
    }

    @Override
    public synchronized void onPause() {
        unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public synchronized void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
        super.onResume();
    }

    private class MusicIntentReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        FirstActivity.lock=0;
                        lock = 0;
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        FirstActivity.lock = 1;
                        lock = 1;
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }
//    public static void tutorial_speak(String s){
//        textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
//    }

}
