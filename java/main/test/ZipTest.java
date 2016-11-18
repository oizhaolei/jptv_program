package test;

import java.util.Date;

import setting.GlobalSetting;

public class ZipTest {

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		String program_time_jp = GlobalSetting.DB_DATETIME_FORMATTER.format(new Date(time));
		String program_time_cn = GlobalSetting.DB_DATETIME_FORMATTER9.format(new Date(time));
		System.out.println("JP:" + program_time_jp + ", CN:" + program_time_cn);
		program_time_jp = GlobalSetting.DB_DATETIME_FORMATTER.format(new Date(time - 3600000));
		program_time_cn = GlobalSetting.DB_DATETIME_FORMATTER9.format(new Date(time - 3600000));
		System.out.println("JP:" + program_time_jp + ", CN:" + program_time_cn);
//		ZipUtil.decompressMultiFiles("C:/Work/logs/20161114_2a22daa3c1994914bbf124c0dde0aa8a.zip", "C:/Work/logs/20161114/");
	}
}
