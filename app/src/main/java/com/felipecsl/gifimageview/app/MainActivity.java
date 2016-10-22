package com.felipecsl.gifimageview.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.felipecsl.gifimageview.library.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private static final String TAG = "MainActivity";
  private GifImageView gifImageView;
  private Button btnToggle;
  private Button btnBlur;
  private boolean shouldBlur = false;
  private Blur blur;
  MediaPlayer mPlayer;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    gifImageView = (GifImageView) findViewById(R.id.gifImageView);
    btnToggle = (Button) findViewById(R.id.btnToggle);
    btnBlur = (Button) findViewById(R.id.btnBlur);
    final Button btnClear = (Button) findViewById(R.id.btnClear);

    blur = Blur.newInstance(this);
    gifImageView.setOnFrameAvailable(new GifImageView.OnFrameAvailable() {
      @Override
      public Bitmap onFrameAvailable(Bitmap bitmap) {
        if (shouldBlur) {
          return blur.blur(bitmap);
        }
        return bitmap;
      }
    });

    gifImageView.setOnAnimationStop(new GifImageView.OnAnimationStop() {
      @Override public void onAnimationStop() {
        runOnUiThread(new Runnable() {
          @Override public void run() {
            Toast.makeText(MainActivity.this, "Animation stopped", Toast.LENGTH_SHORT).show();
          }
        });
      }
    });

    btnToggle.setOnClickListener(this);
    btnClear.setOnClickListener(this);
    btnBlur.setOnClickListener(this);

    new GifDataDownloader() {
      @Override protected void onPostExecute(final byte[] bytes) {
        gifImageView.setBytes(bytes);
        gifImageView.startAnimation();
        playSound();
        Log.d(TAG, "GIF width is " + gifImageView.getGifWidth());
        Log.d(TAG, "GIF height is " + gifImageView.getGifHeight());
      }
    }.execute("http://4.bp.blogspot.com/-82Rob0nGCL8/VIRRGqztAqI/AAAAAAAAYfU/XSdVyJJw76s/s1600/frosch075.gif");

  }

  void playSound() {
    if (mPlayer != null) {
      mPlayer.stop();
      mPlayer.reset();
    }

    mPlayer = MediaPlayer.create(MainActivity.this, R.raw.frog);
    mPlayer.setLooping(true);
    mPlayer.start();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public void onDestroy() {
    // Stop the sound
    if (mPlayer != null) {
      mPlayer.stop();
      mPlayer = null;
    }

    super.onDestroy();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.show_grid) {
      startActivity(new Intent(this, GridViewActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onClick(final View v) {
    if (v.equals(btnToggle)) {
      if (gifImageView.isAnimating()) {
        v.setBackgroundResource(R.drawable.playfilled);
        if (mPlayer != null) {
          mPlayer.pause();
        }
        gifImageView.stopAnimation();
      }
      else{
        gifImageView.startAnimation();
        if (mPlayer != null) {
          mPlayer.start();
        }
        v.setBackgroundResource(R.drawable.pausefilled);
      }

    } else if (v.equals(btnBlur)) {
      shouldBlur = !shouldBlur;
    } else {
      gifImageView.clear();
    }
  }
}
