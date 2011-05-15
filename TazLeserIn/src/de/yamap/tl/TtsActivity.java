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
 * @since 13:20:25 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl;

import de.yamap.tl.R;
import de.yamap.tl.reader.Article;
import de.yamap.tl.reader.Page;
import de.yamap.tl.service.TazService;
import de.yamap.tl.service.TazServiceBinder;
import de.yamap.tl.ttsService.TtsService;
import de.yamap.tl.ttsService.TtsService.TtsServiceBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class TtsActivity
	extends Activity
{
	TazServiceBinder serviceBinder = null;
	TtsServiceBinder ttsBinder = null; 
	TableLayout tableLayout = null;
	ScrollView scrollView = null;
	boolean destroyed = false;
	int currentArticle = 0;
	
	public static final String RESULT_SELECTED_ARTICLE = "SELECTED_ARTICLE";
	public static final String PARAM_CURRENT_ARTICLE = "PARAM_CURRENT"; 
	
	ServiceConnection serviceConnection = new ServiceConnection()
	{
		/* (non-Javadoc)
		 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder)
		{		
			serviceBinder = (TazServiceBinder)binder;
			if (destroyed)
			{
				unbindService(serviceConnection);
			}
			else
			{
				fillDirectory(serviceBinder);
				if (ttsBinder != null)
				{
				  speekContent(serviceBinder.getArticle(currentArticle));
				}
			}
		}
	
		/* (non-Javadoc)
		 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
		}
	};
	
	ServiceConnection ttsServiceConnection = new ServiceConnection()
	{
		/* (non-Javadoc)
		 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			if (destroyed)
			{
				unbindService(ttsServiceConnection);
			}
			else
			{
				ttsBinder = (TtsServiceBinder)service;	
				ttsBinder.addCallBackHandler(ttsMessageHandler);
			}
      if (serviceBinder != null)
      {
        speekContent(serviceBinder.getArticle(currentArticle));
      }
		}

		/* (non-Javadoc)
		 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			if (ttsBinder != null)
			{
				ttsBinder.removeCallBackHandler(ttsMessageHandler);
				ttsBinder = null;
			}
		}
	};
	
	Handler ttsMessageHandler = new Handler()
	{
    @Override
    public void handleMessage(Message msg)
    {
      final Bundle bundle = msg.getData();
      switch (bundle.getInt(TtsServiceBinder.SERVICE_EVENT))
      {
        case TtsServiceBinder.FINISHED_TALKING:
          skipFwd();
          break;
      }
      super.handleMessage(msg);
    }
	};

	private View.OnClickListener stopListener = new View.OnClickListener() 
  {
    @Override
    public void onClick(View v)
    {
      if (null != ttsBinder)
      {
        ttsBinder.stop();
      }
    }
  };
  
  private View.OnClickListener nextListener = new View.OnClickListener() 
	{
    @Override
    public void onClick(View arg0)
    {
      skipFwd();
    }
	};
	
	protected void skipFwd()
	{
    if ((null != ttsBinder) &&
        (null != serviceBinder))
    {
      if (currentArticle + 1 < serviceBinder.getNumberOfArticles())
      {
      	TextView textView = (TextView)tableLayout.getChildAt(currentArticle);
	      if (currentArticle % 2 == 1)
	      {
	      	textView.setBackgroundColor(0xffeeeeee);
	      }
	      else
	      {
	      	textView.setBackgroundColor(0xffffffff);
	      }
        ++currentArticle;
        textView = (TextView)tableLayout.getChildAt(currentArticle);
      	textView.setBackgroundColor(0xffbbbbbb);
        ttsBinder.stop();
        speekContent(serviceBinder.getArticle(currentArticle));
      }
    }
	};
	
	protected void closeDirectory(Page page)
	{
		if ((page != null) &&
				(page.countArticle() > 0))
		{
			Intent resultIntent = new Intent();
			Article article = page.articleIterator().next();
			if (article != null)
			{
				resultIntent.putExtra(RESULT_SELECTED_ARTICLE, article.getIndex());	
			}
      setResult(RESULT_OK, resultIntent);
		}
    finish();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tts);
		
		if (null == savedInstanceState)
		{
			currentArticle = getIntent().getIntExtra(PARAM_CURRENT_ARTICLE, 0);
		}
		else
		{
			currentArticle = savedInstanceState.getInt(PARAM_CURRENT_ARTICLE);
		}
		
		tableLayout = (TableLayout)findViewById(R.id.tts_tableLayout);
		scrollView = (ScrollView)findViewById(R.id.tts_scrollView);

		ImageButton stopButton = (ImageButton)findViewById(R.id.tts_stopButton);
		stopButton.setOnClickListener(stopListener);
		ImageButton nextButton = (ImageButton)findViewById(R.id.tts_skipButton);
		nextButton.setOnClickListener(nextListener);
		
		final Intent tazServiceIntent = new Intent(this,TazService.class);
		bindService(tazServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		
		final Intent ttsServiceIntent = new Intent(this, TtsService.class);
		bindService(ttsServiceIntent, ttsServiceConnection, Context.BIND_AUTO_CREATE);
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
	
	public void speekContent(Article article)
	{
		if ((null != article) &&
				(null != ttsBinder))
		{
			StringBuffer buffer = new StringBuffer();

			buffer.append(article.getTitel().trim() + ".\r\n\r\n");
			if (null != article.getUnderTitle())
			{
				buffer.append(article.getUnderTitle().toString().trim() + ".\r\n\r\n");
			}
			Article.ArticleContent content = article.getContent();
			String lines[] = content.toArray();
			for (int i = 0; i < lines.length; i++)
			{
				buffer.append(lines[i]);
			}
			ttsBinder.speak(buffer.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		
		if (savedInstanceState != null)
		{
			savedInstanceState.putInt(PARAM_CURRENT_ARTICLE, currentArticle);
		}
	}
	
	protected void fillDirectory(TazServiceBinder binder)
	{
	  int number = binder.getNumberOfArticles();
	  TextView focusText = null;
	  if (null != tableLayout)
	  {
	    for (int i = 0; i < number; ++i)
	    {
	      Article article = binder.getArticle(i);
	      TextView textView = new TextView(this);
	      textView.setText(article.getTitel());
	      tableLayout.addView(textView);
	      
	      if (i % 2 == 1)
	      {
	      	textView.setBackgroundColor(0xffeeeeee);
	      }
	      
	      
	      if (i == currentArticle)
	      {
	      	focusText = textView;
	      }
	    }
	    if (null != focusText)
	    {
	    	scrollView.requestLayout();
	    	scrollView.post(new FocusTextViewRunnable(scrollView, focusText));
	    }
	  }
	}
	
	private class FocusTextViewRunnable 
	implements Runnable
	{
		ScrollView scrollView;
		TextView textView;

		/**
		 * @param scrollView
		 * @param textView
		 */
		public FocusTextViewRunnable(ScrollView scrollView, TextView textView)
		{
			super();
			this.scrollView = scrollView;
			this.textView = textView;
		}

		@Override
		public void run()
		{
			if ((null != scrollView) &&
					(null != textView))
			{
				int addY = (scrollView.getHeight() - textView.getHeight()) / 2;
				scrollView.scrollTo(0, textView.getTop() - addY);
				
				textView.setBackgroundColor(0xffbbbbbb);
			}
		}
	}
}
