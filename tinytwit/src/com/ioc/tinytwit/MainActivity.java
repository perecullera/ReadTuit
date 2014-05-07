package com.ioc.tinytwit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnInitListener {

	// Constants personals, modifiqueu per les vostres dades!
	protected static final String TWITTER_API_KEY = "3osOY45EfHgAaM7ancZUlg";
	protected static final String TWITTER_API_SECRET = "cNc5PDqKMU0SAF1RRHUUgpaJ5UNjv54AEl3v51ricoI";

	// Constants
	protected static final String CALLBACK = "tinytwit:///";
	protected static final String tag = "tinytwit";

	// Gestió de les preferències
	private SharedPreferences prefs;

	// Twitter variables
	private Twitter twitter;
	private TwitterFactory factory;
	static List<String> tweetString;
	private RequestToken rqToken;
	private AccessToken accessToken;

	private String verifier;

	// Vistes
	private Button login;
	private ListView feed;
	private TextView user;
	private ProgressBar updateBar;

	// Altres
	private boolean menuVisibility;
	protected static ConnectivityManager connMgr;
	private DBInterface db;
	
	// tts
	private int result=0;
	TextToSpeech tts;
	int isPlaying = 0;
	ReadTuit Rt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Rt = new ReadTuit(this,this);
		
			/*tts = new TextToSpeech(this, this);
			tts.setLanguage(Locale.ITALIAN);
			tts.speak("Text to say aloud", TextToSpeech.QUEUE_ADD, null);
			 */
		
		// Inicialitzem el connectivity manager.
		connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		// Instanciem la BD
		db = new DBInterface(this);

		// Obtenim l'objecte SharedPreferences
		prefs = getSharedPreferences("FitxerPreferences", MODE_PRIVATE);

		// Inicialització listeners
		login = (Button) findViewById(R.id.login_button);
		login.setOnClickListener(this);

		// L'array que contindrà els tweets
		tweetString = new ArrayList<String>();

		// Altres vistes
		user = (TextView) findViewById(R.id.user);
		updateBar = (ProgressBar) findViewById(R.id.updateBar);

		// Si l'usuari no ha fet login
		if (!prefs.getBoolean("userLogged", false)) {

			// Oculta menú
			login.setText(getResources().getString(R.string.login));
			menuVisibility = false;
			invalidateOptionsMenu();

		} else {

			login.setText(getResources().getString(R.string.logout));

			// Creem les instàncies de Twitter i de TwitterFactory
			factory = new TwitterFactory(newConfiguration());
			accessToken = new AccessToken(prefs.getString("OAUTH_TOKEN", ""),
					prefs.getString("OAUTH_TOKEN_SECRET", ""));
			twitter = factory.getInstance(accessToken);
			menuVisibility = true;
			invalidateOptionsMenu();

		}

	}

	// True si tenim connexió, False en cas contrari.
	public static Boolean comprovaConnexio() {

		// Obtenim l'estat de la xarxa mòbil
		NetworkInfo networkInfo = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean connectat3G = networkInfo.isConnected();

		// Obtenim l'estat de la xarxaWi-Fi
		networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean connectatWifi = networkInfo.isConnected();

		// O bé hem de tenir 3G o bé wifi
		return connectat3G || connectatWifi;

	}

	@Override
	protected void onStart() {

		// Quan tornem a carregar l'aplicació, torna a emplenar la informació
		tweetString = new ArrayList<String>();
		if (prefs.getBoolean("userLogged", false)) {
			user.setText("@" + prefs.getString("SCREEN_NAME", ""));
			feedUpdate();
		}
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		// Agafem els MenuItem
		MenuItem refresh = menu.findItem(R.id.action_refresh);
		MenuItem post = menu.findItem(R.id.action_post);
		MenuItem read = menu.findItem(R.id.action_read);
		
		// S'han de veure els elements del menú?
		refresh.setVisible(menuVisibility);
		post.setVisible(menuVisibility);
		post.setVisible(menuVisibility);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_refresh:

			if (comprovaConnexio()) {
				// Obtenim el timeline
				new FeedAsyncTask().execute();
			} else
				// No tenim connexió, error
				missatge(getResources().getString(R.string.errorConnexio));
			return true;

		case R.id.action_post:
			// Obrim la pantalla de post
			Intent intent = new Intent(this, Tweet.class);
			startActivity(intent);
			return true;
			
		case R.id.action_read:
			if (isPlaying == 0){
				
				Rt.onIn();
				while (Rt.tts.isSpeaking()){
					
				}
				isPlaying = 1;
			}else if (isPlaying ==1){
				Rt.tts.stop();
				
			}
			

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Quan ens retorna la informació Twitter
	@Override
	protected void onNewIntent(Intent intent) {

		if (!prefs.getBoolean("userLogged", false)) {
			Uri uri = intent.getData();
			// Si no és null i comença amb "tinytwit:///"
			if (uri != null && uri.toString().startsWith(CALLBACK)) {

				// Guardem el verificador
				verifier = uri.getQueryParameter("oauth_verifier");

				// AsyncTask per obtenir l'AccesToken
				new AccessTokenAsyncTask().execute();
			}
		}
	}

	// Mètode per connectar a twitter
	private void twitter_connect() {

		factory = new TwitterFactory(newConfiguration());
		twitter = factory.getInstance();

		// Obtenim l'AuthToken des de la xarxa (s'ha de fer amb AsyncTask
		new TwitterAsyncTask().execute();

	}

	// Si ja hem fet login
	public void twitter_logout() {

		// Canviem el text del botó
		login.setText(getResources().getString(R.string.login));

		// Actualitzem les preferències, esborrem el token i el token_secret
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("userLogged", false);
		editor.putString("OAUTH_TOKEN", "");
		editor.putString("OAUTH_TOKEN_SECRET", "");
		editor.putString("SCREEN_NAME", "");
		editor.commit();

		// Eliminem la informació de l'usuai i eliminem els objectes de twitter
		user.setText("");
		accessToken = null;
		twitter = null;

		// Esborrem la BD per a que ningú pugui accedir als tweets.
		db.obre();
		db.esborraTaula();
		db.tanca();

		// Forcem l'actualització del menú i fem que no sigui accessible
		menuVisibility = false;
		invalidateOptionsMenu();

		// Esborrem la llista i ocultem el feed
		tweetString.clear();
		feed.setVisibility(View.GONE);

	}

	// Mètode per generar la configuració.
	protected static Configuration newConfiguration() {

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(TWITTER_API_KEY);
		configurationBuilder.setOAuthConsumerSecret(TWITTER_API_SECRET);
		Configuration configuration = configurationBuilder.build();
		return configuration;

	}

	public void feedUpdate() {

		updateBar.setVisibility(View.VISIBLE);

		if (prefs.getBoolean("userLogged", false) && comprovaConnexio()
				&& db.isEmpty()) {

			new FeedAsyncTask().execute();

		} else {

			db.obre();
			Cursor dbTw = db.obtenirTotsElsTweets();
			tweetString.clear();

			// Movem el cursor a la primera posició
			if (dbTw.moveToFirst()) {
				do {
					// Mostrem el nom seguit del tweet
					tweetString.add(dbTw.getString(1) + " - "
							+ dbTw.getString(3));

				} while (dbTw.moveToNext());
			}
			// Tanquem la BD
			db.tanca();
			feed = (ListView) findViewById(R.id.listView1);
			// Preparem l'adapter de la llista
			feed.setAdapter(new ArrayAdapter<String>(MainActivity.this,
					R.layout.element_llista, tweetString));
			// Mostrem el feed
			feed.setOnItemClickListener(MainActivity.this);
			feed.setVisibility(View.VISIBLE);
			// Ocultem la barra
			updateBar.setVisibility(View.GONE);

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
		case R.id.login_button:
			if (login.getText() == getResources().getString(R.string.logout)) {
				twitter_logout();

			} else if (comprovaConnexio()) {
				twitter_connect();
			} else {
				missatge(getResources().getString(R.string.errorConnexio));
			}
			break;
		default:
			break;
		}

	}

	// Per obtenir el AuthToken
	class TwitterAsyncTask extends AsyncTask<Void, Void, RequestToken> {

		@Override
		protected RequestToken doInBackground(Void... arg0) {

			try {

				rqToken = twitter.getOAuthRequestToken(CALLBACK);

			} catch (TwitterException e) {

				Log.d(tag, e.getErrorMessage());
				rqToken = null;
			}
			return rqToken;
		}

		@Override
		protected void onPostExecute(RequestToken rqToken) {

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rqToken
					.getAuthenticationURL()));
			startActivity(intent);

		}

	}

	// Obtenció de l'AccessToken
	class AccessTokenAsyncTask extends AsyncTask<Void, Void, AccessToken> {

		@Override
		protected AccessToken doInBackground(Void... arg0) {

			try {

				accessToken = twitter.getOAuthAccessToken(rqToken, verifier);

			} catch (TwitterException e) {

				Log.d(tag, e.getErrorMessage());
				accessToken = null;
			}
			return accessToken;
		}

		@Override
		protected void onPostExecute(AccessToken AccToken) {

			if (AccToken != null) {

				user.setText("@" + AccToken.getScreenName());
				login.setText(getResources().getString(R.string.logout));
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("userLogged", true);
				editor.putString("OAUTH_TOKEN", AccToken.getToken());
				editor.putString("OAUTH_TOKEN_SECRET",
						AccToken.getTokenSecret());
				editor.putString("SCREEN_NAME", AccToken.getScreenName());
				editor.commit();
				menuVisibility = true;
				invalidateOptionsMenu();
				feedUpdate();
			}

		}
	}

	// Obtenció del feed de tweets
	class FeedAsyncTask extends
			AsyncTask<Void, Void, ResponseList<twitter4j.Status>> {

		@Override
		protected void onPreExecute() {
			// Posem a visible la barra
			updateBar.setVisibility(View.VISIBLE);

		}

		@Override
		protected ResponseList<twitter4j.Status> doInBackground(Void... params) {

			ResponseList<twitter4j.Status> tws;
			tws = null;

			// Demanem 50
			Paging pag = new Paging(1, 50);

			try {

				tws = twitter.getHomeTimeline(pag);

			} catch (TwitterException e) {
				Log.d(tag, e.getErrorMessage());
			}

			return tws;

		}

		@Override
		protected void onPostExecute(ResponseList<twitter4j.Status> llista) {

			if (llista != null) {
				// Esborrem l'anterior llista
				tweetString.clear();

				db.obre();
				db.esborraTaula();
				String url;

				// Per cadascun dels estatus, el guardarem a la BD i el posarem
				// a la llista.
				for (twitter4j.Status item : llista) {

					if (item.getURLEntities().length != 0) {

						url = item.getURLEntities()[0].getExpandedURL();

					} else
						url = null;
					// Inserim a la BD
					db.insereixTweet(item.getUser().getScreenName(), item
							.getUser().getName(), item.getText(), item
							.getCreatedAt(), item.getRetweetCount(), item
							.getFavoriteCount(), url);

					// Afegim tots els elements a la llista temporal.
					tweetString.add(item.getUser().getName() + " - "
							+ item.getText());

				}

				db.tanca();

				feed = (ListView) findViewById(R.id.listView1);
				// Preparem l'adapter de la llista
				feed.setAdapter(new ArrayAdapter<String>(MainActivity.this,
						R.layout.element_llista, tweetString));
				// Mostrem el feed
				feed.setOnItemClickListener(MainActivity.this);
				feed.setVisibility(View.VISIBLE);
			} else {

				Toast.makeText(MainActivity.this, "Rate limit exceded!",
						Toast.LENGTH_LONG).show();

			}
			// Ocultem la barra
			updateBar.setVisibility(View.GONE);

		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// Obrim la BD
		db.obre();
		// Sumem 1 ja que a la base de dades es comença des de l'1 i l'índex de
		// la llista amb 0.
		Cursor c = db.obtenirTweet(arg2 + 1);

		// Fer crida a la següent activity per mostrar l'ítem
		Intent intent = new Intent(this, ShowTweet.class);
		intent.putExtra("user", c.getString(c.getColumnIndex("user")));
		intent.putExtra("user_complet",
				c.getString(c.getColumnIndex("user_complet")));
		intent.putExtra("tweet", c.getString(c.getColumnIndex("tweet")));
		intent.putExtra("data", c.getString(c.getColumnIndex("data")));
		intent.putExtra("retweets", c.getString(c.getColumnIndex("retweets")));
		intent.putExtra("fav", c.getString(c.getColumnIndex("fav")));
		intent.putExtra("url", c.getString(c.getColumnIndex("url")));

		startActivity(intent);

	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}
	
	/*@Override
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
		String text;
		
	    if(result!=tts.setLanguage(Locale.US))
	    {
	    Toast.makeText(getApplicationContext(), "Hi Please enter the right Words......  ", Toast.LENGTH_LONG).show();
	    }else
	    {
	    	for (int i = 0; i< MainActivity.tweetString.size(); i++){
	    		text = MainActivity.tweetString.get(i);
	    		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
			}
	    
	    }
		
	}*/

}
