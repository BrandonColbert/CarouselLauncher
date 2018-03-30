package exn.database.android.carousellauncher.settings;

import android.view.View;

public class SubSettingAction extends SubSetting {
    public SubSettingAction(String title, String description, SSAction action) {
        super(title, description);
        hasTapAction = true;
        tapAction = action;
    }

    public boolean useSaveSystem() { return false; }
    public void load(String loadedValue) {}
    public String save() {
        return null;
    }
    public void localReset() {}
    public String getDisplayValue() {
        return null;
    }
    public void onTap(View view) {}
}
