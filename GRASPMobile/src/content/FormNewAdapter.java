package content;

import java.util.ArrayList;
import it.fabaris.wfp.activities.*;

import object.FormInnerListProxy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * classe FormNewAdapter per la gestione del layout della lista nuove form
 * 
 * @author UtenteSviluppo
 */


public class FormNewAdapter extends BaseAdapter
{
	private Activity activity; 
	private ArrayList<FormInnerListProxy> item;
	private static LayoutInflater inflater = null; 
	
	public FormNewAdapter(Activity a, ArrayList<FormInnerListProxy> list)
	{
		activity = a;
		item = list;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	}

	@Override
	public int getCount() 
	{
		return item.size();
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
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View vi=convertView; 
        if(convertView==null) 
            vi = inflater.inflate(R.layout.formlist_rownew, null); 
          
        //relativeBG = (RelativeLayout) vi.findViewById(R.id.prodottiBackground); 
  
        TextView formLabel = (TextView)vi.findViewById(R.id.label); 
        formLabel.setText(item.get(position).getFormName()); 
        
        TextView dataSync = (TextView) vi.findViewById(R.id.dataSync);
        dataSync.setText(item.get(position).getDataDownload());
        
        return vi;
	}

}
