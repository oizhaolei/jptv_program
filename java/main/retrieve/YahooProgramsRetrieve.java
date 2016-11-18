package retrieve;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.ChannelProgram;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import setting.GlobalSetting;
import util.CommonUtil;

import com.mysql.jdbc.StringUtils;

import db.DBclass;

/**
 *
 * @author Administrator
 *
 */
public class YahooProgramsRetrieve {
	static String url = "http://tv.yahoo.co.jp/listings/%s/?st=4&s=1&va=24&vc=0&vd=0&ve=0&vb=0&a=23&d=%s";

	static Connection conn;
	static PreparedStatement existsCheckPS;
	static PreparedStatement prevProgramPS;
	static PreparedStatement insertPS;
	static PreparedStatement deletePS;
	public static String today;

	public static String[] channelsetArray = { "23 ", "bs1", "bs2", "bs3",
			"bs4", "bs5", "nottv" };
	public static List<String[][]> channelList = new ArrayList<String[][]>();
	public static String[][] channelArray_tokyo = { { "1ch", "NHK総合・東京" },
			{ "2ch", "NHKEテレ東京" }, { "4ch", "日テレ" }, { "5ch", "テレビ朝日" },
			{ "6ch", "TBS" }, { "7ch", "テレビ東京" }, { "8ch", "フジテレビ" },
			{ "9ch", "TOKYO　MX" }, { "12ch", "放送大学" } };
	public static String[][] channelArray_bs1 = { { "101ch", "NHKBS1" },
			{ "103ch", "NHKBSプレミアム" }, { "141ch", "BS日テレ" },
			{ "151ch", "BS朝日1" }, { "161ch", "BS-TBS" }, { "171ch", "BSジャパン" },
			{ "181ch", "BSフジ・181" } };
	public static String[][] channelArray_bs2 = { { "191ch", "WOWOWプライム" },
			{ "192ch", "WOWOWライブ" }, { "193ch", "WOWOWシネマ" },
			{ "200ch", "スターチャンネル1" }, { "201ch", "スターチャンネル2" },
			{ "202ch", "スターチャンネル3" }, { "211ch", "BS11" },
			{ "222ch", "BS12トゥエルビ" } };
	public static String[][] channelArray_bs3 = { { "231ch", "放送大学BS1" },
			{ "232ch", "放送大学BS2" }, { "233ch", "放送大学BS3" },
			{ "234ch", "グリーンチャンネル" }, { "236ch", "BSアニマックス" },
			{ "238ch", "FOXスポーツエンタ" }, { "241ch", "BSスカパー!" } };
	public static String[][] channelArray_bs4 = { { "242ch", "J SPORTS 1" },
			{ "243ch", "J SPORTS 2" }, { "244ch", "J SPORTS 3" },
			{ "245ch", "J SPORTS 4" }, { "251ch", "BS釣りビジョン" },
			{ "252ch", "イマジカBS・映画" }, { "255ch", "BS日本映画専門ch" } };
	public static String[][] channelArray_bs5 = { { "256ch", "ディズニーチャンネル" },
			{ "258ch", "ディーライフ" }, { "531ch", "放送大学ラジオ" } };
	public static String[][] channelArray_nottv = { { "nottv1", "NOTTV1" },
			{ "nottv2", "NOTTV2byホウドウキョク24" },
			{ "nottv3", "フジテレビONEスポーツ・バラエティ" },
			{ "nottv4", "フジテレビTWOドラマ・アニメ" }, { "nottv5", "時代劇専門チャンネル" },
			{ "nottv6", "AXN　海外ドラマ" }, { "nottv7", "アニマックス" },
			{ "nottv8", "スカサカ！24時間サッカー専門チャンネル" } };

