"C:\Program Files\Java\jdk1.8.0_45\bin\javac.exe" -classpath "C:\Program Files\Java\jdk1.8.0_45\jre\lib\plugin.jar" "JavaSocketBridge.java" "Listener.java"
"C:\Program Files\Java\jdk1.8.0_45\bin\jar.exe" cvf JavaSocketBridge.jar "JavaSocketBridge.class" "Listener.class"
"C:\Program Files\Java\jdk1.8.0_45\bin\jarsigner.exe" JavaSocketBridge.jar myNewKey
move JavaSocketBridge.jar ..
del *.class
