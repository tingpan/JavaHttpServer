package server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;

public class ConcreteLogCenter extends Observable {
    private static final ConcreteLogCenter logCenter = new ConcreteLogCenter();
    private static boolean logMode = false;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat formatDate = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z");

    // Singleton
    private ConcreteLogCenter() {

    }

    public static ConcreteLogCenter getInstance() {
        return logCenter;
    }

    public static void setLogMode(boolean mode) {
        logMode = mode;
    }

    public void setLog(String logContent) {
        if (logMode) {
            setChanged();
            notifyObservers(logContent);
        }
    }

    public void startLog() {
        if (logMode) {
            setChanged();
            notifyObservers(formatDate.format(calendar.getTime())
                    + ", $, starting registering\r\n");
        }
    }
}
