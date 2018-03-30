package exn.database.android.carousellauncher.settings;

import android.view.View;

public class SubSettingBoolean extends SubSetting {
    private boolean defaultValue;
    private boolean value;

    public SubSettingBoolean(String key, String title, String description, boolean value) {
        super(key, title, description);
        defaultValue = value;
    }

    public boolean getValue() {
        return value;
    }

    public void load(String loadedValue) {
        value = Boolean.valueOf(loadedValue);
    }

    public String save() {
        return String.valueOf(value);
    }

    public void localReset() {
        value = defaultValue;
    }

    public String getDisplayValue() {
        return value ? "On" : "Off";
    }

    public void onTap(View view) {
        value = !value;
        SettingsManager.saveSetting(this);
        display(view);
    }
}
