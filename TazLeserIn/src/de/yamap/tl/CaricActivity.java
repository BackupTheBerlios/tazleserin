package de.yamap.tl;

import java.io.File;

import de.yamap.tl.service.TazService;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

public class CaricActivity
	extends Activity
{

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.caric);
		
		ImageView imageView = (ImageView)findViewById(R.id.imageView1);
		File bitmapFile = TazService.getTempPngFile();
		if (bitmapFile.exists())
		{
			imageView.setImageURI(Uri.fromFile(bitmapFile));
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
