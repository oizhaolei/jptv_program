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

import com.mysql.jdbc.StringUtils;

public class SoNetTVProgramsRetrieve {
	static String url = "http://tv.so-net.ne.jp/chart/%s.action?head=%s&span=1&sticky=false&descriptive=true";

	static String tokyo_chijo_url = "http://tv.so-net.ne.jp/chart/23.action?head=%s&span=24&sticky=false&cellHeight=5&descriptive=true";
	static String osaka_chijo_url = "http://tv.so-net.ne.jp/chart/40.action?head=%s&span=24&sticky=false&cellHeight=5&descriptive=true";
	static String kobe_chijo_url = "http://tv.so-net.ne.jp/chart/42.action?head=%s&span=24&sticky=false&cellHeight=5&descriptive=true";
	static String bs1 = "http://tv.so-net.ne.jp/chart/bs1.action?head=%s&span=24&sticky=false&cellHeight=5&descriptive=true";
	static String bs2 = "http://tv.so-net.ne.jp/chart/bs2.action?head=%s&span=24&sticky=false&cellHeight=5&descriptive=true";
	static String bs3 = "http://tv.so-net.ne.jp/chart/bs3.action?head=%s&span=24&sticky=false&cellHeight=5&descriptive=true";
	static String bs4 = "http://tv.so-net.ne.jp/chart/bs4.action?head=%s&span=24&sticky=false&cellHeight=5&descriptive=true";

