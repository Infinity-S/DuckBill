/**
 * MainActivity.java 1.0 Nov 16, 2013
 * 
 * Copyright (c) 2013 Schuyler Goodwin, Miles Camp, Thomas Robbins and Evan Walmer
 */
package edu.elon.hackdukeelon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.netcompss.ffmpeg4android_client.BaseWizard;

public class MainActivity extends BaseWizard {
	private Bundle recivedBundle; 
	
	private final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	public final int MEDIA_TYPE_IMAGE = 1;
	public final int MEDIA_TYPE_VIDEO = 2;
	private Uri fileUri;

	private ListView songSegements;
	private ArrayList<Clip> clipList = new ArrayList<Clip>();
	private Button merge; 
	private Button addMusic; 
	private Button removeAudio;
	private String clipName = ""; 
	private int currClipPos = 0; 

	private final String AUDIO_REMOVED = "/sdcard/videokit/noAudio.mp4";
	private final String SONG_ADDED = "/sdcard/videokit/withSong.mp4";
	private final String MERGED_VIDEO = "/sdcard/videokit/merge.mp4";
	private final String COMPILATION = "/sdcard/videokit/mylist.txt";
	private final String SONG = "/sdcard/videokit/song.mp3";
	private String s;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		songSegements = (ListView) findViewById(R.id.songs);
		songSegements.setOnItemClickListener(clickListener);
		//songSegements.setOnItemLongClickListener(longClickListener);  

		recivedBundle = this.getIntent().getExtras(); 
		
		makeClipList(recivedBundle.getStringArrayList("durationsStr")); 
		s = (String) recivedBundle.get("filePath");

		DeleteRecursive(new File("/sdcard/videokit"));
		
		//here
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		String directoryPath = Environment.getExternalStorageDirectory()
				+ "/Music Videos/";
		File directory = new File(directoryPath);

		if (!directory.exists()) {
			directory.mkdir();

		}
		
		
		copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();

		try {
			saveRawToSD();
		} catch (Exception e) {
			e.printStackTrace();
		}

