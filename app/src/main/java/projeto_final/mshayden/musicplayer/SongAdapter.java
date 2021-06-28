package projeto_final.mshayden.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by User on 14/12/2016.
 */

public class SongAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Map to song layout
        LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.activity_song, parent, false);

        // Pegar title e artist views
        TextView songView = (TextView) songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
        TextView durationView = (TextView) songLay.findViewById(R.id.song_duration);

        // Pegar song usando position
        Song currentSong = songs.get(position);

        // Pegar título, artista e duração em strings
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());
        durationView.setText(currentSong.getDuration());

        // Set position as tag
        songLay.setTag(position);

        return songLay;
    }

    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        this.songs = theSongs;
        this.songInf = LayoutInflater.from(c);
    }// Fecha SongAdapter
}
