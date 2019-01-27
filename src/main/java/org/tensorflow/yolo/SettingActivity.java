package org.tensorflow.yolo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.tensorflow.yolo.view.CameraActivity;
import org.tensorflow.yolo.view.ClassifierActivity;

public class SettingActivity extends AppCompatActivity {
    public static Switch Switch,soundSwitch,gesSwitch;
    public static boolean backFirst = true;
    //record play
    final int REQUEST_PERMISSION_CODE = 1000;
    public static final String TAG = "RecordPlayActivity";
    public boolean first = true;
    public boolean exitState;
    //earphone
    public static int lock=-1;
    private MusicIntentReceiver myReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Log.d("aaa","setting create");
        SharedPreferences tuto = getSharedPreferences("Tutorial", Activity.MODE_PRIVATE);
        SharedPreferences sound = getSharedPreferences("Sound", Activity.MODE_PRIVATE);
        SharedPreferences gesture = getSharedPreferences("Gesture", Activity.MODE_PRIVATE);
        //Button button1 = (Button) findViewById(R.id.set_cam); //레이아웃에 button 이라는 id를 가진 애를 button1이라는 가상이름을 붙여주고 그 버튼을 찾는다.
        //Button button2 = (Button) findViewById(R.id.set_bus);
        //Button button3 = (Button) findViewById(R.id.set_map);
        Button back = (Button)findViewById(R.id.btn_back);
        Switch=(Switch)findViewById(R.id.switch1);
        soundSwitch=(Switch)findViewById(R.id.switch2);
        gesSwitch=(Switch)findViewById(R.id.ges_switch);
        SharedPreferences.Editor editor = tuto.edit();
        SharedPreferences.Editor s_editor = sound.edit();
        SharedPreferences.Editor g_editor = gesture.edit();

        //record play setting
        first = false;
        exitState = true;

        //request permission
        if (!checkPermissionFromDevice()) {
            requestRecordPlayPermission();
        }
        //recPlay = (Button) findViewById(R.id.btn_busRecord);
        myReceiver = new MusicIntentReceiver();

        SharedPreferences spref = getSharedPreferences("isFirsts", Activity.MODE_PRIVATE);
        SharedPreferences.Editor seditor = spref.edit();

//        SharedPreferences soundPref = getSharedPreferences("isFirstSound", Activity.MODE_PRIVATE);
//        SharedPreferences.Editor soundEditor = soundPref.edit();
//
        if(!spref.getBoolean("isFirsts", false)){
            Log.d("Is first Time?", "first");
            editor.putInt("tutorial",1);
            editor.commit();
            back.setContentDescription(getResources().getString(R.string.Tback));
            //button1.setContentDescription(getResources().getString(R.string.Tcamera_button));
            //button2.setContentDescription(getResources().getString(R.string.Tbus_button));
            //button3.setContentDescription(getResources().getString(R.string.Tmap_button));
            gesSwitch.setContentDescription(getResources().getString(R.string.Tgesture_switch));
            soundSwitch.setContentDescription(getResources().getString(R.string.Tsound));
            Switch.setContentDescription(getResources().getString(R.string.Ttutorial));
        }
//        if(!soundPref.getBoolean("isFirstSound", false)){
//            Log.d("Is first?", "first");
//            s_editor.putInt("sound",1);
//            s_editor.commit();
//        }
        if(tuto.getInt("tutorial", 0)==1) {
            Switch.setChecked(true);
            back.setContentDescription(getResources().getString(R.string.Tback));
            //button1.setContentDescription(getResources().getString(R.string.Tcamera_button));
            //button2.setContentDescription(getResources().getString(R.string.Tbus_button));
            //button3.setContentDescription(getResources().getString(R.string.Tmap_button));
            gesSwitch.setContentDescription(getResources().getString(R.string.Tgesture_switch));
            soundSwitch.setContentDescription(getResources().getString(R.string.Tsound));
            Switch.setContentDescription(getResources().getString(R.string.Ttutorial));
        }
        else {
            Switch.setChecked(false);
            back.setContentDescription(getResources().getString(R.string.back));
            //button1.setContentDescription(getResources().getString(R.string.camera_button));
            //button2.setContentDescription(getResources().getString(R.string.bus_button));
            //button3.setContentDescription(getResources().getString(R.string.map_button));
            gesSwitch.setContentDescription(getResources().getString(R.string.gesture_switch));
            soundSwitch.setContentDescription(getResources().getString(R.string.sound));
            Switch.setContentDescription(getResources().getString(R.string.tutorial));
        }
        if(sound.getInt("sound", 0)==1) {
            soundSwitch.setChecked(true);

        }
        else {
            soundSwitch.setChecked(false);

        }
        if(gesture.getInt("gesture", 0)==1) {
            gesSwitch.setChecked(true);

        }
        else {
            gesSwitch.setChecked(false);

        }

