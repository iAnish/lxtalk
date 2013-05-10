package com.mac.app;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class StatusSpinner extends Spinner {
	public static final int STATUS_POS_FREE=0;
	public static final int STATUS_POS_AVAILABLE=1;
	public static final int STATUS_POS_AWAY=2;
	public static final int STATUS_POS_XAWAY=3;
	public static final int STATUS_POS_DND=4;
	public static final int STATUS_POS_OFFLINE=5;
	
	public StatusSpinner(Context ctx){
		super(ctx);

		init();
	}
	
	public StatusSpinner(Context ctx, AttributeSet attrs){
		super(ctx, attrs);
		
		init();
	}
	
	private void init(){
		String[] statusArray=this.getResources().getStringArray(R.array.statusArray);

        ArrayAdapter<String> adapter=new StatusSpinnerAdapter(this.getContext(), R.layout.status_spinner_row, statusArray);
        this.setAdapter(adapter);
        this.setSelection(STATUS_POS_OFFLINE);
	}
	
	// Sets spinner selection without calling listener
	public void setSelectionFixed(final int selection) {
	    final OnItemSelectedListener l = this.getOnItemSelectedListener();
	    this.setOnItemSelectedListener(null);
	    this.post(new Runnable() {
	        @Override
	        public void run() {
	        	StatusSpinner.this.setSelection(selection);
	        	StatusSpinner.this.post(new Runnable() {
	                @Override
	                public void run() {
	                	StatusSpinner.this.setOnItemSelectedListener(l);
	                }
	            });
	        }
	    });
	}

	private static class StatusSpinnerAdapter extends ArrayAdapter<String> {	
		private final String[] objects;
		
		public StatusSpinnerAdapter(Context ctx, int resource, String[] objects){
			super(ctx, resource, R.id.status_name, objects);
			this.objects=objects;
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v=convertView;
			if(v==null)
				v=((LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.status_spinner_row, null, false);
			
			((TextView)v.findViewById(R.id.status_name)).setText(objects[position]);
	
			ImageView img=(ImageView)v.findViewById(R.id.status_icon);
	

			img.setImageResource(Utils.presenceDrawable(position));
			
			
			return v;
		}
	
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View v=convertView;
			if(v==null)
				v=((LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.status_spinner_dropdown_row, null, false);
			
			((TextView)v.findViewById(R.id.status_name)).setText(objects[position]);
			
			ImageView img=(ImageView)v.findViewById(R.id.status_icon);
			
			img.setImageResource(Utils.presenceDrawable(position));
			
			return v;
		}
		
	}
}
