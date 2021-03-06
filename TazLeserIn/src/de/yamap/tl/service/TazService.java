/**
 * 
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
 * @since 13:16:35 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

import de.yamap.tl.R;
import de.yamap.tl.NetConnection;
import de.yamap.tl.TazSettings;
import de.yamap.tl.reader.AsciiReader;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;
public class TazService
	extends Service
{
	protected TazServiceBinder binder = new TazServiceBinder(this);
	protected AsciiReader reader = null;
	protected String userId = "";
	protected String passwd = "";
	protected int preferedBitmapHeight = 200;
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return binder;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate()
	{
		SharedPreferences preferences = TazSettings.getTazSettings(this);
		final String userId = preferences.getString(getResources().getString(R.string.pref_id_name), "");
		final String passwd = preferences.getString(getResources().getString(R.string.pref_id_passwd), "");
		
		WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display defaultDisplay = windowManager.getDefaultDisplay();
		preferedBitmapHeight = defaultDisplay.getHeight() - 10;
		
		setUserId(userId);
		setPasswd(passwd);
		buildReader(false);
		
		
		super.onCreate();
	}
	
	protected void handleCommand(Intent intent)
	{
		// currently nothing is to do
	}
	
	//This is the old onStart method that will be called on the pre-2.0
	//platform.  On 2.0 or later we override onStartCommand() so this
	//method will not be called.
	@Override
	public void onStart(Intent intent, int startId) 
	{
	   handleCommand(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
	   handleCommand(intent);
	   // We want this service to continue running until it is explicitly
	   // stopped, so return sticky.
	   return START_STICKY;
	}	
	
	public File getTempFile()
	{
		File dir = new File("/mnt/sdcard/.tazLeserIn/");
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return new File("/mnt/sdcard/.tazLeserIn/taz.txt");
	}

	public static File getTempPngFile()
	{
		File dir = new File("/mnt/sdcard/.tazLeserIn/");
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return new File("/mnt/sdcard/.tazLeserIn/tom.png");
	}
	
	public void readTaz(final Date date)
	{
		if ((userId.length() > 0) && (passwd.length() > 0))
		{
			new Thread() 
			{
				public void run()
				{
					readTom(date);
					int publishResult = TazServiceBinder.SERVICE_RESULT_TAZ_NOT_AVAILABLE;
			    Calendar calendar = Calendar.getInstance();
			    calendar.setTime(date);
			    String dateString = (new SimpleDateFormat("yyyy_MM_dd").format(date));
			    NetConnection.Result result = NetConnection.getConnection("http://dl.taz.de/taz/abo/get.php?f=" + dateString +".txt", userId, passwd);
			    																													 
			  	InputStream is = result.getInputStream();  
			  	int get = 1;
			  	byte b[] = new byte[1000];
			  	if (is == null)
			  	{
			  		Exception resultExp = result.getException(); 
			  		if (resultExp == null)
			  		{
			  			publishResult = TazServiceBinder.SERVICE_RESULT_TAZ_NOT_AVAILABLE;
			  		}
			  		else if (resultExp instanceof UnknownHostException)
			  		{
			  			publishResult = TazServiceBinder.SERVICE_RESULT_TAZ_SERVER_NOT_REACHABLE;
			  		}
			  		else if (resultExp instanceof HttpResponseException)
			  		{
			  			HttpResponseException responseException = (HttpResponseException)resultExp;
			  			switch (responseException.getStatusCode())
			  			{
			  				case HttpStatus.SC_NOT_FOUND:
			  					publishResult = TazServiceBinder.SERVICE_RESULT_TAZ_NOT_AVAILABLE;
			  					break;
			  				case HttpStatus.SC_UNAUTHORIZED:
			  				case HttpStatus.SC_FORBIDDEN:
			  					publishResult = TazServiceBinder.SERVICE_RESULT_TAZ_INVALID_LOGIN;
			  					break;
		  					default:
			  					publishResult = TazServiceBinder.SERVICE_RESULT_TAZ_NOT_AVAILABLE;
		  						break;
			  			}
			  		}
			  		else
			  		{
	  					publishResult = TazServiceBinder.SERVICE_RESULT_TAZ_NOT_AVAILABLE;
			  		}
			  	}
			  	else
			  	{
			    	try
			    	{
			    		FileOutputStream os = new FileOutputStream(getTempFile());
			    		get = is.read(b);
			      	while (get > 0)
			      	{
			      		os.write(b, 0, get);
			      		get = is.read(b);
			      	}
			      	os.close();
			      	is.close();
			      	readTom(date);
			      	publishResult = TazServiceBinder.SERVICE_RESULT_NEW_TAZ_LOADED;
			    	}
			    	catch (Exception e)
			    	{
			    	}
			  	}
			  	if (binder != null)
			  	{
			  		binder.publishResult(publishResult);
			  	}
				}
			}.start();
		}
	}
	
	public void readTom(Date date)
	{
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    String dateString = (new SimpleDateFormat("yyyy/MM/dd").format(date));
		String referer = "http://taz.de/digitaz/.tom/tomdestages?day=" + dateString;
		
		Calendar reference = Calendar.getInstance();
		reference.setTime(new Date(111, 11, 9));
	
		long diffMillis = calendar.getTimeInMillis() - reference.getTimeInMillis();
		long pastDays = diffMillis /(24l*60l*60l*1000l);
		String toucheUri = "http://taz.de/digitaz/.tom/gif.t,tom.d," + (1323385200l + pastDays * 86400l);
		
    NetConnection.Result result = NetConnection.getConnection(toucheUri, referer);
		 
  	InputStream is = result.getInputStream(); 
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
  	byte b[] = new byte[10000];
  	if (is != null)
  	{
  		try
  		{
  			int got = 0;
  			got = is.read(b);
  			while (got > 0)
  			{
  				bos.write(b, 0, got);
  				got = is.read(b);
  			}
  			byte imageB[] = bos.toByteArray();
  			Bitmap image = BitmapFactory.decodeByteArray(imageB, 0, imageB.length);
  			is.close();
  			bos.close();
  			if (null != image)
  			{
					int width = image.getWidth() ;
					int height = image.getHeight();
					int newWidth = (int)(((float)width * (float)preferedBitmapHeight) / (float)height);
					Bitmap tempBitmap = Bitmap.createScaledBitmap(image, newWidth, preferedBitmapHeight, true);
					if (tempBitmap.getHeight() >= 0)
					{
						image.recycle();
						image = tempBitmap;
					}
					else
					{
						tempBitmap.recycle();
					}
					FileOutputStream fos = new FileOutputStream(getTempPngFile());
					image.compress(Bitmap.CompressFormat.PNG, 95, fos);
					fos.flush();
					fos.close();
					image.recycle();
  			}
  		}
  		catch (Exception exp)
  		{
  		}
  	}
	}
	
	public void buildReader()
	{
		buildReader(false);
	}
	
	public void buildReader(boolean force)
	{
		if ((null == reader) ||
				force)
		{
			File tempFile = getTempFile();

			if (!tempFile.exists())
			{
		    Date date = new Date(System.currentTimeMillis());
	      Calendar calendar = Calendar.getInstance();
	      calendar.setTime(date);
	      if (Calendar.SUNDAY == calendar.get(Calendar.DAY_OF_WEEK))
	      {
	        calendar.add(Calendar.HOUR, -24);
	        date = calendar.getTime();
	      }	    
				readTaz(date);
			}
			try
			{
				FileInputStream is = new FileInputStream(tempFile);
				if (reader == null)
				{
					reader = new AsciiReader(is);	
				}
				else
				{
					reader.readTaz(is);
				}
		  	is.close();
			}
			catch (Exception exp)
			{
			}
		}
  	if (binder != null)
  	{
  		binder.publishResult(TazServiceBinder.SERVICE_RESULT_READER_BUILDED);
  	}
	}

	/**
	 * @return the reader
	 */
	protected AsciiReader getReader()
	{
		return reader;
	}

	/**
	 * @return the userId
	 */
	protected String getUserId()
	{
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	protected void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * @return the passwd
	 */
	protected String getPasswd()
	{
		return passwd;
	}

	/**
	 * @param passwd the passwd to set
	 */
	protected void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
}
