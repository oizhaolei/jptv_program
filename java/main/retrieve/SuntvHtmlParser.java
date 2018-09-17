package retrieve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import model.ChannelProgram;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import setting.GlobalSetting;
import util.CommonUtil;
import db.DBclass;

/**
 * 获取电视节目预告
 * 
 * @author lenovo
 * 
 */
public class SuntvHtmlParser {
	final static String SUNTV_WEEKLY_URL = "http://sun-tv.co.jp/weekly";
	final static String CHANNEL = "サンテレビ";
	static Connection conn;

	static PreparedStatement existsCheckPS;
	static PreparedStatement getPrevProgramPS;
	static PreparedStatement insertPS;
	static PreparedStatement deletePS;

	public static Date date;

	public static String[] channelNames = { "サンテレビ" };

	private static void delete(String date) throws ParseException, SQLException {
		for (String channelName : channelNames) {
			List<ChannelProgram> cps = GlobalSetting
					.onSetChannelname(channelName);
			//
			Date dt = GlobalSetting.DB_DATETIME_FORMATTER4.parse(date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			calendar.add(Calendar.DATE, 1);
			String date1 = GlobalSetting.DB_DATETIME_FORMATTER4.format(calendar
					.getTime());
			date1 = date1.substring(0, 4) + "-" + date1.substring(4, 6) + "-"
					+ date1.substring(6, 8) + " " + "00:00";
			String date2 = date.substring(0, 4) + "-" + date.substring(4, 6)
					+ "-" + date.substring(6, 8) + " " + "00:00";
			for (ChannelProgram cp : cps) {
				deletePS.setInt(1, cp.channelid);
				deletePS.setString(2, date1);
				deletePS.setString(3, date2);
				deletePS.executeUpdate();
			}
		}
	}

	public static void help() {
		CommonUtil.print("java -cp epg-service.jar retrieve.SuntvHtmlParser");
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			CommonUtil.print("SuntvHtmlParser-------------now: %s", DateFormat
					.getDateTimeInstance().format(new Date()));
			date = new Date();

			conn = DBclass.getConn();
			existsCheckPS = conn.prepareStatement(GlobalSetting.existsCheck);
			getPrevProgramPS = conn
					.prepareStatement(GlobalSetting.getPrevProgram);
			insertPS = conn.prepareStatement(GlobalSetting.insert_suntv);
			deletePS = conn.prepareStatement(GlobalSetting.delete);
			// retrieve
			delete(GlobalSetting.DB_DATETIME_FORMATTER4.format(new Date()));
			retrieveProgramByUrl(SUNTV_WEEKLY_URL, CHANNEL);

			//String html = readFileByLines("D:/logs/epgdata/suntv-21080917.html");
			//parseDoc(html, "サンテレビ");

			existsCheckPS.close();
			getPrevProgramPS.close();
			insertPS.close();
			deletePS.close();

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

	public static void parseDoc(String htmlStr, String channelName)
			throws SQLException, ParseException {
		Document doc = Jsoup.parse(htmlStr);
		parseDoc(doc, channelName);
	}

	public static void parseDoc(Document doc, String channelName)
			throws SQLException, ParseException {
		Date program_date = new Date(); // getDateAfterSpecifiedDay(new Date(),
										// day);
		String program_date_string = GlobalSetting.DB_DATETIME_FORMATTER5
				.format(program_date);
		String current_year = new SimpleDateFormat("yyyy年", Locale.getDefault())
				.format(new Date());

		boolean timeFlg = true;

		Elements box = doc.getElementById("divboxBOX").children();
		int total_count = 0;
		for (int i = 0; i < box.size(); i++) {
			Element div = box.get(i);
			String attr = div.attr("class");
			if (null != attr && attr.indexOf("float_divbox daybox day") > -1) {
				Elements day_data = div.children();

				for (Element sub_day_data : day_data) {
					attr = sub_day_data.attr("class");
					if (("weekbox").equals(attr)) {
						// 日期
						program_date_string = current_year
								+ CommonUtil.xmlFilter(sub_day_data.text())
										.trim();
						// int end = program_date_string.indexOf("(");
						// program_date_string =
						// program_date_string.substring(0, end);
						program_date = GlobalSetting.DB_DATETIME_FORMATTER12
								.parse(program_date_string);
						program_date_string = GlobalSetting.DB_DATETIME_FORMATTER5
								.format(program_date);

					} else if (attr != null
							&& (attr.indexOf("box sv0 day") > -1 || attr
									.indexOf("box sv1 day") > -1)) {
						// 节目
						String title = "";
						String contents = "";
						String program_time = "";

						Elements detail = sub_day_data.children().get(0)
								.children();
						for (Element program : detail) {
							if (("st-time").equals(program.attr("class"))) {
								// 节目时间
								String hour_min = CommonUtil.xmlFilter(
										program.text()).trim();
								if (("00:00").equals(hour_min)) {
									program_date = CommonUtil
											.getDateAfterSpecifiedDay(
													program_date, 1);
									program_date_string = GlobalSetting.DB_DATETIME_FORMATTER5
											.format(program_date);
									program_time = GlobalSetting.DB_DATETIME_FORMATTER5
											.format(program_date)
											+ " "
											+ hour_min;
								} else {
									program_time = program_date_string + " "
											+ hour_min;
								}
							} else if (("prg-icon-re").equals(program
									.attr("class"))) {
								title += "「"
										+ CommonUtil.xmlFilter(program.text())
												.trim() + "」";
							} else if (("prg-icon-end").equals(program
									.attr("class"))) {
								title += "「"
										+ CommonUtil.xmlFilter(program.text())
												.trim() + "」";
							} else if (("prg-icon-new").equals(program
									.attr("class"))) {
								title += "「"
										+ CommonUtil.xmlFilter(program.text())
												.trim() + "」";
							} else if (("prg-icon-lang").equals(program
									.attr("class"))) {
								title += "「"
										+ CommonUtil.xmlFilter(program.text())
												.trim() + "」";
							} else if (("st-title").equals(program
									.attr("class"))) {
								Elements program_children = program.children();
								title += CommonUtil.xmlFilter(program.text())
										.trim();
							} else if (("st-detail").equals(program
									.attr("class"))) {
								Elements program_children = program.children();
								for (Element content_el : program_children) {
									if (("st-number").equals(content_el
											.attr("class"))) {
										contents += CommonUtil.xmlFilter(
												content_el.text()).trim()
												+ "　";
									} else if (("st-detailtxt")
											.equals(content_el.attr("class"))) {
										contents += CommonUtil.xmlFilter(
												content_el.text()).trim();
									}
								}

							}
						}
						if (!("").equals(program_time) && !("").equals(title)) {
							List<ChannelProgram> cps = GlobalSetting
									.onSetChannelname(channelName);
							for (ChannelProgram cp : cps) {
								cp.program_time = program_time;
								cp.title = title;
								cp.content = contents;
								if (cp.channelid != -1 && timeFlg) {
									CommonUtil
											.print("SuntvHtmlParser-------------add:%s, %s, %s",
													cp.channelid, cp.title,
													cp.program_time);
									DBclass.addToDbWithProgramEndTime(cp,
											getPrevProgramPS, insertPS,
											existsCheckPS);
									total_count++;
								} else {
									CommonUtil
											.print("SuntvHtmlParser-------------ignore:%s, %s, %s, %s",
													cp.channelid, cp.title,
													cp.program_time, timeFlg);
								}
							}
						}
					}
				}
			}
		}

		CommonUtil.print(
				"SuntvHtmlParser-------------------total count : 【%d】",
				total_count);
	}

	private static void retrieveProgramByUrl(String url, String channel_name)
			throws IOException {

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

		try {
			parseDoc(doc, channel_name);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */
	public static String readFileByLines(String fileName) {
		String result_str = "";
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			// int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				// System.out.println("line " + line + ": " + tempString);
				result_str += tempString;
				// line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		;
		return result_str;
	}
}
