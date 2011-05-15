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
 * @since 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Article
{
	static final public String devider = "************************************************************";
	
	String ausgabe = "taz";
  String ressort = "";
  String seite = "";
  String titel = "";
  ArticleContent content = null;
  UnderTitle underTitle = null;
  int index = -1;
  
  @Override
  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Seite : " + seite + "\r\n");
    buffer.append("Ressort : " + ressort + "\r\n");
    buffer.append("Titel : " + titel + "\r\n");
    
    return buffer.toString();
  }
  
  public String getAusgabe()
  {
    return ausgabe;
  }
  
  public void setAusgabe(String ausgabe)
  {
    this.ausgabe = ausgabe;
  }
  
  public String getRessort()
  {
    return ressort;
  }
  
  public void setRessort(String ressort)
  {
    this.ressort = ressort;
  }
  
  public String getSeite()
  {
    return seite;
  }
  
  public void setSeite(String seite)
  {
    this.seite = seite;
  }
  
  public String getTitel()
  {
    return titel;
  }
  
  public void setTitel(String titel)
  {
    this.titel = titel.trim();
  }
  
  public ArticleContent getContent()
  {
    return content;
  }
  
  public void setContent(ArticleContent content)
  {
    this.content = content;
  }
  
  public UnderTitle getUnderTitle()
  {
    return underTitle;
  }
  
  public void setUnderTitle(UnderTitle underTitle)
  {
    this.underTitle = underTitle;
  }
  
  public static class UnderTitle
  {
    List<String>lines = new ArrayList<String>();

    static String magic = "Untertitel:";
    
    public UnderTitle(BufferedReader br, String firstLine)
    throws IOException
    {
      if (firstLine.startsWith(magic))
      {
        lines.add(firstLine.substring(magic.length()).trim());
      }
      String line = br.readLine();
      while ((null != line) &&
             (line.trim().length() > 0) &&
             (!line.equals(devider)))
      {
        lines.add(line.trim());
        line = br.readLine();
      }
    }
    
    public String[] toArray()
    {
      int length = lines.size();
      return toArray(new String[length]);
    }
    
    public String[] toArray(String[] a)
    {
      return lines.toArray(a);
    }

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			StringBuffer buffer = new StringBuffer();
	    for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();)
			{
				String string = iterator.next();
				buffer.append(string.trim() + " ");
				
			}
			return buffer.toString();
		}
  }

  public static class ArticleContent
  {
    List<String>lines = new ArrayList<String>();

    public ArticleContent(BufferedReader br)
    throws IOException
    {
      String line = br.readLine();
      while ((null != line) &&
             (!line.equals(devider)))
      {
        lines.add(line);
        line = br.readLine();
      }
    }
    
    public String[] toArray()
    {
      int length = lines.size();
      return toArray(new String[length]);
    }
    
    public String[] toArray(String[] a)
    {
      return lines.toArray(a);
    }
  }

	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}
}