package nmargie.scaleapptest;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class NickAdapter extends ArrayAdapter<Entry> {
			// member fields:
			Context context;
			static ArrayList<Entry> dataArray;
			// pass the constructor the context and the arraylist of entries
			NickAdapter(Context c, ArrayList<Entry> entries){
				super(c, R.layout.custom_row_entry, R.id.tvScanInfo1, entries);
				this.context = c;
				NickAdapter.dataArray = entries;
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View row = inflater.inflate(R.layout.custom_row_entry, parent, false);
				
				// get views within row layout
				TextView text1 = (TextView) row.findViewById(R.id.tvScanInfo1);
				TextView text2 = (TextView) row.findViewById(R.id.tvScanInfo2);
				ImageButton imgDelete = (ImageButton) row.findViewById(R.id.imgBtnDelete);
				ImageView imgCheck = (ImageView) row.findViewById(R.id.imgUploadCheck);
				
				imgDelete.setTag(position);
				
				int size = dataArray.size();
				String strText1 = "" + dataArray.get(size - position - 1).getBarcode() + ";  " + dataArray.get(size - position - 1).getStringWeightG() + " g;  " + dataArray.get(size - position - 1).getStringWeightOZ() + " oz";
				String strText2 = "" + dataArray.get(size - position - 1).getScanTime();
				text1.setText(strText1);
				text2.setText(strText2);
				
				// if the entry was uploaded, change the image on the row to the STAR 
				if (dataArray.get(size - position - 1).isUploaded()) {
					imgDelete.setVisibility(ImageButton.GONE);
					imgCheck.setVisibility(ImageView.VISIBLE);
				}
				else{ // if entry was not uploaded, set the onClickListener for the Delete Button
					imgDelete.setOnClickListener(new ImageButton.OnClickListener() {
						
						@Override
						public void onClick(View v)
						{
							// delete entry in ArrayList
							Integer index = (Integer) v.getTag();
							int size= dataArray.size();
							int position = index.intValue();
							
							
							//show dialog
							//ConfirmDeleteDialogFragment confirmDialog = new ConfirmDeleteDialogFragment();
							//confirmDialog.setParameters(size, position, dataArray, NickAdapter.this );
							//confirmDialog.show(confirmDialog.getFragmentManager(), "confirmDelete");
							
							dataArray.remove(size - position -1);
							System.out.println("removing list at position: " + (size-position-1));
							notifyDataSetChanged(); //update adapter
						}
					});
					
				}
				
				return row;
			}

			
	
	
}

