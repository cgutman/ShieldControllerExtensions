package org.cgutman.shieldcontrollerextensions;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

class IExposedControllerBinderWrapper {
    private final IBinder binder;

    IExposedControllerBinderWrapper(IBinder binder) {
        this.binder = binder;
    }

    int registerListener(IExposedControllerManagerListener.Stub controllerListener) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeStrongBinder(controllerListener);

            binder.transact(20, input, output, 0);

            output.readException();
            return output.readInt();
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    boolean unregisterListener(int listenerId) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeInt(listenerId);

            binder.transact(21, input, output, 0);

            output.readException();
            return output.readInt() != 0;
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    int getInputDeviceId(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(13, input, output, 0);

            output.readException();
            return output.readInt();
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    // Rumble duration maximum of 1 second
    boolean rumble(String controllerToken, int lowFreqMotor, int highFreqMotor) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);
            input.writeInt(lowFreqMotor);
            input.writeInt(highFreqMotor);

            binder.transact(18, input, output, 0);

            output.readException();
            return output.readInt() != 0;
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    // Rumble duration maximum of 1.5 seconds
    boolean rumbleWithDuration(String controllerToken, int lowFreqMotor, int highFreqMotor, long durationMs) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);
            input.writeInt(lowFreqMotor);
            input.writeInt(highFreqMotor);
            input.writeLong(durationMs);

            binder.transact(19, input, output, 0);

            output.readException();
            return output.readInt() != 0;
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    SceCategory getCategory(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(11, input, output, 0);

            output.readException();
            switch (output.readInt()) {
                case 1:
                    return SceCategory.CONTROLLER;
                case 2:
                    return SceCategory.REMOTE;
                default:
                    return SceCategory.UNKNOWN;
            }
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    String getNickname(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(3, input, output, 0);

            output.readException();
            return output.readString();
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    int getBatteryPercentage(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(28, input, output, 0);

            output.readException();

            String[] batteryState = output.readString().split(";");
            return Integer.parseInt(batteryState[0]);
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    SceChargingState getChargingState(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(9, input, output, 0);

            output.readException();
            switch (output.readInt()) {
                case 1:
                    return SceChargingState.NOT_CHARGING;
                case 2:
                    return SceChargingState.CHARGING;
                default:
                    return SceChargingState.UNKNOWN;
            }
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    SceConnectionState getConnectionState(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(10, input, output, 0);

            output.readException();
            switch (output.readInt()) {
                case 1:
                    return SceConnectionState.DISCONNECTED;
                case 2:
                    return SceConnectionState.CONNECTED;
                default:
                    return SceConnectionState.UNKNOWN;
            }
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    SceConnectionType getConnectionType(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(11, input, output, 0);

            output.readException();
            switch (output.readInt()) {
                case 1:
                    return SceConnectionType.WIRED;
                case 2:
                    return SceConnectionType.WIRELESS;
                case 3:
                    return SceConnectionType.BOTH;
                default:
                    return SceConnectionType.UNKNOWN;
            }
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    boolean identify(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(14, input, output, 0);

            output.readException();
            return output.readInt() != 0;
        } finally {
            input.recycle();
            output.recycle();
        }
    }

    boolean hasHeadset(String controllerToken) throws RemoteException {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();
        try {
            input.writeInterfaceToken("com.nvidia.blakepairing.IExposedControllerBinder");
            input.writeString(controllerToken);

            binder.transact(31, input, output, 0);

            output.readException();
            return output.readInt() != 0;
        } finally {
            input.recycle();
            output.recycle();
        }
    }
}
