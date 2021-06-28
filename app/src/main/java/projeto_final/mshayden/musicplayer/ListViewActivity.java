package projeto_final.mshayden.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.content.ServiceConnection;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import projeto_final.mshayden.musicplayer.MusicService.MusicBinder;

public class ListViewActivity extends AppCompatActivity implements MediaPlayerControl{

    // Widgets
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicController controller;
    private MusicService musicServ;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setController(){
        controller = new MusicController(this);
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.listView_sounds));
        controller.setEnabled(true);
    }

    public void getSongList(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            // Adicionar canções a lista
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDuration = musicCursor.getString(durationColumn);

                songList.add(new Song(thisId, thisTitle, thisArtist, thisDuration));
            }while (musicCursor.moveToNext());
        }// Fecha if (musicCursor!=...)
    }// Fecha getSongList

    // Tocar próxima
    private void playNext(){
        musicServ.playNext();
        if (playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
    // Tocar anterior
    private void playPrev(){
        musicServ.playPrev();
        if (playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        // Referências
        songView = (ListView) findViewById(R.id.listView_sounds);
        songList = new ArrayList<Song>();

        getSongList();
        setController();

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               playNext();
            }
        }/* Fecha controller.setPrevNextListeners... */, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });// Fecha new View.OnClickList...

    }// Fecha onCreate

    // Conectar ao MusicService
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicServ = binder.getService();
            //pass list
            musicServ.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    }; // Fecha conectar MusicService

    @Override
    public void onStart(){
        super.onStart();
        if (playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public void start() {
        musicServ.go();
    }

    public void songPicked(View view){
        musicServ.setSong(Integer.parseInt(view.getTag().toString()));
        musicServ.playSong();
        if (playbackPaused){
            setController();
            playbackPaused=false;
        }controller.show(0);
    }// Fecha songPicked

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_shuffle:
                musicServ.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicServ=null;
                System.exit(0);
                break;
        }return super.onOptionsItemSelected(item);
    }// Fecha boolean onOptionsItemSelected

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicServ=null;
        super.onDestroy();
    }

    @Override
    public void onPause(){
        super.onPause();
        paused=true;
    }// Fecha onPause

    @Override
    public void onResume(){
        super.onResume();
        if (paused){
            setController();
            paused=false;
        }
    }// Fecha onResume

    @Override
    public void onStop(){
        controller.hide();
        super.onStop();
    }// Fecha onStop

    @Override
    public void pause() {
        playbackPaused = true;
        musicServ.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicServ!=null && musicBound && musicServ.isPng())
            return musicServ.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicServ!=null && musicBound && musicServ.isPng())
            return musicServ.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicServ.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicServ!=null && musicBound)
        return musicServ.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}// Fecha classe ListViewActivity
