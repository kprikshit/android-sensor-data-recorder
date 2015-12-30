package com.prikshit.recorder.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.prikshit.recorder.R;

import java.util.List;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 *
 * Adapter for Recycler View
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Information> dataset;

    public MyAdapter(List<Information> myDataset) {
        dataset = myDataset;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customrow, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Information curr = dataset.get(position);
        holder.devName.setText(curr.name);
        holder.devEmail.setText(curr.email);
        holder.devImage.setImageResource(curr.picId);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        //It has to match with the xml file for the individual row layout
        public TextView devName;
        public TextView devEmail;
        public ImageView devImage;

        public ViewHolder(View v) {
            super(v);
            devName = (TextView) v.findViewById(R.id.devname);
            devEmail = (TextView) v.findViewById(R.id.devemail);
            devImage = (ImageView) v.findViewById(R.id.devpic);
        }
    }
}
