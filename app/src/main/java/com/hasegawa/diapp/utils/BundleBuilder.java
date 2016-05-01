/**
 * from: https://raw.githubusercontent.com/bluelinelabs/Conductor/develop/demo/src/main/java/com/bluelinelabs/conductor/demo/util/BundleBuilder.java
 */

package com.hasegawa.diapp.utils;


import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

public class BundleBuilder {

    private final Bundle mBundle;

    public BundleBuilder(Bundle bundle) {
        mBundle = bundle;
    }

    public BundleBuilder putAll(Bundle bundle) {
        mBundle.putAll(bundle);
        return this;
    }

    public BundleBuilder putBoolean(String key, boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    public BundleBuilder putBooleanArray(String key, boolean[] value) {
        mBundle.putBooleanArray(key, value);
        return this;
    }

    public BundleBuilder putDouble(String key, double value) {
        mBundle.putDouble(key, value);
        return this;
    }

    public BundleBuilder putDoubleArray(String key, double[] value) {
        mBundle.putDoubleArray(key, value);
        return this;
    }

    public BundleBuilder putLong(String key, long value) {
        mBundle.putLong(key, value);
        return this;
    }

    public BundleBuilder putLongArray(String key, long[] value) {
        mBundle.putLongArray(key, value);
        return this;
    }

    public BundleBuilder putString(String key, String value) {
        mBundle.putString(key, value);
        return this;
    }

    public BundleBuilder putStringArray(String key, String[] value) {
        mBundle.putStringArray(key, value);
        return this;
    }

    public BundleBuilder putBundle(String key, Bundle value) {
        mBundle.putBundle(key, value);
        return this;
    }

    public BundleBuilder putByte(String key, byte value) {
        mBundle.putByte(key, value);
        return this;
    }

    public BundleBuilder putByteArray(String key, byte[] value) {
        mBundle.putByteArray(key, value);
        return this;
    }

    public BundleBuilder putChar(String key, char value) {
        mBundle.putChar(key, value);
        return this;
    }

    public BundleBuilder putCharArray(String key, char[] value) {
        mBundle.putCharArray(key, value);
        return this;
    }

    public BundleBuilder putCharSequence(String key, CharSequence value) {
        mBundle.putCharSequence(key, value);
        return this;
    }

    public BundleBuilder putCharSequenceArray(String key, CharSequence[] value) {
        mBundle.putCharSequenceArray(key, value);
        return this;
    }

    public BundleBuilder putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        mBundle.putCharSequenceArrayList(key, value);
        return this;
    }

    public BundleBuilder putInt(String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }

    public BundleBuilder putIntArray(String key, int[] value) {
        mBundle.putIntArray(key, value);
        return this;
    }

    public BundleBuilder putFloat(String key, float value) {
        mBundle.putFloat(key, value);
        return this;
    }

    public BundleBuilder putFloatArray(String key, float[] value) {
        mBundle.putFloatArray(key, value);
        return this;
    }

    public BundleBuilder putIntegerArrayList(String key, ArrayList<Integer> value) {
        mBundle.putIntegerArrayList(key, value);
        return this;
    }

    public BundleBuilder putParcelable(String key, Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }

    public BundleBuilder putParcelableArray(String key, Parcelable[] value) {
        mBundle.putParcelableArray(key, value);
        return this;
    }

    public BundleBuilder putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
        mBundle.putParcelableArrayList(key, value);
        return this;
    }

    public BundleBuilder putSerializable(String key, Serializable value) {
        mBundle.putSerializable(key, value);
        return this;
    }

    public BundleBuilder putShort(String key, short value) {
        mBundle.putShort(key, value);
        return this;
    }

    public BundleBuilder putShortArray(String key, short[] value) {
        mBundle.putShortArray(key, value);
        return this;
    }

    public BundleBuilder putSparseParcelableArray(String key, SparseArray<? extends Parcelable> value) {
        mBundle.putSparseParcelableArray(key, value);
        return this;
    }

    public BundleBuilder putStringArrayList(String key, ArrayList<String> value) {
        mBundle.putStringArrayList(key, value);
        return this;
    }

    public Bundle build() {
        return mBundle;
    }

}
