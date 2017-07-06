# Hacky solution to bundle the dispatcher as a jar inside the agent jar
all:
	mvn package
	cp dispatcher/target/leakchecker-dispatcher-1.0-SNAPSHOT.jar agent/src/main/resources/leakchecker-dispatcher.jar
	mvn package