		merge = (Button) findViewById(R.id.mergeButton); 
		merge.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					createTextFile();
				} catch (FileNotFoundException e) {
					showDialog("Error", "Trouble writing the file!"); 
				} 
				copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();
				compileVideos(); 

			}
		});

		addMusic = (Button) findViewById(R.id.musicButton); 
		addMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addAudio(); 

			}
		}); 
		
		removeAudio = (Button) findViewById(R.id.removeAudio);
		removeAudio.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				dropAudio();
			}
			
		});

		updateList();

	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		DeleteRecursive(new File("/sdcard/videokit"));
	}
	
	private void DeleteRecursive(File file) {
		Log.d("asdf", "destroying file");
		if(file.isDirectory()){
			for(File child: file.listFiles()){
				Log.d("deleting", "Deleting" + child.toString());
				DeleteRecursive(child);
			}
		}
		file.delete();
	}

	public void makeClipList(ArrayList<String> clipStr) {
		for(String s : clipStr) {
			clipList.add(new Clip(Integer.parseInt(s))); 
		}
	}

	public void showDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		builder.setTitle(title);
		builder.setMessage(message); 
		builder.setCancelable(false); 
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		}); 
		builder.show(); 
	}

	public void nameClip() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		builder.setTitle("Name Your Clip"); 
		final EditText input = new EditText(this);
		input.setText("Clip "+(currClipPos+1)); 
		input.setInputType(InputType.TYPE_CLASS_TEXT); 
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("Record", new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clipName = input.getText().toString(); 
				Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

				fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  // create a file to save the video
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name
				intent.putExtra("android.intent.extra.durationLimit", clipList.get(currClipPos).getDuration()); 
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high

				// start the Video Capture Intent
				startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);


			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clipName = ""; 
				dialog.cancel();
			}
		});

		builder.show();

	}

	private OnItemClickListener clickListener = new OnItemClickListener() {

		/* Clicking on list items */

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			currClipPos = position; 
			nameClip(); 		 
		}
	};

	private OnItemLongClickListener longClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long arg3) {
			String path = clipList.get(position).getPath(); 
			if(clipList.get(position).isRecorded()) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
				intent.setDataAndType(Uri.parse(path.substring(0, path.length()-4)), "video/mp4");
				startActivity(intent);
			} else {
				//Toast.makeText(getApplicationContext(), "Cannot Preview File. You haven't recorded one yet!", Toast.LENGTH_SHORT).show();
				showDialog("Cannot Preview Clip", "You must have recorded a clip first, in order for it to be previewed!"); 
			}

			return false; 
		}
	};

	private void updateList() { 
		boolean enable = true; 
		ArrayList<String> clipNames = new ArrayList<String>(); 
		for(Clip c : clipList) {
			clipNames.add(c.toString()); 
			if(!c.isRecorded()) {
				enable = true; 
			}
		}

		merge.setEnabled(enable); 

		ColorAdapter adapter = new ColorAdapter(this, android.R.layout.simple_list_item_1, clipNames); 
		songSegements.setAdapter(adapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Clip current = clipList.get(currClipPos); 
				String fileName = clipList.get(currClipPos).getPath();
				current.setTitle(clipName); 
				current.setPath("/sdcard/videokit/"+clipName);
				updateList(); 
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the video capture
			} else {
				// Video capture failed, advise user
			}
		}
	}

	private boolean isGoodRecord(String uri, int expectedDuration) { 
		Log.d("READ THIS!!!", "URI: "+uri); 
		MediaPlayer mp = MediaPlayer.create(this, Uri.parse(uri)); 
		int duration = mp.getDuration(); 
		mp.release(); 

		duration = duration / 1000; 

		if(duration == expectedDuration) {
			return true;
		} else {
			return false;
		}
	}

	private Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private File getOutputMediaFile(int type){
		File mediaStorageDir = new File("/sdcard/videokit/");

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("HackDukeVideo", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath()+"/"+clipName+".mp4");
			clipList.get(currClipPos).setPath(mediaFile.getPath()); 
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath()+"/"+clipName+".mp4");
			clipList.get(currClipPos).setPath(mediaFile.getPath());
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class ColorAdapter extends ArrayAdapter<String> {

		public ColorAdapter(Context context, int resID, ArrayList<String> items) {
			super(context, resID, items);                       
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			Clip current = clipList.get(position); 
			if (current.isRecorded()) {
				((TextView) v).setBackgroundColor(Color.GREEN);
			} else {
				((TextView) v).setBackgroundColor(Color.RED); 
			}
			return v;
		}
	}

	private void compileVideos(){

		copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();
		String commandStr = "ffmpeg -f concat -i " + COMPILATION + " -c copy " + MERGED_VIDEO;

		setCommand(commandStr);


		setProgressDialogTitle("Compiling your music video!");
		setProgressDialogMessage("Depends on your video size, it can take a few minutes");

		setNotificationTitle("HackDuke Application");
		setNotificationfinishedMessageTitle("Compiling finished");
		setNotificationfinishedMessageDesc("Click to play demo");
		setNotificationStoppedMessage("Demo Transcoding stopped");

		runTranscoing();

	}
	
	private void dropAudio(){

		String commandStr = "ffmpeg -i " + MERGED_VIDEO + " -vcodec copy -an " + AUDIO_REMOVED;

		setCommand(commandStr);

		setProgressDialogTitle("Removing audio!");
		setProgressDialogMessage("Depends on your video size, it can take a few minutes");
		setNotificationTitle("HackDuke Application");
		setNotificationfinishedMessageTitle("Audio all removed");

		runTranscoing();

	}

	private void addAudio(){

		String commandStr = "ffmpeg -i " + AUDIO_REMOVED + " -i " + SONG + " -map 0 -map 1 -codec copy -shortest " + SONG_ADDED/* + System.currentTimeMillis() + ".mp4"*/;
		Log.d("saving", SONG_ADDED /*+ System.currentTimeMillis() + ".mp4")*/);
		setCommand(commandStr);


		setProgressDialogTitle("Adding audio!");
		setProgressDialogMessage("Depends on your video size, it can take a few minutes");
		setNotificationTitle("HackDuke Application");
		setNotificationfinishedMessageTitle("Audio added!");

		runTranscoing();

	}

	private void saveRawToSD() throws IOException {
		InputStream in;
		Log.d("asdf", s);
		if (s.equals("R.raw.song")){
			Log.d("asdf", s);
			in = getResources().openRawResource(R.raw.song);
		} else {
			Log.d("asdf", s+"ihihuiu");
			in = getResources().openRawResource(R.raw.song2);
		}
		
		
		FileOutputStream out = new FileOutputStream(SONG);
		byte[] buff = new byte[1024];
		int read = 0;
		try{
			while((read = in.read(buff)) > 0){
				out.write(buff, 0, read);
			}
		}finally{
			in.close();
			out.close();
		}

	}

	private void createTextFile() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File(COMPILATION)); 

		for(int i = 0; i < clipList.size(); i++){
			if(i < clipList.size()-1){
				String txt = "file '"+clipList.get(i).getPath()+".mp4'"; 
				writer.println(txt);
			}
			else{
				String txt = "file '"+clipList.get(i).getPath()+".mp4'"; 
				writer.print(txt);
			}
		}
		writer.close(); 
	}


}
