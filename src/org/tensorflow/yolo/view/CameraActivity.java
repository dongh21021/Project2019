package org.tensorflow.yolo.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.skt.Tmap.TMapGpsManager;

import org.tensorflow.yolo.AppLocationService;
import org.tensorflow.yolo.BusActivity;
import org.tensorflow.yolo.FirstActivity;
import org.tensorflow.yolo.LocationAddress;
import org.tensorflow.yolo.MapActivity;
import org.tensorflow.yolo.R;
import org.tensorflow.yolo.SettingActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.tensorflow.yolo.Config.LOGGING_TAG;

/**
 * Camera activity class.
 * Modified by Zoltan Szabo
 */
public class CameraActivity extends Activity implements OnImageAvailableListener, GestureOverlayView.OnGesturePerformedListener, SensorEventListener {
    private static final int PERMISSIONS_REQUEST = 1;
    private SensorManager mSensorManager;
    private float currentDegree = 0f;
    private Handler handler;
    private HandlerThread handlerThread;
    //
    private GestureLibrary gestureLib;

    //recordplay
    private Button map;
    private Button bus;
    ////////////////////////////////recordPlay/////////////////
    public static final String TAG = "RecordPlayActivity";
    public static String direction;
    public boolean exitState;
    public boolean first = true;
    //earphone
    public static int lock=-1;
    private MusicIntentReceiver myReceiver;
    final int REQUEST_PERMISSION_CODE = 1000;

