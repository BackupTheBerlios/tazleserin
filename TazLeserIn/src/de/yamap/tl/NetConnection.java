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
 * @since 13:18:43 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class NetConnection
{
	static String base64Codes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	
	static public class Result
	{
		InputStream inputStream = null;
		Exception exception = null;
		
		/**
		 * @param inputStream
		 * @param exception
		 */
		public Result(InputStream inputStream, Exception exception)
		{
			super();
			this.inputStream = inputStream;
			this.exception = exception;
		}
		
		/**
		 * @return the inputStream
		 */
		public InputStream getInputStream()
		{
			return inputStream;
		}
		
		/**
		 * @param inputStream the inputStream to set
		 */
		public void setInputStream(InputStream inputStream)
		{
			this.inputStream = inputStream;
		}
		
		/**
		 * @return the exception
		 */
		public Exception getException()
		{
			return exception;
		}
		
		/**
		 * @param exception the exception to set
		 */
		public void setException(Exception exception)
		{
			this.exception = exception;
		}
	}
	
	public static String getBase64Code(byte b[])
	{
		StringBuffer ret = new StringBuffer();
		int fillBytes = b.length % 3;
		String append = "";
		if (fillBytes > 0)
		{
			append = "===".substring(fillBytes);
			byte temp[] = new byte[b.length + 3 - fillBytes];
			System.arraycopy(b, 0, temp, 0, b.length);
			for (int i = b.length; i < temp.length; ++i)
			{
				temp[i] = 0;
			}
			b = temp;
		}
		int a[] = new int[4];
		for (int i = 0; i < b.length / 3; ++i)
		{
			a[0] = (b[i * 3] & 0xfc) >> 2;
			a[1] = ((b[i * 3] & 0x03) << 4) + ((b[i * 3 + 1] & 0xf0) >> 4);
			a[2] = ((b[i * 3 + 1] & 0x0f) << 2) + ((b[i * 3 + 2] & 0xC0) >> 6);
			a[3] = b[i * 3 + 2] & 0x3F;
			for (int j = 0; j < a.length; ++j)
			{
				ret.append(base64Codes.substring(a[j], a[j] + 1));
			}
		}
		String sub = ret.toString();
		sub = sub.substring(0, sub.length() - append.length()) + append; 
		return sub;
	}

	
	static public Result getConnection(String uri, String userName, String password)
	{
		InputStream retIs = null;
		Exception retExp = null;
		
		int responseStatus = HttpStatus.SC_OK;
		HttpResponse response = null;
		HttpClient client = new DefaultHttpClient();
		String userPassword =  userName + ":" + password;
		String passwEncode = getBase64Code(userPassword.getBytes());
		
		HttpGet request = null;
		try
		{
			request = new HttpGet(new URI(uri));
			request.setHeader("Authorization", "Basic " + passwEncode);
			response = client.execute(request);	
			responseStatus = response.getStatusLine().getStatusCode();
			if (null != response)
			{
				switch (responseStatus)
				{
					case HttpStatus.SC_OK:
						ByteArrayOutputStream ostream = new ByteArrayOutputStream();
						response.getEntity().writeTo(ostream);
						retIs = new ByteArrayInputStream(ostream.toByteArray());
						ostream.close();						
						break;
					case HttpStatus.SC_NOT_FOUND:
					case HttpStatus.SC_UNAUTHORIZED:
					case HttpStatus.SC_FORBIDDEN:
						default:
						retExp = new HttpResponseException(responseStatus, uri);
						break;
				}
			}
		}
		catch (Exception exp)
		{
			retExp = exp;
		}
		return new Result(retIs, retExp);
	}

	static public Result getConnection(String uri)
	{
		InputStream retIs = null;
		Exception retExp = null;
		
		int responseStatus = HttpStatus.SC_OK;
		HttpResponse response = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = null;
		try
		{
			request = new HttpGet(new URI(uri));
			response = client.execute(request);	
			responseStatus = response.getStatusLine().getStatusCode();
			if ((null != response) &&
					(responseStatus == HttpStatus.SC_OK))
			{
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				response.getEntity().writeTo(ostream);
				retIs = new ByteArrayInputStream(ostream.toByteArray());
				ostream.close();				
			}
		}
		catch (Exception exp)
		{
			retExp = exp;
		}
		return new Result(retIs, retExp);
	}
	
	static public Result getConnection(String uri, String referer)
	{
		InputStream retIs = null;
		Exception retExp = null;
		
		int responseStatus = HttpStatus.SC_OK;
		HttpResponse response = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = null;
		try
		{
			request = new HttpGet(new URI(uri));
			request.setHeader("Referer", referer);
			response = client.execute(request);	
			responseStatus = response.getStatusLine().getStatusCode();
			if ((null != response) &&
					(responseStatus == HttpStatus.SC_OK))
			{
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				response.getEntity().writeTo(ostream);
				retIs = new ByteArrayInputStream(ostream.toByteArray());
				ostream.close();
			}
		}
		catch (Exception exp)
		{
			retExp = exp;
		}
		return new Result(retIs, retExp);
	}	
}
