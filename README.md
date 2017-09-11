# Leakchecker

This project creates a java agent used for checking for certain memory leaks.
Specifically it checks for `new java.util.zip.Inflater` with no following `end()`
call which might indicate a native memory leak.
The agent prints out potential new leaks every minute.

# Building

```
make all
```

# Running with test class

The test class is just a simple class that allocates Inflater and sleeps. It should print out that it found
one leak, and the stacktrace for the allocation.
```
java -javaagent:agent/target/leakchecker-agent-1.0-SNAPSHOT.jar -jar agent/target/leakchecker-agent-1.0-SNAPSHOT.jar
```
Example output:
```
Potential leaks:
Total: 1
New:
java.lang.RuntimeException: Created: java.util.zip.Inflater@57d5872c
        at java.util.zip.Inflater.<init>(Unknown Source)
        at java.util.zip.Inflater.<init>(Unknown Source)
        at co.elastic.leakchecker.TestMain.main(TestMain.java:8)
```

# Running with Elasticsearch
Copy the agent into the Elasticsearch install directory.
```
ES_JAVA_OPTS="-javaagent:leakchecker-agent-1.0-SNAPSHOT.jar" bin/elasticsearch
```
It will report some allocations during startup and plugin loading, which will stay allocated due to various caches. 
The important part is that it doesn't continue to increase over time.
