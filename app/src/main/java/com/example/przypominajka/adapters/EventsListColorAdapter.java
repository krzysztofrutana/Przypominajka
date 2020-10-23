package com.example.przypominajka.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.przypominajka.models.Event;
import com.example.przypominajka.R;

import java.util.List;

public class EventsListColorAdapter extends ArrayAdapter<Event> {

    protected Context context;
    ;
    protected int resourceId;
    protected List<Event> data;

    public EventsListColorAdapter(@NonNull Context context, int resource, @NonNull List<Event> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
        this.data = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.row_list, parent, false);
        }

        Event tempEvent = data.get(position);
        TextView eventName = convertView.findViewById(R.id.rowEventName);
        FrameLayout frameLayout = convertView.findViewById(R.id.frameColor);

        // add gradient color in row
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFFFFFFFF, tempEvent.getEventColor()});

        frameLayout.setBackground(gd);
        String tempEventName = tempEvent.getEventName().replaceAll("_", " ");
        eventName.setText(tempEventName);

        return convertView;
    }

}

