package com.ioc.tinytwit;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Tweet extends Activity implements OnClickListener {

	private SharedPreferences prefs;
	private TextView username;
	private Button ok_tweet, cancel_tweet;
	private Twitter twitter;
	private TwitterFactory factory;
	private AccessToken accessToken;
	private EditText tweet_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);

		ok_tweet = (Button) findViewById(R.id.okTweet);
		cancel_tweet = (Button) findViewById(R.id.cancelTweet);
		tweet_text = (EditText) findViewById(R.id.postTweet);

		ok_tweet.setOnClickListener(this);
		cancel_tweet.setOnClickListener(this);

		prefs = getSharedPreferences("FitxerPreferences", MODE_PRIVATE);

		// Fem la comprovació no sigui que arribi aquí sense haver fet login
		if (prefs.getBoolean("userLogged", false)) {

			// Creem les instàncies de Twitter i de TwitterFactory a partir del que tenim guardat a les preferències

			factory = new TwitterFactory(MainActivity.newConfiguration());
			accessToken = new AccessToken(prefs.getString("OAUTH_TOKEN", ""),
					prefs.getString("OAUTH_TOKEN_SECRET", ""));
			twitter = factory.getInstance(accessToken);
			
			// Posem el nom d'usuari 
			username = (TextView) findViewById(R.id.username);
			username.setText("@" + prefs.getString("SCREEN_NAME", ""));

		}

		else {
			missatge("Què fas aquí? Fóra!");
			this.finish();
		}

	}

	public void publish_tweet() {

		// Comprovem que hi ha el nombre correcte de caràcters.
		if (tweet_text.length() == 0 || tweet_text.length() > 140)
			
			missatge("Error! Un tweet ha de tenir entre 1 i 140 caràcters!");
		
		else {
			new PublishTweet().execute();
			finish();
		}

	}
	
	// Per mostra la informació per pantalla
	public void missatge(String msg) {

		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onClick(View v) {
		// Gestió dels bottons

		switch (v.getId()) {
		case R.id.okTweet:
			// Comprovació de la connectivitat
			if(MainActivity.comprovaConnexio()){
				publish_tweet();
			}
			else missatge(getResources().getString(R.string.errorConnexio));
			break;
		case R.id.cancelTweet:
			finish();
			break;
		default:
			break;
		}

	}

	class PublishTweet extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			try {
				// Actualitzem l'estat.
				twitter.updateStatus(tweet_text.getText().toString());
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

}
