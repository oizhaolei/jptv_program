package service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipException;

import model.ChannelProgram;
import model.JsonChannel;
import model.JsonProgram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import setting.GlobalSetting;
import util.CommonUtil;
import util.ZipUtil;
import db.DBclass;

public class EpgService {
	private final static String md5Key = "a4b8c1x0y7z4";
	private static String zipFilePath = "/root/epgdata";
//	private static String zipFilePath = "D:/logs";
	private static String channelJsonFile = "EnumService";
	private static String programsJsonFile = "EnumServiceEvent";
	private final static String TOKYO = "tky_";
	private final static String OSAKA = "osk_";

	private static Map<String, JsonChannel> channelMap = new HashMap<String, JsonChannel>();
	private static Connection conn;

	private static PreparedStatement selectPS;
	private static PreparedStatement updatePS;
	private static PreparedStatement existsCheckPS;
	private static PreparedStatement getPrevProgramPS;
	private static PreparedStatement insertPS;
	private static PreparedStatement deletePS;

	private static void unzipFile(String filePre) throws ZipException, IOException {
        //加密后的字符串
		String key = GlobalSetting.DB_DATETIME_FORMATTER8.format(new Date()) + md5Key;
		String hash = CommonUtil.toMD5(key.getBytes("utf-8"));
		String zipFile = zipFilePath + File.separator + filePre + GlobalSetting.DB_DATETIME_FORMATTER8.format(new Date()) + "_" + hash + ".zip";
		ZipUtil.decompressMultiFiles(zipFile, zipFilePath + File.separator + filePre + GlobalSetting.DB_DATETIME_FORMATTER8.format(new Date()) + File.separator);
	}
	private static String scanJsonFile(String fullName) {
		Scanner scanner = null;
		StringBuilder buffer = new StringBuilder();
		File file = new File(fullName);
		
		try {
			scanner = new Scanner(file, "utf-8");
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return buffer.toString();
	}

	private static void parseChannelJsonFile(String filePre) {
		String buffer = scanJsonFile(zipFilePath + File.separator + filePre + GlobalSetting.DB_DATETIME_FORMATTER8.format(new Date()) + "/" + channelJsonFile);

		try {
			JSONArray channelsArray = new JSONObject(buffer).getJSONArray("Data");
			for (int i = 0; i < channelsArray.length(); i ++) {
				JSONObject channelObject = channelsArray.getJSONObject(i);
				JsonChannel channel = new JsonChannel(channelObject);
				if (GlobalSetting.isAvailiable(channel.service_name)) {
					channelMap.put(channel.key, channel);
					//System.out.println("{key:" + channel.key + ", name:" + channel.service_name + "}");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace(System.out);
		}
	}

	private static void parseProgramJsonFile(String filePre) {
		String buffer = scanJsonFile(zipFilePath + File.separator + filePre + GlobalSetting.DB_DATETIME_FORMATTER8.format(new Date()) + "/" + programsJsonFile);

		try {
			JSONArray channelprogramArray = new JSONObject(buffer).getJSONObject("Data").getJSONArray("Epg");
			for (int i = 0; i < channelprogramArray.length(); i ++) {
				JSONObject channelprogramObject = channelprogramArray.getJSONObject(i);
				String channnel_key = channelprogramObject.getString("Key");
				JSONArray programArray = channelprogramObject.getJSONArray("Value");
				for (int j = 0; j < programArray.length(); j ++) {
					JSONObject programObject = programArray.getJSONObject(j);
					JsonProgram program = new JsonProgram();
					program.channel_key = channnel_key;
					program.key = programObject.getString("Key");
					program.starttime = programObject.getLong("Start");
					program.endtime = programObject.getLong("End");
					try {
						JSONObject shortInfo = programObject.getJSONObject("Short");
						program.title = shortInfo.getString("EventName");
						program.content = shortInfo.getString("Text");
					} catch (Exception e1) {
						CommonUtil.print("program short is empty , {channal_key: %s, key: %s}", channnel_key, program.key);
					}
					if (program.isEmpty()) {
						continue;
					}
					// 存入数据库
					try {
						addProgramToDB(program);
					} catch (SQLException e) {
						e.printStackTrace(System.out);
					}
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace(System.out);
		}
	}

	private static void addProgramToDB(JsonProgram program) throws SQLException {
		JsonChannel channel = channelMap.get(program.channel_key);
		if (null == channel) {
			return;
		}
		String channelName = channel.service_name;
		List<ChannelProgram> cps = GlobalSetting.onSetChannelname(channelName);
		for (ChannelProgram cp : cps) {
			cp.program_time = GlobalSetting.DB_DATETIME_FORMATTER.format(new Date(program.starttime + 3600000));
			cp.title = program.title;
			cp.content = program.content;
			if (cp.channelid != -1) {
//				DBclass.addToDb(cp, getPrevProgramPS, insertPS, existsCheckPS);
				DBclass.addToDb(cp, getPrevProgramPS, insertPS, updatePS, selectPS);
			} else {
				CommonUtil.print("ignore:%s, %s, %s", cp.channelid, cp.title, cp.program_time);
			}
		}
	}

	private static void parseZipFile(String filePre) throws SQLException, NoSuchAlgorithmException, ZipException, IOException {
		unzipFile(filePre);
		try {
			conn = DBclass.getConn();
			selectPS = conn.prepareStatement(GlobalSetting.selectProgram);
			updatePS = conn.prepareStatement(GlobalSetting.updateProgram);
			existsCheckPS = conn.prepareStatement(GlobalSetting.existsCheck);
			getPrevProgramPS = conn.prepareStatement(GlobalSetting.getPrevProgram);
			insertPS = conn.prepareStatement(GlobalSetting.insert_epg);

			parseChannelJsonFile(filePre);

			parseProgramJsonFile(filePre);

			selectPS.close();
			updatePS.close();
			existsCheckPS.close();
			getPrevProgramPS.close();
			insertPS.close();

			// 删除旧数据
			deletePS = conn.prepareStatement(GlobalSetting.deleteOldProgram);
			deletePS.execute();
			deletePS.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
	}

	public static void main(String[] args) {
		String pre;
			try {
				pre = TOKYO;
				parseZipFile(pre);
			} catch (NoSuchAlgorithmException e) {
				CommonUtil.print("NoSuchAlgorithmException(TOKYO): " + e.toString());
			} catch (UnsupportedEncodingException e) {
				CommonUtil.print("UnsupportedEncodingException(TOKYO): " + e.toString());
			} catch (SQLException e) {
				CommonUtil.print("SQLException(TOKYO): " + e.toString());
			} catch (ZipException e) {
				CommonUtil.print("ZipException(TOKYO): " + e.toString());
			} catch (IOException e) {
				CommonUtil.print("IOException(TOKYO): " + e.toString());
			}
			try {
				pre = OSAKA;
				parseZipFile(pre);
			} catch (NoSuchAlgorithmException e) {
				CommonUtil.print("NoSuchAlgorithmException(OSAKA): " + e.toString());
			} catch (UnsupportedEncodingException e) {
				CommonUtil.print("UnsupportedEncodingException(OSAKA): " + e.toString());
			} catch (SQLException e) {
				CommonUtil.print("SQLException(OSAKA): " + e.toString());
			} catch (ZipException e) {
				CommonUtil.print("ZipException(OSAKA): " + e.toString());
			} catch (IOException e) {
				CommonUtil.print("IOException(OSAKA): " + e.toString());
			}
		
		CommonUtil.print("-- over --");
	}
}
