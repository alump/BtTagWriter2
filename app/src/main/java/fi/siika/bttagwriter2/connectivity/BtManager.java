/*
 * BluetoothManager.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */
package fi.siika.bttagwriter2.connectivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import java.util.Set;

import fi.siika.bttagwriter2.R;

/**
 * Manager wrapping all Bluetooth API calls for application
 */
public class BtManager {

    public interface DiscoveryListener {
        /**
         * @param device Device found
         * @param fromPaired true if device was from paired list (and not proper discovery)
         * @param btLe true if Bluetooth LE device
         */
        public void bluetoothDeviceFound(BluetoothDevice device, boolean fromPaired, boolean btLe);

        public void bluetoothDiscoveryStateChanged(boolean active);
    }

    private Activity mActivity = null;
    private BluetoothAdapter mBtAdapter = null;
    private boolean mEnabledBt = false;
    private DiscoveryListener mDiscoveryListener = null;
    private boolean mReceiverConnected = false;
    private final static String TAG = BtManager.class.getSimpleName();
    private boolean mLeMode = false;

    private static final long LE_SCAN_PERIOD_MS = 10000;
    private Handler mLeScanStopHandler;

    public BtManager(Activity activity) {
        mActivity = activity;
    }

    /**
     * Check if Bluetooth LE is supported by device
     * @return true if supported
     */
    public boolean isBtLeSupported() {
        //return false;
        return mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private void connectReceiver() {
        if (!mReceiverConnected) {
            //setup broadcaster listener
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            mActivity.registerReceiver(mBCReceiver, filter);
            mReceiverConnected = true;
        }
    }

    public void releaseAdapter() {
        if (mReceiverConnected) {
            mActivity.unregisterReceiver(mBCReceiver);
            mReceiverConnected = false;
        }
    }

    /*
     * Get Bluetooth adapter.
     */
    public BluetoothAdapter getBluetoothAdapter() {
        if (mBtAdapter == null) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        connectReceiver();

        return mBtAdapter;
    }

    /**
     * Is bluetooth connectivity enabled.
     *
     * @return true if connectivity is enabled
     */
    public boolean isEnabled() {
        boolean enabled = false;
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (adapter != null) {
            enabled = adapter.isEnabled();
        }
        return enabled;
    }

    /**
     * This will call state listener's state changed function even if bluetooth
     * was already enabled!
     */
    public void enable() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (!isEnabled()) {
            Log.d(TAG, "Enable bluetooth");
            mEnabledBt = true;
            adapter.enable();
        }
    }

    private void browsePairedDevices() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        Set<BluetoothDevice> paired = adapter.getBondedDevices();

        for (BluetoothDevice device : paired) {
            if (mDiscoveryListener != null) {
                mDiscoveryListener.bluetoothDeviceFound(device, true, false);
            }
        }
    }

    /**
     * Start Bluetooth discovery (if not active)
     * @param listener Listener for events
     * @param btLe If true will scan LE devices, with false classic Bluetooth devices
     */
    public boolean startDiscovery(DiscoveryListener listener, boolean btLe) {
        mDiscoveryListener = listener;
        BluetoothAdapter adapter = getBluetoothAdapter();
        boolean success = true;
        mLeMode = btLe;

        if (adapter == null) {
            success = false;
        } else if (!adapter.isEnabled()) {
            enable();
            Toast toast = Toast.makeText(mActivity,
                    R.string.toast_bluetooth_enabled_str, Toast.LENGTH_LONG);
            toast.show();
        } else if (btLe) {
            if (mLeScanStopHandler != null) {
                mLeScanStopHandler.removeCallbacks(mStopLeScanRunnable);
                mLeScanStopHandler.postDelayed(mStopLeScanRunnable, LE_SCAN_PERIOD_MS);
            } else {
                if (mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                }
                startBtLeScan();
            }
        } else if (!adapter.isDiscovering()) {
            if(mLeScanStopHandler != null) {
                stopDiscovery();
            }
            adapter.startDiscovery();
            browsePairedDevices();
        }

        return success;
    }

    /**
     * Disable bluetooth if it was originally enabled by BtManager
     */
    public void disableIfEnabled() {
        if (mBtAdapter != null) {
            if (mEnabledBt) {
                Log.d(TAG, "Disable bluetooth");
                mBtAdapter.disable();
                mEnabledBt = false;
            }
        }
    }

    public void stopDiscovery() {
        if(mLeScanStopHandler  != null) {
            mLeScanStopHandler.removeCallbacks(mStopLeScanRunnable);
            mLeScanStopHandler = null;
        }
        if (mBtAdapter != null) {
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }
            disableIfEnabled();
        }
    }

    private final BroadcastReceiver mBCReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE);

                if (mDiscoveryListener != null) {
                    mDiscoveryListener.bluetoothDeviceFound(device, false, false);
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(
                    action)) {

                //Workaround, first call of paired devices does not work if
                //Bluetooth was just enabled.
                //browsePairedDevices();

                if (mDiscoveryListener != null) {
                    mDiscoveryListener.bluetoothDiscoveryStateChanged(false);
                }
                disableIfEnabled();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                if (mDiscoveryListener != null) {
                    mDiscoveryListener.bluetoothDiscoveryStateChanged(true);
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF);
                if (state == BluetoothAdapter.STATE_ON) {
                    if (mDiscoveryListener != null) {
                        if(mLeMode) {
                            startBtLeScan();
                        } else {
                            browsePairedDevices();
                            getBluetoothAdapter().startDiscovery();
                        }
                    }
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    mEnabledBt = false;
                }
            }

        }
    };

    private void startBtLeScan() {
        mBtAdapter.startLeScan(mLeScanCallback);
        mDiscoveryListener.bluetoothDiscoveryStateChanged(true);
        if(mLeScanStopHandler == null) {
            mLeScanStopHandler = new Handler();
        } else {
            mLeScanStopHandler.removeCallbacks(mStopLeScanRunnable);
        }
        mLeScanStopHandler.postDelayed(mStopLeScanRunnable, LE_SCAN_PERIOD_MS);

    }

    private final Runnable mStopLeScanRunnable = new Runnable() {
        @Override
        public void run() {
            mLeScanStopHandler = null;
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mBtAdapter.stopLeScan(mLeScanCallback);
                    mDiscoveryListener.bluetoothDiscoveryStateChanged(false);
                }
            });
        }
    };

    private final BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDiscoveryListener.bluetoothDeviceFound(device, false, true);
                        }
                    });
                }
            };

}
