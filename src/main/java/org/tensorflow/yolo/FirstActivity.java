package org.tensorflow.yolo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import org.tensorflow.yolo.view.CameraActivity;
import org.tensorflow.yolo.view.ClassifierActivity;

import java.util.LinkedList;

public class FirstActivity extends AppCompatActivity {
    public static final String TAG = "RecordPlayActivity";
    public static String direction;
    public static int m_in_buf_size;
    public static AudioRecord m_in_rec;
    public static byte[] m_in_bytes;
    public static LinkedList<byte[]> m_in_q;
    public static int m_out_buf_size;
    public static AudioTrack m_out_trk;
    public static byte[] m_out_bytes;
    public static Thread record;
    public static Thread play;
    public static boolean flag = true;
    public static Button button2,button1,button3,button4;
    public static boolean mIsRecording = false;
    public boolean first = true;
    public boolean exitState;                          //앱 밖으로 나감 - TRUE, ACTIVITY움직임- FALSE
    public static boolean playLock = false;

    //earphone
    public static int lock=-1;
    private MusicIntentReceiver myReceiver;
    final int REQUEST_PERMISSION_CODE = 1000;

    private static SharedPreferences recPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        //recplay
        first = false;
        
        myReceiver = new MusicIntentReceiver();

        final Activity activity = this;
        final Context context = this;
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final int PERMISSION_ALL = 1;
        final String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_PHONE_STATE
        };
        if(!hasPermissions(context, PERMISSIONS)){
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
       button1 = (Button) findViewById(R.id.cam_but); //레이아웃에 button 이라는 id를 가진 애를 button1이라는 가상이름을 붙여주고 그 버튼을 찾는다.button2 = (Button) findViewById(R.id.map_but);
       button3 = (Button) findViewById(R.id.bus_but);
       button4 = (Button) findViewById(R.id.sett_but);
        button2 = (Button) findViewById(R.id.map_but);
        SharedPreferences tuto =
                getSharedPreferences("Tutorial", Activity.MODE_PRIVATE);
        recPlay = getSharedPreferences("Sound", Activity.MODE_PRIVATE);
//
//
        SharedPreferences spref = getSharedPreferences("isFirsts", Activity.MODE_PRIVATE);
        SharedPreferences soundPref = getSharedPreferences("isFirstSound", Activity.MODE_PRIVATE);
        if(tuto.getInt("tutorial", 0)==1){
            button2.setContentDescription(getResources().getString(R.string.Tmap_button));
            button1.setContentDescription(getResources().getString(R.string.Tcamera_button));
            button4.setContentDescription(getResources().getString(R.string.Tsetting_button));
            button3.setContentDescription(getResources().getString(R.string.Tbus_button));
        }
        else if(spref.getBoolean("isFirsts", false)&&(tuto.getInt("tutorial", 0)==0)){
            button2.setContentDescription(getResources().getString(R.string.map_button));
            button1.setContentDescription(getResources().getString(R.string.camera_button));
            button4.setContentDescription(getResources().getString(R.string.setting_button));
            button3.setContentDescription(getResources().getString(R.string.bus_button));
        }

//        if(!spref.getBoolean("isFirsts", false)){
//            Log.d("Is first Time?", "first");
//            button2.setContentDescription(getResources().getString(R.string.Tmap_button));
//            button1.setContentDescription(getResources().getString(R.string.Tcamera_button));
//            button4.setContentDescription(getResources().getString(R.string.Tsetting_button));
//            button3.setContentDescription(getResources().getString(R.string.Tbus_button));
//        }else{
//            Log.d("Is first Time?", "not first");
//        }

        if(!soundPref.getBoolean("isFirstSound", false)){
            Log.d("Is first?", "first");

        }else{
            Log.d("Is first?", "not first");

        }
        button1.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
            @Override
            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
                Intent intent = new Intent(FirstActivity.this,ClassifierActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                exitState = false;
                startActivity(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
            @Override
            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
                Intent intent = new Intent(FirstActivity.this,MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                exitState = false;
                startActivity(intent);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
            @Override
            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
                Intent intent = new Intent(FirstActivity.this,BusActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                exitState = false;
                startActivity(intent);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
            @Override
            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
                Intent intent = new Intent(FirstActivity.this,SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("ACTIVITY","first");
                exitState = false;
                startActivity(intent);
            }
        });

        if(checkRecplay()){
            init();
            mIsRecording = true;
            flag = true;
            record = new Thread(new recordSound());
            play = new Thread(new playRecord());

            record.start();
            play.start();
        }else{

        }


    }

    @Override
    protected void onResume() {
        Log.d("aaa","enter first resume");
        //earphone
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
        exitState = true;

        //처음 first실행시켰는데 pause자동 실행 된 경우
        if(checkRecplay()){
            if(playLock){
                playLock = false;
                //
                mIsRecording =true;
                init();
                flag=true;
                record = new Thread(new recordSound());
                play = new Thread(new playRecord());
                record.start();
                play.start();
            }else{

            }
        }else{

        }
//        if(!mIsRecording){
//            if(checkRecplay()){
//                init();
//                mIsRecording = true;
//                flag = true;
//                record = new Thread(new recordSound());
//                play = new Thread(new playRecord());
//
//                record.start();
//                play.start();
//            }else{
//
//            }
//            //Toast.makeText(this,"Recording 안 하는 상태로 setting 갔다 온 것",Toast.LENGTH_LONG).show();
//        }else{
//            if(!checkRecplay()){
//                flag = false;
//                mIsRecording = false;
//                m_in_rec.stop();
//                m_in_rec = null;
//                m_out_trk.stop();
//                m_out_trk = null;
//                init();
//
//            }else{
//
//            }
//           // Toast.makeText(this,"Recording 하는 상태로 setting 갔다옴",Toast.LENGTH_LONG).show();
//        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("aaa","enter first pause");
        //earphone
        unregisterReceiver(myReceiver);

        if(exitState){
            if(mIsRecording){
                playLock = true;
                //
                flag=false;
                m_in_rec.stop();
                m_in_rec= null;
                m_out_trk.stop();
                m_out_trk = null;
                init();
            }

        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        playLock = true;
        super.onBackPressed();
    }

    public static void init()
    {
        m_in_buf_size = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, m_in_buf_size);

        m_in_bytes = new byte[m_in_buf_size];

        m_in_q = new LinkedList<byte[]>();


        m_out_buf_size = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        m_out_trk = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, m_out_buf_size,
                AudioTrack.MODE_STREAM);

        m_out_bytes = new byte[m_out_buf_size];
    }
    public static class recordSound implements Runnable
    {
        @Override
        public void run()
        {
            Log.i(TAG, "........recordSound run()......");
            byte[] bytes_pkg;

            m_in_rec.startRecording();

            while (flag)
            {
                m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
                bytes_pkg = m_in_bytes.clone();
                Log.i(TAG, "........recordSound bytes_pkg==" + bytes_pkg.length);
                if (m_in_q.size() >= 2)
                {
                    m_in_q.removeFirst();
                }
                m_in_q.add(bytes_pkg);
            }
        }

    }

    public static class playRecord implements Runnable
    {
        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            Log.i(TAG, "........playRecord run()......");
            byte[] bytes_pkg = null;

            m_out_trk.play();

            while (flag)
            {
                try
                {
                    m_out_bytes = m_in_q.getFirst();
                    bytes_pkg = m_out_bytes.clone();
                    if (lock == 1 && playLock != true) {
                        m_out_trk.write(bytes_pkg, 0, bytes_pkg.length);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    public static boolean checkRecplay(){

        if(recPlay.getInt("sound",0)==1){
            return true;
        }else{
            return false;
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission((Context) context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private class MusicIntentReceiver extends BroadcastReceiver{
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        //Toast.makeText(FirstActivity.this,"Unplugged",Toast.LENGTH_LONG).show();
                        lock=0;
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        //Toast.makeText(FirstActivity.this,"plugged",Toast.LENGTH_LONG).show();
                        lock=1;
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }
}
