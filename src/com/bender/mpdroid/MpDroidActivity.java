package com.bender.mpdroid;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.bender.mpdroid.mpdService.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for the mpd droid application
 */
public class MpDroidActivity extends Activity {
    private static final String TAG = MpDroidActivity.class.getSimpleName();
    private static final int REQUEST_PREFERENCES = 1;
    private static final int REQUEST_SYSTEM_SETTINGS = 2;

    private TextView serverTextView;
    private TextView portTextView;
    private CheckBox useAuthenticationCheckbox;
    private Button connectButton;
    private SeekBar volumeSeekBar;
    private TextView songNameTextView;
    private TextView songDetailsTextView;
    private ImageButton repeatCheckbox;
    private ImageButton randomCheckbox;
    private TextView nowPlayingTextView;

    private MpdPreferences myPreferences;

    // todo: remove static field
    private static MpdServiceAdapterIF mpdServiceAdapterIF;
    private MpdPlayerAdapterIF mpdPlayerAdapterIF;

    private List<MpDroidActivityWidget> mpDroidActivityWidgetList;
    private volatile MpdOptionsIF mpdOptionsIF = new NullMpdOptions();

    public MpDroidActivity() {
        mpDroidActivityWidgetList = new ArrayList<MpDroidActivityWidget>();
        mpDroidActivityWidgetList.add(new PlayerFrame());
        mpDroidActivityWidgetList.add(new SongProgressFrame());
        mpdServiceAdapterIF = MpdAdapterFactory.createAdapter();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary);
        myPreferences = new MpdPreferences(this);
        for (MpDroidActivityWidget mpDroidActivityWidget : mpDroidActivityWidgetList) {
            mpDroidActivityWidget.onCreate(this);
        }
        initializeWidgets();
        initializeListeners();
        updateServerSettingsDisplay();
        updateConnectedStatusOnUI(false);
        if (autoConnect()) {
            ConnectTask connectTask = new ConnectTask();
            connectTask.execute();
        }
        Log.v(TAG, "onCreate: DONE");
    }

    private boolean autoConnect() {
        boolean autoConnect = myPreferences.autoConnect();
        if (autoConnect) {
            if (myPreferences.autoConnectWithWifiOnly()) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                autoConnect = networkInfo.isConnected();
            }
        }
        return autoConnect;
    }

    private void initializeWidgets() {
        serverTextView = (TextView) findViewById(R.id.server_name);
        portTextView = (TextView) findViewById(R.id.port);
        useAuthenticationCheckbox = (CheckBox) findViewById(R.id.use_authentication);
        connectButton = (Button) findViewById(R.id.connect);
        volumeSeekBar = (SeekBar) findViewById(R.id.volume);
        songNameTextView = (TextView) findViewById(R.id.song_name);
        songDetailsTextView = (TextView) findViewById(R.id.song_details);
        repeatCheckbox = (ImageButton) findViewById(R.id.repeat);
        randomCheckbox = (ImageButton) findViewById(R.id.random);
        nowPlayingTextView = (TextView) findViewById(R.id.now_playing_label);
    }

    private void initializeListeners() {
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        connectButton.setOnClickListener(buttonClickListener);
        volumeSeekBar.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener());
        repeatCheckbox.setOnClickListener(buttonClickListener);
        randomCheckbox.setOnClickListener(buttonClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PREFERENCES) {
            Log.v(TAG, "Server Settings Updated");
            updateServerSettingsDisplay();
        } else if (requestCode == REQUEST_SYSTEM_SETTINGS) {
            Log.v(TAG, "System settings updated");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
            case R.id.settings_menu:
                openSystemSettings();
                break;
            case R.id.server_menu:
                openServerSettings();
                break;
            case R.id.search:
                Log.v(TAG, "search!");
                onSearchRequested();
                break;
            default:
                ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }

    private void openSystemSettings() {
        Intent launchPreferencesIntent = new Intent();
        launchPreferencesIntent.setClass(MpDroidActivity.this, SystemSettingsActivity.class);
        startActivityForResult(launchPreferencesIntent, REQUEST_SYSTEM_SETTINGS);
    }

    private void openServerSettings() {
        Intent launchPreferencesIntent = new Intent();
        launchPreferencesIntent.setClass(MpDroidActivity.this, MpDroidPreferenceActivity.class);
        startActivityForResult(launchPreferencesIntent, REQUEST_PREFERENCES);
    }

    private void updateServerSettingsDisplay() {
        String server = myPreferences.getServer();
        serverTextView.setText(getText(R.string.server) + ": " + server);
        String port = String.valueOf(myPreferences.getPort());
        portTextView.setText(getText(R.string.port) + ": " + port);
        boolean useAuthentication = myPreferences.useAuthentication();
        useAuthenticationCheckbox.setChecked(useAuthentication);
    }

    MpdServiceAdapterIF getMpdServiceAdapterIF() {
        return mpdServiceAdapterIF;
    }

    private void updateConnectedStatusOnUI(Boolean connected) {
        String text = connected ? getString(R.string.disconnect) : getString(R.string.connect);
        connectButton.setText(text);
        volumeSeekBar.setEnabled(connected);
        int visibility = connected ? View.VISIBLE : View.INVISIBLE;
        songNameTextView.setVisibility(visibility);
        songDetailsTextView.setVisibility(visibility);
        repeatCheckbox.setVisibility(visibility);
        randomCheckbox.setVisibility(visibility);
        nowPlayingTextView.setVisibility(visibility);

        if (connected) {
            GetVolumeTask getVolumeTask = new GetVolumeTask();
            getVolumeTask.execute();
            GetSongTask getSongTask = new GetSongTask(this, mpdPlayerAdapterIF);
            getSongTask.execute();
            AsyncTask<Void, Void, Boolean> getRepeatTask = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    return mpdOptionsIF.getRepeat();
                }

                @Override
                protected void onPostExecute(Boolean repeat) {
                    updateRepeat(repeat);
                }
            };
            getRepeatTask.execute();
            AsyncTask<Void, Void, Boolean> getRandomTask = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    return mpdOptionsIF.getRandom();
                }

                @Override
                protected void onPostExecute(Boolean random) {
                    updateRandom(random);
                }
            };
            getRandomTask.execute();
        }
        for (MpDroidActivityWidget mpDroidActivityWidget : mpDroidActivityWidgetList) {
            mpDroidActivityWidget.onConnectionChange(connected);
        }
    }

    private void updateRandom(Boolean random) {
        int resource = random ? R.drawable.ic_mp_shuffle_on_btn : R.drawable.ic_mp_shuffle_off_btn;
        randomCheckbox.setImageResource(resource);
    }

    private void updateRepeat(Boolean repeat) {
        int resource = repeat ? R.drawable.ic_mp_repeat_all_btn : R.drawable.ic_mp_repeat_off_btn;
        repeatCheckbox.setImageResource(resource);
    }

    public static MpdServiceAdapterIF getMpdService() {
        return mpdServiceAdapterIF != null ? mpdServiceAdapterIF : new NullMpdService();
    }

    void setMpdService(MpdServiceAdapterIF service) {
        mpdServiceAdapterIF = service;
    }


    private class ConnectTask extends AsyncTask<Void, MpdPlayerAdapterIF.PlayStatus, Boolean> {
        @Override
        protected Boolean doInBackground(Void... unused) {
            String server = myPreferences.getServer();
            int port = myPreferences.getPort();
            boolean useAuthentication = myPreferences.useAuthentication();
            boolean usePort = myPreferences.usePort();
            if (useAuthentication) {
                String password = myPreferences.getPassword();
                mpdServiceAdapterIF.connect(server, port, password);
            } else if (usePort) {
                mpdServiceAdapterIF.connect(server, port);
            } else {
                mpdServiceAdapterIF.connect(server);
            }
            boolean connected = mpdServiceAdapterIF.isConnected();
            mpdServiceAdapterIF.addConnectionListener(new MyMpdConnectionListenerIF());

            if (connected) {
                mpdPlayerAdapterIF = mpdServiceAdapterIF.getPlayer();
                mpdPlayerAdapterIF.addSongChangeListener(new SongListener());
                mpdPlayerAdapterIF.addVolumeListener(new UiVolumeListener());

                mpdOptionsIF = mpdServiceAdapterIF.getOptions();
                mpdOptionsIF.addOptionsListener(new MyOptionsListener());
                for (MpDroidActivityWidget mpDroidActivityWidget : mpDroidActivityWidgetList) {
                    mpDroidActivityWidget.onConnect();
                }

                Log.v(TAG, "MPD Server version: " + mpdServiceAdapterIF.getServerVersion());
            }
            String connectedText = makeConnectedText(server, connected);
            Log.v(TAG, connectedText);
            return connected;
        }

        private String makeConnectedText(String server, boolean connected) {
            return connected ? "Connected to " + server + "!"
                    : "Unable to Connect to " + server + "!";
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            updateConnectedStatusOnUI(connected);
            if (!connected) {
                Toast.makeText(MpDroidActivity.this,
                        makeConnectedText(myPreferences.getServer(), connected), Toast.LENGTH_SHORT).show();
            }
        }

        private class MyMpdConnectionListenerIF implements MpdConnectionListenerIF {
            public void connectionStateUpdated(final boolean connected) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateConnectedStatusOnUI(connected);
                    }
                });
            }
        }

    }

    private class ButtonClickListener implements View.OnClickListener {
        public void onClick(View view) {
            if (view == connectButton) {
                boolean connected = mpdServiceAdapterIF.isConnected();
                if (connected) {
                    DisconnectTask disconnectTask = new DisconnectTask();
                    disconnectTask.execute();
                } else {
                    ConnectTask connectTask = new ConnectTask();
                    connectTask.execute();
                }
            } else if (view == repeatCheckbox) {
                AsyncTask<Void, Void, Void> toggleRepeatTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        mpdOptionsIF.toggleRepeat();
                        return null;
                    }
                };
                toggleRepeatTask.execute();
            } else if (view == randomCheckbox) {
                AsyncTask<Void, Void, Void> toggleRandomTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        mpdOptionsIF.toggleRandom();
                        return null;
                    }
                };
                toggleRandomTask.execute();
            }
        }
    }

    private class DisconnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... ignored) {
            mpdServiceAdapterIF.disconnect();
            return mpdServiceAdapterIF.isConnected();
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            updateConnectedStatusOnUI(connected);
        }
    }

    private class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int volume, boolean fromUser) {
            if (seekBar == volumeSeekBar && fromUser) {
                VolumeTask volumeTask = new VolumeTask(mpdPlayerAdapterIF);
                volumeTask.execute(volume);
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    }

    private class GetVolumeTask extends AsyncTask<Void, Object, Integer> {
        public GetVolumeTask() {
        }

        @Override
        protected Integer doInBackground(Void... objects) {
            return mpdPlayerAdapterIF.getVolume();
        }

        @Override
        protected void onPostExecute(Integer volume) {
            volumeSeekBar.setProgress(volume);
        }
    }

    void updateSongOnUI(MpdSongAdapterIF mpdSongAdapterIF) {
        String songName = mpdSongAdapterIF.getSongName();
        songNameTextView.setText(songName);
        String artist = mpdSongAdapterIF.getArtist();
        String album = mpdSongAdapterIF.getAlbumName();
        String date = mpdSongAdapterIF.getDate();
        StringBuilder details = new StringBuilder();
        if (artist != null) {
            details.append("by ").append(artist);
        }
        if (album != null) {
            details.append(" from ").append(album);
        }
        if (date != null) {
            details.append(" (").append(date).append(")");
        }
        songDetailsTextView.setText(details);
    }

    private class SongListener implements MpdSongListener {
        public void songUpdated(final MpdSongAdapterIF song) {
            Runnable runnable = new Runnable() {
                public void run() {
                    updateSongOnUI(new SongNameDecorator(song));
                }
            };
            runOnUiThread(runnable);
        }
    }


    private class UiVolumeListener implements MpdPlayerAdapterIF.MpdVolumeListener {
        public void volumeUpdated(final Integer volume) {
            Runnable runnable = new Runnable() {
                public void run() {
                    volumeSeekBar.setProgress(volume);
                }
            };
            runOnUiThread(runnable);
        }
    }


    private class MyOptionsListener implements OptionsListener {
        public void repeatUpdated(final boolean repeat) {
            Runnable runnable = new Runnable() {
                public void run() {
                    updateRepeat(repeat);
                }
            };
            runOnUiThread(runnable);
        }

        public void randomUpdated(final boolean newRandom) {
            Runnable runnable = new Runnable() {
                public void run() {
                    updateRandom(newRandom);
                }
            };
            runOnUiThread(runnable);
        }
    }
}
