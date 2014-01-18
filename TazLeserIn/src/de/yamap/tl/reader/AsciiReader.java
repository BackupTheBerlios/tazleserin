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
 * @since 13:15:28 14.05.2011
 * @version 1.0
 * @author oliver
 */

package de.yamap.tl.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class AsciiReader
{
	static final public String devider = "************************************************************";
	static final public String ausgabe =    "Ausgabe   :";
	static final public String ressort =    "Ressort   :";
	static final public String seite =      "Seite     :";
	static final public String titel =      "Titel     :";
	static final public String untertitel = "Untertitel:";

	static int titleSize = 32;
	static int subTitleSize = 20;
	static int articleSize = 18;

	List<Article> articles = new ArrayList<Article>();
	List<Page> pages = new ArrayList<Page>();

	public AsciiReader(InputStream is)
	{
		readTaz(is);
	}

	public void readTaz(InputStream is)
	{
		pages.clear();
		articles.clear();
		try
		{
//			BufferedReader reader = new BufferedReader(new InputStreamReader(is,
//					Charset.forName("ISO-8859-1")));
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			readContent(reader);
			Article nextArticle = readArticle(reader);
			Hashtable<String, Page> pageTable = new Hashtable<String, Page>();
			while (null != nextArticle)
			{
				Page page = pageTable.get(nextArticle.getRessort().trim());
				if (page == null)
				{
					page = new Page();
					page.setRessort(nextArticle.getRessort());
					page.setPage(nextArticle.getSeite());
					pages.add(page);
					pageTable.put(nextArticle.getRessort().trim(), page);
				}
				page.addArticle(nextArticle);
				nextArticle = readArticle(reader);
			}
			int count = 0;
			for (Iterator<Page> iterator = pages.iterator(); iterator.hasNext();)
			{
				Page page = iterator.next();
				Iterator<Article> articleIterator = page.articleIterator();
				while (articleIterator.hasNext())
				{
					Article article = articleIterator.next();
					article.setIndex(count);
					++count;
					articles.add(article);
				}
			}
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
		}
	}

	public int getNumberOfArticle()
	{
		return articles.size();
	}

	public Article getArticle(int number)
	{
		Article ret = null;
		if (number < articles.size())
		{
			ret = articles.get(number);
		}
		return ret;
	}

	public int getNumberOfPages()
	{
		return pages.size();
	}

	public Page getPage(int number)
	{
		Page ret = null;
		if (number < pages.size())
		{
			ret = pages.get(number);
		}
		return ret;
	}

	public void readContent(BufferedReader br)
			throws IOException
	{
		String line = br.readLine();
		while ((null != line) && (!line.equals(devider)))
		{
			line = br.readLine();
		}
	}

	public Article readArticle(BufferedReader br)
			throws IOException
	{
		Article ret = new Article();
		String line = br.readLine();
		boolean foundText = false;
		while ((null != line) && (!line.equals(devider)))
		{
			if (line.startsWith(titel))
			{
				foundText = true;
				ret.setTitel(line.substring(titel.length()));
				line = br.readLine();
			}
			else if (line.startsWith(ausgabe))
			{
				foundText = true;
				ret.setAusgabe(line.substring(ausgabe.length()));
				line = br.readLine();
			}
			else if (line.startsWith(ressort))
			{
				foundText = true;
				ret.setRessort(line.substring(ressort.length()));
				line = br.readLine();
			}
			else if (line.startsWith(seite))
			{
				foundText = true;
				ret.setSeite(line.substring(seite.length()));
				line = br.readLine();
			}
			else if (line.startsWith(untertitel))
			{
				foundText = true;
				Article.UnderTitle underTitle = new Article.UnderTitle(br, line);
				ret.setUnderTitle(underTitle);
				Article.ArticleContent content = new Article.ArticleContent(br);
				ret.setContent(content);
				line = devider;
			}
			else if (line.trim().length() == 0)
			{
				Article.ArticleContent content = new Article.ArticleContent(br);
				ret.setContent(content);
				line = devider;
			}
			else
			{
				line = br.readLine();
			}
		}
		if (!foundText)
		{
			ret = null;
		}
		return ret;
	}

	/**
	 * @return the articles
	 */
	public List<Article> getArticles()
	{
		return articles;
	}

	/**
	 * @return the pages
	 */
	public List<Page> getPages()
	{
		return pages;
	}
}
