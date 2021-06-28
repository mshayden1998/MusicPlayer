package projeto_final.mshayden.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by User on 14/12/2016.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
                                                     MediaPlayer.OnErrorListener,
                                                     MediaPlayer.OnCompletionListener{

    private boolean shuffle=false;
    private Random rand;
    private String songTitle="";
    private String songArtist="";
    private static final int NOTIFY_ID=1;
    // Media player
    private MediaPlayer player;
    // Song list
    private ArrayList<Song> songs;
    // Current position
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        player.reset();
        songPosn--;
        if (songPosn < 0) songPosn=songs.size()-1;
        playSong();
    }

    public void playNext(){
        player.reset();
        songPosn++;
        if (songPosn >= songs.size()) songPosn=0;
        playSong();

        if (shuffle){
            int newSong = songPosn;
            while (newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }else {
            songPosn++;
            if (songPosn>=songs.size()) songPosn=0;
        }playSong();
    }// Fecha playNext

    public void onCreate(){
        // Referências
        rand=new Random();

        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();

        initMediaPlayer();
    }// Fecha onCreate

    public void setShuffle(){
        if (shuffle) shuffle=false;
        else shuffle=true;
    }

    public void initMediaPlayer(){
        // propriedades do player
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }// Fecha initMediaPlayer

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }// Fecha setList

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }// Fecha musicBinder

    public void playSong(){
        player.reset();
        // pegar som
        Song playSong = songs.get(songPosn);
        songTitle=playSong.getTitle();
        songArtist=playSong.getArtist();
        // pegar ID
        long currSong = playSong.getId();
        // set uri
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        }catch (Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }// Fecha playSong

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition()>0){
            player.reset();
            playNext();
        }
    }// Fecha onCompletion

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        player.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // start playBack
        player.start();

        Intent notIntent = new Intent(this, ListViewActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songArtist)
                .setOngoing(true)
                .setContentTitle(songTitle)
                .setContentText(songArtist);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
    }
}// Fecha classe MusicService
