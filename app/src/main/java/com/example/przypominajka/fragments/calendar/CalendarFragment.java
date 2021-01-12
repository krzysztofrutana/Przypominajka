package com.example.przypominajka.fragments.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.activities.AddNewEventActivity;
import com.example.przypominajka.adapters.EventListAdapter;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.utils.MonthViewBuild;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.R;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.TranslateMonths;
import com.example.przypominajka.databases.viewModels.EventsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import static com.example.przypominajka.R.drawable.text_view_border;
import static com.example.przypominajka.R.drawable.text_view_border_clicked;


public class CalendarFragment extends Fragment {

    private Context context;

    public LocalDate currentDateInPreview;
    public static LocalDate selectedDateInPreview;

    Typeface defaultTypeface;
    ColorStateList normalTextColor;

    ArrayList<EventModel> eventArray = new ArrayList<>();
    ArrayList<Integer> eventColorForCurrentDateArray = new ArrayList<>();

    RecyclerView eventList;

    LinearLayout linearLayoutlinearLayoutIfTodayNothing;
    View viewThis;

    FloatingActionButton fab;

    Drawable textBorder;
    Drawable textBorderWhenClicked;

    Button buttonLeft;
    Button buttonRight;

    TextView listViewLabel;

    private EventListAdapter eventListAdapter;

    private final EventsViewModel eventsViewModel = new EventsViewModel(MyPrzypominajkaApp.get());

    LinearLayout previousClickedLinearLayout;