    double min=1000000;
    private List<RoadSample> roadSamples;
    RoadSample nearest;
    public static Location a=new Location("point A");
    AppLocationService appLocationService;
    TMapGpsManager gps = null;
    public double longitude,latitude;
    LocationManager locationManager;
    public float degree=0;
    public double c_lat=0.0;
    public double c_lon=0.0;
    public double c_length;
    public double angle=0.0;
    public boolean jong = false;
    SharedPreferences recPlay;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d("aaa","enter camera create");
        super.onCreate(null);
        //
        first = false;
        exitState = true;
        SharedPreferences gesture =
                getSharedPreferences("Gesture", Activity.MODE_PRIVATE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        appLocationService = new AppLocationService(CameraActivity.this);;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(gesture.getInt("gesture", 0)==1) {
        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_camera,null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.addOnGesturePerformedListener(this);
        gestureLib = GestureLibraries.fromRawResource(this,R.raw.gestures);
        if(!gestureLib.load())
            finish();
        setContentView(gestureOverlayView);
        }else{
            setContentView(R.layout.activity_camera);
        }
        Button set_but = (Button) findViewById(R.id.cam_sett);

        map = (Button)findViewById(R.id.btn_map);
        bus = (Button)findViewById(R.id.btn_bus);
        Button cam = (Button)findViewById(R.id.cam_cam);
        cam.setEnabled(false);
        SharedPreferences tuto =
                getSharedPreferences("Tutorial", Activity.MODE_PRIVATE);
        recPlay = getSharedPreferences("Sound", Activity.MODE_PRIVATE);
        SharedPreferences spref = getSharedPreferences("isFirsts", Activity.MODE_PRIVATE);
        SharedPreferences soundPref = getSharedPreferences("isFirstSound", Activity.MODE_PRIVATE);
        if(tuto.getInt("tutorial", 0)==1){
            set_but.setContentDescription(getResources().getString(R.string.Tsetting_button));
            cam.setContentDescription(getResources().getString(R.string.Tcamera_button));
            map.setContentDescription(getResources().getString(R.string.Tmap_button));
            bus.setContentDescription(getResources().getString(R.string.Tbus_button));
        }else{
            set_but.setContentDescription(getResources().getString(R.string.setting_button));
            cam.setContentDescription(getResources().getString(R.string.camera_button));
            map.setContentDescription(getResources().getString(R.string.map_button));
            bus.setContentDescription(getResources().getString(R.string.bus_button));
        }

        if(!spref.getBoolean("isFirsts", false)){
            set_but.setContentDescription(getResources().getString(R.string.Tsetting_button));
            map.setContentDescription(getResources().getString(R.string.Tmap_button));
            bus.setContentDescription(getResources().getString(R.string.Tbus_button));
            cam.setContentDescription(getResources().getString(R.string.Tcamera_button));
        }
        if(!soundPref.getBoolean("isFirstSound", false)){
            Log.d("Is first?", "first");

        }
        myReceiver = new MusicIntentReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
        }
        set_but.setOnClickListener(new View.OnClickListener() { //button1을 눌렀을때~~
            @Override
            public void onClick(View view) { //여기에 눌렀을때의 일어날 일을 적는다.
                Intent intent = new Intent(CameraActivity.this,SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("ACTIVITY","cam");
                exitState = false;
                startActivity(intent);
                //Toast.makeText(getApplicationContext(), "버튼이 눌러졌나 ?", Toast.LENGTH_SHORT).show();
            }
        });

        setFragment();

    }
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
        for(Prediction prediction : predictions){
            if(prediction.score>1.0){
                if(prediction.name.equals("leftSwipe")){
                    Toast.makeText(this,"지도",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CameraActivity.this,MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } else if (prediction.name.equals("Circle")){
                    Toast.makeText(this,"현재 페이지 입니다",Toast.LENGTH_SHORT).show();
                }
                else if(prediction.name.equals("rightSwipe")){
                    Toast.makeText(this,"버스 안내",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CameraActivity.this,BusActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            }
        }
    }
    private void readRoadData(){
        InputStream is = getResources().openRawResource(R.raw.crosswalk);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        try{
            reader.readLine(); //first row is all string
            while((line = reader.readLine())!=null){
                //split by ','
                String[] tokens = line.split(",");

                //read the data
                RoadSample sample = new RoadSample();
                Log.d("123", tokens[0]);
                sample.setNo(tokens[0]);
                sample.setLat(tokens[1]);
                sample.setLon(tokens[2]);
                sample.setLane(tokens[3]);
                sample.setWidth(tokens[4]);
                sample.setLength(tokens[5]);

                nearest = sample;
                roadSamples.add(sample);
            }

            //Log.d("MyActivity","nearest Crosswalk: "+nearest);
        } catch (IOException e) {
            Log.wtf("MyActivity","Error reading data file on line"+line,e);
            e.printStackTrace();
        }
    }
    private void findNearest() {

        int size = roadSamples.size();
        for(int i=0;i<size;i++){
            RoadSample sample = roadSamples.get(i);
            double lat = Double.parseDouble(sample.getLat());
            double lon = Double.parseDouble(sample.getLon());
            double len = Double.parseDouble(sample.getLength());
            double dist = calculateDistance(lat,lon);
            if(dist<min){
                nearest = sample;
                min = dist;
                c_length = len;
                c_lat = lat;
                c_lon = lon;
            }
        }
        Double angle2=distance(a.getLatitude(), a.getLongitude(),c_lat,c_lon);
        Log.d("123!!!!!!", Double.toString(c_lat));
        angle = angle2+degree;
        if(angle>=0)
            angle=angle-360;
        if(0<=angle&&angle<=30){
           direction="열두시 방향에있습니다.";
        }
        else if(30<angle&&angle<=60){
            direction= "한시 방향에있습니다.";
        }
        else if(60<angle&&angle<=90){
            direction="두시 방향에있습니다.";
        }
        else if(90<angle&&angle<=120){
            direction="세시 방향에있습니다.";
        }
        else if(120<angle&&angle<=150){
            direction="네시 방향에있습니다.";
        }
        else if(150<angle&&angle<=180){
            direction="다섯시 방향에있습니다.";
        }
        else if(180<angle&&angle<=210){
            direction="여섯시 방향에있습니다.";
        }
        else if(210<angle&&angle<=240){
            direction="일곱시 방향에있습니다.";
        }
        else if(240<angle&&angle<=270){
            direction="여덟시 방향에있습니다.";
        }
        else if(270<angle&&angle<=300){
            direction="아홉시 방향에있습니다.";
        }
        else if(300<angle&&angle<=330){
            direction="열시 방향에있습니다.";
        }
        else if(330<angle&&angle<=360){
            direction="열한시 방향에있습니다.";
        }


        Log.d("answer1",c_lat+" "+c_lon);
        Log.d("answer2",Double.toString(a.getLatitude())+" "+Double.toString(a.getLongitude()));
      //  Log.d("answer3",Double.toString(distance(a.getLatitude(), a.getLongitude(),Double.parseDouble(nearest.getLat()),Double.parseDouble(nearest.getLon()))));
        Log.d("length",Double.toString(calculateDistance(c_lat,c_lon)));
        Log.d("angle", (Double.toString(angle))+" "+Double.toString(angle2)+" "+Double.toString(degree));

    }

    private double calculateDistance(double lat, double lon) {

        Location b = new Location("point B");

        b.setLatitude(lat);
        b.setLongitude(lon);

        return a.distanceTo(b);
    }

    public double distance(double p1_lat, double p1_long, double p2_lat, double p2_long){
        double cur_lat_radian= p1_lat*(3.141592/180);
        double cur_lon_radian=p1_long*(3.141592/180);

        double dst_lat_rad=p2_lat*(3.141592/180);
        double dst_lon_rad=p2_long*(3.141592/180);

        double radian_distance=0;
        radian_distance=Math.acos(Math.sin(cur_lat_radian)*Math.sin(dst_lat_rad)+Math.cos(cur_lat_radian)*Math.cos(dst_lat_rad)*Math.cos(cur_lon_radian-dst_lon_rad));
        double radian_bearing=Math.acos((Math.sin(dst_lat_rad)-Math.sin(cur_lat_radian)*Math.cos(radian_distance))/(Math.cos(cur_lat_radian)*Math.sin(radian_distance)));
        double true_bearing=0;
        Log.d("123", Double.toString(radian_distance));

        if(Math.sin(dst_lon_rad-cur_lon_radian)<0){
            true_bearing=radian_distance*(180/3.141592);
            Log.d("123", Double.toString(true_bearing));
            true_bearing=360-true_bearing;
        }
        else{
            true_bearing=radian_bearing * (180/3.141592);
        }
        return true_bearing;
    }

    //
    public void onBusClick(View view){
        Toast.makeText(this,"버스 안내",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CameraActivity.this,BusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        exitState = false;
        startActivity(intent);
    }

    public void onMapClick(View view){
        Toast.makeText(this,"지도",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CameraActivity.this,MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        exitState = false;
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        degree = Math.round(event.values[0]);
        // create a rotation animation (reverse turn degree degrees)
        currentDegree = -degree;
    }

    @Override
    public synchronized void onResume() {

        Log.d("aaa","enter camera resume");

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

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
    public synchronized void onPause() {
        Log.d("aaa","enter camera pause");
        if (!isFinishing()) {
            finish();
        }

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException ex) {
            Log.e(LOGGING_TAG, "Exception: " + ex.getMessage());
        }

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
        super.onBackPressed();
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {

    }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        FirstActivity.lock = 0;
                        lock=0;
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        FirstActivity.lock=1;
                        lock=1;
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }

    protected synchronized void runInBackground(final Runnable runnable) {
        if (handler != null) {
            handler.post(runnable);
            roadSamples=new ArrayList<>();
            nearest=new RoadSample();
            gps = new TMapGpsManager(this);
            gps.setMinTime(1000);
            gps.setMinDistance(5);
            gps.setProvider(gps.NETWORK_PROVIDER);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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
                try {
                    Location location = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);/////////////////////////////////////////
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        a.setLatitude(latitude);
                        a.setLongitude(longitude);
                        String str = Double.toString(longitude) + ", " + Double.toString(latitude);
                   //     Toast.makeText(CameraActivity.this, str, Toast.LENGTH_LONG).show();
                        LocationAddress locationAddress = new LocationAddress();
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

            nearest = new RoadSample();
            readRoadData();
             findNearest();
        }
    }
    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {

            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }

            if(locationAddress.contains("종로구")){
                jong = true;
            }else{
                jong = false;
            }
       //     Toast.makeText(CameraActivity.this, locationAddress+jong, Toast.LENGTH_LONG).show();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 기능을 킨 후 다시 동작해주세요");

        final AlertDialog alert = builder.create();
        alert.show();
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
                a.setLatitude(location.getLatitude());
                a.setLongitude(location.getLongitude());
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
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setFragment();
                } else {
                    requestPermission();
                }
            }
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(CameraActivity.this,
                        "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

    protected void setFragment() {
        CameraConnectionFragment cameraConnectionFragment = new CameraConnectionFragment();
        cameraConnectionFragment.addConnectionListener((final Size size, final int rotation) ->
                CameraActivity.this.onPreviewSizeChosen(size, rotation));
        cameraConnectionFragment.addImageAvailableListener(this);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, cameraConnectionFragment)
                .commit();
    }

    public void requestRender() {
        final OverlayView overlay = (OverlayView) findViewById(R.id.overlay);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }

    public void addCallback(final OverlayView.DrawCallback callback) {
        final OverlayView overlay = (OverlayView) findViewById(R.id.overlay);
        if (overlay != null) {
            overlay.addCallback(callback);
        }
    }

    protected void onPreviewSizeChosen(final Size size, final int rotation){}
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
