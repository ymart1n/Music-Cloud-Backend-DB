package com.csc301.profilemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {
		
		// Get necessary variables from params
		String userName = params.get(KEY_USER_NAME);
		String fullName = params.get(KEY_USER_FULLNAME);
		String password = params.get(KEY_USER_PASSWORD);
		
		// creating response
		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			// call the query in profile driver
			DbQueryStatus status = profileDriver.createUserProfile(userName, fullName, password);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
//		response.put("path", String.format("POST %s", Utils.getUrl(request)));

		return response;
	}

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			// call the query in profile driver
			DbQueryStatus status = profileDriver.followFriend(userName, friendUserName);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
//		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			// call the query in profile driver
			DbQueryStatus status = profileDriver.getAllSongFriendsLike(userName);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				// put userName: songs[] data we obtained in getAllSongFriends() into response
				response.put("data", status.getData());
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
//		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return response;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			// call the query in profile driver
			DbQueryStatus status = profileDriver.unfollowFriend(userName, friendUserName);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
//		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			// call the query in playlist driver
			DbQueryStatus status = playlistDriver.likeSong(userName, songId);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
//		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		return response;
	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			// call the query in playlist driver
			DbQueryStatus status = playlistDriver.unlikeSong(userName, songId);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
//		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		return response;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			// call the query in playlist driver
			DbQueryStatus status = playlistDriver.deleteSongFromDb(songId);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
//		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		return response;
	}
}