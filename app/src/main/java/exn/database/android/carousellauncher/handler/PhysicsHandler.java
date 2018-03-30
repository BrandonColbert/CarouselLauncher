package exn.database.android.carousellauncher.handler;

public class PhysicsHandler {
    public static double velocityX, velocityY;
    public static boolean goingHome;
    public static int returnSpeed;
    public static float returnSpeedZoom;
    public static int topBound, bottomBound, rightBound, leftBound;

    public static double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }

    public static void resetBounds() {
        rightBound = 0;
        leftBound = 0;
        bottomBound = 0;
        topBound = 0;
    }

    public static void checkBounds(int checkX, int checkY) {
        if(checkX > rightBound) {
            rightBound = checkX;
        }
        else if(checkX < leftBound) {
            leftBound = checkX;
        }

        if(checkY > topBound) {
            topBound = checkY;
        }
        else if(checkY < bottomBound) {
            bottomBound = checkY;
        }
    }

    public static void velocity(double friction, boolean hasBounds, boolean panning) {
        int nsX = (int)(RenderHandler.scrollX + velocityX);
        int nsY = (int)(RenderHandler.scrollY + velocityY);
        boolean insideBoundX = !hasBounds || (leftBound < nsX && nsX < rightBound);
        boolean insideBoundY = !hasBounds || (bottomBound < nsY && nsY < topBound);
        double acceleration = Math.abs(velocityX) + Math.abs(velocityY);

        if(acceleration >= 1) {
            double slowdown = 0.8;
            double frictionMod = 0;
            RenderHandler.scrollX += velocityX * (insideBoundX ? 1 : slowdown);
            RenderHandler.scrollY += velocityY * (insideBoundY ? 1 : slowdown);
            velocityX *= friction * (insideBoundX ? 1 : frictionMod);
            velocityY *= friction * (insideBoundY ? 1 : frictionMod);
        }
        else {
            velocityX = 0;
            velocityY = 0;
        }

        if(!panning) {
            double f1 = 0.015;
            if(!insideBoundX && velocityX == 0) {
                velocityX = 0;
                int speed = (int)Math.round((dist(RenderHandler.scrollX, 0, 0, 0) * f1));
                RenderHandler.scrollX += leftBound < RenderHandler.scrollX ? -speed : speed;
            }

            if(!insideBoundY && velocityY == 0) {
                velocityY = 0;
                int speed = (int)Math.round((dist(0, RenderHandler.scrollY, 0, 0) * f1));
                RenderHandler.scrollY += bottomBound < RenderHandler.scrollY ? -speed : speed;
            }
        }
    }

    public static void goHome() {
        goingHome = true;
        velocityX = 0;
        velocityY = 0;
        returnSpeed = (int)Math.round(dist(RenderHandler.scrollX, RenderHandler.scrollY, 0, 0) / 6d);
        returnSpeedZoom = Math.abs(RenderHandler.zoom-1) / 6f;
    }

    public static void returnHome() {
        if(goingHome) {
            boolean isZoomOne = RenderHandler.zoom == 1f;
            boolean isScrollCenter = RenderHandler.scrollX == 0 && RenderHandler.scrollY == 0;
            if(!isScrollCenter) {
                RenderHandler.moveToHome(returnSpeed);
            }
            else if(!isZoomOne) {
                RenderHandler.zoomToOne(returnSpeedZoom);
            }
            else {
                returnSpeed = 0;
                returnSpeedZoom = 0;
                goingHome = false;
            }
        }
    }
}
