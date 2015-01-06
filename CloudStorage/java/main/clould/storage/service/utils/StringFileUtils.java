package clould.storage.service.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StringFileUtils {

	/**
	 * change the content to a string.
	 * 
	 * @param file
	 * @return the content of file.
	 */
	public static final String file2String(File file) {
		BufferedReader br;
		StringBuilder strBlder = new StringBuilder("");
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));
			String line = "";
			while (null != (line = br.readLine())) {
				strBlder.append(line + "\n");
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return strBlder.toString();
	}

	/**
	 * change the content to a string.
	 * 
	 * @param file
	 * @return the content of file.
	 */
	public static final String fileInputStream2String(InputStream is) {
		BufferedReader br;
		StringBuilder strBlder = new StringBuilder("");
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			while (null != (line = br.readLine())) {
				strBlder.append(line + "\n");
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return strBlder.toString();
	}

	/**
	 * save string to a file(recover).
	 * 
	 * @param file
	 * @param content
	 * @return success flag.
	 */
	public static final boolean saveString2File(File file, String content) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file)));
			bw.write(content);
			bw.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static final String changeFirstCharacterToLowerCase(
			String upperCaseStr) {
		char[] chars = new char[1];
		chars[0] = upperCaseStr.charAt(0);
		String temp = new String(chars);
		if (chars[0] >= 'A' && chars[0] <= 'Z') {
			upperCaseStr = upperCaseStr.replaceFirst(temp, temp.toLowerCase());
		}
		return upperCaseStr;
	}

	private final static String SYS_TEMP_FILE = System
			.getProperty("java.io.tmpdir") + "\\9884698793643198z.properties";
	private static Map<String, String> fileNames = null;

	/**
	 * get a file path from a system temp file.
	 * 
	 * @param keyOfFileName
	 * @return
	 */
	public static final String getAFilePathFromSysTempFile(String keyOfFileName) {
		if (null == fileNames) {
			fileNames = new HashMap<String, String>();
			loadFilePathsFromSysTempFile();
		}
		String path = fileNames.get(keyOfFileName);
		return null == path ? "" : path;
	}

	private static final boolean loadFilePathsFromSysTempFile() {
		try {
			if (!new File(SYS_TEMP_FILE).exists()) {
				new File(SYS_TEMP_FILE).createNewFile();
				return true;
			}
			Properties props = new Properties();
			InputStream in = new BufferedInputStream(new FileInputStream(
					SYS_TEMP_FILE));
			props.load(in);
			Enumeration<?> en = props.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String prop = props.getProperty(key);
				fileNames.put(key, prop);

			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * save a file path to a system temp file.
	 * 
	 * @param parameterName
	 * @param parameterValue
	 * @return
	 */
	public static final int saveAFilePathToSysTempFile(String parameterName,
			String parameterValue) {
		Properties prop = new Properties();
		try {
			InputStream fis = new FileInputStream(SYS_TEMP_FILE);
			prop.load(fis);

			OutputStream fos = new FileOutputStream(SYS_TEMP_FILE);
			prop.setProperty(parameterName, parameterValue);
			prop.store(fos, "Update '" + parameterName + "' value");

			fileNames.put(parameterName, parameterValue);
			return fileNames.size();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	
}

