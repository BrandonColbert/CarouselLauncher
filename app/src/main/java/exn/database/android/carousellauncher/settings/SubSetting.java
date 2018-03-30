package exn.database.android.carousellauncher.settings;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import exn.database.android.carousellauncher.R;
import exn.database.android.carousellauncher.main.CarouselLauncher;

public abstract class SubSetting {
    private boolean usable;
    private String unusableMessage;
    protected View view;
    protected boolean hasTapAction;
    protected SSAction tapAction;
    private final String key;
    private final String title;
    private final String desc;

    public SubSetting(String title, String description) {
        this("", title, description);
    }

    public SubSetting(String key, String title, String description) {
        this.key = key;
        this.title = title;
        this.desc = description;
        hasTapAction = false;
        usable = true;
        unusableMessage = title + " cannot be used at the moment";
    }

    public void setUsable(boolean value) {
        usable = value;
    }

    public boolean isUsable() {
        return usable;
    }

    public SubSetting setUnusableMessage(String msg) {
        unusableMessage = msg;
        return this;
    }

    public String getUnusableMessage() {
        return unusableMessage;
    }

    public boolean useSaveSystem() { return true; }
    public int getSettingViewID() { return R.layout.ss_layout; }
    public View getView() { return view; }

    public abstract void load(String loadedValue);
    public abstract String save();
    public abstract void localReset();
    public abstract String getDisplayValue();
    public abstract void onTap(View view);
    public void onExit() {}

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return desc;
    }
    public String getKey() { return "settings." + key; }

    public void display(View view) {
        this.view = view;
        TextView title = (TextView)view.findViewById(R.id.csTitle);
        title.setText(getTitle());
        title.setTextColor(ContextCompat.getColor(CarouselLauncher.getLauncher(), usable ? R.color.colorAccent : R.color.colorAccentDark));
        TextView desc = (TextView)view.findViewById(R.id.csDesc);
        desc.setText(getDescription());
        TextView value = (TextView)view.findViewById(R.id.csValue);
        value.setText(useSaveSystem() && usable ? getDisplayValue() : "");
    }

    public void reset() {
        localReset();
        SettingsManager.saveSetting(this);
    }

    public SubSetting setTapAction(SSAction action) {
        hasTapAction = true;
        tapAction = action;
        return this;
    }

    public void extraSave() {}

    public void extraLoad() {}

    public SubSettingBoolean asBoolean() {
        return (SubSettingBoolean)this;
    }
    public SubSettingEnum asEnum() {
        return (SubSettingEnum)this;
    }
    public SubSettingInteger asInteger() {
        return (SubSettingInteger)this;
    }
    public SubSettingPercent asPercent() {
        return (SubSettingPercent)this;
    }
    public SubSettingString asString() {
        return (SubSettingString)this;
    }
    public SubSettingDualInteger asDualInteger() { return (SubSettingDualInteger)this; }
    public SubSettingDualPercent asDualPercent() { return (SubSettingDualPercent)this; }

    public interface DisplayableValue {
        String getDisplayValue();
    }

    public interface ObjectValue {
        Object getObjectValue();
    }

    public interface SSAction {
        void action();
    }
}
