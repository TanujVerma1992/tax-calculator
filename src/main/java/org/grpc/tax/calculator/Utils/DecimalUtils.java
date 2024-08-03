package org.grpc.tax.calculator.Utils;

import java.text.DecimalFormat;

public class DecimalUtils {

    private DecimalUtils() {
    }

    public static String formatToPrecision(Double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(value);
    }


}
