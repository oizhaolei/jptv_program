package gather;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

public class DBclass {
	public final static DateFormat DB_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER2 = new SimpleDateFormat("yyyy-MM-dd HH", Locale.JAPAN);
	static Properties env = new Properties();

	public static Connection getConn() throws Exception {
		InputStream input = DBclass.class.getClassLoader().getResourceAsStream("env.properties");
		env.load(input);
		Class.forName(env.getProperty("jdbc.driverClassName"));
		Connection c = DriverManager.getConnection(env.getProperty("jdbc.url"), env.getProperty("jdbc.username"),
				env.getProperty("jdbc.password"));
		return c;
	}

	public static void print(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	public static String xmlFilte(String str) {
		if (str == " ") // 如果字符串为空，直接返回。
		{
			return str;
		} else {
			str = str.replace("'", " ");
			str = str.replace("<", "「");
			str = str.replace(">", "」");
			str = str.replace("%", "％");
			str = str.replace("'", " ");
			str = str.replace("''", " ");
			str = str.replace("\"\" ", " ");
			str = str.replace(",", " ");
			str = str.replace(".", " ");
			str = str.replace(">=", " ");
			str = str.replace("=<", " ");
			str = str.replace("-", " ");
			str = str.replace("_", " ");
			str = str.replace(";", " ");
			str = str.replace("||", " ");
			str = str.replace("[", "「");
			str = str.replace("]", "」");
			str = str.replace("&", "＆");
			str = str.replace("/", " ");
			str = str.replace("-", " ");
			str = str.replace("|", " ");
			str = str.replace("?", " ");
			str = str.replace(">?", " ");
			str = str.replace("?<", " ");
			str = str.replace(" ", " ");
			return str;
		}
	}

	public static void addToDb(ChannelProgram cp, PreparedStatement prevProgramPS, PreparedStatement insertPS,
			PreparedStatement existsCheckPS) throws SQLException {
		if (!exists(cp, existsCheckPS)) {
			try {
				addSplit(cp.channelid, cp.program_time, prevProgramPS, insertPS);
			} catch (ParseException e) {
				e.printStackTrace(System.out);
			}

			DBclass.print("add:%s, %s, %s", cp.channelid, cp.title, cp.program_time);
			insertPS.setInt(1, cp.channelid);
			insertPS.setString(2, cp.title);
			insertPS.setString(3, cp.contents);
			insertPS.setString(4, cp.program_time);

			insertPS.addBatch();
			insertPS.executeBatch();
		} else {
			DBclass.print("ignore:%s, %s, %s", cp.channelid, cp.title, cp.program_time);
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
				DBclass.print("add split:%s, %s", channelid, split[i]);

				insertPS.setInt(1, channelid);
				insertPS.setString(2, "");
				insertPS.setString(3, "");
				insertPS.setString(4, DB_DATETIME_FORMATTER.format(split[i]));

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
		Date date2 = DB_DATETIME_FORMATTER2.parse(program_time);
		int hourMillis = 60 * 60 * 1000;

		if (prev_program_time == null) {
			return null;
		}
		Date date1 = DB_DATETIME_FORMATTER2.parse(prev_program_time);
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
}
