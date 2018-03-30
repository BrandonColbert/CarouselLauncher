package exn.database.android.carousellauncher.settings;

public class SubSettingPercent extends SubSettingInteger {
    public SubSettingPercent(String key, String title, String description, int baseValue, int maxPercent, int minPercent, int step) {
        super(key, title, description, 0, baseValue, 0, step);
        max = maxPercent;
        min = minPercent;
    }

    public void localReset() {
        value = 100;
    }

    public double getPercentage() {
        return baseValue * value * 0.0001;
    }

    public String getDisplayValue() {
        return String.valueOf(value)+"%";
    }
}
