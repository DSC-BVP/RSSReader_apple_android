package com.example.kunal.tentop;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class FeedAdapter extends ArrayAdapter<FeedEntry> {
    private static final String TAG = "FeedAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<FeedEntry> applications;

    public FeedAdapter(@NonNull Context context, int resource, List<FeedEntry> applications) {
        super(context, resource);
        this.layoutResource=resource;
        this.layoutInflater=LayoutInflater.from(context);
        this.applications = applications;
    }


    @Override
    public int getCount() {
        return applications.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            Log.d(TAG, "getView: called with null convertView");
            convertView =layoutInflater.inflate(layoutResource,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            Log.d(TAG, "getView: provided convertView");
            viewHolder=(ViewHolder)convertView.getTag();
        }

        FeedEntry currentApp = applications.get(position);

        viewHolder.tvName.setText(currentApp.getName());
        viewHolder.tvArtist.setText(currentApp.getArtist());
        viewHolder.tvSummary.setText(currentApp.getSummary());

        return convertView;
    }

    private class ViewHolder{
        private final TextView tvName;
        private final TextView tvArtist;
        private  final TextView tvSummary;

        private ViewHolder(View v){
            this.tvName=v.findViewById(R.id.tvName);
            this.tvArtist=v.findViewById(R.id.tvArtist);
            this.tvSummary=v.findViewById(R.id.tvSummary);
        }
    }
}
