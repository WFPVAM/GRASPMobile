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
 * classe FormPendingAdapter per la gestione del layout della lista delle form pending
 *
 * @author UtenteSviluppo
 */

public class FormPendingAdapter extends BaseAdapter
{
    private Activity activity;
    private ArrayList<FormInnerListProxy> item;
    private static LayoutInflater inflater = null;

    public FormPendingAdapter(Activity a, ArrayList<FormInnerListProxy> list)
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
            vi = inflater.inflate(R.layout.formlist_rowpending, null);

        //relativeBG = (RelativeLayout) vi.findViewById(R.id.prodottiBackground); 

        TextView text = (TextView)vi.findViewById(R.id.label);
        text.setText(item.get(position).getFormName());

        TextView textLastSaveDate = (TextView) vi.findViewById(R.id.textLastSaveDate);
        textLastSaveDate.setText(item.get(position).getDataDownload());

        TextView textBy = (TextView) vi.findViewById(R.id.textBy);
        textBy.setText(item.get(position).getDataDownload());

        return vi;
    }
}
