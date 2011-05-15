
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
 * @since 13:19:45 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl;

import java.io.File;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TazUncaughtExceptionHandler
		implements UncaughtExceptionHandler
{
	UncaughtExceptionHandler defaultHandler = null;
	
	public TazUncaughtExceptionHandler()
	{
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	@Override
	public void uncaughtException(Thread arg0, Throwable exp)
	{
    Date date = new Date(System.currentTimeMillis());
    String dateString = (new SimpleDateFormat("yyyy_MM_dd__hh_mm_ss").format(date));
    String filename = dateString + ".txt";
    try
    {
    	PrintWriter pr = new PrintWriter(new File("/mnt/sdcard/.tazLeserIn/" + filename));
    	exp.printStackTrace(pr);
    	pr.close();
    }
    catch (Exception e)
    {
    }
    if (null != defaultHandler)
    {
    	defaultHandler.uncaughtException(arg0, exp);	
    }
	}
}
