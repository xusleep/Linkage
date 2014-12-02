package test.framework.concurrence.utils;

public class ConcurrentUtils {
	public static synchronized void printString(String str) {
		System.out.println(str);
	}
}