        Intent gIntent = getIntent();
        String name = gIntent.getStringExtra("ACTIVITY");


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch(name){
                    case "first":
                        exitState=false;
                        onBackPressed();
//                        intent = new Intent(SettingActivity.this,FirstActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                        exitState = false;
//                        startActivity(intent);
                        break;
                    case "map":
                        intent = new Intent(SettingActivity.this,MapActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        exitState = false;
                        startActivity(intent);
                        break;
                    case "cam":
                        intent = new Intent(SettingActivity.this,ClassifierActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        exitState = false;
                        startActivity(intent);
                        break;
                    case "bus":
                        intent = new Intent(SettingActivity.this,BusActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        exitState = false;
                        startActivity(intent);
                        break;
                }
            }
        });

//        button1.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
//            @Override
//            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
//                Intent intent = new Intent(SettingActivity.this,ClassifierActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                exitState = false;
//                startActivity(intent);
//                //Toast.makeText(getApplicationContext(), "버튼이 눌러졌나 ?", Toast.LENGTH_SHORT).show();
//            }
//        });
//        button2.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
//            @Override
//            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
//                Intent intent = new Intent(SettingActivity.this,BusActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                exitState = false;
//                startActivity(intent);
//                //Toast.makeText(getApplicationContext(), "버튼이 눌러졌나 ?", Toast.LENGTH_SHORT).show();
//            }
//        });
//        button3.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
//            @Override
//            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
//                Intent intent = new Intent(SettingActivity.this,MapActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                exitState = false;
//                startActivity(intent);
//                //Toast.makeText(getApplicationContext(), "버튼이 눌러졌나 ?", Toast.LENGTH_SHORT).show();
//            }
//        });

        Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    Switch.setChecked(true);
                    Toast.makeText(SettingActivity.this, "Switch-ON", Toast.LENGTH_SHORT).show();
                    //스위치 on시 action
                    editor.putInt("tutorial",1);
                    backFirst=true;
                    back.setContentDescription(getResources().getString(R.string.Tback));
//                    button1.setContentDescription(getResources().getString(R.string.Tcamera_button));
//                    button2.setContentDescription(getResources().getString(R.string.Tbus_button));
//                    button3.setContentDescription(getResources().getString(R.string.Tmap_button));
                    gesSwitch.setContentDescription(getResources().getString(R.string.Tgesture_switch));
                    soundSwitch.setContentDescription(getResources().getString(R.string.Tsound));
                    Switch.setContentDescription(getResources().getString(R.string.Ttutorial));
                    editor.commit();
                } else {
                    Toast.makeText(SettingActivity.this, "Switch-OFF", Toast.LENGTH_SHORT).show();
                    //스위치 off시 action
                    backFirst=false;
                    seditor.putBoolean("isFirsts",true);
                    seditor.commit();
                    editor.putInt("tutorial",0);
                    back.setContentDescription(getResources().getString(R.string.back));
//                    button1.setContentDescription(getResources().getString(R.string.camera_button));
//                    button2.setContentDescription(getResources().getString(R.string.bus_button));
//                    button3.setContentDescription(getResources().getString(R.string.map_button));
                    gesSwitch.setContentDescription(getResources().getString(R.string.gesture_switch));
                    soundSwitch.setContentDescription(getResources().getString(R.string.sound));
                    Switch.setContentDescription(getResources().getString(R.string.tutorial));
                    editor.commit();
                }
            }
        });
        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    soundSwitch.setChecked(true);
                    Toast.makeText(SettingActivity.this, "Switch-ON", Toast.LENGTH_SHORT).show();
                    //스위치 on시 action
                    s_editor.putInt("sound",1);
                    s_editor.commit();

                    FirstActivity.playLock = false;
                    //
                    FirstActivity.init();
                    FirstActivity.mIsRecording = true;
                    FirstActivity.flag=true;
                    FirstActivity.record = new Thread(new FirstActivity.recordSound());
                    FirstActivity.play = new Thread(new FirstActivity.playRecord());
                    FirstActivity.record.start();
                    FirstActivity.play.start();
                } else {
                    Toast.makeText(SettingActivity.this, "Switch-OFF", Toast.LENGTH_SHORT).show();
                    //스위치 off시 action
//                    soundEditor.putBoolean("isFirstSound",true);
//                    soundEditor.commit();
                    s_editor.putInt("sound",0);
                    s_editor.commit();
                    FirstActivity.playLock = true;
                    FirstActivity.flag = false;
                    //FirstActivity.mIsRecording = false;
                    FirstActivity.m_in_rec.stop();
                    FirstActivity.m_in_rec = null;
                    FirstActivity.m_out_trk.stop();
                    FirstActivity.m_out_trk  = null;
                    FirstActivity.init();
                }
            }
        });
        gesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    gesSwitch.setChecked(true);
                    Toast.makeText(SettingActivity.this, "Switch-ON", Toast.LENGTH_SHORT).show();
                    g_editor.putInt("gesture",1);
                    g_editor.commit();
                } else {
                    Toast.makeText(SettingActivity.this, "Switch-OFF", Toast.LENGTH_SHORT).show();
                    g_editor.putInt("gesture",0);
                    g_editor.commit();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d("aaa","setting resume");
        exitState = true;
        if(FirstActivity.checkRecplay()){
            if(FirstActivity.playLock){
                FirstActivity.playLock = false;
                //
                FirstActivity.init();
                FirstActivity.flag=true;
                FirstActivity.record = new Thread(new FirstActivity.recordSound());
                FirstActivity.play = new Thread(new FirstActivity.playRecord());
                FirstActivity.record.start();
                FirstActivity.play.start();
            }
        }else{

        }
        //earphone
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("aaa","setting pause");
        if(exitState){
            if(FirstActivity.mIsRecording){
                FirstActivity.playLock = true;
                FirstActivity.flag = false;
                FirstActivity.m_in_rec.stop();
                FirstActivity.m_in_rec = null;
                FirstActivity.m_out_trk.stop();
                FirstActivity.m_out_trk  = null;
                FirstActivity.init();
            }
            else{

            }
        }else{
            Log.d("exit","go to other activity");
        }
        //earphone
        unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(backFirst){
            //on
            FirstActivity.button2.setContentDescription(getResources().getString(R.string.Tmap_button));
            FirstActivity.button1.setContentDescription(getResources().getString(R.string.Tcamera_button));
            FirstActivity.button4.setContentDescription(getResources().getString(R.string.Tsetting_button));
            FirstActivity.button3.setContentDescription(getResources().getString(R.string.Tbus_button));
        }else{
            //off
            FirstActivity.button2.setContentDescription(getResources().getString(R.string.map_button));
            FirstActivity.button1.setContentDescription(getResources().getString(R.string.camera_button));
            FirstActivity.button4.setContentDescription(getResources().getString(R.string.setting_button));
            FirstActivity.button3.setContentDescription(getResources().getString(R.string.bus_button));
        }
        super.onBackPressed();
    }
    private class MusicIntentReceiver extends BroadcastReceiver {
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
                        FirstActivity.lock=1;
                        lock = 1;
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }
    private boolean checkPermissionFromDevice(){
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result==PackageManager.PERMISSION_GRANTED;
    }
    private void requestRecordPlayPermission(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }
}


