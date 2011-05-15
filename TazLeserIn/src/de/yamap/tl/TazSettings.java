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
 * @since 13:19:36 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl;

import de.yamap.tl.R;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TazSettings
	extends PreferenceActivity
{
	/**
	 * 
	 */
	public TazSettings()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		this.addPreferencesFromResource(R.xml.preferences);		
	}
	
	public static SharedPreferences getTazSettings(final ContextWrapper ctx)
	{
		return ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", MODE_PRIVATE);
	}
}
