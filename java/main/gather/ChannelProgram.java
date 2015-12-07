package gather;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author zhaolei
 * 
 */
public class ChannelProgram {
	// [数据库里面的channelid, channelname, RSS的节目名称]关系映射表
	static String[][] channelMatrix = new String[][] { { "86", "01.ＮＨＫ総合", "ＮＨＫ総合・東京(Ch.1)" },
			{ "87", "02.ＮＨＫ教育", "ＮＨＫＥテレ１・東京(Ch.2)" }, { "88", "03.日本テレビ", "日テレ(Ch.4)" },
			{ "89", "04.テレビ朝日", "テレビ朝日(Ch.5)" }, { "90", "05.TBS", "ＴＢＳ(Ch.6)" }, { "91", "06.テレビ東京", "テレビ東京(Ch.7)" },
			{ "92", "07.フジテレビ", "フジテレビ(Ch.8)" }, { "93", "08.TOKYOMX", "ＴＯＫＹＯ ＭＸ１(Ch.9)" },
			{ "94", "09.BS1", "ＮＨＫ ＢＳ１(Ch.1)" }, { "95", "10.BSﾌﾟﾚﾐｱﾑ", "ＮＨＫ ＢＳプレミアム(Ch.3)" },
			{ "96", "11.BS日テレ", "ＢＳ日テレ(Ch.4)" }, { "97", "12.BS朝日", "ＢＳ朝日(Ch.5)" },
			{ "98", "13.BS-TBS", "ＢＳ-ＴＢＳ(Ch.6)" }, { "99", "14.BSフジ・181", "ＢＳフジ(Ch.8)" },
			{ "100", "15.BSジャパン", "ＢＳジャパン(Ch.7)" }, { "101", "16.WOWOWﾌﾟﾗｲﾑ", "ＷＯＷＯＷプライム(Ch.9)" },
			{ "102", "17.ｽﾀｰ・ﾁｬﾝﾈﾙ1", "スター・チャンネル1(Ch.10)" }, { "103", "18.ｺﾞﾙﾌﾈｯﾄﾜｰｸ", "ｺﾞﾙﾌﾈｯﾄﾜｰｸ" },
			{ "104", "19.ｷｯｽﾞｽﾃｰｼｮﾝ", "ｷｯｽﾞｽﾃｰｼｮﾝ" }, { "105", "20.NHK総合", "ＮＨＫ総合・大阪(Ch.1)" },
			{ "106", "21.NHK教育", "ＮＨＫＥテレ１・大阪(Ch.2)" }, { "107", "22.毎日放送", "ＭＢＳ毎日放送(Ch.4)" },
			{ "108", "23.朝日放送", "ＡＢＣテレビ(Ch.6)" }, { "109", "24.関西テレビ", "関西テレビ(Ch.8)" },
			{ "110", "25.読売テレビ", "よみうりテレビ(Ch.10)" }, { "111", "26.テレビ大阪", "テレビ大阪(Ch.7)" },
			{ "112", "27.サンテレビ", "サンテレビ(Ch.3)" }, { "119", "28.WOWOWﾗｲﾌﾞ", "ＷＯＷＯＷライブ" },
			{ "122", "29.WOWOWｼﾈﾏ", "ＷＯＷＯＷシネマ" }, { "133", "30.J Sports 1", "" }, { "135", "31.J Sports 2", "" },
			{ "136", "32.衛星劇場", "" }, { "137", "33.ディスカバリー", "" }, { "157", "34.NHK 総合 HD", "ＮＨＫ総合・東京(Ch.1)" },
			{ "158", "35.NHK Eテレ HD", "ＮＨＫＥテレ１・東京(Ch.2)" }, { "159", "36.日本テレビ HD", "日テレ(Ch.4)" },
			{ "160", "37.テレビ朝日 HD", "テレビ朝日(Ch.5)" }, { "161", "38.TBS HD", "ＴＢＳ(Ch.6)" },
			{ "162", "39.テレビ東京 HD", "テレビ東京(Ch.7)" }, { "163", "40.フジテレビ HD", "フジテレビ(Ch.8)" },
			{ "165", "48.NHK総合 HD", "ＮＨＫ総合・大阪(Ch.1)" }, { "166", "49.MBS毎日放送 HD", "ＭＢＳ毎日放送(Ch.4)" },
			{ "167", "50.ABC放送 HD", "ＡＢＣテレビ(Ch.6)" }, { "168", "51.関西テレビ HD", "関西テレビ(Ch.8)" },
			{ "169", "52.読売テレビ HD", "よみうりテレビ(Ch.10)" }, { "170", "53.テレビ大阪 HD", "テレビ大阪(Ch.7)" },
			{ "171", "41.BS1 HD", "ＮＨＫ ＢＳ１(Ch.1)" }, { "172", "42.BSﾌﾟﾚﾐｱﾑ HD", "ＮＨＫ ＢＳプレミアム(Ch.3)" },
			{ "173", "43.WOWOWﾌﾟﾗｲﾑ HD", "ＷＯＷＯＷプライム(Ch.9)" }, { "174", "44.WOWOWｼﾈﾏ HD", "ＷＯＷＯＷシネマ" },
			{ "175", "45.ｽﾀｰ・ﾁｬﾝﾈﾙ1 HD", "スター・チャンネル1(Ch.10)" }, { "176", "46.ｺﾞﾙﾌﾈｯﾄﾜｰｸ HD", "ｺﾞﾙﾌﾈｯﾄﾜｰｸ" },
			{ "177", "47.ｷｯｽﾞｽﾃｰｼｮﾝ HD", "ｷｯｽﾞｽﾃｰｼｮﾝ" } };

	int channelid = -1;
	String channelname = " ";
	String title = " ";
	String contents = " ";
	String program_time = " ";

	public static List<ChannelProgram> onSetChannelname(String text) {
		List<ChannelProgram> clones = new ArrayList<ChannelProgram>();
		for (int i = 0; i < channelMatrix.length; i++) {
			String[] chInfo = channelMatrix[i];
			if (chInfo[2].equals(text)) {
				int id = Integer.parseInt(chInfo[0]);
				ChannelProgram cp = new ChannelProgram();
				cp.channelid = id;
				cp.channelname = text;

				clones.add(cp);
			}
		}
		return clones;
	}

	@Override
	public String toString() {
		return "ChannelProgram [channelid=" + channelid + ", channelname=" + channelname + ", title=" + title
				+ ", contents=" + contents + ", program_time=" + program_time + "]";
	}

}
