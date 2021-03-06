package com.bender.mpdroid.mpdService;

import java.util.ArrayList;
import java.util.List;

/**
 * Null object playlist adapter
 */
class NullPlaylistAdapter implements MpdPlaylistAdapterIF {
    public MpdSongAdapterIF getCurrentSong() {
        return new NullSongAdapter();
    }

    public int getPlaylistSize() {
        return 0;
    }

    public MpdSongAdapterIF getSongInfo(int songPosition) {
        return new NullSongAdapter();
    }

    public void play(int songPos) {
    }

    public void setListener(MpdPlaylistListenerIF playlistListenerIF) {

    }

    public List<MpdSongAdapterIF> search(String query) {
        return new ArrayList<MpdSongAdapterIF>(0);
    }

    public void play(MpdSongAdapterIF mpdSongAdapterIF) {

    }


}
