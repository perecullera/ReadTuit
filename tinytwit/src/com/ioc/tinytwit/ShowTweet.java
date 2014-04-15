package com.ioc.tinytwit;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

public class ShowTweet extends Activity {

	// TextViews
	private TextView user_complet, favs_count, rt_count, tweet_data;

	// EditText de mostra del tweet
	private EditText tweet;

	// Webview on es mostrarà la URL si existeix.
	private WebView web;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_tweet);

		user_complet  = (TextView) findViewById(R.id.showUserComplet);
		favs_count  = (TextView) findViewById(R.id.showFavs);
		rt_count  = (TextView) findViewById(R.id.showRT);
		tweet_data  = (TextView) findViewById(R.id.showData);
		
		tweet = (EditText) findViewById(R.id.showTweet);

		web = (WebView) findViewById(R.id.webView);

		String url;

		Bundle extras = getIntent().getExtras();

		if (extras != null) {

			// Posem tota la informació del tweet a la pantalla
			this.setTitle("@" + extras.getString("user"));
			user_complet.setText(extras.getString("user_complet"));
			tweet.setText(extras.getString("tweet"));
			tweet.setFocusable(false);
			tweet_data.setText(extras.getString("data"));
			rt_count.setText(extras.getString("retweets"));
			favs_count.setText(extras.getString("fav"));

			url = extras.getString("url");

			// Si té url
			if (url != "") {

				// Si tenim connexió carreguem la web
				if (MainActivity.comprovaConnexio()) {
					web.setWebViewClient(new WebViewClient() {
						public boolean shouldOverrideUrlLoading(WebView view,
								String url) {

							view.loadUrl(url);
							return false;
						}
					});

					web.loadUrl(url);
				} else {
					// Anem a crear una pàgina informant de l'error
					StringBuilder htmlString = new StringBuilder();
					htmlString.append("<h2> Mode offline </h2>");
					htmlString
							.append("<p>Al mode offline no es poden carregar les pàgines web");
					web.loadData(htmlString.toString(),
							"text/html; charset=UTF-8", null);

				}

			}

		}

	}
}
