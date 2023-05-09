package util;

import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import java.text.ParseException;

public class ExtendedNumberFormatter extends NumberFormatter {

    //Responsible: Michael

    //This class functions as a NumberFormatter where empty strings are allowed (in case the user wants to delete everything from a text field).

    public ExtendedNumberFormatter(NumberFormat f) {
        super(f);
    }

    //Returns null if string is empty.
    @Override
    public Object stringToValue(String text) throws ParseException {
        if (text == "") {
            return null;
        }
        return super.stringToValue(text);
    }
}