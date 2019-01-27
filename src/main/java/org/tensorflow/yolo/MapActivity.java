package org.tensorflow.yolo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.*;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapData.ConvertGPSToAddressListenerCallback;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import android.content.DialogInterface;
import android.os.Handler;
import android.content.Intent;
import android.provider.Settings;

import org.tensorflow.yolo.view.CameraActivity;
import org.tensorflow.yolo.view.TextToSpeechActivity;
import org.tensorflow.yolo.view.ClassifierActivity;
import org.tensorflow.yolo.SettingActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;


public class MapActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback,GestureOverlayView.OnGesturePerformedListener{

    private static final int REQUEST_LOCATION = 1;
    final int REQUEST_PERMISSION_CODE = 1000;

    LocationManager locationManager;

    EditText callnum;

    LocationAddress locationAddress;
    String addressStr;
    private TMapData tmapdata = null;
    TMapGpsManager gps = null;
    TMapView tMapView = null;
    TextView textView = null;
    String address;
    AppLocationService appLocationService;
    Handler handler = new Handler();

    //capture variable
    private ImageView imageView;
    private LinearLayout rootContent;
    private Button capture;

    private LinearLayout linearLayout = null;
    //
    private GestureLibrary gestureLib;
    double latitude,longitude;
    ////////////////////////////////recordPlay/////////////////
    public static final String TAG = "RecordPlayActivity";
    public boolean exitState;
    public boolean first = true;
    //earphone
    public static int lock=-1;
    private MusicIntentReceiver myReceiver;
    SharedPreferences recPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("aaa","enter map create");
        super.onCreate(savedInstanceState);
        SharedPreferences gesture =
                getSharedPreferences("Gesture", Activity.MODE_PRIVATE);
        if(gesture.getInt("gesture", 0)==1) {
            GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
            View inflate = getLayoutInflater().inflate(R.layout.activity_map, null);
            gestureOverlayView.addView(inflate);
            gestureOverlayView.addOnGesturePerformedListener(this);
            gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
            if (!gestureLib.load())
                finish();
            setContentView(gestureOverlayView);
        }else{
            setContentView(R.layout.activity_map);
        }
        SharedPreferences tuto = getSharedPreferences("Tutorial", Activity.MODE_PRIVATE);

        Log.d("sss",Integer.toString((tuto.getInt("tutorial", 0))));

