/**
 * Copyright (C) 2011 Oliver Schuenemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 13:19:21 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl;

import java.util.Calendar;
import java.util.Date;

import de.yamap.tl.R;
import de.yamap.tl.reader.Article;
import de.yamap.tl.service.TazService;
import de.yamap.tl.service.TazServiceBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class TazReaderActivity 
extends Activity
{
	static
	{
		Thread.setDefaultUncaughtExceptionHandler(new TazUncaughtExceptionHandler());
	}
	static final int CHECK_TTS = 0x01;
	static final int SELECT_DIRECTORY = 0x02;
	static final int TTS_SPEECH = 0x03;

	TextView textView = null;
	Button nextButton = null; 
	Button prevButton = null;
	Button menuButton = null;
	ScrollView scrollView = null;

	int currentArticle = 0;
	
	TazServiceBinder serviceBinder = null;
	
	ServiceConnection serviceConnection = new TazServiceConnection();
	
	static final String currentArticleKey = "currentArticle";

	boolean destroyed = false;
	
	boolean ttsIsAvailable = false;
	
	private View.OnClickListener nextListener = new View.OnClickListener() 
	{
		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
    public void onClick(View v) 
    {
      if (serviceBinder != null)
      {
      	if (currentArticle + 1 < serviceBinder.getNumberOfArticles())
      	{
      		++currentArticle;
      		setContent(serviceBinder.getArticle(currentArticle));
      	}
      } 
    }
	};	
	
	private View.OnClickListener prevListener = new View.OnClickListener() 
	{
		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
    public void onClick(View v) 
    {
      if (serviceBinder != null)
      {
      	if (currentArticle > 0)
      	{
      		--currentArticle;
      		setContent(serviceBinder.getArticle(currentArticle));
      	}
      } 
    }
	};	
	
	private void callTtsActivity()
	{
		Intent dirIntent = new Intent(this, TtsActivity.class);
		dirIntent.putExtra(TtsActivity.PARAM_CURRENT_ARTICLE, currentArticle);
		startActivityForResult(dirIntent, TTS_SPEECH);
	}
	
	private void storeCurrentActicle()
	{
		SharedPreferences preferences = TazSettings.getTazSettings(this);
		Editor editor = preferences.edit();
		editor.putInt(getResources().getString(R.string.pref_id_CurrentArticle), currentArticle);
		editor.commit();		
	}

	public void setContent(Article article)
	{
		if (null == article)
		{
			if (scrollView != null)
			{
				scrollView.smoothScrollTo(0,0);
			}
	    if (textView != null)
	    {
	    	textView.setText("Taz LeserIn");
	    }
		}
		else
		{
			SharedPreferences preferences = TazSettings.getTazSettings(this);
			final String textSize = preferences.getString(getResources().getString(R.string.pref_id_lst_TextSize), "0");
			
			String sizes[] = getResources().getStringArray(R.array.textSizeValues);
			int size = -2;
			boolean found = false;
			String startTag = "";
			String endTag = "";
			
			for (int i = 0; (!found) && (i < sizes.length); ++i)
			{
				if (textSize.equals(sizes[i]))
				{
					found = true;
				}
				else
				{
					size++;
				}
			}
			if (found)
			{
				switch (size)
				{
					case -2:
						startTag = "<small><small>";
						endTag = "</small></small>";
						break;
					case -1:
						startTag = "<small>";
						endTag = "</small>";
						break;
					case 1:
						startTag = "<big>";
						endTag = "</big>";
						break;
					case 2:
						startTag = "<big><big>";
						endTag = "</big></big>";
						break;
					case 0:
				  default:
						startTag = "";
				  	endTag = "";
						break;
				}
			}
			
			StringBuffer br = new StringBuffer();
			br.append(startTag + "\r\n");
			br.append("<h1>" + article.getTitel() + "</h1>");
			br.append("\r\n");
			if (null != article.getUnderTitle())
			{
				br.append("<h4>");
				br.append(article.getUnderTitle());	
				br.append("</h4>");
				br.append("\r\n");
			}
			
			Article.ArticleContent content = article.getContent();
			String lines[] = content.toArray();
			for (int i = 0; i < lines.length; i++)
			{
				br.append(lines[i] + " ");
				if (lines[i].trim().length() == 0)
				{
					br.append("<br/><br/>\r\n\r\n");	
				}
			}
			if (scrollView != null)
			{
				scrollView.smoothScrollTo(0,0);
			}
			br.append("\r\n" + endTag + "\r\n");
			if (textView != null)
	    {
	    	textView.setText(Html.fromHtml(br.toString()));
	    }
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		SharedPreferences preferences = TazSettings.getTazSettings(this);
		currentArticle = preferences.getInt(getResources().getString(R.string.pref_id_CurrentArticle), 0);

		setContentView(R.layout.main);

		scrollView = (ScrollView) findViewById(R.id.textScrollView);
		textView = (TextView) findViewById(R.id.articleTextView);
		nextButton = (Button) findViewById(R.id.nextButton);
		nextButton.setOnClickListener(nextListener);
		prevButton = (Button) findViewById(R.id.prevButton);
		prevButton.setOnClickListener(prevListener);
	
		final Intent tazServiceIntent = new Intent(this,TazService.class);
		startService(tazServiceIntent);
		final Intent tazServiceBindIntent = new Intent(this,TazService.class);
		bindService(tazServiceBindIntent, serviceConnection, 0);		
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, CHECK_TTS);
		
		destroyed = false;
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		destroyed = true;
		if (null != serviceBinder)
		{
			unbindService(serviceConnection);
			serviceBinder = null;
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = "Aktuelle Ausgabe ist nicht verfügbar";
		switch (id)
		{
			case TazServiceBinder.SERVICE_RESULT_TAZ_INVALID_LOGIN:
				message = "Falsche Abo Id oder falsches Passwort";
				break;
			case TazServiceBinder.SERVICE_RESULT_TAZ_SERVER_NOT_REACHABLE:
				message = "www.taz.de ist nicht erreichbar";
				break;
			case TazServiceBinder.SERVICE_RESULT_NETWORK_NOT_CONNECTED:
				message = "Kein Netzwerk verbunden";
				break;
			case TazServiceBinder.SERVICE_RESULT_TAZ_NOT_AVAILABLE:
			default:
				message = "Aktuelle Ausgabe ist nicht verfügbar";
				break;
		}
		builder.setCancelable(false)
			.setTitle("Fehler")
			.setMessage(message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});

		return builder.create();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		
		storeCurrentActicle();
	}
	
	private class TazServiceConnection 
	implements ServiceConnection
	{
	  TazMessageHandler messageHandler = new TazMessageHandler();
		public TazServiceConnection()
		{
		}
		/* (non-Javadoc)
		 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder)
		{
			if (!destroyed)
			{
				serviceBinder = (TazServiceBinder)binder;
				serviceBinder.addCallBackHandler(messageHandler);
				
	    	if (currentArticle < serviceBinder.getNumberOfArticles())
	    	{
	    		setContent(serviceBinder.getArticle(currentArticle));
	    	}
	    	else
	    	{
	    		currentArticle = 0;
	    		setContent(serviceBinder.getArticle(currentArticle));
	    	}
			}
		}

		/* (non-Javadoc)
		 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			if (null != serviceBinder)
			{
				serviceBinder.removeCallBackHandler(messageHandler);
			}
		}
	}
	
	private class TazMessageHandler
	extends Handler
	{
		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg)
		{
			final Bundle bundle = msg.getData();
			int result = bundle.getInt(TazServiceBinder.SERVICE_RESULT_PROP);
			switch (result)
			{
				case TazServiceBinder.SERVICE_RESULT_NEW_TAZ_LOADED:
		      if (serviceBinder != null)
		      {
		      	currentArticle = 0;
		      	serviceBinder.buildReader(true);
		      } 
					break;
				case TazServiceBinder.SERVICE_RESULT_READER_BUILDED:
					if (null != serviceBinder)
					{
						setContent(serviceBinder.getArticle(currentArticle));	
					}
					break;
				case TazServiceBinder.SERVICE_RESULT_NETWORK_NOT_CONNECTED:
				case TazServiceBinder.SERVICE_RESULT_TAZ_SERVER_NOT_REACHABLE:
				case TazServiceBinder.SERVICE_RESULT_TAZ_NOT_AVAILABLE:
				case TazServiceBinder.SERVICE_RESULT_TAZ_INVALID_LOGIN:
				default:
					showDialog(result);
					break;
			}
			super.handleMessage(msg);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.edit_load:
	    	currentArticle = 0;
	    	if (serviceBinder != null)
	    	{
	    		Date date = new Date(System.currentTimeMillis());
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTime(date);
	        if (Calendar.SUNDAY == calendar.get(Calendar.DAY_OF_WEEK))
	        {
	          calendar.add(Calendar.HOUR, -24);
	          date = calendar.getTime();
	        }
	    		serviceBinder.readTaz(date);
	    	}
				break;
			case R.id.edit_preferences:
				Intent i = new Intent(this, TazSettings.class);
				startActivity(i);
				if (null != serviceBinder)
				{
					SharedPreferences preferences = TazSettings.getTazSettings(this);

					final String userId = preferences.getString(getResources().getString(R.string.pref_id_name), "");
					final String passwd = preferences.getString(getResources().getString(R.string.pref_id_passwd), "");
					serviceBinder.setUserId(userId);
					serviceBinder.setPasswd(passwd);
					
					setContent(serviceBinder.getArticle(currentArticle));	
				}
				break;
			case R.id.edit_directory:
				Intent dirIntent = new Intent(this, DirectoryActivity.class);
				dirIntent.putExtra(DirectoryActivity.PARAM_CURRENT_ARTICLE, currentArticle);
				startActivityForResult(dirIntent, SELECT_DIRECTORY);
				break;
			case R.id.edit_read:
				callTtsActivity();
				break;
			case R.id.edit_about:
				Intent aboutIntent = new Intent(this, AboutActivity.class);
				startActivity(aboutIntent);
				break;
			case R.id.edit_finish:
				if (null != serviceBinder)
				{
					unbindService(serviceConnection);
					serviceBinder = null;
				}
				final Intent tazServiceIntent = new Intent(this, TazService.class);
				stopService(tazServiceIntent);
				
				finish();
				storeCurrentActicle();
				System.runFinalizersOnExit(true);
				System.exit(0);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSearchRequested()
	 */
	@Override
	public boolean onSearchRequested()
	{
		Intent dirIntent = new Intent(this, DirectoryActivity.class);
		dirIntent.putExtra(DirectoryActivity.PARAM_CURRENT_ARTICLE, currentArticle);
		startActivityForResult(dirIntent, SELECT_DIRECTORY);
		return false;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case CHECK_TTS:
				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
				{
					ttsIsAvailable = true;
				}
				break;
			case SELECT_DIRECTORY:
				if (null != data)
				{
					int newPage = data.getIntExtra(DirectoryActivity.RESULT_SELECTED_ARTICLE, currentArticle);
					if (null == serviceBinder)
					{
						currentArticle = newPage;
					}
					else if ((newPage < serviceBinder.getNumberOfArticles()) &&
	      			     (0 <= newPage))
	      	{
	      		currentArticle = newPage;
	      		setContent(serviceBinder.getArticle(currentArticle));
	      	}
				}
				break;
		}
	}
}
