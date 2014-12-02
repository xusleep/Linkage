package test.framework.run;

import java.io.PrintStream;

import test.framework.printer.BaseResultPrinter;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;

public class MyTestRunner extends BaseTestRunner{
	
	private BaseResultPrinter fPrinter;
	public static final int SUCCESS_EXIT = 0;
	public static final int FAILURE_EXIT = 1;
	public static final int EXCEPTION_EXIT = 2;

	public MyTestRunner() {
		fPrinter = new BaseResultPrinter();
	}

	public MyTestRunner(PrintStream writer) {
		this(new BaseResultPrinter(writer));
	}

	public MyTestRunner(BaseResultPrinter printer) {
		fPrinter = printer;
	}

	public static void run(Class testClass) {
		run(((Test) (new TestSuite(testClass))));
	}

	public static TestResult run(Test test) {
		MyTestRunner runner = new MyTestRunner();
		return runner.doRun(test);
	}

	public static void runAndWait(Test suite) {
		MyTestRunner aTestRunner = new MyTestRunner();
		aTestRunner.doRun(suite, true);
	}

	public void testFailed(int i, Test test1, Throwable throwable) {
	}

	public void testStarted(String s) {
	}

	public void testEnded(String s) {
	}

	protected TestResult createTestResult() {
		return new TestResult();
	}

	public TestResult doRun(Test test) {
		return doRun(test, false);
	}

	public TestResult doRun(Test suite, boolean wait) {
		TestResult result = createTestResult();
		result.addListener(fPrinter);
		long startTime = System.currentTimeMillis();
		suite.run(result);
		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		fPrinter.print(result, runTime);
		pause(wait);
		return result;
	}

	protected void pause(boolean wait) {
		if (!wait)
			return;
		fPrinter.printWaitPrompt();
		try {
			System.in.read();
		} catch (Exception exception) {
		}
	}

	protected TestResult start(String[] args) throws Exception {
		String testCase = "";
		boolean wait = false;
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-wait"))
				wait = true;
			else if (args[i].equals("-c"))
				testCase = extractClassName(args[++i]);
			else
				testCase = args[i];

		if (testCase.equals(""))
			throw new Exception(
					"Usage: BneTestRunner [-wait] testCaseName, where name is the name of the TestCase class");
		try {
			Test suite = getTest(testCase);
			return doRun(suite, wait);
		} catch (Exception e) {
			throw new Exception("Could not create and run test suite: " + e);
		}
	}

	protected void runFailed(String message) {
		System.err.println(message);
		System.exit(1);
	}

	public void setPrinter(BaseResultPrinter printer) {
		fPrinter = printer;
	}
}
