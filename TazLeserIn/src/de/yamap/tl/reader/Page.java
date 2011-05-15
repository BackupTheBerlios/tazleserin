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
 * @since 13:15:51 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Page
{
	 List<Article> articles = new ArrayList<Article>();
	 String page = "0";
	 String ressort = "0";
	/**
	 * @return the page
	 */
	public String getPage()
	{
		return page;
	}
	/**
	 * @param page the page to set
	 */
	public void setPage(String page)
	{
		this.page = page;
	}
	/**
	 * @return the ressort
	 */
	public String getRessort()
	{
		return ressort;
	}
	/**
	 * @param ressort the ressort to set
	 */
	public void setRessort(String ressort)
	{
		this.ressort = ressort;
	}
	/**
	 * @param object
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addArticle(Article object)
	{
		return articles.add(object);
	}
	/**
	 * 
	 * @see java.util.List#clear()
	 */
	public void clearArticle()
	{
		articles.clear();
	}
	/**
	 * @return
	 * @see java.util.List#iterator()
	 */
	public Iterator<Article> articleIterator()
	{
		return articles.iterator();
	}
	/**
	 * @param object
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean removeArticle(Object object)
	{
		return articles.remove(object);
	}
	/**
	 * @return
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray()
	{
		return articles.toArray();
	}
	/**
	 * @param <T>
	 * @param array
	 * @return
	 * @see java.util.List#toArray(T[])
	 */
	public <T> T[] toArray(T[] array)
	{
		return articles.toArray(array);
	}
	/**
	 * @return
	 * @see java.util.List#size()
	 */
	public int countArticle()
	{
		return articles.size();
	}
}
