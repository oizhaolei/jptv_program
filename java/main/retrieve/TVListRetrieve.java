package retrieve;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import model.ChannelProgram;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import setting.GlobalSetting;
import util.CommonUtil;
import db.DBclass;

/**
 * 把http://tv.yahoo.co.jp/listings/realtime/内容保存到本地
 * 
 * @author zhaolei
 * 
 */
public class TVListRetrieve{
	static String tokyo_chijo_url = "http://tv.so-net.ne.jp/rss/schedulesByCurrentTime.action?group=10&stationAreaId=23";
	static String osaka_chijo_url = "http://tv.so-net.ne.jp/rss/schedulesByCurrentTime.action?group=10&stationAreaId=40";
	static String kobe_chijo_url = "http://tv.so-net.ne.jp/rss/schedulesByCurrentTime.action?group=10&stationAreaId=42";
	static String bs1 = "http://tv.so-net.ne.jp/rss/schedulesByCurrentTime.action?group=21";
	static String bs2 = "http://tv.so-net.ne.jp/rss/schedulesByCurrentTime.action?group=22";
	static String bs3 = "http://tv.so-net.ne.jp/rss/schedulesByCurrentTime.action?group=23";
	static String bs4 = "http://tv.so-net.ne.jp/rss/schedulesByCurrentTime.action?group=24";

	static Connection conn;
	static PreparedStatement existsCheckPS;
	static PreparedStatement getPrevProgramPS;
	static PreparedStatement insertPS;
	public static Date date;

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			CommonUtil.print("now: %s", DateFormat.getDateTimeInstance().format(new Date()));
			date = new Date();

			conn = DBclass.getConn();
			existsCheckPS = conn.prepareStatement(GlobalSetting.existsCheck);
			getPrevProgramPS = conn.prepareStatement(GlobalSetting.getPrevProgram);
			insertPS = conn.prepareStatement(GlobalSetting.insert_cron);
			// retrieve
			retrieveTokyoChijo(tokyo_chijo_url);
			retrieveTokyoChijo(osaka_chijo_url);
			retrieveTokyoChijo(kobe_chijo_url);
			retrieveTokyoChijo(bs1);
			retrieveTokyoChijo(bs2);
			retrieveTokyoChijo(bs3);
			retrieveTokyoChijo(bs4);

			existsCheckPS.close();
			insertPS.executeBatch();

			// 删除旧数据
			conn.createStatement().execute(GlobalSetting.deleteOldProgram);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
		CommonUtil.print("-- over --");
	}

	// 地上波
	private static void retrieveTokyoChijo(String url) throws IOException {

		Document doc = null;
		for (int i = 0; doc == null && i < 5; i++) {
			CommonUtil.print("%d. retrieving %s", i + 1, url);
			try {
				doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; rv:11.0) Gecko/20100101 Firefox/11.0").timeout(50000).get();
			} catch (Exception e1) {
				e1.printStackTrace(System.out);
			}
		}
		Elements programs = doc.select("item");
		for (Element program : programs) {
			try {
				parseProgram(program);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}

	private static void parseProgram(Element element) throws SQLException, ParseException {
		// cp.contents = element.attr("rdf:about");
		boolean timeFlg = true;
		Elements children = element.children();
		String channelName = null;
		String title = null;
		String contents = null;
		String program_time = null;

		for (Element child : children) {
			if ("description".equals(child.tagName())) {
				String cn = child.text();
				int start = cn.indexOf('[');
				int end = cn.indexOf(']');
				if (start < end) {
					channelName = cn.substring(start + 1, end);
				}
			} else if ("title".equals(child.tagName())) {
				title = CommonUtil.xmlFilter(child.text());
				if (title.length() > 16)
					title = title.substring(0, 16);

				contents = CommonUtil.xmlFilter(child.text());
				if (contents.length() > 66)
					contents = contents.substring(0, 66);
				// } else if ("link".equals(child.tagName())) {
				// contents = child.text();
			} else if ("dc:date".equals(child.tagName())) {
				// 2012-05-08T18:20+09:00 ==>2012-05-08 18:20
				String pt = child.text();
				if (pt.length() > 16) {
					program_time = pt.substring(0, 10) + ' ' + pt.substring(11, 16);
					long h = Math.abs(GlobalSetting.DB_DATETIME_FORMATTER.parse(program_time).getTime() - date.getTime());
					if (h > 1000 * 5 * 60 * 60) {
						timeFlg = false;
					}
				}
			}
		}
		List<ChannelProgram> cps = GlobalSetting.onSetChannelname(channelName);
		for (ChannelProgram cp : cps) {
			cp.program_time = program_time;
			cp.title = title;
			cp.content = contents;
			if (cp.channelid != -1 && timeFlg)
				DBclass.addToDb(cp, getPrevProgramPS, insertPS, existsCheckPS);
			else
				CommonUtil.print("ignore:%s, %s, %s, %s", cp.channelid, cp.title, cp.program_time, timeFlg);

			// print("channelname:%s, title:%s, contents:%s, program_time:%s",
			// cp.channelname, cp.title, cp.contents, cp.program_time);
			// print("channelname:%s", cp.channelname);
		}

	}

	public static void help() {
		CommonUtil.print("java -cp tvlist_gather.jar gather.TVListRetrieve");
		CommonUtil.print("java -cp tvlist_gather.jar gather.TVListRetrieve");
	}

}
