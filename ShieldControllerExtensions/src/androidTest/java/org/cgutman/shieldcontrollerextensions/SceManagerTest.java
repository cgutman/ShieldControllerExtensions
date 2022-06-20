package org.cgutman.shieldcontrollerextensions;

import android.view.InputDevice;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SceManagerTest {
    private SceManager sceManager;

    @Before
    public void setupSceManager() throws InterruptedException {
        sceManager = new SceManager(ApplicationProvider.getApplicationContext());
        assertTrue(sceManager.start());

        // HACK: Wait for service binding and callbacks
        Thread.sleep(1000);
    }

    @After
    public void destroySceManager() {
        sceManager.stop();
    }

    private List<InputDevice> getCompatibleDevices() {
        LinkedList<InputDevice> devices = new LinkedList<>();
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(id);
            if (device != null && sceManager.isRecognizedDevice(device)) {
                devices.add(device);
            }
        }
        assertFalse(devices.isEmpty());
        return devices;
    }

    @Test
    public void testCategory() {
        for (InputDevice dev : getCompatibleDevices()) {
            assertNotEquals(SceCategory.UNKNOWN, sceManager.getCategory(dev));
        }
    }

    @Test
    public void testBatteryPercentage() {
        for (InputDevice dev : getCompatibleDevices()) {
            assertNotEquals(-1, sceManager.getBatteryPercentage(dev));
        }
    }

    @Test
    public void testChargingState() {
        for (InputDevice dev : getCompatibleDevices()) {
            assertNotEquals(SceChargingState.UNKNOWN, sceManager.getChargingState(dev));
        }
    }

    @Test
    public void testConnectionState() {
        for (InputDevice dev : getCompatibleDevices()) {
            assertEquals(SceConnectionState.CONNECTED, sceManager.getConnectionState(dev));
        }
    }

    @Test
    public void testConnectionType() {
        for (InputDevice dev : getCompatibleDevices()) {
            assertNotEquals(SceConnectionType.UNKNOWN, sceManager.getConnectionType(dev));
        }
    }

    @Test
    public void testIdentify() {
        for (InputDevice dev : getCompatibleDevices()) {
            assertTrue(sceManager.identify(dev));
        }
    }

    @Test
    public void testHasHeadset() {
        for (InputDevice dev : getCompatibleDevices()) {
            // Can't really test for correctness here
            sceManager.hasHeadset(dev);
        }
    }

    @Test
    public void testRumble() throws InterruptedException {
        for (InputDevice dev : getCompatibleDevices()) {
            assertTrue(sceManager.rumble(dev, 65535, 65535));
            Thread.sleep(500);
            assertTrue(sceManager.rumble(dev, 0, 0));
        }
    }

    @Test
    public void testRestart() {
        sceManager.stop();
        assertTrue(sceManager.start());
    }
}