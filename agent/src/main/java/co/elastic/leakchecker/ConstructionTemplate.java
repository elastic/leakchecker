package co.elastic.leakchecker;

import net.bytebuddy.asm.Advice;

public class ConstructionTemplate {

    @Advice.OnMethodExit
    static void exit(@Advice.This Object self) {
        Throwable throwable = new RuntimeException("Created: " + self).fillInStackTrace();
        AllocationRecorder.allocated(self, throwable);
    }

}
