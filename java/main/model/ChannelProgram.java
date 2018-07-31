package model;

public class ChannelProgram {

	public int channelid = -1;
	public String channelname = " ";
	public String title = " ";
	public String content = " ";
	public String program_time = " ";
	public String program_start_time = " ";
	public String program_end_time = " ";

	@Override
	public String toString() {
		return "ChannelProgram [channelid=" + channelid + ", channelname="
				+ channelname + ", title=" + title + ", content=" + content
				+ ", program_time=" + program_time + ", program_start_time="
				+ program_start_time + ", program_end_time=" + program_end_time
				+ "]";
	}

}
