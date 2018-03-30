package exn.database.android.carousellauncher.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;

import exn.database.android.carousellauncher.handler.SaveHandler;

public class AppUsage extends AppContainer {
    public long timesOpened;

    public AppUsage(Parcel in) {
        super(in);
        timesOpened = in.readLong();
        saveUsage();
    }

    public AppUsage(String name, String label, Drawable icon) {
        super(name, label, icon);
        loadData();
    }

    public boolean launchApp(Activity activity, PackageManager manager) {
        timesOpened++;
        saveUsage();
        return super.launchApp(activity, manager);
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(timesOpened);
    }

    public void saveUsage() {
        SaveHandler.saveData("app_usage." + name + ".times_opened", timesOpened);
    }

    public void loadData() {
        timesOpened = SaveHandler.loadDataAsLong("app_usage." + name + ".times_opened");
    }
}
