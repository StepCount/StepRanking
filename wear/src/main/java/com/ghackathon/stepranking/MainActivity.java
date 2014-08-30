package com.ghackathon.stepranking;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private Sensor mStepConterSensor;
    private MainActivity self = this;
    private float mStepCount = 0;

    Node node; // メッセージを送るために接続されたデバイス
    GoogleApiClient mGoogleApiClient;
    public static final String START_ACTIVITY_PATH = "/start/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mStepConterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("WearActivity", "onConnected: " + connectionHint);
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("WearActivity", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("WearActivity", "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        Log.v("passed","passed");
        new Thread(new Runnable() {
            @Override
            public void run() {

                node = getNode();

                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                        mGoogleApiClient,node.getId(), START_ACTIVITY_PATH, null).await();

                if (!result.getStatus().isSuccess()) {

                    Log.e("MainActivity", "ERROR: failed to send Message: " + result.getStatus());
                }else{

                    Log.i("MainActivity", "SUCCESS: success to send Message: " + result.getStatus());
                }

            }
        }).start();


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);

                mSensorManager.registerListener (self,
                        mStepConterSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);

                mSensorManager.registerListener(self,
                        mStepDetectorSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;
        long timestamp = event.timestamp;
        if(sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            Log.d("type_step_counter", String.valueOf(values[0]));
            mTextView.setText(String.valueOf(values[0]));

            mStepCount = values[0];


        }
    }

    private Node getNode() {
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {

            return node;
        }
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this,mStepConterSensor);
        mSensorManager.unregisterListener(this,mStepDetectorSensor);
    }
}
