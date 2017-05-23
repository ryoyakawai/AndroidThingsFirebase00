package com.ryoyakawai.androidthingsfirebase00;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import javax.xml.validation.Schema;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static long blinkInterval = 1000;

    // see more information from
    // https://developer.android.com/things/hardware/raspberrypi-io.html
    private static final String pinName = "BCM4";
    private boolean pinNameInUse = false;

    private Handler mHandler = new Handler();
    private Gpio mLedGpio;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUpdateHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mLedGpio.setValue(false);
            // Remove pending blink Runnable from the handler.
            mHandler.removeCallbacks(mBlinkRunnable);
        } catch(IOException e) {
            Log.e(TAG, "Filed to LED light off on onDestroy.");
            e.printStackTrace();
        }
        try {
            mLedGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on API to control Peripheral I/O at onDestroy");
        } finally {
            mLedGpio = null;
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if(mLedGpio == null) {
                return;
            }
            try {
                // Toggle the GPIO state
                mLedGpio.setValue(!mLedGpio.getValue());
                mHandler.postDelayed(mBlinkRunnable, blinkInterval);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Filed: mBlinkRunnable.");
            }
        }
    };

    private void firebaseUpdateHandler() {
        ValueEventListener fbDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Fbschema fbschema = dataSnapshot.getValue(Fbschema.class);
                blinkInterval = fbschema.getInterval();
                Log.i(TAG, "Interval: "+ blinkInterval);

                TextView interval_text = (TextView)findViewById(R.id.interval);
                String s_blinkInterval = Long.toString(blinkInterval);
                interval_text.setText(s_blinkInterval);

                if(pinNameInUse == false) {
                    PeripheralManagerService pmService = new PeripheralManagerService();
                    try {
                        mLedGpio = pmService.openGpio(pinName);
                        pinNameInUse = true;
                        mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                        Log.i(TAG, "Start blink PinName: "+ pinName);
                        mHandler.post(mBlinkRunnable);
                    } catch (IOException e) {
                         e.printStackTrace();
                        Log.e(TAG, "Error on API to control Peripheral I/O at firebaseUpdateHandler.");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(fbDataListener);
    }
}
