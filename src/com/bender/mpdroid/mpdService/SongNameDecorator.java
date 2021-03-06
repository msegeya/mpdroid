package com.bender.mpdroid.mpdService;

public class SongNameDecorator implements MpdSongAdapterIF {
    private MpdSongAdapterIF implementation;

    public SongNameDecorator(MpdSongAdapterIF implementation) {
        this.implementation = implementation;
    }

    public String getSongName() {
        String name = implementation.getSongName();
        if (name == null) {
            name = implementation.getFile();
            if (name == null) return "<unknown song name>";
            name = name.substring(name.lastIndexOf('/') + 1, name.length());
            int endIndex = name.lastIndexOf('.');
            if (endIndex != -1) {
                name = name.substring(0, endIndex);
            }
        }
        return name;
    }

    public String getArtist() {
        return implementation.getArtist();
    }

    public String getAlbumName() {
        return implementation.getAlbumName();
    }

    public String getFile() {
        return implementation.getFile();
    }

    public String getDate() {
        return implementation.getDate();
    }

    public Integer getSongLength() {
        return implementation.getSongLength();
    }

    public int getId() {
        return implementation.getId();
    }

    @Override
    public String toString() {
        return getSongName();
    }
}
