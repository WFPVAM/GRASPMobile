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
 * classe FormSubmittedAdapter per la gestione del layout della lista delle form inviate
 *
 * @author UtenteSviluppo
 */

public class FormSubmittedAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<FormInnerListProxy> item;
    private ArrayList<FormInnerListProxy> data;
    private static LayoutInflater inflater = null;

    //public FormSubmittedAdapter(Activity a, ArrayList<FormInnerListProxy> list, ArrayList<FormInnerListProxy> submitted)//LL 14-05-2014 modificata per dismissione del db grasp
    public FormSubmittedAdapter(Activity a, ArrayList<FormInnerListProxy> list)//LL 14-05-2014 modificata per dismissione del db grasp
    {
        activity = a;
        item = list;
        //data = submitted; LL 14-05-2014 elminato per dismissione del db grasp non ci sono piu' due oggetti parcellizzati quindi non c'e' piu' problema di disallineamento
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.formlist_rowsubmitted, null);

        TextView text = (TextView) vi.findViewById(R.id.label);
        text.setText(item.get(position).getFormName()); //LL deleted because the object "item" is not aligned with the position's value//LL 14-05-2014 rimesso per dismissione db grasp

        TextView textBy = (TextView) vi.findViewById(R.id.textBy);
        TextView textSubmittedOn = (TextView) vi.findViewById(R.id.textSubmittedOn);

        try {
            textSubmittedOn.setText(item.get(position).getSubmissionDate());
            textBy.setText(item.get(position).getFormEnumeratorId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }
}
