# ShieldControllerExtensions

This Android library enables access to rumble and other special functionality on NVIDIA Shield controllers by communicating with the Shield Accessories service included on the NVIDIA Shield Android TV firmware.

This is required because the NVIDIA Shield Controller does not work with the standard APIs such as `getVibrator()`, `getVibrationManager()`, `getBatteryState()`, etc.

# Usage

The SceManager class provides the primary interface to the library. It is designed to easily integrate with existing `KeyEvent` and `MotionEvent` processing code because it works exclusively with `InputDevice` references which are easily obtained from such events.

### Instantiating a SceManager
```java
sceManager = new SceManager(activity);
sceManager.start();
```

You may add an `SceManager.SceDeviceListener` using `setDeviceListener()` prior to calling `start()`, but this is not required.

### Destroying a SceManager
```java
sceManager.stop();
```

### Listening for device add/remove events
```java
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
});
sceManager.start();
```

For your listener to receive the initial `onDeviceAdded()` callbacks for existing devices, it must be registered prior to calling `start()`.

`SceManager.SceDeviceListener` contains support for many additional device state callbacks that can be optionally implemented.

### Determining if an InputDevice is compatible with SceManager APIs

```java
if (sceManager.isRecognizedDevice(inputDevice)) {
    // Use SceManager to do something cool
}
else {
    // This doesn't look like a SHIELD controller
}
```

### Rumbling an NVIDIA Shield Controller

The `rumble()` function provided by `SceManager` behaves similarly to common APIs like `XInputSetState()` or `SDL_GameControllerRumble()`. It takes left and right actuator amplitude in a 0 to 65535 range and rumbles continuously until cancelled by a zero amplitude rumble, or `SceManager.stop()` is called.

The following code rumbles a Shield Controller for 1 second at maximum amplitude:
```java
sceManager.rumble(inputDevice, 65535, 65535);
Thread.sleep(1000);
sceManager.rumble(inputDevice, 0, 0);
```

For a more dynamic example, this `Activity` code will rumble based upon trigger input:
```java
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        InputDevice device = event.getDevice();
        if (device != null && sceManager.isRecognizedDevice(device)) {
            float leftTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER);
            float rightTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER);

            sceManager.rumble(device, (int)(leftTrigger * 65535), (int)(rightTrigger * 65535));
        }

        return super.onGenericMotionEvent(event);
    }
```

### Identifying a device

The `identify()` function can be used to play a haptic effect or activate some other device-specific method of self-identification.

The following example triggers identification when the A button is presssed:
```java
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
```

# Code Sample

This repository includes an [example app](https://github.com/cgutman/ShieldControllerExtensions/blob/main/app/src/main/java/org/cgutman/shieldcontrollerextensionsexample/MainActivity.java) that can be used as a reference for interaction with the library. 
