package ee.forgr.capacitor_updater;

import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.IOException;
import java.util.ArrayList;

@CapacitorPlugin(name = "CapacitorUpdater")
public class CapacitorUpdaterPlugin extends Plugin{
// public class CapacitorUpdaterPlugin extends Plugin, Application.ActivityLifecycleCallbacks {
    private CapacitorUpdater implementation;
    // private SharedPreferences.Editor editor;
    // private var autoUpdateUrl = ""
    // private var autoUpdate = false

    @Override
    public void load() {
        super.load();
        // this.editor = prefs.edit();
        implementation = new CapacitorUpdater(this.getContext());
        // this.autoUpdateUrl = getConfigValue("autoUpdateUrl") || "";
        // if (this.autoUpdateUrl != "") {
        //     this.autoUpdate = true
        // registerActivityLifecycleCallbacks(AppLifecycleTracker());
        // }
    }

    // override fun onActivityStarted(activity: Activity?) {
    //     Log.i("CapacitorUpdater", "on foreground");
    //     URL u = new URL(autoUpdateUrl);
    //     JSObject res = implementation.getLatest(u);
    //     if (!res.url) {
    //         return;
    //     }
    //     String name = implementation.getVersionName();
    //     if (res?.version != name) {
    //         String dl = implementation.download(url: downloadUrl);
    //         editor.putString("nextVersion", dl);
    //         editor.putString("nextVersionName", res?.version);
    //     }
    // }

    // override fun onActivityStopped(activity: Activity?) {
    //     Log.i("CapacitorUpdater", "on  background");
    //     String nextVersion = prefs.getString("nextVersion", "");
    //     String nextVersionName = prefs.getString("nextVersionName", "");
    //     if (nextVersion != "" && nextVersionName != "") {
    //         String res = implementation.set(version: nextVersion, versionName: nextVersionName)
    //         if (res) {
    //             if (this._reload()) {
    //                 Log.i("CapacitorUpdater", "Auto update to VersionName: " + nextVersionName + ", Version: " + nextVersion);
    //             }
    //             editor.putString("nextVersion", "");
    //             editor.putString("nextVersionName", "");
    //         }

    //     }
    // }

    @PluginMethod
    public void download(PluginCall call) {
        String url = call.getString("url");

        String res = implementation.download(url);
        if ((res) != null) {
            JSObject ret = new JSObject();
            ret.put("version", res);
            call.resolve(ret);
        } else {
            call.reject("download failed");
        }
    }

    private boolean _reload() {
        String pathHot = implementation.getLastPathHot();
        Log.i("CapacitorUpdater", "getLastPathHot : " + pathHot);
        this.bridge.setServerBasePath(pathHot);
        return true;
    }
    @PluginMethod
    public void reload(PluginCall call) {
        if (this._reload()) {
            call.resolve();
        } else {
            call.reject("reload failed");
        }
    }
    @PluginMethod
    public void set(PluginCall call) {
        String version = call.getString("version");
        String versionName = call.getString("versionName", version);
        Boolean res = implementation.set(version, versionName);

        if (!res) {
            call.reject("Update failed, version don't exist");
        } else {
            String pathHot = implementation.getLastPathHot();
            Log.i("CapacitorUpdater", "getLastPathHot : " + pathHot);
            this.bridge.setServerBasePath(pathHot);
            call.resolve();
        }
    }

    @PluginMethod
    public void delete(PluginCall call) {
        String version = call.getString("version");
        try {
            Boolean res = implementation.delete(version);

            if (res) {
                call.resolve();
            } else {
                call.reject("Delete failed, version don't exist");
            }
        } catch(IOException ex) {
            Log.e("CapacitorUpdater", "An unexpected error occurred during deletion of folder. Message: " + ex.getMessage());
            call.reject("An unexpected error occurred during deletion of folder.");
        }
    }

    @PluginMethod
    public void list(PluginCall call) {
        ArrayList<String> res = implementation.list();
        JSObject ret = new JSObject();
        ret.put("versions", new JSArray(res));
        call.resolve(ret);
    }

    @PluginMethod
    public void reset(PluginCall call) {
        implementation.reset();
        String pathHot = implementation.getLastPathHot();
        this.bridge.setServerAssetPath(pathHot);
        call.resolve();
    }

    @PluginMethod
    public void current(PluginCall call) {
        String pathHot = implementation.getLastPathHot();
        JSObject ret = new JSObject();
        String current = pathHot.length() >= 10 ? pathHot.substring(pathHot.length() - 10) : "default";
        ret.put("current", current);
        call.resolve(ret);
    }

    @PluginMethod
    public void versionName(PluginCall call) {
        String name = implementation.getVersionName();
        JSObject ret = new JSObject();
        ret.put("versionName", name);
        call.resolve(ret);
    }
}
