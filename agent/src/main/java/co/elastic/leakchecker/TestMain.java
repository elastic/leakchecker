package co.elastic.leakchecker;

import java.util.zip.Inflater;

public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("New inflater");
        Inflater inflater = new Inflater();
        Thread.sleep(100000);
    }
}
