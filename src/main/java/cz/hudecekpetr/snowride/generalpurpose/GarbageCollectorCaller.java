package cz.hudecekpetr.snowride.generalpurpose;

import cz.hudecekpetr.snowride.semantics.Setting;
import cz.hudecekpetr.snowride.settings.Settings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GarbageCollectorCaller {

    public static void maybeStart() {
        if (Settings.getInstance().cbRunGarbageCollection) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleWithFixedDelay(System::gc, 300,300, TimeUnit.SECONDS);
        }
    }
}
