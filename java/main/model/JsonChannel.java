package model;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonChannel {
	public String epg_cap_flag;
	public String key;
	public String network_name;
	public String onid;
	public String partial_flag;
	public String remocon_id;
	public String sid;
	public String search_flag;
	public String service_name;
	public String service_type;
	public String tsid;
	public JsonChannel(JSONObject channelObject) throws JSONException {
		this.epg_cap_flag = channelObject.getString("EpgCapFlag");
		this.key = channelObject.getString("Key");
		this.network_name = channelObject.getString("NetworkName");
		this.onid = channelObject.getString("ONID");
		this.partial_flag = channelObject.getString("PartialFlag");
		this.remocon_id = channelObject.getString("RemoconID");
		this.sid = channelObject.getString("SID");
		this.search_flag = channelObject.getString("SearchFlag");
		this.service_name = channelObject.getString("ServiceName");
		this.service_type = channelObject.getString("ServiceType");
		this.tsid = channelObject.getString("TSID");
	}
}
