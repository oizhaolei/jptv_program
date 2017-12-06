package retrieve;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import setting.GlobalSetting;
import util.CommonUtil;
import db.DBclass;

/**
 * 插入间隔的整点时间，暂时不使用
 * @author lenovo
 *
 */
public class ChannelListRetrieve {

	static Connection conn;
	static PreparedStatement getLatestProgramPS;
	static PreparedStatement insertPS;
	static PreparedStatement deletePS;

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			CommonUtil.print("now: %s", DateFormat.getDateTimeInstance().format(new Date()));

			conn = DBclass.getConn();
			getLatestProgramPS = conn.prepareStatement(GlobalSetting.getMaxProgram);
			insertPS = conn.prepareStatement(GlobalSetting.insert_cron);

			// 对所有的channelid进行循环
			for (int i = 0; i < GlobalSetting.channelMatrix.length; i++) {
				String[] cp = GlobalSetting.channelMatrix[i];

				addChannelTimeStep(cp[0]);
			}

			getLatestProgramPS.close();
			insertPS.executeBatch();

			// 删除旧数据
			deletePS = conn.prepareStatement(GlobalSetting.deleteOldProgram);
			deletePS.execute();
			deletePS.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
		CommonUtil.print("-- over --");
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
		Date date1 = GlobalSetting.DB_DATETIME_FORMATTER2.parse(prev_program_time);
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
		getLatestProgramPS.setString(1, channelid);
		ResultSet rs = getLatestProgramPS.executeQuery();
		String prev_program_time = null;
		if (rs.next()) {
			prev_program_time = rs.getString(1);
			rs.close();
		}
		Date[] split = getSplit(prev_program_time);
		if (split != null)
			for (int i = 0; i < split.length; i++) {
				CommonUtil.print("add split: %s, %s", channelid, split[i]);

				insertPS.setString(1, channelid);
				insertPS.setString(2, "");
				insertPS.setString(3, "");
				insertPS.setString(4, GlobalSetting.DB_DATETIME_FORMATTER.format(split[i]));

				insertPS.addBatch();
			}
	}

}
