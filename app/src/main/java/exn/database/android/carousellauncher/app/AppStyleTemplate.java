package exn.database.android.carousellauncher.app;

import android.graphics.Canvas;

import java.util.List;

import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.settings.SettingsManager;
import exn.database.android.carousellauncher.settings.SubSettingDualPercent;

public abstract class AppStyleTemplate {
    public abstract void findLocation(List<App2D> apps);
    public abstract void showBoundary(Canvas canvas);

    public void edgeAction(App2D app) {
        if(SettingsManager.edgeEffect.asBoolean().getValue()) {
            int appSize = ViewHandler.appSize;
            int x = app.getStaticX();
            int y = app.getStaticY();
            double distX = Math.abs(Math.abs(x + RenderHandler.scrollX) - RenderHandler.centerX / RenderHandler.zoom);
            double distY = Math.abs(Math.abs(y + RenderHandler.scrollY) - RenderHandler.centerY / RenderHandler.zoom);
            if(x + appSize < -RenderHandler.centerX / RenderHandler.zoom - RenderHandler.scrollX ||
               x - appSize > RenderHandler.centerX / RenderHandler.zoom - RenderHandler.scrollX ||
               y + appSize < -RenderHandler.centerY / RenderHandler.zoom - RenderHandler.scrollY ||
               y - appSize > RenderHandler.centerY / RenderHandler.zoom - RenderHandler.scrollY) {
                app.setScale(0);
            } else if(distX < appSize || distY < appSize) {
                double shrink = (distX < distY ? distX : distY) / appSize;
                app.setScale(shrink > 0.3 ? shrink : 0.3);
            } else {
                app.setScale(1);
            }
        } else {
            int x = app.getStaticX();
            int y = app.getStaticY();
            double zFactor = 1 / RenderHandler.zoom;
            double centerXZ = RenderHandler.centerX * zFactor;
            double centerYZ = RenderHandler.centerY * zFactor;
            SubSettingDualPercent shrink = SettingsManager.shrinkFactor.asDualPercent();
            double distX = (centerXZ - (centerXZ - Math.abs(x + RenderHandler.scrollX))) / centerXZ * shrink.getPercentage(true);
            double distY = (centerYZ - (centerYZ - Math.abs(y + RenderHandler.scrollY))) / centerYZ * shrink.getPercentage(false);
            app.setScale(1 - ((distX + distY) * 0.5));
        }
    }
}
