package model;

public class JsonProgram {
	public String channel_key;
	public String key;
	public long starttime;
	public long endtime;
	public String title;
	public String content;
	public boolean isEmpty() {
		if (null == channel_key || ("").equals(channel_key)) {
			return true;
		}
		if (null == title || ("").equals(title)) {
			return true;
		}
		if (null == content || ("").equals(content)) {
			return true;
		}
		return false;
	}
}
