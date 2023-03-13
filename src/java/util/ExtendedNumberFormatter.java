package util;

import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import java.text.ParseException;

public class ExtendedNumberFormatter extends NumberFormatter {
    public ExtendedNumberFormatter(NumberFormat f) {
        super(f);
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        if (text == "") {
            return null;
        }
        return super.stringToValue(text);
    }
}