    private static final String TAG = "CalendarFragment";

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
                    Button buttonMonth = viewThis.findViewById(R.id.btnMonth);
                    buttonMonth.setText(TranslateMonths.translateMonth(previousMonth));
                    Button buttonYear = viewThis.findViewById(R.id.btnYear);
                    buttonYear.setText(String.valueOf(previousMonth.getYear()));
                } catch (Exception exept) {
                    Log.e(TAG, "Button Left Arrow Click:" + exept.getMessage());
                }
            }
        });
        buttonRight = v.findViewById(R.id.btnRigth);
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalDate nextMonth = currentDateInPreview.plusMonths(1);
                    currentDateInPreview = nextMonth;
                    setCurrentMonth(nextMonth);
                    Button buttonMonth = viewThis.findViewById(R.id.btnMonth);
                    buttonMonth.setText(TranslateMonths.translateMonth(nextMonth));
                    Button buttonYear = viewThis.findViewById(R.id.btnYear);
                    buttonYear.setText(String.valueOf(nextMonth.getYear()));
                } catch (Exception exept) {
                    Log.e(TAG, "Button Right Arrow Click:" + exept.getMessage());
                }
            }
        });

        textBorder = ContextCompat.getDrawable(context, text_view_border);
        textBorderWhenClicked = ContextCompat.getDrawable(context, text_view_border_clicked);

        selectedDateInPreview = LocalDate.now();

        return v;
    }


    // set context and db helper in this place is best options
    @Override
    public void onAttach(Context cont) {
        super.onAttach(context);
        context = cont;


    }

    @Override
    public void onResume() {
        super.onResume();
        setCurrentMonth(currentDateInPreview);
        showEventList(currentDateInPreview, currentDateInPreview.toDateTimeAtStartOfDay().getMillis() <= LocalDate.now().toDateTimeAtStartOfDay().getMillis());

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

        // set actually month and year for current previews of months
        Button buttonMonth = view.findViewById(R.id.btnMonth);
        buttonMonth.setText(TranslateMonths.translateMonth(currentDateInPreview));

        Button buttonYear = view.findViewById(R.id.btnYear);
        buttonYear.setText(String.valueOf(currentDateInPreview.getYear()));
        RecyclerView.LayoutManager recycelLayoutManager = new
                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        eventList.setLayoutManager(recycelLayoutManager);
        eventListAdapter = new EventListAdapter(context, true);
        eventList.setAdapter(eventListAdapter);

        // using LiveData observer to refresh calendar view and recycle view when add or delete event
        eventsViewModel.getAllEvents().observe(getViewLifecycleOwner(), new Observer<List<EventModel>>() {
            @Override
            public void onChanged(List<EventModel> eventModels) {
                setCurrentMonth(currentDateInPreview);
                showEventList(currentDateInPreview, currentDateInPreview.toDateTimeAtStartOfDay().getMillis() <= LocalDate.now().toDateTimeAtStartOfDay().getMillis());

            }
        });
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
                        selectedDateInPreview = tempMonthModel[finalX][finalI - 1];
                        if (selectedDateInPreview.equals(LocalDate.now())) {
                            listViewLabel.setText("Lista zdarzeń na dziś:");
                            showEventList(LocalDate.now(), true);
                            linearLayout.setBackground(textBorder);
                            if (previousClickedLinearLayout != null) {
                                previousClickedLinearLayout.setBackground(new ColorDrawable(Color.TRANSPARENT)); // reset previous cell to transparent border
                            }
                        } else {
                            if (previousClickedLinearLayout != null) {
                                previousClickedLinearLayout.setBackground(new ColorDrawable(Color.TRANSPARENT)); // reset previous cell to transparent border
                                previousClickedLinearLayout = linearLayout;
                            }
                            String temp = "Lista zdarzeń na " + selectedDateInPreview.toString(DateTimeFormat.forPattern("dd.MM.YYYY"));
                            listViewLabel.setText(temp);
                            showEventList(selectedDateInPreview, selectedDateInPreview.toDateTimeAtStartOfDay().getMillis() <= LocalDate.now().toDateTimeAtStartOfDay().getMillis());
                            linearLayout.setBackground(textBorderWhenClicked); //set gray border in clicked cell of table

                            previousClickedLinearLayout = linearLayout;

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
                        // if there is an event on a given day, a small frame layout with the event color background is added to frame layout
                        LinearLayout frameEvents = (LinearLayout) linearLayout.getChildAt(z);
                        frameEvents.removeAllViews();
                        LocalDate tempDataTime = monthModel[x][i - 1];
                        List<EventModel> events = eventsViewModel.getAllEventsList();
                        if (events == null) {
                            Toast.makeText(context, "Wystąpił problem z pobraniem wydarzeń z  bazy danych", Toast.LENGTH_LONG).show();
                            Log.w(TAG, "setCurrentMonth: Wystąpił problem z pobraniem wydarzeń z  bazy danych");
                            return;
                        } else if (events.size() == 0) {
                            eventColorForCurrentDateArray.clear();
                            Log.d(TAG, "setCurrentMonth: Brak wydarzeń do pokazania");
                            continue;
                        } else {
                            eventColorForCurrentDateArray.clear();
                            for (EventModel tempEvent : events) {
                                boolean isEventToday = PrzypominajkaDatabaseHelper.checkTableForCurrentDate(tempDataTime,
                                        tempEvent);
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

                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.gravity = Gravity.CENTER;
                                frameEvents.addView(frameToAddEmpty);
                                frameEvents.setLayoutParams(params);
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
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    params.gravity = Gravity.CENTER;
                                    frameEvents.setLayoutParams(params);
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
    public void showEventList(LocalDate localDate, boolean useCheckbox) {
        // get all event from database
        if (eventArray != null) {
            eventArray.clear();
        }
        eventArray = PrzypominajkaDatabaseHelper.getEventForCurrentDay(localDate);
        if (eventArray == null) {
            Log.w(TAG, "showEventList: Wystąpił problem z pobraniem wydarzeń z baza danych");
            return;
        }
        if (eventArray.size() > 0) {
            linearLayoutlinearLayoutIfTodayNothing.setVisibility(LinearLayout.GONE);
            eventListAdapter.setList(eventArray, useCheckbox);
        } else {
            linearLayoutlinearLayoutIfTodayNothing.setVisibility(LinearLayout.VISIBLE);
            eventListAdapter.setList(eventArray, useCheckbox);
        }
    }


}