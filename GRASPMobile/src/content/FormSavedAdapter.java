package content;
import it.fabaris.wfp.activities.*;

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
 * classe FormSavedAdapter per la gestione del layout della lista delle form salvate
 * 
 * @author UtenteSviluppo
 */

public class FormSavedAdapter extends BaseAdapter
{
	private Activity activity; 
	private ArrayList<FormInnerListProxy> item;
	private ArrayList<FormInnerListProxy> data;
	private static LayoutInflater inflater = null; 
	
	public FormSavedAdapter(Activity a, ArrayList<FormInnerListProxy> list, ArrayList<FormInnerListProxy> saved)
	{
		activity = a;
		item = list;
		data = saved;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	}
	
	public void add(ArrayList<FormInnerListProxy> list)
	{
		data = list;
	}
	
	public void clear()
	{
		data.clear();
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
            vi = inflater.inflate(R.layout.formlist_rowsaved, null); 
          
        //relativeBG = (RelativeLayout) vi.findViewById(R.id.prodottiBackground); 
          
        TextView text = (TextView)vi.findViewById(R.id.label); 
        //text.setText(item.get(position).getFormName()); //LL deleted because the object "item" is not aligned with the position's value
        String[] formNameA = data.get(position).getFormName().split("_");
        String formName = formNameA[0];
        if(formNameA.length > 2){//if the form name contains one or more underscores
        	for(int i=1; i < formNameA.length-1; i++){
        		formName = formName + "_"+formNameA[i];
        	}
        }
        text.setText(formName);
        
        
        
        
        
        TextView textLastSaveDate = (TextView) vi.findViewById(R.id.textLastSaveDate);
        TextView textBy = (TextView) vi.findViewById(R.id.textBy);
        
        try
        {
        	textLastSaveDate.setText(data.get(position).getLastSavedDateOn());
            textBy.setText(data.get(position).getFormEnumeratorId());
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        return vi;
	}
}
