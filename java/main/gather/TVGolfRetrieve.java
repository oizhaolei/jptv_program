package gather;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
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
	static int i = 0;
	static String str[][];
	public static String date;

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			DBclass.print("now: %s", DateFormat.getDateTimeInstance().format(new Date()));
//			args = new String[]{"20120709"};
			if (args.length > 0)
				date = args[0];
			else
				date = DB_DATETIME_FORMATTER4.format(new Date());

			conn = DBclass.getConn();
			existsCheckPS = conn.prepareStatement("select count(0) from tbl_channel_program where channelid=? and program_time=?");
			prevProgramPS = conn.prepareStatement("select max(program_time) from tbl_channel_program where channelid=? and program_time<?");
			insertPS = conn
					.prepareStatement("insert into tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'cron', now(), 'cron', now())");
			// retrieve
			deletePS = conn.prepareStatement("DELETE from tbl_channel_program where channelid=? and program_time < ? and program_time >= ?");
			delete(date);
			
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
		Elements programs = doc.select("tbody").select("tr");
		str = new String[programs.size()][7];
		for (Element program : programs) {
			try {
				parseProgram(program);
				i++;
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		int weekDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK);
		Date dt = DB_DATETIME_FORMATTER4.parse(date);
		Calendar calendar=Calendar.getInstance(); 
	    calendar.setTime(dt); 
	    calendar.add(Calendar.DAY_OF_MONTH,1); 
	    String date2 = DB_DATETIME_FORMATTER4.format(calendar.getTime());
		for(int n = 0; n<i; n++){
			for(int m = 0; m<7; m++){
				if(m==weekDay-2 && !"*".equals(str[n][m])){
					String time = str[n][m].substring(0, 5);
					String program_time;
					if (Integer.valueOf(time.substring(0, 2)) < 24) {
						program_time = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8)
								+ " " + time;
					} else {
						time = (Integer.valueOf(time.substring(0, 2)) - 24) + time.substring(2);
						program_time = date2.substring(0, 4) + "-" + date2.substring(4, 6) + "-"
								+ date2.substring(6, 8) + " " + time;
					}
					String title = str[n][m].substring(5);
					if (title.length() > 16)
						title = title.substring(0, 16);
					String contents = str[n][m].substring(5);
					if (contents.length() > 66)
						contents = contents.substring(0, 66);

					List<ChannelProgram> cps = ChannelProgram.onSetChannelname("ｺﾞﾙﾌﾈｯﾄﾜｰｸ");
					for (ChannelProgram cp : cps) {
						cp.program_time = program_time;
						cp.title = title;
						cp.contents = contents;
						if (cp.channelid != -1)
							DBclass.addToDb(cp, prevProgramPS, insertPS, existsCheckPS);
					}
				}
			}
		}

	}

	private static void parseProgram(Element element) throws SQLException {
        
		Elements children = element.children();
		
		int j = 0;
		int n = 0;
		for (Element child : children) {
			
			if("td".equals(child.tagName())){
				int rowspan = Integer.valueOf(child.attr("rowspan"));
				if(i>0){
					for(int m = j+n; m < 7; m++){
						if(!"*".equals(str[i][m])){
							str[i][m] =DBclass.xmlFilte(child.select("dt").text())+ DBclass.xmlFilte(child.select("dd").text());
							break;
						}
						n++;
					}				
				}else{
					str[i][j] =DBclass.xmlFilte(child.select("dt").text()) + DBclass.xmlFilte(child.select("dd").text());				
				}
				for(int k = 0; k < rowspan - 1; k++){
					str[i+k+1][j+n] = "*";
				}
				j++;
				
			}
		}
	}

	public static void help() {
		DBclass.print("java -cp tvlist_gather.jar gather.TVGolfRetrieve");
		DBclass.print("java -cp tvlist_gather.jar gather.TVGolfRetrieve");
	}

}
