package com.bender.mpdroid;

import android.os.AsyncTask;
import com.bender.mpdroid.mpdService.MpdPlayerAdapterIF;
import com.bender.mpdroid.mpdService.MpdSongAdapterIF;
import com.bender.mpdroid.mpdService.SongNameDecorator;

/**
 * todo: replace with documentation
 */
class GetSongTask extends AsyncTask<Object, Object, MpdSongAdapterIF>
{
    private MpDroidActivity mpDroidActivity;
    private MpdPlayerAdapterIF mpdPlayerAdapterIF;

    public GetSongTask(MpDroidActivity mpDroidActivity, MpdPlayerAdapterIF mpdPlayerAdapterIF)
    {
        this.mpDroidActivity = mpDroidActivity;
        this.mpdPlayerAdapterIF = mpdPlayerAdapterIF;
    }

    @Override
    protected MpdSongAdapterIF doInBackground(Object... objects)
    {
        MpdSongAdapterIF currentSong = mpdPlayerAdapterIF.getCurrentSong();
        return currentSong;
    }

    @Override
    protected void onPostExecute(MpdSongAdapterIF mpdSongAdapterIF)
    {
        mpDroidActivity.updateSongOnUI(new SongNameDecorator(mpdSongAdapterIF));
    }

}
