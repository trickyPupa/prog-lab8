package gui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class LocaleListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Locale locale = (Locale) value;
        String displayName = locale.getDisplayName(locale);
        return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
    }
}