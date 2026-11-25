package group.backgroundgremlin.clankcon.probe;

import android.app.Application;
import android.webkit.WebView;

public final class ProbeApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        String process = Application.getProcessName();
        if (process.endsWith(":profile_a")) WebView.setDataDirectorySuffix("profile_a");
        else if (process.endsWith(":profile_b")) WebView.setDataDirectorySuffix("profile_b");
    }
}
