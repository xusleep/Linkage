package test.framework.summary;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestFailure;
import junit.framework.TestResult;

public class SummaryTestResult {

	private HashMap hmFailures;
	private HashMap hmErrors;
	private TestResult tr;

	public SummaryTestResult(TestResult tr) {
		this.tr = tr;
		hmFailures = new HashMap(tr.failureCount());
		hmErrors = new HashMap(tr.errorCount());
		executeSummary();
	}
	
	public HashMap getHmFailures() {
		return hmFailures;
	}

	public HashMap getHmErrors() {
		return hmErrors;
	}

	public void printFailures() {
		printDefects(tr.failures());
	}

	public void executeSummary() {
		Enumeration fail = tr.failures();
		for (int i = 1; fail.hasMoreElements(); i++) {
			TestFailure tf = (TestFailure) fail.nextElement();
			Object objCount = hmFailures.get(tf.failedTest().toString());
			if (objCount != null) {
				long lcount = (long) objCount;
				lcount = lcount + 1;
				hmFailures.put(tf.failedTest().toString(), lcount);
			} else {
				hmFailures.put(tf.failedTest().toString(), 1L);
			}
		}

		fail = tr.errors();
		for (int i = 1; fail.hasMoreElements(); i++) {
			TestFailure tf = (TestFailure) fail.nextElement();
			Object objCount = hmErrors.get(tf.failedTest().toString());
			if (objCount != null) {
				long lcount = (long) objCount;
				lcount = lcount + 1;
				hmErrors.put(tf.failedTest().toString(), lcount);
			} else {
				hmErrors.put(tf.failedTest().toString(), 1L);
			}
		}
	}

	public void printErrosr() {
		printDefects(tr.errors());
	}

	public static void printDefects(Enumeration fail) {
		for (int i = 1; fail.hasMoreElements(); i++)
			printDefect((TestFailure) fail.nextElement(), i);
	}

	public static void printDefect(TestFailure fail, int count) {
		System.out.println(fail.failedTest().toString());
	}

}
