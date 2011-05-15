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
 * @since 13:17:25 14.05.2011
 * @version 1.0
 * @author oliver
 */
package de.yamap.tl.ttsService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
public class TtsService
	extends Service
	implements OnInitListener
{
	protected TextToSpeech textToSpeech;
	
	protected static final int CHECK_TTS = 0x01;
	
	final static public String TEXT_FLAG = "text";
	
	final static public String FLUSh_FLAG = "flush";

	protected boolean textToSpeechIsAvailable = false;

	protected TtsServiceBinder binder = new TtsServiceBinder();
	
	protected StringBuffer pendingText = new StringBuffer();
	
	protected String currentUtteranceId = FLUSh_FLAG;
	
	protected int currentTextId = 0;

	protected TextToSpeech.OnUtteranceCompletedListener utteranceCompletedListener = new TextToSpeech.OnUtteranceCompletedListener()
	{
		@Override
		public void onUtteranceCompleted(String utteranceId)
		{
	  	if ((currentUtteranceId.equals(utteranceId) && 
	  			(!FLUSh_FLAG.equals(currentUtteranceId))))
	  	{
	  		binder.publishEvent(TtsServiceBinder.FINISHED_TALKING);
	  	}
		}
	};
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy()
	{
		if (null != textToSpeech)
		{
			textToSpeech.shutdown();
		}
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent arg0)
	{
		return binder;
	}

	
	/* (non-Javadoc)
	 * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
	 */
	@Override
	public void onInit(int status)
	{
		if (status == TextToSpeech.SUCCESS)
		{
		  textToSpeechIsAvailable = true;
		  if (pendingText.length() > 0)
		  {
				textToSpeech.setOnUtteranceCompletedListener(utteranceCompletedListener);
				binder.speak(pendingText.toString());
		    pendingText.delete(0, pendingText.length());
		  }
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate()
	{
		textToSpeech = new TextToSpeech(this, this);
		textToSpeech.setLanguage(Locale.GERMAN);
		super.onCreate();
	}

	public class TtsServiceBinder
	extends Binder
	implements TtsServiceIntf
	{
		protected List<Handler>handlers = new Vector<Handler>();
		
		static public final String SERVICE_EVENT = "EREIGNIS";
		
		static public final int FINISHED_TALKING = 0x01;

		/* (non-Javadoc)
		 * @see de.ddchat.ttsService.TtsServiceIntf#stop()
		 */
		@Override
		public void stop()
		{
			if (null != textToSpeech)
			{
				currentUtteranceId = FLUSh_FLAG;
        HashMap<String, String>map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, FLUSh_FLAG);
				textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, map);
			}			
		}

		@Override
		public boolean isTalking()
		{
			if (null != textToSpeech)
			{
				return textToSpeech.isSpeaking();
			}
			return false;
		} 

		/* (non-Javadoc)
		 * @see de.ddchat.ttsService.TtsServiceIntf#speakText(java.lang.String)
		 */
		@Override
		public void speak(String text)
		{
		  if ((textToSpeechIsAvailable) &&
		      (null != textToSpeech))
			{
		  	currentUtteranceId = TEXT_FLAG + "_" + currentTextId;
		  	currentTextId++;
				HashMap<String, String>map = new HashMap<String, String>();
				map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, currentUtteranceId);
				textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, map);
			}
		  else
		  {
		    pendingText.append(text + ".");
		  }
		}
		
		public void addCallBackHandler(Handler handler)
		{
		  handlers.add(handler);
		}
		
		public void removeCallBackHandler(Handler handler)
		{
		  handlers.remove(handler);
		}
		
		public void publishEvent(int event)
		{
		  for (Iterator<Handler> iterator = handlers.iterator(); iterator.hasNext();)
	    {
	      Handler handler = (Handler) iterator.next();
	      final Message msg = new Message();
	      final Bundle bundle = new Bundle();
	      bundle.putInt(SERVICE_EVENT, event);
	      msg.setData(bundle);
	      handler.sendMessage(msg);
			}
		}
	}
}
