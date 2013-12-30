package nmargie.scaleapptest;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ConfirmDeleteDialogFragment extends DialogFragment {
	// set parameters: size of ArrayList and position (i.e. row number pressed)
	int size, position;
	NickAdapter adapter;
	ArrayList<Entry> entries;
	public void setParameters(int size, int pos, ArrayList<Entry> e, NickAdapter a){
		this.size = size;
		this.position = pos;
		this.adapter = a;
		this.entries =e;
	}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete Entry?")
               .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Delete the entry:
                	   // Can't seem to pass int size or int position
                	   entries.remove(size - position - 1);
           			   adapter.notifyDataSetChanged();
                	   
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   // Do I need to put anything else here??
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}


