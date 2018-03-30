package exn.database.android.carousellauncher.settings;

import android.view.View;
import android.widget.TextView;

import exn.database.android.carousellauncher.R;

public class SettingSeperator extends SubSetting {
    public SettingSeperator(String title) {
        super(title, "");
    }

    public void load(String loadedValue) {}
    public String save() { return null; }
    public void localReset() {}
    public String getDisplayValue() { return null; }
    public void onTap(View view) {}
    public int getSettingViewID() { return R.layout.ss_layout_3; }
    public void display(View view) {
        this.view = view;
        ((TextView)view.findViewById(R.id.csTitle)).setText(getTitle());
    }
}
