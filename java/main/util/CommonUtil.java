package util;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;

import setting.GlobalSetting;

public class CommonUtil {
	public static String xmlFilter(String str) {
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
	public static void print(String msg, Object... args) {
		System.out.print(GlobalSetting.DB_DATETIME_FORMATTER10.format(new Date()) + " ");
		System.out.println(String.format(msg, args));
	}
	public static String toMD5(byte[] source) {    	
    	try{
    		MessageDigest md = MessageDigest.getInstance("MD5");
    	    md.update( source );    	    
    	    StringBuffer buf=new StringBuffer();    	    
    	    for(byte b:md.digest())
    	    	buf.append(String.format("%02x", b&0xff) );    	     
    	    return buf.toString();
    	}catch( Exception e ){
    		e.printStackTrace(); return null;
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
}
