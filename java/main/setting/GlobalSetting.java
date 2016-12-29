package setting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import model.ChannelProgram;

public class GlobalSetting {
	public static String getMaxProgram = "SELECT MAX(program_time) FROM tbl_channel_program WHERE channelid=? ";
	public static String getPrevProgram = "SELECT MAX(program_time) FROM tbl_channel_program WHERE channelid=? AND program_time<?";
	public static String insert_cron = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) VALUES (?, ?, ?, ?, 'cron', now(), 'cron', now())";
	public static String insert_sonet = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) VALUES (?, ?, ?, ?, 'sonet', now(), 'sonet', now())";
	public static String insert_golf = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) VALUES (?, ?, ?, ?, 'golf', now(), 'golf', now())";
	public static String insert_kids = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'kids', now(), 'kids', now())";
	public static String insert_epg = "INSERT INTO tbl_channel_program (channelid, title, contents, program_time, create_id, create_date, update_id, update_date) values (?, ?, ?, ?, 'epg', now(), 'epg', now())";
	public static String existsCheck = "SELECT COUNT(0) FROM tbl_channel_program WHERE channelid=? AND program_time=?";
	public static String delete = "DELETE FROM tbl_channel_program WHERE channelid=? AND program_time<? AND program_time>=?";
	public static String deleteOldProgram = "DELETE FROM tbl_channel_program WHERE DATEDIFF(now(), program_time) >8";
	public static String selectProgram = "SELECT title, contents FROM tbl_channel_program WHERE channelid=? AND program_time=?";
	public static String updateProgram = "UPDATE tbl_channel_program SET title=?, contents=? WHERE channelid=? AND program_time=?";
	
	public final static DateFormat DB_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER2 = new SimpleDateFormat("yyyy-MM-dd HH", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER3 = new SimpleDateFormat("HH:mm", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER4 = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER5 = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
	public final static DateFormat DB_DATETIME_FORMATTER6 = new SimpleDateFormat("yyyyMMdd a K:mm", Locale.ENGLISH);
	public final static DateFormat DB_DATETIME_FORMATTER7 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
	public final static DateFormat DB_DATETIME_FORMATTER8 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	public final static DateFormat DB_DATETIME_FORMATTER9 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

	// [数据库里面的channelid, channelname, RSS的节目名称]关系映射表
	public static String[][] channelMatrix = new String[][] {
		// 1.東京地上波HD
			{ "157", "01.NHK 総合 HD", "ＮＨＫ総合・東京(Ch.1)" }, // 201
			{ "157", "01.NHK 総合 HD", "ＮＨＫ総合・東京" },
			{ "157", "01.NHK 総合 HD", "ＮＨＫ総合１・東京" },
			{ "158", "02.NHK Eテレ HD", "ＮＨＫＥテレ１・東京(Ch.2)" }, // 202
			{ "158", "02.NHK Eテレ HD", "ＮＨＫＥテレ１・東京" },
			{ "158", "02.NHK Eテレ HD", "ＮＨＫＥテレ１東京" },
			{ "159", "03.日本テレビ HD", "日テレ(Ch.4)" }, // 203
			{ "159", "03.日本テレビ HD", "日テレ" },
			{ "159", "03.日本テレビ HD", "日テレ１" },
			{ "160", "04.テレビ朝日 HD", "テレビ朝日(Ch.5)" }, // 204
			{ "160", "04.テレビ朝日 HD", "テレビ朝日" },
			{ "161", "05.TBS HD", "ＴＢＳ(Ch.6)" }, // 205
			{ "161", "05.TBS HD", "ＴＢＳ" },
			{ "161", "05.TBS HD", "ＴＢＳ１" },
			{ "162", "06.テレビ東京 HD", "テレビ東京(Ch.7)" }, // 206
			{ "162", "06.テレビ東京 HD", "テレビ東京" },
			{ "162", "06.テレビ東京 HD", "テレビ東京１" },
			{ "163", "07.フジテレビ HD", "フジテレビ(Ch.8)" }, // 207
			{ "163", "07.フジテレビ HD", "フジテレビ" },
			{ "181", "08.TOKYOMX HD", "ＴＯＫＹＯ　ＭＸ１(Ch.9)" }, // 108
			{ "181", "08.TOKYOMX HD", "ＴＯＫＹＯ ＭＸ１" },
			{ "181", "08.TOKYOMX HD", "ＴＯＫＹＯ　ＭＸ１" },
		// 2.東京地上波
			{ "86", "09.NHK 総合", "ＮＨＫ総合・東京(Ch.1)" }, // 101
			{ "86", "09.NHK 総合", "ＮＨＫ総合・東京" },
			{ "86", "09.NHK 総合", "ＮＨＫ総合１・東京" },
			{ "87", "10.NHK Eテレ", "ＮＨＫＥテレ１・東京(Ch.2)" }, // 102
			{ "87", "10.NHK Eテレ", "ＮＨＫＥテレ１・東京" },
			{ "87", "10.NHK Eテレ", "ＮＨＫＥテレ１東京" },
			{ "88", "11.日本テレビ", "日テレ(Ch.4)" }, // 103
			{ "88", "11.日本テレビ", "日テレ" },
			{ "88", "11.日本テレビ", "日テレ１" },
			{ "89", "12.テレビ朝日", "テレビ朝日(Ch.5)" }, // 104
			{ "89", "12.テレビ朝日", "テレビ朝日" },
			{ "90", "13.TBS", "ＴＢＳ(Ch.6)" }, // 105
			{ "90", "13.TBS", "ＴＢＳ" },
			{ "90", "13.TBS", "ＴＢＳ１" },
			{ "91", "14.テレビ東京", "テレビ東京(Ch.7)" }, // 106
			{ "91", "14.テレビ東京", "テレビ東京" },
			{ "91", "14.テレビ東京", "テレビ東京１" },
			{ "92", "15.フジテレビ", "フジテレビ(Ch.8)" }, // 107
			{ "92", "15.フジテレビ", "フジテレビ" },
		// 3.大阪地上波
			{ "105", "16.NHK 総合", "ＮＨＫ総合・大阪(Ch.1)" }, // 121
			{ "105", "16.NHK 総合", "ＮＨＫ総合・大阪" },
			{ "105", "16.NHK 総合", "ＮＨＫ総合１・大阪" },
			{ "106", "17.NHK Eテレ", "ＮＨＫＥテレ１・大阪(Ch.2)" }, // 122
			{ "106", "17.NHK Eテレ", "ＮＨＫＥテレ１・大阪" },
			{ "106", "17.NHK Eテレ", "ＮＨＫＥテレ１大阪" },
			{ "107", "18.MBS 毎日放送", "ＭＢＳ毎日放送(Ch.4)" }, // 123
			{ "107", "18.MBS 毎日放送", "ＭＢＳ毎日放送" },
			{ "108", "19.ABC放送", "ＡＢＣテレビ(Ch.6)" }, // 124
			{ "108", "19.ABC放送", "ＡＢＣテレビ" },
			{ "108", "19.ABC放送", "ＡＢＣテレビ１" },
			{ "109", "20.関西テレビ", "関西テレビ(Ch.8)" }, // 125
			{ "109", "20.関西テレビ", "関西テレビ" },
			{ "109", "20.関西テレビ", "関西テレビ１" },
			{ "110", "21.読売テレビ", "よみうりテレビ(Ch.10)" }, // 126
			{ "110", "21.読売テレビ", "よみうりテレビ" },
			{ "110", "21.読売テレビ", "読売テレビ１" },
			{ "111", "22.テレビ大阪", "テレビ大阪(Ch.7)" }, // 127
			{ "111", "22.テレビ大阪", "テレビ大阪" },
			{ "111", "22.テレビ大阪", "テレビ大阪１" },
			{ "112", "23.サンテレビ", "サンテレビ(Ch.3)" }, // 128
			{ "112", "23.サンテレビ", "サンテレビ" },
			{ "112", "23.サンテレビ", "サンテレビ１" },
		// 4.BS衛星放送
			{ "94", "24.BS1", "ＮＨＫ ＢＳ１(Ch.1)" }, // 109
			{ "94", "24.BS1", "ＮＨＫ ＢＳ１" },
			{ "94", "24.BS1", "ＮＨＫＢＳ１" },
			{ "95", "25.BSﾌﾟﾚﾐｱﾑ", "ＮＨＫ ＢＳプレミアム(Ch.3)" }, // 111
			{ "95", "25.BSﾌﾟﾚﾐｱﾑ", "ＮＨＫ ＢＳプレミアム" },
			{ "95", "25.BSﾌﾟﾚﾐｱﾑ", "ＮＨＫＢＳプレミアム" },
			{ "96", "26.BS日テレ", "ＢＳ日テレ(Ch.4)" }, // 112
			{ "96", "26.BS日テレ", "ＢＳ日テレ" },
			{ "97", "27.BS朝日", "ＢＳ朝日(Ch.5)" }, // 113
			{ "97", "27.BS朝日", "ＢＳ朝日" },
			{ "97", "27.BS朝日", "ＢＳ朝日１" },
			{ "98", "28.BS-TBS", "ＢＳ-ＴＢＳ(Ch.6)" }, // 114
			{ "98", "28.BS-TBS", "ＢＳ-ＴＢＳ" },
			{ "98", "28.BS-TBS", "ＢＳ－ＴＢＳ" },
			{ "99", "29.BSフジ・181", "ＢＳフジ(Ch.8)" }, // 115
			{ "99", "29.BSフジ・181", "ＢＳフジ" },
			{ "99", "29.BSフジ・181", "ＢＳフジ・１８１" },
			{ "100", "30.BSジャパン", "ＢＳジャパン(Ch.7)" }, // 116
			{ "100", "30.BSジャパン", "ＢＳジャパン" },
			{ "180", "31.BS12トゥエルビ", "ＢＳ１２トゥエルビ" }, // 217
			{ "185", "32.BSスカパー！", "ＢＳスカパー！" }, // 219
		// 5.映画ドラマ
			{ "119", "33.WOWOWﾗｲﾌﾞ", "ＷＯＷＯＷライブ" }, // 110
			{ "101", "34.WOWOWﾌﾟﾗｲﾑ", "ＷＯＷＯＷプライム(Ch.9)" }, // 117
			{ "101", "34.WOWOWﾌﾟﾗｲﾑ", "ＷＯＷＯＷプライム" },
			{ "122", "35.WOWOWｼﾈﾏ", "ＷＯＷＯＷシネマ" }, // 130
			{ "102", "36.ｽﾀｰ・ﾁｬﾝﾈﾙ1", "スター・チャンネル1(Ch.10)" }, // 118
			{ "102", "36.ｽﾀｰ・ﾁｬﾝﾈﾙ1", "スター・チャンネル1" },
			{ "102", "36.ｽﾀｰ・ﾁｬﾝﾈﾙ1", "スターチャンネル１" },
			{ "183", "37.ｽﾀｰ・ﾁｬﾝﾈﾙ2", "スターチャンネル２" }, // 215
			{ "179", "38.ｽﾀｰ・ﾁｬﾝﾈﾙ3", "スターチャンネル３" }, // 216
			{ "167", "39.日本映画専門ch", "ＢＳ日本映画専門ｃｈ" }, // 210
			{ "168", "40.時代劇専門ch", "時代劇専門ｃｈＨＤ" }, // 211
			{ "182", "41.ムービープラス", "ムービープラスＨＤ" }, // 214
			{ "136", "42.衛星劇場", "衛星劇場" }, // 132
		// 6.BS/CS衛星放送
			{ "104", "43.ｷｯｽﾞｽﾃｰｼｮﾝ", "ｷｯｽﾞｽﾃｰｼｮﾝ" }, // 120
			{ "104", "43.ｷｯｽﾞｽﾃｰｼｮﾝ", "キッズステーション" },
			{ "166", "44.ディズニーチャンネル", "ディズニーチャンネル" }, // 209
			{ "165", "45.BSアニマクス", "ＢＳアニマックス}" }, // 208
			{ "103", "46.ｺﾞﾙﾌﾈｯﾄﾜｰｸ", "ｺﾞﾙﾌﾈｯﾄﾜｰｸ" }, // 119
			{ "103", "46.ｺﾞﾙﾌﾈｯﾄﾜｰｸ", "ゴルフネットＨＤ" },
			{ "133", "47.J Sports 1", "J SPORTS 1" }, // 129
			{ "133", "47.J Sports 1", "Ｊ　ＳＰＯＲＴＳ　１" },
			{ "135", "48.J Sports 2", "J SPORTS 2" }, // 131
			{ "135", "48.J Sports 2", "Ｊ　ＳＰＯＲＴＳ　２" },
			{ "169", "49.J Sports 3", "Ｊ　ＳＰＯＲＴＳ　３" }, // 212
			{ "170", "50.J Sports 4", "Ｊ　ＳＰＯＲＴＳ　４" }, // 213
			{ "137", "51.Discovery", "ディスカバリー" }, // 133
			{ "182", "52.ｸﾞﾘｰﾝﾁｬﾝﾈﾙ", "グリーンチャンネル" }  // 218
		};
/*
 *
 		// 大阪地上波HD
			//{ "165", "48.NHK総合 HD", "ＮＨＫ総合・大阪(Ch.1)" },
			//{ "165", "48.NHK総合 HD", "ＮＨＫ総合・大阪" },
			//{ "165", "48.NHK総合 HD", "ＮＨＫ総合１・大阪" },
			//{ "166", "49.MBS毎日放送 HD", "ＭＢＳ毎日放送(Ch.4)" },
			//{ "166", "49.MBS毎日放送 HD", "ＭＢＳ毎日放送" },
			//{ "167", "50.ABC放送 HD", "ＡＢＣテレビ(Ch.6)" },
			//{ "167", "50.ABC放送 HD", "ＡＢＣテレビ" },
			//{ "167", "50.ABC放送 HD", "ＡＢＣテレビ１" },
			//{ "168", "51.関西テレビ HD", "関西テレビ(Ch.8)" },
			//{ "168", "51.関西テレビ HD", "関西テレビ" },
			//{ "168", "51.関西テレビ HD", "関西テレビ１" },
			//{ "169", "52.読売テレビ HD", "よみうりテレビ(Ch.10)" },
			//{ "169", "52.読売テレビ HD", "よみうりテレビ" },
			//{ "169", "52.読売テレビ HD", "読売テレビ１" },
			//{ "170", "53.テレビ大阪 HD", "テレビ大阪(Ch.7)" },
			//{ "170", "53.テレビ大阪 HD", "テレビ大阪" },
			//{ "170", "53.テレビ大阪 HD", "テレビ大阪１" },
		// BS衛星放送
			//{ "171", "41.BS1 HD", "ＮＨＫ ＢＳ１(Ch.1)" },
			//{ "171", "41.BS1 HD", "ＮＨＫ ＢＳ１" },
			//{ "171", "41.BS1 HD", "ＮＨＫＢＳ１" },
			//{ "172", "42.BSﾌﾟﾚﾐｱﾑ HD", "ＮＨＫ ＢＳプレミアム(Ch.3)" },
			//{ "172", "42.BSﾌﾟﾚﾐｱﾑ HD", "ＮＨＫ ＢＳプレミアム" },
			//{ "172", "42.BSﾌﾟﾚﾐｱﾑ HD", "ＮＨＫＢＳプレミアム" },
			//{ "173", "43.WOWOWﾌﾟﾗｲﾑ HD", "ＷＯＷＯＷプライム(Ch.9)" },
			//{ "173", "43.WOWOWﾌﾟﾗｲﾑ HD", "ＷＯＷＯＷプライム" },
			//{ "174", "44.WOWOWｼﾈﾏ HD", "ＷＯＷＯＷシネマ" },
			//{ "175", "45.ｽﾀｰ・ﾁｬﾝﾈﾙ1 HD", "スター・チャンネル1(Ch.10)" },
			//{ "175", "45.ｽﾀｰ・ﾁｬﾝﾈﾙ1 HD", "スター・チャンネル1" },
			//{ "175", "45.ｽﾀｰ・ﾁｬﾝﾈﾙ1 HD", "スターチャンネル１" },
		// CS衛星放送	
			//{ "176", "46.ｺﾞﾙﾌﾈｯﾄﾜｰｸ HD", "ｺﾞﾙﾌﾈｯﾄﾜｰｸ" },
			//{ "176", "46.ｺﾞﾙﾌﾈｯﾄﾜｰｸ HD", "ゴルフネットＨＤ" },
			//{ "177", "47.ｷｯｽﾞｽﾃｰｼｮﾝ HD", "ｷｯｽﾞｽﾃｰｼｮﾝ" },
			//{ "177", "47.ｷｯｽﾞｽﾃｰｼｮﾝ HD", "キッズステーション" }
		// 東京地上波
			//{ "93", "08.TOKYOMX", "ＴＯＫＹＯ　ＭＸ１(Ch.9)" },
			//{ "93", "08.TOKYOMX", "ＴＯＫＹＯ ＭＸ１" },
			//{ "93", "08.TOKYOMX", "ＴＯＫＹＯ　ＭＸ１" },
 */
	public static String[] allChannelNames = {
		"ＮＨＫ総合１・東京",
		"ＮＨＫＥテレ１東京",
		"日テレ１",
		"テレビ朝日",
		"ＴＢＳ１",
		"テレビ東京１",
		"フジテレビ",
		"ＴＯＫＹＯ　ＭＸ１",
		"ＮＨＫ総合１・大阪",
		"ＮＨＫＥテレ１大阪",
		"ＭＢＳ毎日放送",
		"ＡＢＣテレビ１",
		"関西テレビ１",
		"読売テレビ１",
		"テレビ大阪１",
		"サンテレビ１",
		"ＮＨＫＢＳ１",
		"ＮＨＫＢＳプレミアム",
		"ＢＳ日テレ",
		"ＢＳ朝日１",
		"ＢＳ－ＴＢＳ",
		"ＢＳフジ・１８１",
		"ＢＳジャパン",
		"ＢＳ１２トゥエルビ",
		"ＢＳスカパー！",
		"ＢＳアニマックス",
		"ＷＯＷＯＷプライム",
		"ＷＯＷＯＷシネマ",
		"ＷＯＷＯＷライブ",
		"スターチャンネル１",
		"スターチャンネル２",
		"スターチャンネル３",
		"ＢＳ日本映画専門ｃｈ",
		"ディズニーチャンネル",
		"Ｊ　ＳＰＯＲＴＳ　１",
		"Ｊ　ＳＰＯＲＴＳ　２",
		"Ｊ　ＳＰＯＲＴＳ　３",
		"Ｊ　ＳＰＯＲＴＳ　４",
		"ゴルフネットＨＤ",
		"キッズステーション",
		"時代劇専門ｃｈＨＤ",
		"衛星劇場",
		"ムービープラスＨＤ",
		"グリーンチャンネル",
		"ディスカバリー"
	};

	//同一个节目单，对应多个节目（SD／HD）
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

	public static boolean isAvailiable(String channel_name) {
		for (String name : allChannelNames) {
			if (name.equals(channel_name)) {
				return true;
			}
		}
		return false;
	}
}
