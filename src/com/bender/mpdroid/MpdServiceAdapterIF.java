package com.bender.mpdroid;

/**
 * An adapter interface to control the mpd server.
 */
public interface MpdServiceAdapterIF
{
    void connect(String server, int port, String password);

    void connect(String server, int port);

    void connect(String server);

    void disconnect();

    boolean isConnected();

    String getServerVersion();

    MpdPlayerAdapterIF getPlayer();

    MpdPlaylistAdapterIF getPlaylist();
}
