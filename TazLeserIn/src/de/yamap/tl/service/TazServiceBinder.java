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
 * @since 13:17:05 14.05.2011
 * @version 1.0
 * @author oliver
 */

package de.yamap.tl.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import de.yamap.tl.reader.Article;
import de.yamap.tl.reader.Page;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
public class TazServiceBinder
	extends Binder
		implements TazAccess
{
	TazService service;
	List<Handler>handlers = new Vector<Handler>();

	static public final int SERVICE_RESULT_NEW_TAZ_LOADED = 0x01;
	static public final int SERVICE_RESULT_TAZ_SERVER_NOT_REACHABLE = 0x02;
	static public final int SERVICE_RESULT_TAZ_NOT_AVAILABLE = 0x03;
	static public final int SERVICE_RESULT_TAZ_INVALID_LOGIN = 0x04;
	static public final int SERVICE_RESULT_READER_BUILDED = 0x05;
	static public final int SERVICE_RESULT_NETWORK_NOT_CONNECTED = 0x06;
	static public final String SERVICE_RESULT_PROP = "ergebnis";
	
	public TazServiceBinder(TazService service)
	{
		this.service = service;
	}
	
	@Override
	public List<Article> getArticles()
	{
		List<Article> ret = null; 
		if ((service != null) &&
				(service.getReader() != null))
		{		
			ret = service.getReader().getArticles();
		}
		return ret;
	}

	public int getNumberOfArticles()
	{
		int ret = 0; 
		if ((service != null) &&
				(service.getReader() != null))
		{
			ret = service.getReader().getNumberOfArticle();
		}
		return ret;
	}
	
	public int getNumberOfPages()
	{
		int ret = 0; 
		if ((service != null) &&
				(service.getReader() != null))
		{
			ret = service.getReader().getNumberOfPages();
		}
		return ret;
	}
	
	@Override
	public List<Page> getPages()
	{
		List<Page> ret = null;
		if ((service != null) &&
				(service.getReader() != null))
		{
			ret = service.getReader().getPages();
		}
		return ret;
	}

	@Override
	public void readTaz(Date date)
	{
		if (service != null)
		{
			boolean connected = false;
			ConnectivityManager connManager = (ConnectivityManager)service.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobileConnection = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (null != mobileConnection)
			{
				connected |= mobileConnection.isConnected();
			}
			
			NetworkInfo wlanConnection = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (null != wlanConnection)
			{
				connected |= wlanConnection.isConnected();
			}

			if (connected)
			{
				service.readTaz(date);	
			}
			else
			{
				publishResult(SERVICE_RESULT_NETWORK_NOT_CONNECTED);
			}
		}
	}
	
	public void buildReader(boolean force)
	{
		if (service != null)
		{
			service.buildReader(force);
		}
	}

	/* (non-Javadoc)
	 * @see de.ddchat.service.TazAccess#getArticle(int)
	 */
	@Override
	public Article getArticle(int number)
	{
		Article ret = null;
		if ((service != null) &&
				(service.getReader() != null))
		{
			ret = service.getReader().getArticle(number);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see de.ddchat.service.TazAccess#getPage(int)
	 */
	@Override
	public Page getPage(int number)
	{
		Page ret = null;
		if ((service != null) &&
				(service.getReader() != null))
		{
			ret = service.getReader().getPage(number);
		}
		return ret;
	}
	
	public void addCallBackHandler(Handler handler)
	{
	  handlers.add(handler);
	}
	
	public void removeCallBackHandler(Handler handler)
	{
	  handlers.remove(handler);
	}
	
	public void publishResult(int result)
	{
	  for (Iterator<Handler> iterator = handlers.iterator(); iterator.hasNext();)
    {
      Handler handler = (Handler) iterator.next();
      final Message msg = new Message();
      final Bundle bundle = new Bundle();
      bundle.putInt(SERVICE_RESULT_PROP, result);
      msg.setData(bundle);
      handler.sendMessage(msg);
		}
	}

	/* (non-Javadoc)
	 * @see de.ddchat.service.TazAccess#setPasswd(java.lang.String)
	 */
	@Override
	public void setPasswd(String passwd)
	{
		if (service != null)
		{
			service.setPasswd(passwd);
		}
	}

	/* (non-Javadoc)
	 * @see de.ddchat.service.TazAccess#setUserId(java.lang.String)
	 */
	@Override
	public void setUserId(String userId)
	{
		if (service != null)
		{
			service.setUserId(userId);
		}
	}
}
