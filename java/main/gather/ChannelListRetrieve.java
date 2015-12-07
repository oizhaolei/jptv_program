package gather;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author zhaolei
 * 
 */
public class ChannelListRetrieve {
	public final static DateFormat DB_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER2 = new SimpleDateFormat("yyyy-MM-dd HH", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER3 = new SimpleDateFormat("HH:mm", Locale.JAPAN);

	static Connection conn;
	static PreparedStatement existsCheckPS;
	static PreparedStatement insertPS;

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			DBclass.print("now: %s", DateFormat.getDateTimeInstance().format(new Date()));

			conn = DBclass.getConn();
			existsCheckPS = conn.prepareStatement("select max(program_time) from tbl_channel_program where channelid=? ");
			insertPS = conn
					.prepareStatement("insert into tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'cron', now(), 'cron', now())");
			// 对所有的channelid进行循环
			for (int i = 0; i < ChannelProgram.channelMatrix.length; i++) {
				String[] cp = ChannelProgram.channelMatrix[i];

				addChannelTimeStep(cp[0]);
			}

			existsCheckPS.close();
			insertPS.executeBatch();

			// 删除超过8天以上的数据
			conn.createStatement().execute("delete from tbl_channel_program where  DATEDIFF(now(), program_time) >8");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
		DBclass.print("-- over --");
	}

	/**
	 * 求指定时间和当前时间之间有多少完整的HOUR时间点
	 * 
	 * @param prev_program_time
	 * @param program_time
	 * @return
	 * @throws ParseException
	 */
	public static Date[] getSplit(String prev_program_time) throws ParseException {
		Date date2 = new Date();
		int hourMillis = 60 * 60 * 1000;

		// 找不到上一个节目的话，就把当前的时间作为起点
		if (prev_program_time == null) {
			return new Date[] { date2 };
		}
		Date date1 = DB_DATETIME_FORMATTER2.parse(prev_program_time);
		int h = (new Long((date2.getTime() - date1.getTime()) / (hourMillis))).intValue();
		if (h < 1) // 时间间隔小于1小时，do nothing
			return null;

		Date[] ret = new Date[h - 1];
		for (int i = 0; i < h - 1; i++) {
			ret[i] = new Date(date1.getTime() + (i + 1) * (hourMillis));
		}

		return ret;
	}

	private static void addChannelTimeStep(String channelid) throws SQLException, ParseException {
		existsCheckPS.setString(1, channelid);
		ResultSet rs = existsCheckPS.executeQuery();
		String prev_program_time = null;
		if (rs.next()) {
			prev_program_time = rs.getString(1);
			rs.close();
		}
		Date[] split = getSplit(prev_program_time);
		if (split != null)
			for (int i = 0; i < split.length; i++) {
				DBclass.print("add split: %s, %s", channelid, split[i]);

				insertPS.setString(1, channelid);
				insertPS.setString(2, "");
				insertPS.setString(3, "");
				insertPS.setString(4, DB_DATETIME_FORMATTER.format(split[i]));

				insertPS.addBatch();
			}
	}

}
