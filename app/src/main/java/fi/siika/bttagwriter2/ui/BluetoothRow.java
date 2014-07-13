/*
 * BluetoothRow.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */
package fi.siika.bttagwriter2.ui;


import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

/**
 * Row used to present a Bluetooth device
 */
public class BluetoothRow {
    private final BluetoothDevice mDevice;
    private boolean mLeDevice = false;
    private boolean mVisible = true;

    /**
     * New row for bluetooth
     * @param device Device presented
     * @param leDevice true if device is LE device
     */
    public BluetoothRow(BluetoothDevice device, boolean leDevice) {
        mDevice = device;
        mLeDevice = leDevice;
    }

    public void setDeviceVisible(boolean visible) {
        mVisible = visible;
    }

    public boolean isDeviceVisible() {
        return mVisible;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public String getName() {
        return mDevice.getName();
    }

    public boolean isAudio() {
        return mDevice.getBluetoothClass().hasService(BluetoothClass.Service.AUDIO);
    }

    /**
     * If device is marked as LE device
     * @return true if LE device
     */
    public boolean isLeDevice() {
        return mLeDevice;
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public boolean isPaired() {
        return mDevice.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BluetoothRow other = (BluetoothRow) obj;
        return mDevice.equals(other.mDevice);
    }

    @Override
    public String toString() {
        return getAddress();
    }
}
