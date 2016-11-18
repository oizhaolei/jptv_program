package setting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class GlobalSetting {
	public static String getMaxProgram = "SELECT MAX(program_time) FROM tbl_channel_program WHERE channelid=? ";
	public static String getPrevProgram = "SELECT MAX(program_time) FROM tbl_channel_program WHERE channelid=? AND program_time<?";
	public static String insert_cron = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) VALUES (?, ?, ?, ?, 'cron', now(), 'cron', now())";
	public static String insert_sonet = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) VALUES (?, ?, ?, ?, 'sonet', now(), 'sonet', now())";
	public static String insert_golf = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) VALUES (?, ?, ?, ?, 'golf', now(), 'golf', now())";
	public static String insert_kids = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'kids', now(), 'kids', now())";
	public static String insert_epg = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'epg', now(), 'epg', now())";
	public static String existsCheck = "SELECT COUNT(0) FROM tbl_channel_program WHERE channelid=? AND program_time=?";
	public static String delete = "DELETE FROM tbl_channel_program WHERE channelid=? AND program_time<? AND program_time>=?";
	public static String deleteOldProgram = "DELETE FROM tbl_channel_program WHERE DATEDIFF(now(), program_time) >8";
	
	
	public final static DateFormat DB_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER2 = new SimpleDateFormat("yyyy-MM-dd HH", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER3 = new SimpleDateFormat("HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER4 = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER5 = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER6 = new SimpleDateFormat("yyyyMMdd a K:mm", Locale.ENGLISH);
	public final static DateFormat DB_DATETIME_FORMATTER7 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
	public final static DateFormat DB_DATETIME_FORMATTER8 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	public final static DateFormat DB_DATETIME_FORMATTER9 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

	
	
}