	public final static DateFormat DB_DATETIME_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER2 = new SimpleDateFormat(
			"yyyy-MM-dd HH", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER3 = new SimpleDateFormat(
			"HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER4 = new SimpleDateFormat(
			"yyyyMMdd", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER5 = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.JAPAN);

	static Connection conn;

	static PreparedStatement existsCheckPS;
	static PreparedStatement prevProgramPS;
	static PreparedStatement insertPS;
	static PreparedStatement deletePS;

	public static Date date;

	public static String[] channelNames = { "ＮＨＫ総合・東京", "ＮＨＫＥテレ１・東京", "日テレ",
			"テレビ朝日", "ＴＢＳ", "テレビ東京", "フジテレビ", "ＴＯＫＹＯ　ＭＸ１", "放送大学１", "ＮＨＫ ＢＳ１",
			"ＮＨＫ ＢＳプレミアム", "ＢＳ日テレ", "ＢＳ朝日", "ＢＳ-ＴＢＳ", "ＢＳジャパン", "ＢＳフジ",
			"ＷＯＷＯＷプライム", "ＷＯＷＯＷライブ", "ＷＯＷＯＷシネマ", "スター・チャンネル1", "スター・チャンネル2",
			"スター・チャンネル3", "ＢＳ１１", "BS12 トゥエルビ", "放送大学BS1", "グリーンチャンネル",
			"BSアニマックス", "FOXスポーツエンタ", "BSスカパー!", "J SPORTS 1", "J SPORTS 2",
			"J SPORTS 3", "J SPORTS 4", "BS釣りビジョン", "イマジカBS", "BS日本映画専門チャンネル",
			"ディズニー・チャンネル", "ディーライフ", "ウェザーニュース", "ＮＨＫ総合・大阪", "ＮＨＫＥテレ１・大阪",
			"ＭＢＳ毎日放送", "ＡＢＣテレビ", "テレビ大阪", "関西テレビ", "よみうりテレビ", "ＮＨＫ総合・神戸",
			"サンテレビ" };

	private static void delete(String date) throws ParseException, SQLException {
		for(String channelName : channelNames) {
			List<ChannelProgram> cps = ChannelProgram
					.onSetChannelname(channelName);
			//
			Date dt = DB_DATETIME_FORMATTER4.parse(date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			calendar.add(Calendar.DATE, 1);
			String date1 = DB_DATETIME_FORMATTER4.format(calendar.getTime());
			date1 = date1.substring(0, 4) + "-" + date1.substring(4, 6) + "-"
					+ date1.substring(6, 8) + " " + "00:00";
			String date2 = date.substring(0, 4) + "-" + date.substring(4, 6) + "-"
					+ date.substring(6, 8) + " " + "00:00";
			for (ChannelProgram cp : cps) {
				deletePS.setInt(1, cp.channelid);
				deletePS.setString(2, date1);
				deletePS.setString(3, date2);
				deletePS.executeUpdate();
			}
		}
	}


	/**
	 * 获得指定日期的间隔日期
	 *
	 * @param specifiedDay
	 * @return
	 */
	public static Date getDateAfterSpecifiedDay(Date specifiedDay, int days) {
		Calendar c = Calendar.getInstance();

		c.setTime(specifiedDay);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day + days);

		return c.getTime();
	}

	private static int getHeightByStyle(String css) {
		int height = 0;
		String px = parseCssStyle(css, "height");
		if (!StringUtils.isNullOrEmpty(px) && px.indexOf("px") > -1) {
			px = px.substring(0, px.length() - 2);
			height = new Integer(px).intValue();
		}
		return height;
	}

	private static int getProgramHour(int top) {
		return ( top -20 ) / 300;
	}

	private static int getValueByCss(String css, String prop) {
		int height = 0;
		String px = parseCssStyle(css, prop);
		if (!StringUtils.isNullOrEmpty(px) && px.indexOf("px") > -1) {
			px = px.substring(0, px.length() - 2);
			height = new Integer(px).intValue();
		}
		return height;
	}

	public static void help() {
		DBclass.print("java -cp tvlist_gather.jar gather.SoNetTVProgramsRetrieve");
		DBclass.print("java -cp tvlist_gather.jar gather.SoNetTVProgramsRetrieve");
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			DBclass.print("now: %s",
					DateFormat.getDateTimeInstance().format(new Date()));
			date = new Date();

			conn = DBclass.getConn();
			existsCheckPS = conn
					.prepareStatement("SELECT COUNT(0) FROM tbl_channel_program WHERE channelid=? AND program_time=?");
			prevProgramPS = conn
					.prepareStatement("SELECT MAX(program_time) FROM tbl_channel_program WHERE channelid=? AND program_time<?");
			insertPS = conn
					.prepareStatement("INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) VALUES (?, ?, ?, ?, 'cron', now(), 'cron', now())");
			deletePS = conn.prepareStatement("DELETE from tbl_channel_program WHERE channelid=? AND program_time < ? AND program_time >= ?");
			// retrieve

			// args = new String[] { "2" };
			int days = 0;

			if (args.length > 0) {
				days = Integer.valueOf(args[0]);
			} else {
				days = 0;
			}

			for (int i = 0; i <= days; i++) {
				Date program_date = getDateAfterSpecifiedDay(new Date(), i);
				String timeStr = DB_DATETIME_FORMATTER4.format(program_date)
						+ "0000";
				delete(DB_DATETIME_FORMATTER4.format(program_date));
				retrieveProgramByUrl(String.format(tokyo_chijo_url, timeStr),
						program_date);
				retrieveProgramByUrl(String.format(osaka_chijo_url, timeStr),
						program_date);
				retrieveProgramByUrl(String.format(kobe_chijo_url, timeStr),
						program_date);
				retrieveProgramByUrl(String.format(bs1, timeStr), program_date);
				retrieveProgramByUrl(String.format(bs2, timeStr), program_date);
				retrieveProgramByUrl(String.format(bs3, timeStr), program_date);
				retrieveProgramByUrl(String.format(bs4, timeStr), program_date);
			}

			deletePS.close();
			existsCheckPS.close();
			insertPS.executeBatch();

			// 删除超过8天以上的数据
			conn.createStatement()
					.execute(
							"delete from tbl_channel_program where  DATEDIFF(now(), program_time) >8");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
		DBclass.print("-- over --");
	}

	private static String parseCssStyle(String str, String key) {
		String value = null;
		str = str.replaceAll("\\r\\n", "").replace(" ", "");
		String[] arr = str.split(";");
		for (String style : arr) {
			String[] prop = style.split(":");
			if (prop[0].equalsIgnoreCase(key)) {
				value = prop[1];
			}
		}
		return value;
	}

	private static void parseDoc(Document doc, Date program_date)
			throws SQLException, ParseException {
		String channelName = null;
		String title = null;
		String contents = null;
		String program_time = null;
		int program_hour = 0;
		boolean timeFlg = true;
		int sum = 0;
		Elements charts = doc.getElementById("chartColumn").children();
		for (int i = 0; i < charts.size(); i++) {
			Element div = charts.get(i);
			String attr = div.attr("class");
			if (("cell-station cell-top").equals(attr)) {
				// 电视台
				sum = 0;
				channelName = DBclass.xmlFilte(div.attr("title"));
				title = null;
				contents = null;
				program_time = null;
				program_hour = 0;

			} else if (attr != null && attr.indexOf("cell-schedule") > -1
					&& attr.indexOf("system-cell-schedule-head") > -1) {
				// 节目
				String style = div.attr("style");
				int height = getHeightByStyle(style);
				int top = getValueByCss(style, "top");

				Elements detail = div.select("table").select("tr").select("td");
				for (Element program : detail) {
					if (("td-minute").equals(program.attr("class"))) {
						// 节目时间
						String min = DBclass.xmlFilte(program.text()).trim();
						if (min.length() == 2) {
							// if (!("00").equals(min)) {
							// int h = sum / 297;
							// if (h > program_hour) {
							// program_hour = h;
							// }
							//
							// } else {
							// if (sum == 0) {
							// program_hour = 0;
							// } else if (sum > 0) {
							// int h = sum / 297;
							// int rest = sum % 297;
							// if (rest > 0) {
							// program_hour = h + 1;
							// } else {
							// program_hour = h;
							// }
							// }
							//
							// }

							program_hour = getProgramHour(top);

							String hour = program_hour > 9 ? ("" + program_hour)
									: ("0" + program_hour);
							program_time = DB_DATETIME_FORMATTER5
									.format(program_date)
									+ " "
									+ hour
									+ ":"
									+ min;


						} else if (min.length() > 2 && min.indexOf(":") > -1) {
							Elements els = program.select("span");
							for (Element el : els) {
								if (("td-minute").equals(el.attr("class"))) {
									min = DBclass.xmlFilte(el.text()).trim();
									program_time = DB_DATETIME_FORMATTER5
											.format(getDateAfterSpecifiedDay(
													program_date, -1))
											+ " " + min;
								} else if (("schedule-titleC").equals(el
										.attr("class"))) {
									// 节目标题
									title = DBclass.xmlFilte(el.text());
									if (title.length() > 16) {
										title = title.substring(0, 16);
									}

									// 2016.02.07 contents的内容只存title
									contents = DBclass.xmlFilte(el.text());
									if (contents.length() > 66) {
										contents = contents.substring(0, 66);
									}
								} else if (("schedule-summaryC").equals(el
										.attr("class"))) {
									// 节目简介
//									contents = DBclass.xmlFilte(el.text());
								}
							}

							sum = sum + height;
						}

					} else if (("td-schedule").equals(program.attr("class"))) {
						// 节目内容
						Elements els = program.select("span");
						for (Element el : els) {
							if (("schedule-title").equals(el.attr("class"))) {
								// 节目标题
								title = DBclass.xmlFilte(el.text());
								if (title.length() > 16) {
									title = title.substring(0, 16);
								}

								// 2016.02.07 contents的内容只存title
								contents = DBclass.xmlFilte(el.text());
								if (contents.length() > 66) {
									contents = contents.substring(0, 66);
								}
							} else if (("schedule-summary").equals(el
									.attr("class"))) {
								// 节目简介
//								contents = DBclass.xmlFilte(el.text());
							}
						}

						sum = sum + height;
					}
				}

				List<ChannelProgram> cps = ChannelProgram
						.onSetChannelname(channelName);
				for (ChannelProgram cp : cps) {
					cp.program_time = program_time;
					cp.title = title;
					cp.contents = contents;
					if (cp.channelid != -1 && timeFlg) {
						DBclass.addToDb(cp, prevProgramPS, insertPS,
								existsCheckPS);
					} else {
						DBclass.print("ignore:%s, %s, %s, %s", cp.channelid,
								cp.title, cp.program_time, timeFlg);
					}
				}

			}

		}

	}

	private static void retrieveProgramByUrl(String url, Date program_date)
			throws IOException {

		Document doc = null;
		for (int i = 0; doc == null && i < 5; i++) {
			DBclass.print("%d. retrieving %s", i + 1, url);
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

		try {
			parseDoc(doc, program_date);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
