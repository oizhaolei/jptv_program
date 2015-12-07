package gather;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author Administrator
 * 
 */
public class TVKidsRetrieve {
	private static final String DELETE1 = "DELETE from tbl_channel_program where channelid=? and program_time < ? and program_time >= ? ";

	static String url = "http://www.kids-station.com/tv/program/daily.aspx?day=%s";

	public final static DateFormat DB_DATETIME_FORMATTER4 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
	public final static DateFormat DB_DATETIME_FORMATTER5 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
	public final static DateFormat DB_DATETIME_FORMATTER6 = new SimpleDateFormat("yyyyMMdd a K:mm", Locale.ENGLISH);

	static Connection conn;
	static PreparedStatement existsCheckPS;
	static PreparedStatement prevProgramPS;
	static PreparedStatement insertPS;
	static PreparedStatement deletePS;

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			DBclass.print("now: %s", DateFormat.getDateTimeInstance().format(new Date()));
			// args = new String[]{"20120709"};
			String dateStr;
			if (args.length > 0)
				dateStr = args[0];
			else
				dateStr = DB_DATETIME_FORMATTER4.format(new Date());

			url = String.format(url, dateStr);

			conn = DBclass.getConn();
			existsCheckPS = conn
					.prepareStatement("select count(0) from tbl_channel_program where channelid=? and program_time=?");
			prevProgramPS = conn
					.prepareStatement("select max(program_time) from tbl_channel_program where channelid=? and program_time<?");
			insertPS = conn
					.prepareStatement("insert into tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'cron', now(), 'cron', now())");
			deletePS = conn.prepareStatement(DELETE1);
			delete(dateStr);
			// retrieve
			retrieveKids(url, dateStr);

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

	private static void delete(String date) throws ParseException, SQLException {
		List<ChannelProgram> cps = ChannelProgram.onSetChannelname("ｷｯｽﾞｽﾃｰｼｮﾝ");
		//
		Date dt = DB_DATETIME_FORMATTER4.parse(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		calendar.add(Calendar.DATE, 1);
		String date1 = DB_DATETIME_FORMATTER4.format(calendar.getTime());
		date1 = date1.substring(0, 4) + "-" + date1.substring(4, 6) + "-" + date1.substring(6, 8) + " " + "06:00";
		String date2 = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8) + " " + "06:00";
		for (ChannelProgram cp : cps) {
			deletePS.setInt(1, cp.channelid);
			deletePS.setString(2, date1);
			deletePS.setString(3, date2);
			System.out.println(String.format("%s, %s, %s", DELETE1, date1, date2));

			int rows = deletePS.executeUpdate();
		}
		deletePS.close();
	}

	// 地上波
	private static void retrieveKids(String url, String dateStr) throws IOException {

		Document doc = null;
		for (int i = 0; doc == null && i < 5; i++) {
			DBclass.print("%d. retrieving %s", i + 1, url);
			try {
				doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36")
						.timeout(200000).get();
			} catch (Exception e1) {
				e1.printStackTrace(System.out);
			}
		}
		Elements programs = doc.select("div#schedule > table > tbody > tr");
		for (Element program : programs) {
			try {
				parseProgram(program, dateStr);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}

	private static void parseProgram(Element element, String dateStr) throws Exception {
		Element a = element.select(".zone > a").get(0);
		String program_time = a.id();
		String pt = program_time.substring(0, 2);
		if (pt.startsWith("am")) {
			program_time = "am " + program_time.substring(2);
		} else if (pt.startsWith("pm")) {
			program_time = "pm " + program_time.substring(2);
		} else {// 深夜 or 翌朝
			dateStr = nextDay(dateStr);
			program_time = "am " + program_time.substring(2);
		}
		program_time = dateStr + " " + program_time;
		program_time = reformat(program_time);

		Element titleE = element.select(".title").get(0);
		String title = titleE.text();
		if (title.length() > 16)
			title = title.substring(0, 16);

		Element num = element.select(".num").get(0);
		Element sub = element.select(".sub").get(0);
		String contents = titleE.text() + "-" + num.text() + sub.text();
		if (contents.length() > 66)
			contents = contents.substring(0, 66);

		List<ChannelProgram> cps = ChannelProgram.onSetChannelname("ｷｯｽﾞｽﾃｰｼｮﾝ");
		for (ChannelProgram cp : cps) {
			cp.program_time = program_time;
			cp.title = title;
			cp.contents = contents;
			if (cp.channelid != -1)
				DBclass.addToDb(cp, prevProgramPS, insertPS, existsCheckPS);
			// System.out.println(cp);
		}
	}

	private static String reformat(String dateStr) throws ParseException {
		Date dt = DB_DATETIME_FORMATTER6.parse(dateStr);
		String format = DB_DATETIME_FORMATTER5.format(dt);
		// System.out.println(dateStr + " >> " + format);
		return format;
	}

	private static String nextDay(String dateStr) throws Exception {
		Date dt = DB_DATETIME_FORMATTER4.parse(dateStr);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		calendar.add(Calendar.DATE, 1);
		String dd = DB_DATETIME_FORMATTER4.format(calendar.getTime());
		return dd;
	}

	private static void help() {
		DBclass.print("java -cp tvlist_gather.jar gather.TVKidsRetrieve");
		DBclass.print("java -cp tvlist_gather.jar gather.TVKidsRetrieve");
	}

}
