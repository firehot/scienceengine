package com.mazalearn.scienceengine;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
 
public class VideoPlayer extends Activity
   implements OnCompletionListener {
 
    private VideoView mVideoView;
 
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Bundle data = getIntent().getExtras();
        String videoFileName = data.getString("com.mazalearn.scienceengine.FileName");
        // Bring the video player to the front
        setContentView(R.layout.videoplayer);
        // Get a handle on the VideoView
        mVideoView =
            (VideoView)findViewById(R.id.surfacevideoview);
        // Load in the video file
        mVideoView.setVideoPath(videoFileName);
        // Enable controller
        mVideoView.setMediaController(new MediaController(this));
        // Handle when the video finishes playing
        mVideoView.setOnCompletionListener(this);
        // Start playing the video
        mVideoView.start();
    }
 
    @Override
    public void onCompletion(MediaPlayer mp) {
        // The video has finished, return from this activity
        finish();
    }
}