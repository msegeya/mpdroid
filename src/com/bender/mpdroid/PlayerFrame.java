package com.bender.mpdroid;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.bender.mpdroid.mpdService.MpdPlayerAdapterIF;

/**
 */
public class PlayerFrame implements MpDroidActivityWidget
{
    private ImageButton playButton;
    private Button stopButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private MpDroidActivity activity;
    private MpdPlayerAdapterIF mpdPlayerAdapterIF;

    public void onCreate(MpDroidActivity activity)
    {
        this.activity = activity;
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        playButton = (ImageButton) activity.findViewById(R.id.play);
        stopButton = (Button) activity.findViewById(R.id.stop);
        nextButton = (ImageButton) activity.findViewById(R.id.next);
        prevButton = (ImageButton) activity.findViewById(R.id.prev);
        playButton.setOnClickListener(buttonClickListener);
        stopButton.setOnClickListener(buttonClickListener);
        nextButton.setOnClickListener(buttonClickListener);
        prevButton.setOnClickListener(buttonClickListener);
    }

    public void onConnect()
    {
        mpdPlayerAdapterIF = activity.getMpdServiceAdapterIF().getPlayer();
        mpdPlayerAdapterIF.addPlayStatusListener(new PlayListener());
        updatePlayStatusOnUI(mpdPlayerAdapterIF.getPlayStatus());
    }

    public void onConnectionChange(boolean connected)
    {
        int visibility = connected ? View.VISIBLE : View.INVISIBLE;
        stopButton.setEnabled(connected);
        playButton.setVisibility(visibility);
        stopButton.setVisibility(visibility);
        nextButton.setVisibility(visibility);
        prevButton.setVisibility(visibility);

    }

    private void updatePlayStatusOnUI(final MpdPlayerAdapterIF.PlayStatus playStatus)
    {
        if (playStatus == null)
        {
            throw new NullPointerException();
        }
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                switch (playStatus)
                {
                    case Playing:
                        playButton.setImageResource(android.R.drawable.ic_media_pause);
                        break;
                    case Paused:
                    default:
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        };
        activity.runOnUiThread(runnable);
    }

    private class ButtonClickListener implements View.OnClickListener
    {
        public void onClick(View view)
        {
            if (view == playButton)
            {
                PlayTask playTask = new PlayTask(mpdPlayerAdapterIF);
                playTask.execute();
            } else if (view == stopButton)
            {
                StopTask stopTask = new StopTask(mpdPlayerAdapterIF);
                stopTask.execute();
            } else if (view == nextButton)
            {
                NextTask nextTask = new NextTask(mpdPlayerAdapterIF);
                nextTask.execute();
            } else if (view == prevButton)
            {
                PrevTask prevTask = new PrevTask(mpdPlayerAdapterIF);
                prevTask.execute();
            }
        }
    }

    private class PlayListener implements MpdPlayerAdapterIF.MpdPlayStatusListener
    {
        public void playStatusUpdated(final MpdPlayerAdapterIF.PlayStatus playStatus)
        {
            updatePlayStatusOnUI(playStatus);
        }
    }

    private static class PlayTask extends AsyncTask<Void, Void, Void>
    {
        private MpdPlayerAdapterIF mpdPlayerAdapterIF;

        public PlayTask(MpdPlayerAdapterIF mpdPlayerAdapterIF)
        {
            this.mpdPlayerAdapterIF = mpdPlayerAdapterIF;
        }

        @Override
        protected Void doInBackground(Void... objects)
        {
            mpdPlayerAdapterIF.playOrPause();
            return null;
        }
    }

    private static class NextTask extends AsyncTask<Void, Void, Void>
    {
        private MpdPlayerAdapterIF mpdPlayerAdapterIF;

        NextTask(MpdPlayerAdapterIF mpdPlayerAdapterIF)
        {
            this.mpdPlayerAdapterIF = mpdPlayerAdapterIF;
        }

        @Override
        protected Void doInBackground(Void... objects)
        {
            mpdPlayerAdapterIF.next();
            return null;
        }
    }

    private static class PrevTask extends AsyncTask<Void, Void, Void>
    {

        private MpdPlayerAdapterIF mpdPlayerAdapterIF;

        public PrevTask(MpdPlayerAdapterIF mpdPlayerAdapterIF)
        {
            this.mpdPlayerAdapterIF = mpdPlayerAdapterIF;
        }

        @Override
        protected Void doInBackground(Void... objects)
        {
            mpdPlayerAdapterIF.prev();
            return null;
        }
    }

    private static class StopTask extends AsyncTask<Void, Void, Void>
    {

        private MpdPlayerAdapterIF mpdPlayerAdapterIF;

        public StopTask(MpdPlayerAdapterIF mpdPlayerAdapterIF)
        {
            this.mpdPlayerAdapterIF = mpdPlayerAdapterIF;
        }

        @Override
        protected Void doInBackground(Void... objects)
        {
            mpdPlayerAdapterIF.stop();
            return null;
        }
    }
}
