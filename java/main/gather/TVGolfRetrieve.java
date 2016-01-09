package gather;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.StringUtils;

/**
 *
 * @author Administrator
 *
 */
public class TVGolfRetrieve {
	static String url = "http://www.golfnetwork.co.jp/program/";

	public final static DateFormat DB_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER2 = new SimpleDateFormat("yyyy-MM-dd HH", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER3 = new SimpleDateFormat("HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER4 = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);

	static Connection conn;
	static PreparedStatement existsCheckPS;
	static PreparedStatement prevProgramPS;
	static PreparedStatement insertPS;
	static PreparedStatement deletePS;
	public static String today;


	private static List<Integer> cell_rowspan_list = new ArrayList<Integer>();

	private static void delete(String date) throws Exception {
		List<ChannelProgram> cps = ChannelProgram.onSetChannelname("ｺﾞﾙﾌﾈｯﾄﾜｰｸ");
		Date dt = DB_DATETIME_FORMATTER4.parse(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String date1 = DB_DATETIME_FORMATTER4.format(calendar.getTime());
		date1 = date1.substring(0, 4) + "-" + date1.substring(4, 6) + "-" + date1.substring(6, 8)+ " "+"04:00";
		String date2 = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8)+ " "+"04:00";
		for (ChannelProgram cp : cps) {
			deletePS.setInt(1, cp.channelid);
			deletePS.setString(2, date1);
			deletePS.setString(3, date2);
			int rs = deletePS.executeUpdate();
		}

		deletePS.close();
	}

	public static void help() {
		DBclass.print("java -cp tvlist_gather.jar gather.TVGolfRetrieve");
		DBclass.print("java -cp tvlist_gather.jar gather.TVGolfRetrieve");
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			DBclass.print("now: %s", DateFormat.getDateTimeInstance().format(new Date()));
			today = DB_DATETIME_FORMATTER4.format(new Date());

			conn = DBclass.getConn();
			existsCheckPS = conn.prepareStatement("select count(0) from tbl_channel_program where channelid=? and program_time=?");
			prevProgramPS = conn.prepareStatement("select max(program_time) from tbl_channel_program where channelid=? and program_time<?");
			insertPS = conn
					.prepareStatement("insert into tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'cron', now(), 'cron', now())");
			// retrieve
			deletePS = conn.prepareStatement("DELETE from tbl_channel_program where channelid=? and program_time < ? and program_time >= ?");
			delete(today);

			retrieveGolf(url);

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

	private static int parseDates(Elements dates) throws ParseException {
		int column = 0;

		Date dt = DB_DATETIME_FORMATTER4.parse(today);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);

		int m = calendar.get(Calendar.MONTH) + 1;
		int d = calendar.get(Calendar.DAY_OF_MONTH);

		String str = d < 10 ? String.valueOf(m) + "月" + "0" + String.valueOf(d)
				+ "日" : String.valueOf(m) + "月" + String.valueOf(d) + "日";


		Elements dateNotes = dates.select("th");
		for (int j = 1; j < dateNotes.size(); j++) {
			Element el = dateNotes.get(j);
			String temp = DBclass.xmlFilte(el.text());
			if(temp.indexOf(str) > -1) {
				column = j;
				break;
			}
		}

		return column;
	}

	/**
	 * 解析数据，默认解析第一列
	 *
	 * @param rows
	 *            源数据集
	 * @return 节目数据
	 */
	private static String[][] parseRows(Elements rows) {
		String[][] programs = new String[rows.size()][2];
		int rowspan_0 = 0;
		int rowspan_1 = 0;
		for (int i = 0; i < rows.size(); i++) {
			Element row = rows.get(i);
			try {
				Elements cells = row.children();

				if (rowspan_0 == 0) {
					Element cell_0 = cells.get(0);
					rowspan_0 = Integer.valueOf(cell_0.attr("rowspan"));
					if (rowspan_1 == 0) {
						Element cell_1 = cells.get(1);
						rowspan_1 = Integer.valueOf(cell_1.attr("rowspan"));
						programs[i][0] = DBclass.xmlFilte(cell_1.select("dt").text());
						programs[i][1] = DBclass.xmlFilte(cell_1.select("dd").text());

					}
				} else if (rowspan_1 == 0) {
					Element cell_0 = cells.get(0);
					rowspan_1 = Integer.valueOf(cell_0.attr("rowspan"));
					programs[i][0] = DBclass.xmlFilte(cell_0.select("dt").text());
					programs[i][1] = DBclass.xmlFilte(cell_0.select("dd").text());
				}
				rowspan_0--;
				rowspan_1--;
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		return programs;
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

		String[][] programs = new String[rows.size()][2];
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
							program[0] = DBclass.xmlFilte(cell.select("dt")
									.text());
							program[1] = DBclass.xmlFilte(cell.select("dd")
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

		for (int n = 0, l = 0; n < programsList.size(); n++, l++) {
			List<String[]> data = programsList.get(n);
			String[] program = data.get(column);
			if (!StringUtils.isNullOrEmpty(program[0])
					&& !StringUtils.isNullOrEmpty(program[1])) {
				programs[l][0] = program[0];
				programs[l][1] = program[1];
			} else {
				l--;
			}
		}

		return programs;
	}

	// 地上波
	private static void retrieveGolf(String url) throws Exception {

		Document doc = null;
		for (int i = 0; doc == null && i < 5; i++) {
			DBclass.print("%d. retrieving %s", i + 1, url);
			try {
				doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; rv:11.0) Gecko/20100101 Firefox/11.0").timeout(50000).get();
			} catch (Exception e1) {
				e1.printStackTrace(System.out);
			}
		}

		Elements dates = doc.select("thead").select("tr");
		int col = parseDates(dates);

		Elements rows = doc.select("tbody").select("tr");
		String[][] programs = parseRows(rows, col);

		int weekDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK);
		Date dt = DB_DATETIME_FORMATTER4.parse(today);
		Calendar calendar=Calendar.getInstance();
	    calendar.setTime(dt);
	    calendar.add(Calendar.DAY_OF_MONTH,1);
	    String nextDay = DB_DATETIME_FORMATTER4.format(calendar.getTime());

		for (int i = 0; i < programs.length; i++) {
			String[] program = programs[i];
			if (program[0] != null && program[0].length()>0){
				String time = program[0].substring(0, 5);
				String program_time;
				Integer hour = Integer.valueOf(time.substring(0, 2));
				if (hour < 24) {
					program_time = today.substring(0, 4) + "-" + today.substring(4, 6) + "-" + today.substring(6, 8)
							+ " " + time;
				} else {
					time = (hour - 24) > 10 ? String.valueOf(hour - 24)
							+ time.substring(2) : "0"
							+ String.valueOf(hour - 24) + time.substring(2);
					program_time = nextDay.substring(0, 4) + "-" + nextDay.substring(4, 6) + "-"
							+ nextDay.substring(6, 8) + " " + time;
				}
				String title = program[1];
				if (title.length() > 16)
					title = title.substring(0, 16);
				String contents = program[1];
				if (contents.length() > 66)
					contents = contents.substring(0, 66);

				List<ChannelProgram> cps = ChannelProgram.onSetChannelname("ｺﾞﾙﾌﾈｯﾄﾜｰｸ");
				for (ChannelProgram cp : cps) {
					cp.program_time = program_time;
					cp.title = title;
					cp.contents = contents;
					DBclass.print(cp.toString());

					if (cp.channelid != -1)
						DBclass.addToDb(cp, prevProgramPS, insertPS, existsCheckPS);
				}
			}
		}
	}

}
