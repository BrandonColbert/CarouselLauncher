package exn.database.android.carousellauncher.app;

import android.graphics.Canvas;

import java.util.List;

import exn.database.android.carousellauncher.handler.PhysicsHandler;
import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.settings.SettingsManager;
import exn.database.android.carousellauncher.settings.SubSettingDualInteger;

public class StyleCarousel extends AppStyleTemplate {
    public void findLocation(List<App2D> apps) {
        int appSize = ViewHandler.screenScale;
        SubSettingDualInteger space = SettingsManager.spacing.asDualInteger();
        
        int step = 0;
        int revolutions = 1;
        int angles = 6 * revolutions;
        boolean firstApp = true;

        for (App2D app : apps) {
            if(firstApp) {
                app.setPosition(0, 0);
                firstApp = false;
            } else {
                double angle = 270 + (360 / angles * step);
                double radAngle = Math.toRadians(angle);

                int circleX = (int) (appSize * (1 + space.getValue(true) * 0.01) * Math.cos(radAngle));
                int circleY = (int) (appSize * (1 + space.getValue(false) * 0.01) * Math.sin(radAngle));
                int appX = circleX * revolutions;
                int appY = circleY * revolutions;

                app.setPosition(appX, appY);
                PhysicsHandler.checkBounds(appX, appY);

                step++;
                if (step >= angles) {
                    step = 0;
                    revolutions++;
                    angles = 6 * revolutions;
                }
            }
        }
    }

    public void showBoundary(Canvas canvas) {
        float radius = ((PhysicsHandler.topBound - PhysicsHandler.bottomBound) + (PhysicsHandler.rightBound - PhysicsHandler.leftBound)) * 0.25f + ViewHandler.appSize * 2;
        canvas.drawCircle(RenderHandler.centerX + RenderHandler.scrollX, RenderHandler.centerY + RenderHandler.scrollY, radius, RenderHandler.paint);
    }
}
