package gui.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import static java.lang.Math.abs;

public class NumberFilter extends DocumentFilter {
    private final int type;

    public NumberFilter(int type){
        this.type = type;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (isNumeric(string)) {
            double number = Double.parseDouble(string);
            if (type == 0 && abs(number) > Integer.MAX_VALUE) {
                int k = number < 0 ? -1 : 1;
                super.insertString(fb, offset, String.valueOf(k * Integer.MAX_VALUE), attr);
            }
            if (type == 1 && abs(number) > Long.MAX_VALUE) {
                int k = number < 0 ? -1 : 1;
                super.insertString(fb, offset, String.valueOf(k * Long.MAX_VALUE), attr);
            }
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (isNumeric(text)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}