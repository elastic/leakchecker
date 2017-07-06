package co.elastic.leakchecker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
  Class where the agent can put state. Needs to be loaded in the bootstrap classloader so that
  java.* classes can access it.
 */
public class AllocationRecorder {

    public static Map<Object, Throwable> allocations = new ConcurrentHashMap<>();

    public static void allocated(Object object, Throwable stacktrace) {
        allocations.put(object, stacktrace);
    }

    public static void freed(Object object) {
        allocations.remove(object);
    }
}
