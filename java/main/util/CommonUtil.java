package util;

import java.security.MessageDigest;

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
}
