package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	/**
	 * 压缩单个文件
	 * 
	 * @param filepath
	 * @param zippath
	 */
	public static void compressSingleFile(String filepath, String zippath) {
		try {
			File file = new File(filepath);
			File zipFile = new File(zippath);
			InputStream input = new FileInputStream(file);
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(
					zipFile));
			zipOut.putNextEntry(new ZipEntry(file.getName()));
			int temp = 0;
			while ((temp = input.read()) != -1) {
				zipOut.write(temp);
			}
			input.close();
			zipOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 压缩多个文件，文件存放至一个文件夹中
	 * 
	 * @param filepath
	 * @param zippath
	 */
	public static void compressMultiFiles(String filepath, String zippath) {
		try {
			File file = new File(filepath);// 要被压缩的文件夹
			File zipFile = new File(zippath);
			InputStream input = null;
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(
					zipFile));
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; ++i) {
					input = new FileInputStream(files[i]);
					zipOut.putNextEntry(new ZipEntry(file.getName()
							+ File.separator + files[i].getName()));
					int temp = 0;
					while ((temp = input.read()) != -1) {
						zipOut.write(temp);
					}
					input.close();
				}
			}
			zipOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压缩（解压缩单个文件）
	 * 
	 * @param zippath
	 * @param outfilepath
	 * @param filename
	 */
	public static void decompressSingleFile(String zippath, String outfilepath,
			String filename) {
		try {
			File file = new File(zippath);// 压缩文件路径和文件名
			File outFile = new File(outfilepath);// 解压后路径和文件名
			ZipFile zipFile = new ZipFile(file);
			ZipEntry entry = zipFile.getEntry(filename);// 所解压的文件名
			InputStream input = zipFile.getInputStream(entry);
			OutputStream output = new FileOutputStream(outFile);
			int temp = 0;
			while ((temp = input.read()) != -1) {
				output.write(temp);
			}
			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压缩（压缩文件中包含多个文件）
	 * 
	 * @param zippath
	 * @param outzippath
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void decompressMultiFiles(String zippath, String outzippath) throws ZipException, IOException {
		File file = new File(zippath);
		File outDir = new File(outzippath);
		if (!outDir.exists() && outDir.isDirectory()) {
			outDir.mkdir();
		}
		File outFile = null;
		ZipFile zipFile = new ZipFile(file);
		ZipEntry entry = null;
		InputStream input = null;
		OutputStream output = null;
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			entry = (ZipEntry) entries.nextElement();
			if (null == entry) {
				break;
			}
			CommonUtil.print("解压缩" + entry.getName() + "文件");
			outFile = new File(outzippath + File.separator + entry.getName());
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdir();
			}
			if (!outFile.exists()) {
				outFile.createNewFile();
			}
			input = zipFile.getInputStream(entry);
			output = new FileOutputStream(outFile);
			int temp = 0;
			while ((temp = input.read()) != -1) {
				output.write(temp);
			}
			input.close();
			output.close();
		}
	}

}
