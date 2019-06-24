package cz.hudecekpetr.snowride.generalpurpose;

import cz.hudecekpetr.snowride.settings.Settings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Calls garbage collection from time to time if you set this up in Settings. This keeps Snowride's memory use low.
 */
public class GarbageCollectorCaller {

    public static void maybeStart() {
        if (Settings.getInstance().cbRunGarbageCollection) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleWithFixedDelay(System::gc, 300, 300, TimeUnit.SECONDS);
        }
    }
}
