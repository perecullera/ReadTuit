package com.ioc.tinytwit;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ReadTweet extends Activity implements TextToSpeech.OnInitListener {
	
	private int result=0;
	TextToSpeech tts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_tweet);
		tts = new TextToSpeech(this, this);
		tts.setLanguage(Locale.ITALIAN);
		tts.speak("Text to say aloud", TextToSpeech.QUEUE_ADD, null);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.read_tweet, menu);
		return true;
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_read_tweet,
					container, false);
			return rootView;
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
	    if (status == TextToSpeech.SUCCESS) {
	        //set Language
	        result = tts.setLanguage(Locale.US);
	        // tts.setPitch(5); // set pitch level
	        // tts.setSpeechRate(2); // set speech speed rate
	        if (result == TextToSpeech.LANG_MISSING_DATA
	                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	        } else {
	            
	            speakOut();
	        }
	    } else {
	        Log.e("TTS", "Initilization Failed");
	    }
		
	}

	private void speakOut() {
		String text = "Hola world";
		
	    if(result!=tts.setLanguage(Locale.US))
	    {
	    Toast.makeText(getApplicationContext(), "Hi Please enter the right Words......  ", Toast.LENGTH_LONG).show();
	    }else
	    {
	    	for (int i = 0; i< MainActivity.tweetString.size(); i++){
	    		text = MainActivity.tweetString.get(i);
	    		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	    		while(tts.isSpeaking()) {
	    		      Log.d("", "speaking");
	    		}
			}
	    
	    }
		
	}

}
