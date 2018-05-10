package net.b07z.sepia.server.core.users;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.b07z.sepia.server.core.server.ConfigDefaults;
import net.b07z.sepia.server.core.tools.Connectors;
import net.b07z.sepia.server.core.tools.Converters;
import net.b07z.sepia.server.core.tools.JSON;

/**
 * Implementation of the Authentication_Interface using the assistant-API.
 *  
 * @author Florian Quirin
 *
 */
public class AuthenticationAssistAPI implements AuthenticationInterface{
	
	//Config
	private static final Logger log = LoggerFactory.getLogger(AuthenticationAssistAPI.class);

	//Stuff
	private String userid = "";
	private int errorCode = 0;
	private final HashMap<String, Object> basicInfo = new HashMap<>();
	private int accessLevel = 0;
	
	//authenticate user
	@Override
	public boolean authenticate(JSONObject info) {
		String userid = (String) info.get("userId");
		String password = (String) info.get("pwd");
		String client = (String) info.get("client");
		
		//check client - client has influence on the password token that is used
		if (client == null || client.isEmpty()){
			client = ConfigDefaults.defaultClientInfo;
		}
		if (password == null || password.trim().isEmpty()) {
			log.warn("Password null or empty for user '" + userid + "': '" + password + "'");
		}
		//make URL
		this.userid = userid;
		String url = ConfigDefaults.defaultAssistAPI + "authentication";
		//System.out.println("Auth. call: " + url); 			//debug
		//data body
		JSONObject body = JSON.make(
				"KEY", (userid + ";" + password),
				"action", "check",
				"client", client
		);
		String dataStr = body.toJSONString();
		//headers
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Content-Length", Integer.toString(dataStr.getBytes().length));
		//call server
		JSONObject response = Connectors.httpPOST(url, dataStr, headers);
		//System.out.println(response.toJSONString()); 			//debug
		
		//Status?
		if (!Connectors.httpSuccess(response)){
			log.warn("No success in auth response for user '" + userid + "', returning false: " + response);
			errorCode = 3; 			//connection error, wrong parameters?
			return false;
		}
		else{
			String result = (String) response.get("result");
			if (result.equals("fail")){
				log.warn("'fail' in auth response for user '" + userid + "', returning false: " + response);
				errorCode = 2;		//authentication failed
				return false;
			}
			//should be fine now - get basic info about user
			
			//TODO: add other IDs? (email, phone)
			
			//ACCESS LEVEL 
			//TODO: not yet fully implemented, but should be 0 for access via token, 1 for real password and -1 for no access.
			accessLevel = Converters.obj_2_int(response.get("access_level"));
			
			//NAME
			JSONObject user_name = (JSONObject) response.get("user_name");
			basicInfo.put("user_name", user_name);
			
			//LANGUAGE
			String language = (String) response.get("user_lang_code");
			basicInfo.put("user_lang_code", language);
			
			//ROLES
			Object roles_o = response.get("user_roles");
			if (roles_o != null){
				basicInfo.put("user_roles", roles_o);
			}
			
			//DONE - note: basicInfo CAN be null, so check for it if you use it.
			errorCode = 0; 			//all fine
			return true;
		}
	}
	
	//get errorCode set during authenticate
	@Override
	public int getErrorCode() {
		return errorCode;
	}
	
	//get basic info acquired during account check
	@Override
	public HashMap<String, Object> getBasicInfo() {
		return basicInfo;
	}
	
	//get user ID
	@Override
	public String getUserID() {
		return userid;
	}

	//get user access level
	@Override
	public int getAccessLevel() {
		return accessLevel;
	}
	
	//------------------------------------------------------------------
	//everything else should not be used inside other APIs than the assist API ... (yet?)

	@Override
	public void setRequestInfo(Object request) {
		// TODO Auto-generated method stub		
	}
	
	@Override
	public JSONObject registrationByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createUser(JSONObject info) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteUser(JSONObject info) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean logout(String userid, String client) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean logoutAllClients(String userid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JSONObject requestPasswordChange(JSONObject info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean changePassword(JSONObject info) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkDatabaseConnection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String userExists(String identifier, String type) throws RuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String writeKeyToken(String userid, String client) {
		// TODO Auto-generated method stub
		return null;
	}
}