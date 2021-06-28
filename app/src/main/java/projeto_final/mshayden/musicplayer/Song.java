package projeto_final.mshayden.musicplayer;

/**
 * Created by User on 14/12/2016.
 */

public class Song {
    private long id;
    private String title;
    private String artist;
    private String duration;

    // Método construtor
    public Song(long SongId, String SongTitle, String SongArtist, String SongDuration) {
        this.id = SongId;
        this.title = SongTitle;
        this.artist = SongArtist;
        this.duration = SongDuration;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() { return duration; }
}// Fecha classe Song
