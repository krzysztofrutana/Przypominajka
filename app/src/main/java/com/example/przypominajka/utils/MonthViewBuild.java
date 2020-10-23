package com.example.przypominajka.utils;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;


public class MonthViewBuild {
    private LocalDate dateTime;
    private LocalDate firstDayOfMonth;
    private int firstDayOfMonthNumber;


    public MonthViewBuild(LocalDate dateTimeNow) {
        this.dateTime = dateTimeNow;
    }

    private void setNecessaryProps() {
        // set date to first day of month
        firstDayOfMonth = dateTime.minusDays(dateTime.dayOfMonth().get() - 1);

        // check what number of day of week have first day of month
        firstDayOfMonthNumber = firstDayOfMonth.dayOfWeek().get();

    }

    public LocalDate[][] buildModel() {
        this.setNecessaryProps();

        // this is needed only for first loop to set where in start of calendar
        int numberOfFirstDay = -1;

        // two dimension because is 7 days per week and sometimes needed is 6 row of calendar for one month
        LocalDate[][] monthView = new LocalDate[7][6];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (numberOfFirstDay == -1) {
                    switch (firstDayOfMonthNumber) {
                        case 1:
                            monthView[0][0] = firstDayOfMonth;
                            numberOfFirstDay = 0;
                            break;
                        case 2:
                            monthView[1][0] = firstDayOfMonth;
                            numberOfFirstDay = 1;
                            break;
                        case 3:
                            monthView[2][0] = firstDayOfMonth;
                            numberOfFirstDay = 2;
                            break;
                        case 4:
                            monthView[3][0] = firstDayOfMonth;
                            numberOfFirstDay = 3;
                            break;
                        case 5:
                            monthView[4][0] = firstDayOfMonth;
                            numberOfFirstDay = 4;
                            break;
                        case 6:
                            monthView[5][0] = firstDayOfMonth;
                            numberOfFirstDay = 5;
                            break;
                        case 7:
                            monthView[6][0] = firstDayOfMonth;
                            numberOfFirstDay = 6;
                            break;
                        default:
                            break;
                    }
                    j = numberOfFirstDay;
                } else {

                    firstDayOfMonth = firstDayOfMonth.plusDays(1);
                    monthView[j][i] = firstDayOfMonth;

                }
            }
        }
        // set empty field before first day of month in monthView table
        if (numberOfFirstDay != 0) {
            firstDayOfMonth = dateTime.plusDays(-(dateTime.dayOfMonth().get() - 1));
            for (int i = numberOfFirstDay - 1; i >= 0; i--) {
                firstDayOfMonth = firstDayOfMonth.plusDays(-1);
                monthView[i][0] = firstDayOfMonth;
            }
        }
        Log.d("Klasa MonthViewBuild", "model kompletny");
        return monthView;
    }

}

