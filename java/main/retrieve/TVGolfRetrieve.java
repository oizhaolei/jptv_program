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
public class TVGolfRetrieve {
	static String url = "http://www.golfnetwork.co.jp/program/";

	static Connection conn;
	static PreparedStatement existsCheckPS;
	static PreparedStatement getPrevProgramPS;
	static PreparedStatement insertPS;
	static PreparedStatement deletePS;
	public static String today;

	private static void delete(String date) throws Exception {
		List<ChannelProgram> cps = GlobalSetting.onSetChannelname("ｺﾞﾙﾌﾈｯﾄﾜｰｸ");
		Date dt = GlobalSetting.DB_DATETIME_FORMATTER4.parse(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String date1 = GlobalSetting.DB_DATETIME_FORMATTER4.format(calendar.getTime());
		date1 = date1.substring(0, 4) + "-" + date1.substring(4, 6) + "-" + date1.substring(6, 8)+ " "+"04:00";
		String date2 = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8)+ " "+"04:00";
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

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			CommonUtil.print("now: %s", DateFormat.getDateTimeInstance().format(new Date()));
			today = GlobalSetting.DB_DATETIME_FORMATTER4.format(new Date());

			conn = DBclass.getConn();
			existsCheckPS = conn.prepareStatement(GlobalSetting.existsCheck);
			getPrevProgramPS = conn.prepareStatement(GlobalSetting.getPrevProgram);
			insertPS = conn.prepareStatement(GlobalSetting.insert_golf);
			// retrieve
			deletePS = conn.prepareStatement(GlobalSetting.delete);
			delete(today);

			retrieveGolf(url);

			existsCheckPS.close();
//			insertPS.executeBatch();

			// 删除超过8天以上的数据
			conn.createStatement().execute("delete from tbl_channel_program where  DATEDIFF(now(), program_time) >8");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
		CommonUtil.print("-- over --");
	}

	private static int parseDates(Elements dates) throws ParseException {
		int column = 0;

		Date dt = GlobalSetting.DB_DATETIME_FORMATTER4.parse(today);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);

		int m = calendar.get(Calendar.MONTH) + 1;
		int d = calendar.get(Calendar.DAY_OF_MONTH);

		String str = d < 10 ? String.valueOf(m) + "月" + "0" + String.valueOf(d)
				+ "日" : String.valueOf(m) + "月" + String.valueOf(d) + "日";


		Elements dateNotes = dates.select("th");
		for (int j = 1; j < dateNotes.size(); j++) {
			Element el = dateNotes.get(j);
			String temp = CommonUtil.xmlFilter(el.text());
			if(temp.indexOf(str) > -1) {
				column = j;
				break;
			}
		}

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

	// 地上波
	private static void retrieveGolf(String url) throws Exception {

		Document doc = null;
		for (int i = 0; doc == null && i < 5; i ++) {
			CommonUtil.print("%d. retrieving %s", i + 1, url);
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

		Date dt = GlobalSetting.DB_DATETIME_FORMATTER4.parse(today);
		Calendar calendar=Calendar.getInstance();
	    calendar.setTime(dt);
	    calendar.add(Calendar.DAY_OF_MONTH,1);
	    String nextDay = GlobalSetting.DB_DATETIME_FORMATTER4.format(calendar.getTime());

		for (int i = 0; i < programs.length; i++) {
			String[] program = programs[i];
			if (program[0] != null && program[0].length() > 0){
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

				List<ChannelProgram> cps = GlobalSetting.onSetChannelname("ｺﾞﾙﾌﾈｯﾄﾜｰｸ");
				for (ChannelProgram cp : cps) {
					cp.program_time = program_time;
					cp.title = title;
					cp.content = contents;
					CommonUtil.print(cp.toString());

					if (cp.channelid != -1)
						DBclass.addToDb(cp, getPrevProgramPS, insertPS, existsCheckPS);
				}
			}
		}
	}

}
