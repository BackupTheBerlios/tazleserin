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
 * @since 13:16:15 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl.service;

import java.util.Date;
import java.util.List;

import de.yamap.tl.reader.Article;
import de.yamap.tl.reader.Page;
public interface TazAccess
{	
	public List<Page> getPages();
	
	public List<Article> getArticles();
	
	public Article getArticle(int number);
	
	public Page getPage(int number);
	
	public int getNumberOfArticles();
	
	public int getNumberOfPages();

	public void readTaz(Date date);
	
	public void buildReader(boolean force);
	
	public void setUserId(String userId);
	
	public void setPasswd(String passwd);
}
