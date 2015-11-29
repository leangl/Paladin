package mobi.tattu.utils.events;

/**
 * Created by Leandro on 19/11/2015.
 */
public class AppUpdated {

    public final int previousVersion;
    public final int currentVersion;

    public AppUpdated(int currentVersion, int previousVersion) {
        this.currentVersion = currentVersion;
        this.previousVersion = previousVersion;
    }
}
