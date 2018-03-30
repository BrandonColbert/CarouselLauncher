package exn.database.android.carousellauncher.settings;

public class SubSettingDualPercent extends SubSettingDualInteger {
    public SubSettingDualPercent(String key, String title, String description, int baseValue, int maxPercent, int minPercent, int step) {
        super(key, title, description, 0, baseValue, 0, step);
        max = maxPercent;
        min = minPercent;
    }

    public void localReset() {
        value = 100;
        valueVertical = 100;
    }

    public double getPercentage(boolean direction) {
        return baseValue * (direction ? value : valueVertical) * 0.0001;
    }

    public String getDisplayValue() {
        return value + "% | " + valueVertical + "%";
    }
}
