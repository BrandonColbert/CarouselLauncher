package exn.database.android.carousellauncher.settings;

import android.view.View;

public class SubSettingString extends SubSetting {
    private String defaultValue;
    protected String value;

    public SubSettingString(String key, String title, String description, String value) {
        super(key, title, description);
        defaultValue = value;
    }

    public String getValue() {
        return value;
    }

    public void load(String loadedValue) {
        value = loadedValue;
    }

    public String save() {
        return value;
    }

    public void localReset() {
        value = defaultValue;
    }

    public String getDisplayValue() {
        return value;
    }

    public void onTap(View view) {
    }
}
