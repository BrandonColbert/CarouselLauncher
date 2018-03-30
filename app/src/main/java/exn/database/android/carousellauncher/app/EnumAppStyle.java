package exn.database.android.carousellauncher.app;

import exn.database.android.carousellauncher.settings.SubSetting;

public enum EnumAppStyle implements SubSetting.DisplayableValue, SubSetting.ObjectValue {
    CAROUSEL("Carousel", new StyleCarousel()),
    ROWS("Rows", new StyleRows()),
    HONEYCOMB("Honeycomb", new StyleHoneycomb());

    private final String displayValue;
    private final AppStyleTemplate styleValue;

    EnumAppStyle(String display, AppStyleTemplate style) {
        displayValue = display;
        styleValue = style;
    }

    public Object getObjectValue() {
        return styleValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
