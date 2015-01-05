package clould.storage.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import clould.storage.service.utils.HexUtils;

public class FileStorageServiceImpl implements FileStorageService{

	@Override
	public String updateFile(String fileName, String hexStringData) {
		byte[] fileByteData = HexUtils.hexStringToByte(hexStringData);
		try {
			FileOutputStream fps = new FileOutputStream("E:\\Storage\\" + fileName);
			fps.write(fileByteData);
			fps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "True";
	}

}
