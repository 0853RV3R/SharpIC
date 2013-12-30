package nmargie.logintest;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnItemClickListener {
	
	ArrayList<Entry> dataList = new ArrayList<Entry>();
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ListView l = (ListView) findViewById(R.id.listView);
		
		Entry entry = new Entry(17, "LibertyGrand", "Bar1","0.0","20039002", new Date());
		dataList.add(entry);
		Entry entry2 = new Entry(01, "LibertyGrand", "Bar2","1.9","66669002", new Date());
		dataList.add(entry2);
		
		
		// Tell ArrayAdapter: Context, Layout file for a single row, ID of the TextView where data is to be displayed, your data -- toString() taken
		ArrayAdapter<Entry> adapter = new ArrayAdapter<Entry>(this, R.layout.custom_row_entry,R.id.tvScanInfo,dataList);
		l.setAdapter(adapter);
		l.setOnItemClickListener(this);
		
		//add new entries
		dataList.add(new Entry(05, "LibertyGrand", "Bar5","15.6","00099002", new Date()));
		//update adapter
		l.setAdapter(adapter);
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long arg3) {
		TextView temp= (TextView) view;
		Toast.makeText(this, temp.getText() + " " + pos, Toast.LENGTH_SHORT).show();
	}
	
	

}
