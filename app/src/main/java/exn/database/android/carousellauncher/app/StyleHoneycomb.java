package exn.database.android.carousellauncher.app;

import android.graphics.Canvas;

import java.util.List;

import exn.database.android.carousellauncher.handler.PhysicsHandler;
import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.settings.SettingsManager;
import exn.database.android.carousellauncher.settings.SubSettingDualInteger;

public class StyleHoneycomb extends AppStyleTemplate {
    private final double hexX[] = {Math.cos(0), Math.cos(60 * Math.PI / 180), Math.cos(120 * Math.PI / 180), Math.cos(Math.PI), Math.cos(240 * Math.PI / 180), Math.cos(300 * Math.PI / 180), Math.cos(360 * Math.PI / 180)};
    private final double hexY[] = {Math.sin(0), Math.sin(60 * Math.PI / 180), Math.sin(120 * Math.PI / 180), Math.sin(Math.PI), Math.sin(240 * Math.PI / 180), Math.sin(300 * Math.PI / 180), Math.sin(360 * Math.PI / 180)};

    public void findLocation(List<App2D> apps) {
        int appSize = ViewHandler.screenScale;
        SubSettingDualInteger space = SettingsManager.spacing.asDualInteger();
        int spaceX = space.getValue(true);
        int spaceY = space.getValue(false);

        boolean firstApp = true;
        int splice = 1;
        int step = 0;

        double width = appSize * (1 + spaceX * 0.01);
        double height = appSize * (1 + spaceY * 0.01);

        for(App2D app : apps) {
            if (firstApp) {
                app.setPosition(0, 0);
                firstApp = false;
            } else {
                int end = 0;
                int num = 0;

                for(int i = 0; i < 6; i++) {
                    if(step < splice * (i + 1)) {
                        end = splice * (i + 1);
                        num = i;
                        break;
                    }
                }

                int appX = (int)(width * (hexX[num + 1] + (hexX[num] - hexX[num + 1]) * (end - step) / splice) * splice);
                int appY = (int)(height * (hexY[num + 1] + (hexY[num] - hexY[num + 1]) * (end - step) / splice) * splice);
                app.setPosition(appX, appY);
                PhysicsHandler.checkBounds(appX, appY);

                if(num == 5 && end - step == 1) {
                    step = 0;
                    splice++;
                } else {
                    step++;
                }
            }
        }
    }

    public void showBoundary(Canvas canvas) {
        float radius = ((PhysicsHandler.topBound - PhysicsHandler.bottomBound) + (PhysicsHandler.rightBound - PhysicsHandler.leftBound)) * 0.25f + ViewHandler.appSize * 2;
        canvas.drawCircle(RenderHandler.centerX + RenderHandler.scrollX, RenderHandler.centerY + RenderHandler.scrollY, radius, RenderHandler.paint);
    }
}
