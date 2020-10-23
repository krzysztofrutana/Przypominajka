package com.example.przypominajka.utils;

import org.joda.time.LocalDate;

public class TranslateMonths {

    // needed because in Polish getMonthName as text generate month with "-a" suffix
    static public String translateMonth(LocalDate dateToTranslate) {
        String monthName = "";
        switch (dateToTranslate.getMonthOfYear()) {
            case 1:
                monthName = "STYCZEŃ";
                break;
            case 2:
                monthName = "LUTY";
                break;
            case 3:
                monthName = "MARZEC";
                break;
            case 4:
                monthName = "KWIECIEŃ";
                break;
            case 5:
                monthName = "MAJ";
                break;
            case 6:
                monthName = "CZERWIEC";
                break;
            case 7:
                monthName = "LIPIEC";
                break;
            case 8:
                monthName = "SIERPIEŃ";
                break;
            case 9:
                monthName = "WRZESIEŃ";
                break;
            case 10:
                monthName = "PAŹDZIERNIK";
                break;
            case 11:
                monthName = "LISTOPAD";
                break;
            case 12:
                monthName = "GRUDZIEŃ";
                break;


        }
        return monthName;
    }
}
