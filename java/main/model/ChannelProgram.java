package model;

public class ChannelProgram {

	public int channelid = -1;
	public String channelname = " ";
	public String title = " ";
	public String content = " ";
	public String program_time = " ";

	@Override
	public String toString() {
		return "ChannelProgram [channelid=" + channelid + ", channelname=" + channelname + ", title=" + title
				+ ", content=" + content + ", program_time=" + program_time + "]";
	}

}
