package com.bender.mpdroid.mpdService;

import android.util.Log;

/**
 * Null object song adapter
 */
class NullSongAdapter implements MpdSongAdapterIF {
    private static final String TAG = NullSongAdapter.class.getSimpleName();

    public String getSongName() {
        Log.e(TAG, "getSongName() called on NULL object");
        return null;
    }

    public String getArtist() {
        Log.e(TAG, "getArtist() called on NULL object");
        return null;
    }

    public String getAlbumName() {
        Log.e(TAG, "getAlbumName() called on NULL object");
        return null;
    }

    public String getFile() {
        Log.e(TAG, "getFile() called on NULL object");
        return null;
    }

    public String getDate() {
        Log.e(TAG, "getDate() called on NULL object");
        return null;
    }

    public Integer getSongLength() {
        Log.e(TAG, "getSongLength() called on NULL object");
        return null;
    }

    public int getId() {
        return -1;
    }
}
