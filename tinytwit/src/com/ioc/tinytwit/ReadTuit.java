package com.ioc.tinytwit;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class ReadTuit implements TextToSpeech.OnInitListener{
	private static Locale local = null;
	private int result=0;
	TextToSpeech tts;
	
	
	public ReadTuit(Context con, OnInitListener OnIn){
		tts = new TextToSpeech(con, OnIn);
		local = new Locale("spa", "ESP");
		tts.setLanguage(local);
		//tts.speak("Text to say aloud", TextToSpeech.QUEUE_ADD, null);
	}

	
	public void onIn() {
		
	        //set Language
	        result = tts.setLanguage(local);
	        // tts.setPitch(5); // set pitch level
	        // tts.setSpeechRate(2); // set speech speed rate
	        if (result == TextToSpeech.LANG_MISSING_DATA
	                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	        	Log.d("", "Language not supported");
	        } else {
	            
	            speakOut();
	        }
	    
		
	}
	
	private void speakOut() {
		String text;
		
	    if(result!=tts.setLanguage(local))
	    {
	    Toast.makeText( null, "Hi Please enter the right Words......  ", Toast.LENGTH_LONG).show();
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
	
	public void para(){
		if (tts.isSpeaking()){
			tts.stop();
		}else {
			
		}
		
	}


	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}

}
