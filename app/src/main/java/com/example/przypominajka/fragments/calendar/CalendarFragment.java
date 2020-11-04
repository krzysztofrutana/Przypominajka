package com.example.przypominajka.fragments.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.activities.AddNewEventActivity;
import com.example.przypominajka.models.Event;
import com.example.przypominajka.activities.EventDetailsActivity;
import com.example.przypominajka.utils.MonthViewBuild;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.R;
import com.example.przypominajka.adapters.EventsListColorAdapter;
import com.example.przypominajka.utils.TranslateMonths;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.example.przypominajka.R.drawable.text_view_border;


public class CalendarFragment extends Fragment {

    private Context context;

    LocalDate currentDateInPreview;
    Typeface defaultTypeface;
    ColorStateList normalTextColor;

    ArrayList<Event> eventArray = new ArrayList<>();
    ArrayList<Integer> eventColorForCurrentDateArray = new ArrayList<>();
    EventsListColorAdapter adapter;

    ListView eventList;

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper;
    LinearLayout linearLayoutlinearLayoutIfTodayNothing;
    View viewThis;

    FloatingActionButton fab;

    Drawable textBorder;

    Button buttonLeft;
    Button buttonRight;

    TextView listViewLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // set floating button
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        fab = v.findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddNewEventActivity.class);
                startActivity(intent);
            }
        });

        // buttons to change month in calendar view
        buttonLeft = v.findViewById(R.id.btnLeft);
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalDate previousMonth = currentDateInPreview.plusMonths(-1);
                    currentDateInPreview = previousMonth;
                    setCurrentMonth(previousMonth);
                    Button buttonMonth = (Button) viewThis.findViewById(R.id.btnMonth);
                    buttonMonth.setText(TranslateMonths.translateMonth(previousMonth));
                    Button buttonYear = viewThis.findViewById(R.id.btnYear);
                    buttonYear.setText(String.valueOf(previousMonth.getYear()));
                } catch (Exception exept) {
                    Log.e("buttonLeft_Click", exept.getMessage());
                }
            }
        });
        buttonRight = (Button) v.findViewById(R.id.btnRigth);
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalDate nextMonth = currentDateInPreview.plusMonths(1);
                    currentDateInPreview = nextMonth;
                    setCurrentMonth(nextMonth);
                    Button buttonMonth = (Button) viewThis.findViewById(R.id.btnMonth);
                    buttonMonth.setText(TranslateMonths.translateMonth(nextMonth));
                    Button buttonYear = viewThis.findViewById(R.id.btnYear);
                    buttonYear.setText(String.valueOf(nextMonth.getYear()));
                } catch (Exception exept) {
                    Log.e("buttonRight_Click", exept.getMessage());
                }
            }
        });

        textBorder = ContextCompat.getDrawable(context, text_view_border);

        return v;
    }


    // set context and db helper in this place is best options
    @Override
    public void onAttach(Context cont) {
        super.onAttach(context);
        context = cont;
        przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(context);

    }

    // only in this place possible is set correct everything views in fragment
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewThis = view;

        listViewLabel = view.findViewById(R.id.textViewListLabel);
        linearLayoutlinearLayoutIfTodayNothing = view.findViewById(R.id.linearLayoutIfTodayNothing);
        eventList = view.findViewById(R.id.eventList);

        // this is for calendar cells that do not belong to the current month
        TextView textView = view.findViewById(R.id.textView1x1);
        defaultTypeface = textView.getTypeface();
        normalTextColor = textView.getTextColors();

        currentDateInPreview = LocalDate.now();

        // run method in other thread because setCurrentMonth need optimalization (to many for loops)
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurrentMonth(currentDateInPreview);
            }
        });

        // set actually month and year for current previews of months
        Button buttonMonth = view.findViewById(R.id.btnMonth);
        buttonMonth.setText(TranslateMonths.translateMonth(currentDateInPreview));

        Button buttonYear = view.findViewById(R.id.btnYear);
        buttonYear.setText(String.valueOf(currentDateInPreview.getYear()));

        // showEventList set ListView and connect to database so the situation as above
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showEventList(currentDateInPreview);
            }
        });

    }

    // after add new event or delete event ListView must be refreshed
    @Override
    public void onResume() {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showEventList(currentDateInPreview);
            }
        });

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurrentMonth(currentDateInPreview);
            }
        });
        super.onResume();
    }

    public void setCurrentMonth(LocalDate date) {

        currentDateInPreview = date;
        // build model of calendar on the date we want
        MonthViewBuild monthViewBuild = new MonthViewBuild(date);
        LocalDate[][] monthModel = monthViewBuild.buildModel();


        TableLayout layout = viewThis.findViewById(R.id.tableMonthView);

        // iterate throws row
        for (int i = 1; i < layout.getChildCount(); i++) {

            TableRow row = (TableRow) layout.getChildAt(i);

            // iterate throw elements in row
            for (int x = 0; x < row.getChildCount(); x++) {
                // row have only Linear layout with text view and frame view
                LinearLayout linearLayout = (LinearLayout) row.getChildAt(x);
                final int finalX = x;
                final int finalI = i;
                final LocalDate[][] tempMonthModel = monthModel;
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tempMonthModel[finalX][finalI - 1].equals(LocalDate.now())) {
                            listViewLabel.setText("Lista zdarzeń na dziś:");
                            showEventList(LocalDate.now());
                        } else {
                            String temp = "Lista zdarzeń na " + tempMonthModel[finalX][finalI - 1].getDayOfMonth() +
                                    "." + tempMonthModel[finalX][finalI - 1].getMonthOfYear() + "." + tempMonthModel[finalX][finalI - 1].getYear();
                            listViewLabel.setText(temp);
                            showEventList(tempMonthModel[finalX][finalI - 1]);
                        }
                    }
                });
                for (int z = 0; z < linearLayout.getChildCount(); z++) {
                    // in text view set day for current cell. If day is from current month is Bold
                    if (z == 0) {
                        TextView textView = (TextView) linearLayout.getChildAt(0);

                        int day = monthModel[x][i - 1].getDayOfMonth();
                        textView.setText(String.valueOf(day));

                        if (currentDateInPreview.getMonthOfYear() == monthModel[x][i - 1].getMonthOfYear()) {

                            textView.setTextColor(Color.BLACK);
                            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                        } else {
                            textView.setTextColor(normalTextColor);
                            textView.setTypeface(defaultTypeface, Typeface.NORMAL);
                        }

                    } else {
                        // if there is an event on a given day, a square with the event color is added to frame layout
                        LinearLayout frameEvents = (LinearLayout) linearLayout.getChildAt(z);
                        frameEvents.removeAllViews();
                        LocalDate tempDataTime = monthModel[x][i - 1];
                        List<Event> events = przypominajkaDatabaseHelper.getAllEvent();
                        if (events == null) {
                            Toast.makeText(context, "Wystąpił problem z pobraniem wydarzeń z  bazy danych", Toast.LENGTH_LONG).show();
                            Log.w("SQLite setCurrentMonth", "Wystąpił problem z pobraniem wydarzeń z  bazy danych");
                            return;
                        }
                        if (events.size() > 0) {
                            eventColorForCurrentDateArray.clear();
                            for (int j = 0; j < events.size(); j++) {

                                Event tempEvent = events.get(j);

                                boolean isEventToday = przypominajkaDatabaseHelper.checkTableForCurrentDate(tempDataTime,
                                        tempEvent.getEventName());
                                if (isEventToday) {
                                    eventColorForCurrentDateArray.add(tempEvent.getEventColor());
                                }
                            }
                        }
                        // more than 4 events cannot be display correctly, so if is more than 4 event, after three squares are three dots
                        for (int c = 0; c < eventColorForCurrentDateArray.size(); c++) {
                            if (eventColorForCurrentDateArray.size() <= 4) {
                                FrameLayout frameToAdd = new FrameLayout(context);
                                frameToAdd.setLayoutParams(new FrameLayout.LayoutParams(20, 20, 1));
                                frameToAdd.setBackgroundColor(eventColorForCurrentDateArray.get(c));

                                frameEvents.addView(frameToAdd);

                                FrameLayout frameToAddEmpty = new FrameLayout(context);
                                frameToAddEmpty.setLayoutParams(new FrameLayout.LayoutParams(10, 10, 1));
                                frameToAddEmpty.setBackgroundColor(Color.TRANSPARENT);

                                frameEvents.addView(frameToAddEmpty);
                            }
                            if (eventColorForCurrentDateArray.size() > 4) {

                                if (c < 3) {
                                    FrameLayout frameToAdd = new FrameLayout(context);
                                    frameToAdd.setLayoutParams(new FrameLayout.LayoutParams(20, 20, 1));
                                    frameToAdd.setBackgroundColor(eventColorForCurrentDateArray.get(c));

                                    frameEvents.addView(frameToAdd);

                                    FrameLayout frameToAddEmpty = new FrameLayout(context);
                                    frameToAddEmpty.setLayoutParams(new FrameLayout.LayoutParams(10, 10, 1));
                                    frameToAddEmpty.setBackgroundColor(Color.TRANSPARENT);

                                    frameEvents.addView(frameToAddEmpty);
                                } else {
                                    TextView textViewETC = new TextView(context);
                                    textViewETC.setText("...");
                                    textViewETC.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f);
                                    textViewETC.setTextColor(Color.BLACK);
                                    frameEvents.addView(textViewETC);
                                    break;
                                }
                            }
                        }
                    }
                    // if cells is today, set border for linear layout
                    if (LocalDate.now().equals(monthModel[x][i - 1])) {
                        linearLayout.setBackground(textBorder);
                    } else {
                        linearLayout.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            }
        }
    }

    // set list view with events
    public void showEventList(LocalDate localDate) {
        // get all event from database
        eventArray = przypominajkaDatabaseHelper.getEventForCurrentDay(localDate);
        if (eventArray == null) {
            Toast.makeText(context, "Wystąpił problem z pobraniem wydarzeń z baza danych", Toast.LENGTH_LONG).show();
            Log.w("SQLite showEventList", "Wystąpił problem z pobraniem wydarzeń z baza danych");
        }

        if (eventArray.size() > 0) {
            linearLayoutlinearLayoutIfTodayNothing.setVisibility(LinearLayout.GONE);


            // using custom adapter for this list view
            adapter = new EventsListColorAdapter(context, R.layout.row_list, eventArray);

            // set adapter and onClickListener for open event details activity
            eventList.setAdapter(adapter);
            eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent eventDetail = new Intent(context.getApplicationContext(), EventDetailsActivity.class);

                    EventsListColorAdapter customAdapter = (EventsListColorAdapter) parent.getAdapter();
                    Event event = customAdapter.getItem(position);

                    assert event != null;
                    eventDetail.putExtra("EVENT_NAME", event.getEventName());
                    startActivity(eventDetail);
                }
            });

        } else {

            linearLayoutlinearLayoutIfTodayNothing.setVisibility(LinearLayout.VISIBLE);
            adapter = new EventsListColorAdapter(context, R.layout.row_list, eventArray);
            eventList.setAdapter(adapter);
        }
    }

}