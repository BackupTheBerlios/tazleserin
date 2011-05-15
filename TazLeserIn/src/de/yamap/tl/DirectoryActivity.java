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
 * @since 13:18:29 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl;

import java.util.Iterator;

import de.yamap.tl.R;
import de.yamap.tl.reader.Article;
import de.yamap.tl.reader.Page;
import de.yamap.tl.service.TazService;
import de.yamap.tl.service.TazServiceBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class DirectoryActivity
	extends Activity
{
	TazServiceBinder serviceBinder;
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
		setContentView(R.layout.directory);
		
		if (null == savedInstanceState)
		{
			currentArticle = getIntent().getIntExtra(PARAM_CURRENT_ARTICLE, 0);
		}
		else
		{
			currentArticle = savedInstanceState.getInt(PARAM_CURRENT_ARTICLE);
		}
		
		tableLayout = (TableLayout)findViewById(R.id.dir_tableLayout);
		scrollView = (ScrollView)findViewById(R.id.dir_scrollView);

		final Intent tazServiceIntent = new Intent(this,TazService.class);
		bindService(tazServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		destroyed = true;
		if (null != serviceBinder)
		{
			unbindService(serviceConnection);
			
			serviceBinder = null;
		}
		super.onDestroy();
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

	protected TextView getPageConsumption(Page page)
	{
	  TextView ret = new TextView(this);
	  StringBuffer buffer = new StringBuffer();
	  buffer.append("<h3>" + page.getRessort() + "</h3>\r\n");
	  
	  Iterator<Article> articleIter = page.articleIterator();
	  for (int i = 0; articleIter.hasNext() && i < 7; ++i)
	  {
	    Article article = articleIter.next();
	    buffer.append("- " + article.getTitel() + "<br/>\r\n");
	  }
	  ret.setText(Html.fromHtml(buffer.toString()));
	  ret.setOnClickListener(new DirectoryViewOnClickListener(this, page));
	  return ret;
	}
	
	protected void fillDirectory(TazServiceBinder binder)
	{
		if (null != binder)
		{
		  int number = binder.getNumberOfPages();
		  int searchArticle = currentArticle;
		  TextView focusText = null;
		  if (null != tableLayout)
		  {
		    for (int i = 0; i < number; ++i)
		    {
		      Page page = binder.getPage(i);
		      TextView textView = getPageConsumption(page);
		      tableLayout.addView(textView);
		      
		      if ((searchArticle >= 0) &&
		      		(searchArticle - page.countArticle()  < 0))
		      {
		      	focusText = textView;
		      }
		      searchArticle -= page.countArticle();
		    }
		    if (null != focusText)
		    {
		    	scrollView.requestLayout();
		    	scrollView.post(new FocusTextViewRunnable(scrollView, focusText));
		    }
		  }
		}
	}
	
	private class DirectoryViewOnClickListener
	implements View.OnClickListener
	{
		DirectoryActivity adaptee;
		Page page;
		public DirectoryViewOnClickListener(DirectoryActivity adaptee, Page page)
		{
			this.adaptee = adaptee;
			this.page = page;
		}
		
		@Override
		public void onClick(View v)
		{
			adaptee.closeDirectory(page);
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
				
				textView.setBackgroundColor(0xffdddddd);
			}
		}
	}
}
