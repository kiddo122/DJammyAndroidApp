package com.example.songsequencerapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity {
	public static final String KEY_STRING = "KEY";
	public static final String INSTRUMENT_STRING = "INSTRUMENT";
	public static boolean onTouch = false;
	public MediaPlayer synth_b5 = null;
	public MediaPlayer synth_d4 = null;
	public MediaPlayer synth_e4 = null;
	public MediaPlayer synth_fsharp4 = null;
	public MediaPlayer synth_a4 = null;
	public MediaPlayer synth_b4 = null;
	public SoundPool soundpool;
	int soundId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_game);

		// Set up a timer task. We will use the timer to check the
		// input queue every 500 ms
		TCPReadTimerTask tcp_task = new TCPReadTimerTask();
		Timer tcp_timer = new Timer();
		tcp_timer.schedule(tcp_task, 3000, 250);
		
		// Set up a timer task. We will use the timer to check the
				// input queue every 500 ms
		BPMTimerTask bpmTask = new BPMTimerTask();
		Timer bpm_timer = new Timer();
		bpm_timer.schedule(bpmTask, 210, 210);

		synth_b5 = MediaPlayer.create(getApplicationContext(), R.raw.synth_b5);
		synth_d4 = MediaPlayer.create(getApplicationContext(), R.raw.synth_d4);
		synth_e4 = MediaPlayer.create(getApplicationContext(), R.raw.synth_e4);
		synth_fsharp4 = MediaPlayer.create(getApplicationContext(),
				R.raw.synth_fsharp4);
		synth_a4 = MediaPlayer.create(getApplicationContext(), R.raw.synth_a4);
		synth_b4 = MediaPlayer.create(getApplicationContext(), R.raw.synth_b4);
		
		soundpool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		/** soundId for Later handling of sound pool **/
		soundId = soundpool.load(getApplicationContext(), R.raw.synth_b5, 1); // in 2nd param u have to pass your desire ringtone
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	private int getKeyPosition(float y_pos) {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		float key_size = size.y / GameView.DIVISIONS;
		return (int) (y_pos / key_size);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float y = event.getY();
		View game_view = findViewById(R.id.gameView1);
		int key_position = getKeyPosition(y);

		switch (event.getAction()) {
		case (MotionEvent.ACTION_DOWN):
			onTouch = true;
			GameView.touchPosition = key_position;
			game_view.invalidate();
			sendMessage(composeMessage(0, key_position));
			Log.d("MyApp", "Action was DOWN: " + GameView.touchPosition);
			return true;

		case (MotionEvent.ACTION_MOVE):
			GameView.touchPosition = key_position;
			game_view.invalidate();
			sendMessage(composeMessage(0, key_position));
			Log.d("MyApp", "Action was MOVE: " + getKeyPosition(y));
			return true;

		case (MotionEvent.ACTION_UP):
			onTouch = false;
			game_view.invalidate();
			Log.d("MyApp", "Action was UP");
			return true;

		default:
			return super.onTouchEvent(event);
		}
	}

	// Compose message before sending
	private String composeMessage(int instrument, int key) {
		JSONObject json = new JSONObject();

		try {
			json.put(INSTRUMENT_STRING, instrument);
			json.put(KEY_STRING, key);
		} catch (JSONException e) {
			Log.d("MyError", "Error putting in JSON!");
			e.printStackTrace();
		}

		return json.toString();
	}

	public void sendMessage(String msg) {
		MyApplication app = (MyApplication) getApplication();

		// Create an array of bytes. First byte will be the
		// message length, and the next ones will be the message
		byte buf[] = new byte[msg.length() + 1];
		buf[0] = (byte) msg.length();
		System.arraycopy(msg.getBytes(), 0, buf, 1, msg.length());

		// Now send through the output stream of the socket
		OutputStream out;
		try {
			out = app.sock.getOutputStream();
			try {
				out.write(buf, 0, msg.length() + 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Used to receive message from the middleman/DE2
	public class TCPReadTimerTask extends TimerTask {
		public void run() {
			MyApplication app = (MyApplication) getApplication();
			if (app.sock != null && app.sock.isConnected()
					&& !app.sock.isClosed()) {

				try {
					InputStream in = app.sock.getInputStream();

					// See if any bytes are available from the Middleman
					int bytes_avail = in.available();
					if (bytes_avail > 0) {

						// If so, read them in and create a sring
						byte buf[] = new byte[bytes_avail];
						in.read(buf);

						final String s = new String(buf, 0, bytes_avail,
								"US-ASCII").substring(1, bytes_avail);
						Log.d("MyMessage", "Received: " + s);
						JSONObject json = new JSONObject(s);

						Log.d("MyMessage",
								"Instrument: "
										+ json.getString(INSTRUMENT_STRING)
										+ " Key: " + json.getString(KEY_STRING));
						// PLAY SOUND HERE

					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d("MyError", "String to JSON conversion error!");
					e.printStackTrace();
				}
			}
		}
	}

	// Used to receive message from the middleman/DE2
	public class BPMTimerTask extends TimerTask {
		public void run() {
			Log.d("BPMTimerTask", "Playing note");
			if (onTouch == true) {
				playSound(GameView.touchPosition);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void playSound(int touchPosition) {
		Log.d("PlaySound", "Key Pressed " + touchPosition);
		switch (touchPosition) {
		case 0:
			playSoundFromMediaPlayer(synth_b5);
			break;
		case 1:
			playSoundFromMediaPlayer(synth_d4);
			break;
		case 2:
			playSoundFromMediaPlayer(synth_e4);
			break;
		case 3:
			playSoundFromMediaPlayer(synth_fsharp4);
			break;
		case 4:
			playSoundFromMediaPlayer(synth_a4);
			break;
		case 5:
			playSoundFromMediaPlayer(synth_b4);
			break;
		default:
			Log.d("PlaySound", "Redundant Key Pressed "
					+ GameView.touchPosition);
			break;
		}
	}

	public void playSoundFromMediaPlayer(MediaPlayer mediaPlayer) {
		mediaPlayer.start();
		// mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
		// public void onCompletion(MediaPlayer mp) {
		// mp.release();
		// };
		// });
	}
}