	private static void delete(String date) throws Exception {
		List<ChannelProgram> cps = ChannelProgram
				.onSetChannelname("ｺﾞﾙﾌﾈｯﾄﾜｰｸ");
		Date dt = GlobalSetting.DB_DATETIME_FORMATTER4.parse(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String date1 = GlobalSetting.DB_DATETIME_FORMATTER4.format(calendar.getTime());
		date1 = date1.substring(0, 4) + "-" + date1.substring(4, 6) + "-"
				+ date1.substring(6, 8) + " " + "04:00";
		String date2 = date.substring(0, 4) + "-" + date.substring(4, 6) + "-"
				+ date.substring(6, 8) + " " + "04:00";
		for (ChannelProgram cp : cps) {
			deletePS.setInt(1, cp.channelid);
			deletePS.setString(2, date1);
			deletePS.setString(3, date2);
			deletePS.executeUpdate();
		}

		deletePS.close();
	}

	public static void help() {
		CommonUtil.print("java -cp tvlist_gather.jar gather.TVGolfRetrieve");
		CommonUtil.print("java -cp tvlist_gather.jar gather.TVGolfRetrieve");
	}

	public static void initChannelList() {
		channelList.add(channelArray_tokyo);
		channelList.add(channelArray_bs1);
		channelList.add(channelArray_bs2);
		channelList.add(channelArray_bs3);
		channelList.add(channelArray_bs4);
		channelList.add(channelArray_bs5);
		channelList.add(channelArray_nottv);
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			initChannelList();

			CommonUtil.print("now: %s",
					DateFormat.getDateTimeInstance().format(new Date()));
			today = GlobalSetting.DB_DATETIME_FORMATTER4.format(new Date());

			args = new String[] { "20160113", "bs1" };
			String dateStr;
			if (args.length > 0) {
				dateStr = args[0];
			} else {
				dateStr = GlobalSetting.DB_DATETIME_FORMATTER4.format(new Date());
			}

			String channelStr;
			if (args.length > 1) {
				channelStr = args[1];
			} else {
				channelStr = "bs1";
			}

			url = String.format(url, channelStr, dateStr);

			// conn = DBclass.getConn();
			// existsCheckPS = conn
			// .prepareStatement("select count(0) from tbl_channel_program where channelid=? and program_time=?");
			// prevProgramPS = conn
			// .prepareStatement("select max(program_time) from tbl_channel_program where channelid=? and program_time<?");
			// insertPS = conn
			// .prepareStatement("insert into tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'cron', now(), 'cron', now())");
			// // retrieve
			// deletePS = conn
			// .prepareStatement("DELETE from tbl_channel_program where channelid=? and program_time < ? and program_time >= ?");
			// delete(today);

			url = "http://tv.yahoo.co.jp/ajax/listings/?.bcrumb=dD1YZ0dsV0Imc2s9ZkRORjREWGR5LjFpRk5uMk10Qzd1c0kwV3ZJLQ==&type=normal&a=23&t=BS1&d=20160113&st=4&s=1&va=24&vb=0&vc=0&vd=0&ve=0";
			retrieveChannel(url, channelStr);

			// existsCheckPS.close();
			// insertPS.executeBatch();

			// // 删除超过8天以上的数据
			// conn.createStatement()
			// .execute(
			// "delete from tbl_channel_program where  DATEDIFF(now(), program_time) >8");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
		CommonUtil.print("-- over --");
	}

	private static int parseChannel(Elements channelElements, int channelset,
			String channelname)
			throws ParseException {
		int column = 0;

		Elements channelNotes = channelElements.select("td");
		for (int j = 1; j < channelNotes.size(); j++) {
			Element el = channelNotes.get(j);
			String temp = CommonUtil.xmlFilter(el.text());
			if (temp.indexOf(channelname) > -1) {
				column = j;
				break;
			}
		}

		return column;
	}

	private static int parseChannel(String channelname) throws ParseException {
		int column = 0;

		return column;
	}

	/**
	 * 解析数据
	 *
	 * @param rows
	 *            源数据集
	 * @param column
	 *            被解释数据所在列
	 * @return 节目数据
	 */
	private static String[][] parseRows(Elements rows, int column) {

		List<List<Integer>> rowspan = new ArrayList<List<Integer>>();
		List<List<String[]>> programsList = new ArrayList<List<String[]>>();

		String[][] temp_programs = new String[rows.size()][2];
		String[][] programs = temp_programs;
		int[] rowspan_rest = new int[8];

		for (int i = 0; i < rows.size(); i++) {
			Element row = rows.get(i);

			try {
				Elements cells = row.children();

				List<Integer> rowspanlist = new ArrayList<Integer>();
				if (null != cells && cells.size() > 0) {
					List<String[]> data = new ArrayList<String[]>();
					for (int m = 0; m < 8; m++) {

						if (rowspan_rest[m] > 1) {
							rowspanlist.add(Integer.valueOf(0));
							String[] program = { "", "" };
							data.add(program);

							rowspan_rest[m]--;

						} else {
							Element cell = cells.get(0);
							Integer span = Integer
									.valueOf(cell.attr("rowspan"));
							rowspanlist.add(span);
							String[] program = new String[2];
							program[0] = CommonUtil.xmlFilter(cell.select("dt")
									.text());
							program[1] = CommonUtil.xmlFilter(cell.select("dd")
									.text());
							data.add(program);

							cells.remove(0);

							rowspan_rest[m] = span.intValue();
						}

					}

					programsList.add(data);

				} else {
					List<String[]> data = new ArrayList<String[]>();
					for (int r = 0; r < 8; r++) {
						rowspanlist.add(Integer.valueOf(0));
						String[] program = { "", "" };
						data.add(program);

						rowspan_rest[r]--;
					}

					programsList.add(data);
				}

				rowspan.add(rowspanlist);

			} catch (Exception e) {
				e.printStackTrace(System.out);
			}

		}

		int l = 0;
		for (int n = 0; n < programsList.size(); n++, l++) {
			List<String[]> data = programsList.get(n);
			String[] program = data.get(column);
			if (!StringUtils.isNullOrEmpty(program[0])
					&& !StringUtils.isNullOrEmpty(program[1])) {
				temp_programs[l][0] = program[0];
				temp_programs[l][1] = program[1];
			} else {
				l--;
			}
		}

		programs = new String[l][2];
		for (int m = 0; m < l; m++) {
			programs[m][0] = temp_programs[m][0];
			programs[m][1] = temp_programs[m][1];
		}

		return programs;
	}

	private static void retrieveChannel(String url, String channelset)
			throws Exception {

		Document doc = null;
		for (int i = 0; doc == null && i < 5; i++) {
			CommonUtil.print("%d. retrieving %s", i + 1, url);
			try {
				doc = Jsoup
						.connect(url)
						.userAgent(
								"Mozilla/5.0 (Windows NT 6.1; rv:11.0) Gecko/20100101 Firefox/11.0")
						.timeout(50000).get();
			} catch (Exception e1) {
				e1.printStackTrace(System.out);
			}
		}

		// Element content = doc.getElementById("Tables");
		// Element tables = doc.child(1).child(2).child(7);
		// http://tv.yahoo.co.jp/ajax/listings/?.bcrumb=dD1YZ0dsV0Imc2s9ZkRORjREWGR5LjFpRk5uMk10Qzd1c0kwV3ZJLQ==&type=normal&a=23&t=BS1&d=20160113&st=4&s=1&va=24&vb=0&vc=0&vd=0&ve=0
		// Elements scripts = doc.getElementsByTag("script");

		Elements channels = doc.getElementById("ListingsHeader")
				.select("tbody").select("tr");

		// TODO ... 循环查找频道

		int channelcol = parseChannel(channels, 0, "101ch");

		Elements rows = doc.select("tbody").select("tr");
		String[][] programs = parseRows(rows, channelcol);



		Date dt = GlobalSetting.DB_DATETIME_FORMATTER4.parse(today);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String nextDay = GlobalSetting.DB_DATETIME_FORMATTER4.format(calendar.getTime());

		for (int i = 0; i < programs.length; i++) {
			String[] program = programs[i];
			if (program[0] != null && program[0].length() > 0) {
				String time = program[0].substring(0, 5);
				String program_time;
				Integer hour = Integer.valueOf(time.substring(0, 2));
				if (hour < 24) {
					program_time = today.substring(0, 4) + "-"
							+ today.substring(4, 6) + "-"
							+ today.substring(6, 8) + " " + time;
				} else {
					time = (hour - 24) > 10 ? String.valueOf(hour - 24)
							+ time.substring(2) : "0"
							+ String.valueOf(hour - 24) + time.substring(2);
					program_time = nextDay.substring(0, 4) + "-"
							+ nextDay.substring(4, 6) + "-"
							+ nextDay.substring(6, 8) + " " + time;
				}
				String title = program[1];
				if (title.length() > 16)
					title = title.substring(0, 16);
				String contents = program[1];
				if (contents.length() > 66)
					contents = contents.substring(0, 66);

				List<ChannelProgram> cps = ChannelProgram
						.onSetChannelname("ｺﾞﾙﾌﾈｯﾄﾜｰｸ");
				for (ChannelProgram cp : cps) {
					cp.program_time = program_time;
					cp.title = title;
					cp.content = contents;
					CommonUtil.print(cp.toString());

					if (cp.channelid != -1)
						DBclass.addToDb(cp, prevProgramPS, insertPS,
								existsCheckPS);
				}
			}
		}
	}

}
