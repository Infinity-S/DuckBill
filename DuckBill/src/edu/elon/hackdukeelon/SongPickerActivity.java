/**
 * SongPickerActivity.java 1.0 Nov 16, 2013
 * 
 * Copyright (c) 2013 Schuyler Goodwin, Miles Camp, Thomas Robbins and Evan Walmer
 */
package edu.elon.hackdukeelon;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SongPickerActivity extends Activity {
	private ListView songView; 
	private ArrayList<Song> songs = new ArrayList<Song>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_picker);
		
		songView = (ListView) findViewById(R.id.songs); 
		songView.setOnItemClickListener(clickListener); 
		buildList(); 
	}
	
	private OnItemClickListener clickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			Bundle b = new Bundle(); 
			b.putStringArrayList("durationsStr", songs.get(position).getClips()); 
			b.putString("filePath", songs.get(position).getFileName()); 
			
			Intent i = new Intent(SongPickerActivity.this, MainActivity.class); 
			i.putExtras(b); 
			startActivity(i); 
			
		}
	};
	
	private void buildList() {
		//Song song1 = new Song("San Francisco Noise","R.raw.song", new int[] {9,9,8,13,12,19,17,13,10,13,18,17,18,19}); 
		Song song1 = new Song("San Francisco Noise","R.raw.song", new int[]   {3,3,3,3,3,3,3,3,3,3,3,3,3,3}); 
		songs.add(song1); 
		//Song song2 = new Song("I'm Just Too Young For This","R.raw.song2", new int[] {4,6,11,11,5,16,15,16,12,12,10,20,11,24,21}); 
		Song song2 = new Song("I'm Just Too Young For This","R.raw.song2", new int[] {4,5,4}); 
		songs.add(song2); 
		
		ArrayList<String> songNames = new ArrayList<String>(); 
		
		for(Song s : songs) {
			songNames.add(s.getTitle()); 
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, songNames);
		songView.setAdapter(adapter); 
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.song_picker, menu);
		return true;
	}

}
