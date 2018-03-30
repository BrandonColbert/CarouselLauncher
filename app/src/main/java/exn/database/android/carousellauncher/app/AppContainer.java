package exn.database.android.carousellauncher.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;

import exn.database.android.carousellauncher.handler.AppHandler;
import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.main.CarouselLauncher;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class AppContainer {
    public String name;
    public String label;
    public Drawable icon;
    public boolean hidden;
    public boolean deletable;

    public AppContainer(Parcel in) {
        name = in.readString();
        label = in.readString();
        icon = new BitmapDrawable(CarouselLauncher.getLauncher().getResources(), (Bitmap)in.readParcelable(getClass().getClassLoader()));
        deletable = AppHandler.canAppBeDeleted(this);
    }

    public AppContainer(String name, String label, Drawable icon) {
        this.name = name;
        this.label = label;
        this.icon = icon;
        if(SettingsManager.circularApps.asBoolean().getValue() && icon instanceof BitmapDrawable) {
            this.icon = RenderHandler.makeIconCircular(this);
        }
        deletable = AppHandler.canAppBeDeleted(this);
    }

    public boolean launchApp(Activity activity, PackageManager manager) {
        if(name.equalsIgnoreCase(CarouselLauncher.getLauncher().getPackageName())) {
            return true;
        }

        Intent launchIntent = manager.getLaunchIntentForPackage(name);
        if(launchIntent != null) {
            activity.startActivity(launchIntent);
            return true;
        }

        return false;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(label);
        Bitmap bitmap;
        if(icon instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) icon).getBitmap();
        }
        else {
            bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(),icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            icon.draw(canvas);
        }
        dest.writeValue(bitmap);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o){
        if (o instanceof AppContainer){
            AppContainer temp = (AppContainer)o;
            return name.equalsIgnoreCase(temp.name);
        }
        return false;
    }
}
