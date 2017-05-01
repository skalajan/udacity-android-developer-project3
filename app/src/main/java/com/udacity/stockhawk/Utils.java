package com.udacity.stockhawk;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by kjs566 on 26.4.2017.
 */
public class Utils {
    private static DecimalFormat sPercentageChangeFormat = null;
    private static DecimalFormat sDollarsChangeFormat = null;

    public static DecimalFormat getPercentageChangeFormat(){
        if(sPercentageChangeFormat == null) {
            sPercentageChangeFormat = (java.text.DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            sPercentageChangeFormat.setMaximumFractionDigits(2);
            sPercentageChangeFormat.setMinimumFractionDigits(2);
            sPercentageChangeFormat.setPositivePrefix("+");
        }
        return sPercentageChangeFormat;
    }

    public static DecimalFormat getDollarsChangeFormat(){
        if(sDollarsChangeFormat == null) {
            sDollarsChangeFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            sDollarsChangeFormat.setPositivePrefix("+$");
        }
        return sDollarsChangeFormat;
    }

    public static DecimalFormat getDollarsFormat(){
        return (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }
}
