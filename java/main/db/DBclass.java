package db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import model.ChannelProgram;
import setting.GlobalSetting;
import util.CommonUtil;

public class DBclass {
	static Properties env = new Properties();

	public static Connection getConn() throws Exception {
		InputStream input = DBclass.class.getClassLoader().getResourceAsStream("env.properties");
//		InputStream input = DBclass.class.getClassLoader().getResourceAsStream("env.epg2.properties");
		env.load(input);
		Class.forName(env.getProperty("jdbc.driverClassName"));
		Connection c = DriverManager.getConnection(env.getProperty("jdbc.url"), env.getProperty("jdbc.username"),
				env.getProperty("jdbc.password"));
		return c;
	}
	private static void trim(ChannelProgram cp) {
		if (cp.title.length() > 50) {
			cp.title = cp.title.substring(0, 50);
		}
		if (cp.content.length() > 200) {
			cp.content = cp.content.substring(0, 50);
		}
	}

	public static void addToDb(ChannelProgram cp, PreparedStatement prevProgramPS, PreparedStatement insertPS,
			PreparedStatement existsCheckPS) throws SQLException {
		trim(cp);
		if (!exists(cp, existsCheckPS)) {
			try {
				addSplit(cp.channelid, cp.program_time, prevProgramPS, insertPS);
			} catch (ParseException e) {
				e.printStackTrace(System.out);
			}

			CommonUtil.print("add:%s, %s, %s", cp.channelid, cp.title, cp.program_time);

			insertPS.setInt(1, cp.channelid);
			insertPS.setString(2, cp.title);
			insertPS.setString(3, cp.content);
			insertPS.setString(4, cp.program_time);

			insertPS.addBatch();
			insertPS.executeBatch();
		} else {
			CommonUtil.print("ignore:%s, %s, %s", cp.channelid, cp.title, cp.program_time);
		}
	}

	public static void addToDb(ChannelProgram cp, PreparedStatement prevProgramPS, PreparedStatement insertPS,
			PreparedStatement updatePS, PreparedStatement selectPS) throws SQLException {
		trim(cp);
		ChannelProgram cp_db = getChannelProgram(cp, selectPS);
		if (!isSameProgram(cp_db, cp)) {
			if (cp.channelid == cp_db.channelid && cp.program_time.equals(cp_db.program_time)) {
				CommonUtil.print("update:%s, %s, %s, %s, %s", cp.channelid, cp.title, cp.program_time, cp.program_start_time, cp.program_end_time);
				updatePS.setString(1, cp.title);
				updatePS.setString(2, cp.content);
				updatePS.setString(3, cp.program_start_time);
				updatePS.setString(4, cp.program_end_time);
				updatePS.setInt(5, cp.channelid);
				updatePS.setString(6, cp.program_time);

				updatePS.execute();
			} else {
				try {
					addSplit(cp.channelid, cp.program_time, prevProgramPS, insertPS);
				} catch (ParseException e) {
					e.printStackTrace(System.out);
				}

				CommonUtil.print("add:%s, %s, %s, %s, %s", cp.channelid, cp.title, cp.program_time, cp.program_start_time, cp.program_end_time);
				insertPS.setInt(1, cp.channelid);
				insertPS.setString(2, cp.title);
				insertPS.setString(3, cp.content);
				insertPS.setString(4, cp.program_time);
				insertPS.setString(5, cp.program_start_time);
				insertPS.setString(6, cp.program_end_time);

				insertPS.addBatch();
				insertPS.executeBatch();
			}
		} else {
			CommonUtil.print("ignore:%s, %s, %s", cp.channelid, cp.title, cp.program_time);
		}
	}

	/**
	 * 检查本节目和上一节目之间，是否有“整小时”的无节目时间段，如果没有，加入整点分隔点
	 * 
	 * @param channelid
	 * @param program_time
	 * @throws SQLException
	 * @throws ParseException
	 */
	private static void addSplit(int channelid, String program_time, PreparedStatement prevProgramPS,
			PreparedStatement insertPS) throws SQLException, ParseException {
		prevProgramPS.setInt(1, channelid);
		prevProgramPS.setString(2, program_time);
		ResultSet rs = prevProgramPS.executeQuery();
		String prev_program_time = null;
		if (rs.next()) {
			prev_program_time = rs.getString(1);
			rs.close();
		}
		Date[] split = getSplit(prev_program_time, program_time);
		if (split != null)
			for (int i = 0; i < split.length; i++) {
				CommonUtil.print("add split:%s, %s", channelid, split[i]);

				insertPS.setInt(1, channelid);
				insertPS.setString(2, "");
				insertPS.setString(3, "");
				insertPS.setString(4, GlobalSetting.DB_DATETIME_FORMATTER.format(split[i]));
				insertPS.setString(5, null);
				insertPS.setString(6, null);

				insertPS.addBatch();
			}
	}

	/**
	 * 求2个时间参数之间有多少完整的时间点
	 * 
	 * @param prev_program_time
	 * @param program_time
	 * @return
	 * @throws ParseException
	 */
	private static Date[] getSplit(String prev_program_time, String program_time) throws ParseException {
		Date date2 = GlobalSetting.DB_DATETIME_FORMATTER2.parse(program_time);
		int hourMillis = 60 * 60 * 1000;

		if (prev_program_time == null) {
			return null;
		}
		Date date1 = GlobalSetting.DB_DATETIME_FORMATTER2.parse(prev_program_time);
		int h = (new Long((date2.getTime() - date1.getTime()) / (hourMillis))).intValue();
		if (h < 1)
			return null;

		Date[] ret = new Date[h - 1];
		for (int i = 0; i < h - 1; i++) {
			ret[i] = new Date(date1.getTime() + (i + 1) * (hourMillis));
		}

		return ret;
	}

	private static boolean exists(ChannelProgram cp, PreparedStatement existsCheckPS) throws SQLException {
		existsCheckPS.setInt(1, cp.channelid);
		existsCheckPS.setString(2, cp.program_time);
		ResultSet rs = existsCheckPS.executeQuery();
		if (rs.next()) {
			int count = rs.getInt(1);
			rs.close();
			return count > 0;
		}
		return false;
	}

	private static ChannelProgram getChannelProgram(ChannelProgram cp, PreparedStatement selectPS) throws SQLException {
		selectPS.setInt(1, cp.channelid);
		selectPS.setString(2, cp.program_time);
		ChannelProgram result = new ChannelProgram();
		ResultSet rs = selectPS.executeQuery();
		if (rs.next()) {
			result.channelid = cp.channelid;
			result.program_time = cp.program_time;
			result.title = rs.getString(1);
			result.content = rs.getString(2);
			rs.close();
		}
		return result;
	}
	private static boolean isSameProgram(ChannelProgram program_db, ChannelProgram program_new) {
		if (program_new.title.equals(program_db.title)
				&& program_new.content.equals(program_db.content)
				&& program_new.program_time.equals(program_db.program_time)
				&& program_new.channelid == program_db.channelid
				) {
			return true;
		} else {
			return false;
		}
	}
}
