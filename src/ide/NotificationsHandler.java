package ide;
import javax.swing.JFrame;

import raven.toast.Notifications;
import raven.toast.Notifications.Location;
public class NotificationsHandler {
    private static Location normal_location;
    public static void init(JFrame frame) {
        Notifications.getInstance().setJFrame(frame);
            normal_location = Notifications.Location.BOTTOM_RIGHT;
    }

    public static void setLocation(Location location) {
        normal_location = location;
    }
    public static void showInfo(String message) {
        Notifications.getInstance().show(Notifications.Type.INFO,normal_location, message);
    }
    public static void showWarning(String message) {
        Notifications.getInstance().show(Notifications.Type.WARNING,normal_location, message);
    }
    public static void showError(String message) {
         Notifications.getInstance().show(Notifications.Type.ERROR,normal_location, message);
    }
}