        //Button 선언
        Button sett = (Button) findViewById(R.id.map_set);
        Button map = (Button)findViewById(R.id.map_map);
        Button cam = (Button)findViewById(R.id.map_camera);
        Button bus = (Button)findViewById(R.id.map_bus);
        Button track = (Button)findViewById(R.id.btnTrack);
        recPlay = getSharedPreferences("Sound", Activity.MODE_PRIVATE);
        map.setEnabled(false);
        sett.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
            @Override
            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
                Intent intent = new Intent(MapActivity.this,SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("ACTIVITY","map");
                exitState = false;
                startActivity(intent);
                //Toast.makeText(getApplicationContext(), "버튼이 눌러졌나 ?", Toast.LENGTH_SHORT).show();
            }
        });
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this,ClassifierActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                exitState = false;
                startActivity(intent);
            }
        });
        bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this,BusActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                exitState = false;
                startActivity(intent);
            }
        });
        //record play setting
        first = false;
        exitState = true;
        //request permission
        myReceiver = new MusicIntentReceiver();
        capture = (Button)findViewById(R.id.btnWhere);
        SharedPreferences spref = getSharedPreferences("isFirsts", Activity.MODE_PRIVATE);
        SharedPreferences soundPref = getSharedPreferences("isFirstSound", Activity.MODE_PRIVATE);
        if(tuto.getInt("tutorial", 0)==1){
            capture.setContentDescription(getResources().getString(R.string.Tmessage_button));
            map.setContentDescription(getResources().getString(R.string.Tmap_button));
            cam.setContentDescription(getResources().getString(R.string.Tcamera_button));
            bus.setContentDescription(getResources().getString(R.string.Tbus_button));
            sett.setContentDescription(getResources().getString(R.string.Tsetting_button));
            track.setContentDescription(getResources().getString(R.string.Ttrack));
        }else{
            capture.setContentDescription(getResources().getString(R.string.message_button));
            map.setContentDescription(getResources().getString(R.string.map_button));
            cam.setContentDescription(getResources().getString(R.string.camera_button));
            bus.setContentDescription(getResources().getString(R.string.bus_button));
            sett.setContentDescription(getResources().getString(R.string.setting_button));
            track.setContentDescription(getResources().getString(R.string.track));
        }
        if(!spref.getBoolean("isFirsts", false)){
            capture.setContentDescription(getResources().getString(R.string.Tmessage_button));
            map.setContentDescription(getResources().getString(R.string.Tmap_button));
            cam.setContentDescription(getResources().getString(R.string.Tcamera_button));
            bus.setContentDescription(getResources().getString(R.string.Tbus_button));
            sett.setContentDescription(getResources().getString(R.string.Tsetting_button));
            track.setContentDescription(getResources().getString(R.string.Ttrack));
        }
        if(!soundPref.getBoolean("isFirstSound", false)){
            Log.d("Is first?", "first");
        }
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        appLocationService = new AppLocationService(
                MapActivity.this);
        linearLayout = new LinearLayout(this);

        tMapView = new TMapView(this);


        tMapView.setSKTMapApiKey("your key");
        linearLayout = (LinearLayout) findViewById(R.id.map_view);
        tMapView.setCompassMode(true);
        tMapView.setIconVisibility(true);
        tMapView.setZoomLevel(15);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);
        linearLayout.addView(tMapView);
        Button mButton = (Button)findViewById(R.id.btnTrack);
        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                tMapView.setTrackingMode(true); // 화면중심을 단말의 현재위치로 이동시키는 트래킹 모드 활성화
            }
        });
        gps = new TMapGpsManager(this);
        gps.setMinTime(1000);
        gps.setMinDistance(5);
        gps.setProvider(gps.GPS_PROVIDER);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //gps start
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                   }
                return;
            }
            setGps();
            gps.OpenGps();
            TMapPoint point = gps.getLocation();

            try {
                Location location = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    locationAddress = new LocationAddress();
                    tMapView.setLocationPoint(location.getLongitude(),location.getLatitude());
                    tMapView.setCenterPoint(longitude, latitude);
                    locationAddress.getAddressFromLocation(latitude, longitude,
                            getApplicationContext(), new GeocoderHandler());

                } else {
                    //  showSettingsAlert();
                }
            } catch (Exception e) {

                e.printStackTrace();
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
                    600, // 통지사이의 최소 시간간격 (miliSecond)
                    (float) 0.5, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
        }


    }

    @Override
    protected void onResume() {
        Log.d("aaa","enter map resume");
        //earphone

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
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("aaa","enter map pause");
        //earphone
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

        }
        unregisterReceiver(myReceiver);

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
        for(Prediction prediction : predictions){
            if(prediction.score>1.0){
                if(prediction.name.equals("leftSwipe")){
                    Toast.makeText(this,"현재 페이지 입니다",Toast.LENGTH_SHORT).show();
                } else if(prediction.name.equals("Circle")){
                    Toast.makeText(this,"카메라",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MapActivity.this,ClassifierActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } else if(prediction.name.equals("rightSwipe")){
                    Toast.makeText(this,"버스 안내",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MapActivity.this,BusActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            }
        }
    }
    public void send(View v){
        takeScreenshot();
    }

    private void takeScreenshot(){
        Bitmap b = null;

        b = ScreenShot.getScreenShot(tMapView);

        if(b!=null){
            File saveFile = ScreenShot.getMainDirectoryName(this);//get the path to save screenshot
            File file = ScreenShot.store(b, "screenshot" + ".jpg", saveFile);//save the screenshot to selected path
            shareScreenshot(file);//finally share screenshot
        } else{
            Toast.makeText(this,"capture failed",Toast.LENGTH_SHORT).show();
        }

    }

    private void showScreenShotImage(Bitmap b) {
        imageView.setImageBitmap(b);
    }

    /*  Share Screenshot  */
    private void shareScreenshot(File file) {
        Uri uri = Uri.fromFile(file);//Convert file path into Uri for sharing
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.sharing_text)+addressStr);
        intent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 기능을 킨 후 다시 동작해주세요");

        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLocationChange(Location location) {
        if (location != null) {
            latitude =location.getLatitude();
            longitude=location.getLongitude();
            tMapView.setLocationPoint(longitude, latitude);
            tMapView.setCenterPoint(longitude, latitude);
            Log.d("TmapTest",""+longitude+","+latitude);
        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    addressStr = bundle.getString("address");
                    break;
                default:
                    addressStr = null;
            }
           // tvAddress.setText(locationAddress);
            Toast.makeText(MapActivity.this, addressStr, Toast.LENGTH_LONG).show();
        }
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
    public void setGps() {
        final LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                (float) 0.5, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
    }
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            //현재위치의 좌표를 알수있는 부분
            if (location != null) {
                latitude =location.getLatitude();
                longitude=location.getLongitude();
                tMapView.setLocationPoint(longitude, latitude);
                tMapView.setCenterPoint(longitude, latitude);
                Log.d("TmapTest",""+longitude+","+latitude);

            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
}
