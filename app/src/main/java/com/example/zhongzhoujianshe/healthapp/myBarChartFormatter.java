package com.example.zhongzhoujianshe.healthapp;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class myBarChartFormatter implements IAxisValueFormatter{


    private String[] mValues;

    public myBarChartFormatter(String[] values) {
        this.mValues = values;
    }
    private static final String TAG = "MyXFormatter";

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        Log.d(TAG, "----->getFormattedValue: "+value);
        return mValues[(int) value % mValues.length];
    }
}

