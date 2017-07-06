package co.elastic.leakchecker;

import net.bytebuddy.asm.Advice;

public class CloseTemplate {

    @Advice.OnMethodEnter
    static void enter(@Advice.This Object self) {
        AllocationRecorder.freed(self);
    }

}
