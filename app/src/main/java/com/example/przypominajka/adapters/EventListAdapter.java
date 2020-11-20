package com.example.przypominajka.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.przypominajka.R;
import com.example.przypominajka.activities.EventDetailsActivity;
import com.example.przypominajka.databases.entities.EventModel;

import java.util.ArrayList;
import java.util.List;

// new adapter for RecycleView, setList method to refresh view when data changed
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {

    private final Context context;
    private List<EventModel> data = new ArrayList<>();

    public EventListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(data.get(position));

        EventModel eventModel = data.get(position);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFFFFFFFF, data.get(position).getEventColor()});

        holder.frameLayout.setBackground(gd);

        String tempEventName = eventModel.getEventName().replaceAll("_", " ");

        holder.eventName.setText(tempEventName);
    }

    public void setList(List<EventModel> eventModelList) {
        this.data = eventModelList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView eventName;
        public TextView frameLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.rowEventName);
            frameLayout = itemView.findViewById(R.id.frameColor);

            // implement onClicke method for current position of Recycle View
            itemView.setOnClickListener(view -> {

                Intent eventDetail = new Intent(context.getApplicationContext(), EventDetailsActivity.class);

                EventModel event = data.get(getAdapterPosition());

                if (event == null) {
                    Log.d("RecycleViewAdapter onCLick", "Problem z pobraniem nazwy wydarzenia");
                } else {
                    eventDetail.putExtra("EVENT_NAME", event.getEventName());
                    context.startActivity(eventDetail);
                }
            });

        }
    }

}
