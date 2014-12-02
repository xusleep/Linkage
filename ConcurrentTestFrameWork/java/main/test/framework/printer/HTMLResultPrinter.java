package test.framework.printer;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import test.framework.summary.SummaryTestResult;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;

public class HTMLResultPrinter extends BaseResultPrinter {

	private PrintStream writer = null;
	private int repeatTime;

	public HTMLResultPrinter(PrintStream output, int repeatTime) {
		super(System.out);
		writer = output;
		this.repeatTime = repeatTime;
	}

	public int getRepeatTime() {
		return repeatTime;
	}

	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}

	public synchronized void print(TestResult result, long runTime) {
		super.print(result, runTime);
		long errorCnt = 0;
		long failCnt = 0;
		
		writer.println("<html><head><title>Unit Test Results</title>"
				+ "<style type=\"text/css\">"
				+ "body {\n"
				+ "    font:normal 68% verdana,arial,helvetica;\n"
				+ "    color:#000000;\n"
				+ "}\n"
				+ "table tr td, table tr th {\n"
				+ "    font-size: 68%;\n"
				+ "}\n"
				+ "table.details tr th{\n"
				+ "    font-weight: bold;\n"
				+ "    text-align:left;\n"
				+ "    background:#a6caf0;\n"
				+ "}\n"
				+ "table.details tr td{\n"
				+ "    background:#eeeee0;\n"
				+ "}\n"
				+ "\n"
				+ "p {\n"
				+ "    line-height:1.5em;\n"
				+ "    margin-top:0.5em; margin-bottom:1.0em;\n"
				+ "}\n"
				+ "h1 {\n"
				+ "    margin: 0px 0px 5px; font: 165% verdana,arial,helvetica\n"
				+ "}\n"
				+ "h2 {\n"
				+ "    margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica\n"
				+ "}\n"
				+ "h3 {\n"
				+ "    margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica\n"
				+ "}\n"
				+ "h4 {\n"
				+ "    margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica\n"
				+ "}\n"
				+ "h5 {\n"
				+ "    margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica\n"
				+ "}\n"
				+ "h6 {\n"
				+ "    margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica\n"
				+ "}\n" + ".Error {\n" + "    font-weight:bold; color:red;\n"
				+ "}\n" + ".Failure {\n"
				+ "    font-weight:bold; color:purple;\n" + "}\n"
				+ ".Properties {\n" + "  text-align:right;\n" + "}\n"
				+ "</style>" + "</head><body>");
		
		writer.println("<h1>Unit Test Results</h1>");
		writer.println("<table width=\"95%\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\">");
		writer.println("<tr valign=\"top\"><th>Tests</th><th>Errors</th><th>Failures</th><th>Repeat</th><th nowrap>Time(s)</th></tr>");
		String errCntCellData = result.errorCount() > 0 ? "<a href=\"#Error1\">"
				+ result.errorCount() + "</a>"
				: "" + result.errorCount();
		String falCntCelData = result.failureCount() > 0 ? "<a href=\"#Failure1\">"
				+ result.failureCount() + "</a>"
				: "" + result.failureCount();
		writer.println("<tr valign=\"top\" class=\"Error\"><td>"
				+ result.runCount() + "</td><td>" + errCntCellData
				+ "</td><td>" + falCntCelData + "</td><td>" + getRepeatTime() + "</td><td>"
				+ elapsedTimeAsString(runTime) + "</td></tr></table>");
		
		writer.println("<h1>Unit Test Results Failures Summary</h1>");
		writer.println("<table width=\"95%\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\">");

		writer.println("<tr valign=\"top\"><th>Fail Class Name</th><th>Fail Method Name</th><th>Failed Times</th><th>Total Times</th><th nowrap>Rate</th></tr>");
		SummaryTestResult summary = new SummaryTestResult(result);
		HashMap hmFailures = summary.getHmFailures();
		Iterator keysIterator = hmFailures.keySet().iterator();
		while (keysIterator.hasNext()) {
			Object key = keysIterator.next();
			long value = (long) hmFailures.get(key);
			String testName = (String)key;
			String className = testName;
			String methodName = testName;
			writer.println("<tr valign=\"top\" class=\"Error\"><td>"
					+ className + "</td><td>" + methodName + "</td><td>" + value
					+ "</td><td>" + getRepeatTime() + "</td><td>"
					+ getRate(value, getRepeatTime()) + "%</td></tr>");
		}
		writer.println("</table>");
		
		writer.println("<h1>Unit Test Results Errors Summary</h1>");
		writer.println("<table width=\"95%\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\">");

		writer.println("<tr valign=\"top\"><th>Error Class Name</th><th>ErrorMethod Name</th><th>Error Times</th><th>Total Times</th><th nowrap>Rate</th></tr>");
		HashMap hmErrors = summary.getHmErrors();
		keysIterator = hmErrors.keySet().iterator();
		while (keysIterator.hasNext()) {
			Object key = keysIterator.next();
			long value = (long) hmErrors.get(key);
			String testName = (String)key;
			String className = testName;
			String methodName = testName;
			if (testName.indexOf("(") != -1) {
				methodName = testName.substring(0, testName.indexOf("("));
			}
			if (testName.indexOf("(") != -1 && testName.indexOf(")") != -1) {
				className = testName.substring(testName.indexOf("(") + 1,
						testName.indexOf(")"));
			}
			writer.println("<tr valign=\"top\" class=\"Error\"><td>"
					+ className + "</td><td>" + methodName + "</td><td>" + value
					+ "</td><td>" + getRepeatTime() + "</td><td>"
					+ getRate(value, getRepeatTime()) + "%</td></tr>");
			System.out.println(key + " : " + value);
		}
		writer.println("</table>");
		
		
		writer.println("<h2>Tests</h2><table width=\"95%\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\">");
		writer.println("<tr valign=\"top\"><th>Class Name</th><th>Method Name</th><th>Status</th><th width=\"80%\">Message</th></tr>");
		Enumeration failures = result.failures();
		Enumeration errors = result.errors();
		for (Entry<String, String> entry : fTestStats.entrySet()) {
			String testName = entry.getKey();
			String className = testName;
			String methodName = testName;
			if (testName.indexOf("(") != -1) {
				methodName = testName.substring(0, testName.indexOf("("));
			}
			if (testName.indexOf("(") != -1 && testName.indexOf(")") != -1) {
				className = testName.substring(testName.indexOf("(") + 1,
						testName.indexOf(")"));
			}
			String status = ("S".equalsIgnoreCase(entry.getValue()) ? "Success"
					: ("F".equalsIgnoreCase(entry.getValue()) ? "Failure"
							: "Error"));
			if ("Success".equalsIgnoreCase(status)) {
				writer.println("<tr valign=\"top\" class=\"TableRowColor\">");
				writer.println("<td>" + className + "</td>");
				writer.println("<td>" + methodName + "</td>");
				writer.println("<td>" + status + "</td>");
				writer.println("<td></td>");
				writer.println("</tr>");
			} else {
				writer.println("<tr valign=\"top\" class=\"" + status + "\">");
				writer.println("<td>" + className + "</td>");
				writer.println("<td>" + methodName + "</td>");
				writer.println("<td>" + status + "</td>");
				if ("Failure".equalsIgnoreCase(status)) {
					TestFailure f = (TestFailure) failures.nextElement();
					writer.println("<td><a name=\"Failure"
							+ (++failCnt)
							+ "\">"
							+ f.thrownException().getMessage()
							+ "<br><br><code>"
							+ BaseTestRunner.getFilteredTrace(f.trace())
							+ "</code><br>"
							+ (failures.hasMoreElements() ? "<a href=\"#Failure"
									+ (failCnt + 1) + "\">&gt;&gt;</a>"
									: "") + "</td>");
				} else {
					TestFailure f = (TestFailure) errors.nextElement();
					writer.println("<td><a name=\"Error"
							+ (++errorCnt)
							+ "\">"
							+ f.thrownException().getMessage()
							+ "<code>"
							+ BaseTestRunner.getFilteredTrace(f.trace())
							+ "</code><br>"
							+ (errors.hasMoreElements() ? "<a href=\"#Error"
									+ (errorCnt + 1) + "\">&gt;&gt;</a>" : "")
							+ "</td>");
				}
				writer.println("</tr>");
			}
		}
		writer.println("</table></body></html>");
		writer.println("");
		writer.flush();
		writer.close();
	}
	
	public static double getRate(long part, long total){
		double dpart = (double)part;
		double dtotal = (double)total;
		return (dpart / dtotal) * 100;
	}
}
