package org.cgutman.shieldcontrollerextensionsexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.cgutman.shieldcontrollerextensions.SceChargingState;
import org.cgutman.shieldcontrollerextensions.SceConnectionState;
import org.cgutman.shieldcontrollerextensions.SceConnectionType;
import org.cgutman.shieldcontrollerextensions.SceManager;

public class MainActivity extends AppCompatActivity {
    private SceManager sceManager;

    private static final String TAG = "SCE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sceManager = new SceManager(this);
        sceManager.setDeviceListener(new SceManager.SceDeviceListener() {
            @Override
            public void onDeviceAdded(int inputDeviceId) {
                Log.i(TAG, "Device added: " + inputDeviceId);
            }

            @Override
            public void onDeviceRemoved(int inputDeviceId) {
                Log.i(TAG, "Device removed: " + inputDeviceId);
            }

            @Override
            public void onBatteryPercentageChanged(int inputDeviceId, int newBatteryPercentage) {
                Log.i(TAG, "Battery percentage changed: " + inputDeviceId + " -> " + newBatteryPercentage);
            }

            @Override
            public void onChargingStateChanged(int inputDeviceId, SceChargingState newChargingState) {
                Log.i(TAG, "Charging state changed: " + inputDeviceId + " -> " + newChargingState);
            }

            @Override
            public void onConnectionStateChanged(int inputDeviceId, SceConnectionState newConnectionState) {
                Log.i(TAG, "Connection state changed: " + inputDeviceId + " -> " + newConnectionState);
            }

            @Override
            public void onConnectionTypeChanged(int inputDeviceId, SceConnectionType newConnectionType) {
                Log.i(TAG, "Connection type changed: " + inputDeviceId + " -> " + newConnectionType);
            }

            @Override
            public void onInputDeviceIdChanged(int oldInputDeviceId, int newInputDeviceId) {
                Log.i(TAG, "Input device ID changed: " + oldInputDeviceId + " -> " + newInputDeviceId);
            }

            @Override
            public void onNicknameChanged(int inputDeviceId, String newNickname) {
                Log.i(TAG, "Nickname changed: " + inputDeviceId + " -> " + newNickname);
            }

            @Override
            public void onHeadsetPresenceChanged(int inputDeviceId, boolean newHeadsetPresence) {
                Log.i(TAG, "Headset presence changed: " + inputDeviceId + " -> " + newHeadsetPresence);
            }
        });
        sceManager.start();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        InputDevice device = event.getDevice();
        if (device != null && sceManager.isRecognizedDevice(device)) {
            float leftTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER);
            float rightTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER);

            sceManager.rumble(device, (int)(leftTrigger * 65535),(int)(rightTrigger * 65535));
        }

        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        InputDevice device = event.getDevice();
        if (device != null && sceManager.isRecognizedDevice(device)) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                sceManager.identify(device);
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        sceManager.stop();
        super.onDestroy();
    }
}