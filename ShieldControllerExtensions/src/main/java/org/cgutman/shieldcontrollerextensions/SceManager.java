package org.cgutman.shieldcontrollerextensions;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.input.InputManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.InputDevice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SceManager {
    private final Context context;

    private IExposedControllerBinderWrapper binder;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = new IExposedControllerBinderWrapper(iBinder);

            try {
                listenerId = binder.registerListener(controllerListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            listenerId = 0;
            tokenToDeviceIdMap.clear();
            deviceIdToTokenMap.clear();

            binder = null;
        }
    };

    // ConcurrentHashMap handles synchronization between the Binder thread adding/removing
    // entries and callers on arbitrary threads that are doing device lookups.
    //
    // Since these are separate maps, they can be temporarily inconsistent (only one-way
    // of the two-way mapping present). This is fine for our purposes here.
    private final ConcurrentHashMap<String, Integer> tokenToDeviceIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> deviceIdToTokenMap = new ConcurrentHashMap<>();

    private int listenerId;
    private final IExposedControllerManagerListener.Stub controllerListener = new IExposedControllerManagerListener.Stub() {
        @Override
        public void onDeviceAdded(String controllerToken) {
            try {
                int inputDeviceId = binder.getInputDeviceId(controllerToken);

                // If we don't have an input device ID yet, ignore this callback.
                // We will invoke it again in onDeviceChanged() when an ID is assigned.
                if (inputDeviceId < 0) {
                    return;
                }

                tokenToDeviceIdMap.put(controllerToken, inputDeviceId);
                deviceIdToTokenMap.put(inputDeviceId, controllerToken);
                if (listener != null) {
                    listener.onDeviceAdded(inputDeviceId);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDeviceChanged(String controllerToken, int changeType) {
            try {
                Integer inputDeviceId = tokenToDeviceIdMap.get(controllerToken);
                if (inputDeviceId == null) {
                    // If this is an input device changed, we will treat this as a device added
                    // when we're gaining an input device ID for the first time.
                    if (changeType == 7 && binder.getInputDeviceId(controllerToken) > 0) {
                        onDeviceAdded(controllerToken);
                    }
                    return;
                }

                // We handle input device ID changed internally to keep our mapping in sync.
                // NB: This can be called for a transition from a valid to invalid input device ID.
                int newInputDeviceId = binder.getInputDeviceId(controllerToken);
                if (changeType == 7) {
                    deviceIdToTokenMap.remove(inputDeviceId);
                    tokenToDeviceIdMap.remove(controllerToken);

                    if (newInputDeviceId >= 0) {
                        deviceIdToTokenMap.put(newInputDeviceId, controllerToken);
                        tokenToDeviceIdMap.put(controllerToken, newInputDeviceId);
                    }
                    else {
                        // Treat transition to -1 as a device removal. We don't notify this case
                        // as a normal input device ID change.
                        if (listener != null) {
                            listener.onDeviceRemoved(inputDeviceId);
                        }
                        return;
                    }
                }

                if (listener != null) {
                    switch (changeType) {
                        case 2:
                            listener.onBatteryPercentageChanged(inputDeviceId, binder.getBatteryPercentage(controllerToken));
                            break;
                        case 3:
                            listener.onChargingStateChanged(inputDeviceId, binder.getChargingState(controllerToken));
                            break;
                        case 4:
                            listener.onConnectionStateChanged(inputDeviceId, binder.getConnectionState(controllerToken));
                            break;
                        case 5:
                            listener.onConnectionTypeChanged(inputDeviceId, binder.getConnectionType(controllerToken));
                            break;
                        case 7:
                            listener.onInputDeviceIdChanged(inputDeviceId, newInputDeviceId);
                            break;
                        case 8:
                            listener.onNicknameChanged(inputDeviceId, binder.getNickname(controllerToken));
                            break;
                        case 11:
                            listener.onHeadsetPresenceChanged(inputDeviceId, binder.hasHeadset(controllerToken));
                            break;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDeviceRemoved(String controllerToken) {
            Integer deviceId = tokenToDeviceIdMap.remove(controllerToken);
            if (deviceId != null) {
                deviceIdToTokenMap.remove(deviceId);
                if (listener != null) {
                    listener.onDeviceRemoved(deviceId);
                }
            }
        }
    };

    private SceDeviceListener listener;

    public SceManager(Context context) {
        this.context = context;
    }

    public void setDeviceListener(SceDeviceListener listener) {
        this.listener = listener;
    }

    public boolean isRecognizedDevice(InputDevice device) {
        return getControllerToken(device) != null;
    }

    public boolean rumble(InputDevice device, int lowFreqMotor, int highFreqMotor) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.rumble(controllerToken, lowFreqMotor, highFreqMotor);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public SceCategory getCategory(InputDevice device) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.getCategory(controllerToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return SceCategory.UNKNOWN;
    }

    public int getBatteryPercentage(InputDevice device) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.getBatteryPercentage(controllerToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public SceChargingState getChargingState(InputDevice device) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.getChargingState(controllerToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return SceChargingState.UNKNOWN;
    }

    public SceConnectionState getConnectionState(InputDevice device) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.getConnectionState(controllerToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return SceConnectionState.UNKNOWN;
    }

    public SceConnectionType getConnectionType(InputDevice device) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.getConnectionType(controllerToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return SceConnectionType.UNKNOWN;
    }

    public boolean identify(InputDevice device) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.identify(controllerToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean hasHeadset(InputDevice device) {
        String controllerToken = getControllerToken(device);
        if (controllerToken != null) {
            try {
                return binder.hasHeadset(controllerToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean start() {
        Intent intent = new Intent();
        intent.setClassName("com.nvidia.blakepairing", "com.nvidia.blakepairing.AccessoryService");
        try {
            // The docs say to call unbindService() even if the bindService() call returns false
            // or throws a SecurityException.
            if (!context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)) {
                context.unbindService(serviceConnection);
                return false;
            }

            return true;
        } catch (SecurityException e) {
            context.unbindService(serviceConnection);
            return false;
        }
    }

    public void stop() {
        tokenToDeviceIdMap.clear();
        deviceIdToTokenMap.clear();

        if (listenerId != 0) {
            try {
                binder.unregisterListener(listenerId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            listenerId = 0;
        }

        if (binder != null) {
            context.unbindService(serviceConnection);
            binder = null;
        }
    }

    private String getControllerToken(InputDevice device) {
        return deviceIdToTokenMap.get(device.getId());
    }

    public abstract static class SceDeviceListener {
        public void onDeviceAdded(int inputDeviceId) {}
        public void onDeviceRemoved(int inputDeviceId) {}

        public void onBatteryPercentageChanged(int inputDeviceId, int newBatteryPercentage) {}
        public void onChargingStateChanged(int inputDeviceId, SceChargingState newChargingState) {}
        public void onConnectionStateChanged(int inputDeviceId, SceConnectionState newConnectionState) {}
        public void onConnectionTypeChanged(int inputDeviceId, SceConnectionType newConnectionType) {}
        public void onInputDeviceIdChanged(int oldInputDeviceId, int newInputDeviceId) {}
        public void onNicknameChanged(int inputDeviceId, String newNickname) {}
        public void onHeadsetPresenceChanged(int inputDeviceId, boolean newHeadsetPresence) {}
    }
}
