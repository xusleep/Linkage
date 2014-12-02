package test.framework.printer;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;

public class BaseResultPrinter implements TestListener {

	public static final String TEST_SUCCESS = "S";

	public static final String TEST_ERROR = "E";

	public static final String TEST_FAILURE = "F";

	protected PrintStream fWriter = null;

	protected LinkedHashMap<String, String> fTestStats = null;

	int fColumn = 0;

	public BaseResultPrinter() {
		fWriter = System.out;
		fTestStats = new LinkedHashMap<String, String>(10);
	}

	public BaseResultPrinter(PrintStream writer) {
		fWriter = writer;
		fTestStats = new LinkedHashMap<String, String>(10);
	}

	public synchronized void print(TestResult result, long runTime) {
		printHeader(runTime);
		printErrors(result);
		printFailures(result);
		printFooter(result);
	}

	public void printWaitPrompt() {
		getWriter().println();
		getWriter().println("<RETURN> to continue");
	}

	protected void printHeader(long runTime) {
		getWriter().println();
		getWriter().println("Time: " + elapsedTimeAsString(runTime));
	}

	protected void printErrors(TestResult result) {
		printDefects(result.errors(), result.errorCount(), "error");
	}

	protected void printFailures(TestResult result) {
		printDefects(result.failures(), result.failureCount(), "failure");
	}

	protected void printDefects(Enumeration fail, int count, String type) {
		if (count == 0)
			return;
		if (count == 1)
			getWriter().println("There was " + count + " " + type + ":");
		else
			getWriter().println("There were " + count + " " + type + "s:");
		for (int i = 1; fail.hasMoreElements(); i++)
			printDefect((TestFailure) fail.nextElement(), i);

	}

	public void printDefect(TestFailure fail, int count) {
		printDefectHeader(fail, count);
		printDefectTrace(fail);
	}

	protected void printDefectHeader(TestFailure fail, int count) {
		getWriter().print(count + ") " + fail.failedTest());
	}

	protected void printDefectTrace(TestFailure fail) {
		getWriter().print(BaseTestRunner.getFilteredTrace(fail.trace()));
	}

	protected void printFooter(TestResult result) {
		if (result.wasSuccessful()) {
			getWriter().println();
			getWriter().print("OK");
			getWriter().println(
					" (" + result.runCount() + " test"
							+ (result.runCount() != 1 ? "s" : "") + ")");
		} else {
			getWriter().println();
			getWriter().println("FAILURES!!!");
			getWriter().println(
					"Tests run: " + result.runCount() + ",  Failures: "
							+ result.failureCount() + ",  Errors: "
							+ result.errorCount());
		}
		getWriter().println();
	}

	protected String elapsedTimeAsString(long runTime) {
		return NumberFormat.getInstance().format((double) runTime / 1000D);
	}

	public PrintStream getWriter() {
		return fWriter;
	}

	public void addError(Test test, Throwable t) {
		getWriter().print("E");
		fTestStats.put(test.toString(), TEST_ERROR);
	}

	public void addFailure(Test test, AssertionFailedError t) {
		getWriter().print("F");
		fTestStats.put(test.toString(), TEST_FAILURE);
	}

	public void endTest(Test test) {
	}

	public void startTest(Test test) {
		getWriter().print(".");
		fTestStats.put(test.toString(), TEST_SUCCESS);
		if (fColumn++ >= 40) {
			getWriter().println();
			fColumn = 0;
		}
	}

}
