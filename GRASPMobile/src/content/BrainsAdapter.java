package content;

import java.util.ArrayList;

import object.FormInnerListProxy;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * classe BaseAdapter per la gestione del layout della lista dei dropdown
 * 
 * @author UtenteSviluppo
 */

public class BrainsAdapter extends BaseAdapter
{
	private Activity activity; 
	private String[] items;
	private int textUnit;
	private float textSize;
	private static LayoutInflater inflater = null;
	public String a = "";
	
	public BrainsAdapter(Activity a, final String[] objects, int textUnit, float textSize)
	{
		
		activity = a;
		items = objects;
		this.textUnit = textUnit;
		this.textSize = textSize;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	}
	
	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView; 
        if(convertView==null) 
            vi = inflater.inflate(android.R.layout.simple_spinner_item, null); 
  
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
		tv.setText(items[position]);
		tv.setTextSize(textUnit, textSize);
        
        return vi;
	}

}
