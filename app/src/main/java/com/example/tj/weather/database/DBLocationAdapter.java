package com.example.tj.weather.database;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tj.weather.R;

import java.util.List;

/**
 * Created by tj on 7/17/2015.
 */
public class DBLocationAdapter extends ArrayAdapter<DBLocation> {
    private Context context;
    private List<DBLocation> list;
    private int resource;
    private ViewHolder holder;

    public DBLocationAdapter(Context context, int resource, List<DBLocation> objects) {
        super(context, resource, objects);
        this.context = context;
        list = objects;
        this.resource = resource;
    }

    @Override
    public DBLocation getItem(int position) {
        return list.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.database_listview_row, null);

            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv.setText(list.get(position).toString());

        return convertView;
    }

    public class ViewHolder {
        public TextView tv;

        public ViewHolder(View v) {
            tv = (TextView) v.findViewById(R.id.dbTextViewLocation);

            v.setTag(this);
        }
    }

}
