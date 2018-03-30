package exn.database.android.carousellauncher.app;

import exn.database.android.carousellauncher.settings.SubSetting;

public enum EnumAppSorting implements SubSetting.DisplayableValue {
    NAME("Name"),
    TIMES_OPENED("Launch Amount"),
    USAGE("Usage");

    private final String localName;
    EnumAppSorting(String name) {
        localName = name;
    }
    public String getDisplayValue() { return localName; }
}
