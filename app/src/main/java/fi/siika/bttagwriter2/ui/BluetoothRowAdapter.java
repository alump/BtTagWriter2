/*
 * BluetoothRowAdapter.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */
package fi.siika.bttagwriter2.ui;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fi.siika.bttagwriter2.R;

/**
 *
 */
public class BluetoothRowAdapter extends ArrayAdapter<Object> {

    private final List<BluetoothRow> list = new ArrayList<BluetoothRow>();
    private final Activity activity;
    private final int discoveredColor;
    private final int pairedColor;

    public BluetoothRowAdapter(Activity activity) {
        super(activity, R.layout.bt_device_layout, R.id.btDeviceNameTextView);
        discoveredColor = activity.getResources().getColor(R.color.bt_device_discovered_fgcolor);
        pairedColor = activity.getResources().getColor(R.color.bt_device_paired_fgcolor);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.bt_device_layout, null);
        }

        BluetoothRow rowData = list.get(position);

        int colorUsed = rowData.isDeviceVisible() ? discoveredColor : pairedColor;

        TextView nameLine = (TextView) row.findViewById(R.id.btDeviceNameTextView);
        if (nameLine != null) {
            nameLine.setText(rowData.getName());
            nameLine.setTextColor(colorUsed);
        }
        TextView addressLine = (TextView) row.findViewById(R.id.btDeviceAddressTextView);

        String addressValue = rowData.getAddress();
        if (rowData.isPaired()) {
            addressValue =
                    activity.getResources().getString(R.string.btscan_paired_str) + " "
                            + addressValue;
        }

        if (addressLine != null) {
            addressLine.setText(addressValue);
            addressLine.setTextColor(colorUsed);
        }

        return (row);
    }

    public BluetoothRow getRow(int index) {
        return list.get(index);
    }

    @Override
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void clearNonAudio() {
        for (int i = list.size() - 1; i >= 0; --i) {
            if (list.get(i).isAudio()) {
                break;
            } else {
                list.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    public void addDeviceIfNotPresent(BluetoothDevice device, boolean visible, boolean le) {
        BluetoothRow row = new BluetoothRow(device, le);
        row.setDeviceVisible(visible);

        if (list.contains(row)) {
            list.set(list.indexOf(row), row);
            return;
        }

        list.add(row);
        notifyDataSetChanged();
    }
}
