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
import okhttp3.HttpUrl;
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
			e.printStackTrace();
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
				
				Map<String, ArrayList<String>> playlists = (Map<String, ArrayList<String>>) status.getData();

				for (Map.Entry<String, ArrayList<String>> playlist : playlists.entrySet()) {
					ArrayList<String> songIds = playlist.getValue();
					
					for (int i=0; i < songIds.size(); i++) {
						HttpUrl.Builder getSongTitleUrl = HttpUrl.parse("http://localhost:3001" 
								+ "/getSongTitleById/" + songIds.get(i)).newBuilder();
						String url = getSongTitleUrl.build().toString();
						
//						System.out.println(url);

						ObjectMapper mapper = new ObjectMapper();

						Request getSongTitleRequest = new Request.Builder()
														.url(url)
														.method("GET", null)
														.build();

						Call call = client.newCall(getSongTitleRequest);
						Response responseFromGetTitle = null;
						
						String songServiceBody = "{}";
						
						try {
							responseFromGetTitle = call.execute();
							songServiceBody = responseFromGetTitle.body().string();
							songIds.set(i, (String) mapper.readValue(songServiceBody, Map.class).get("data"));
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				
				}
				
				response.put("data", playlists);
				
				break;
			case QUERY_ERROR_NOT_FOUND:
				response.put("status", HttpStatus.NOT_FOUND);
				break;
			case QUERY_ERROR_GENERIC:
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				break;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
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
		
		HttpUrl.Builder getSongUrl = HttpUrl.parse("http://localhost:3001" 
				+ "/getSongById/" + songId).newBuilder();
		
		String getsongurl = getSongUrl.build().toString();
		
		ObjectMapper getSongMapper = new ObjectMapper();
		
		Request getSongRequest = new Request.Builder()
						.url(getsongurl)
						.method("GET", null)
						.build();
						
		Call getSongCall = client.newCall(getSongRequest);
		Response responseFromGetSong = null;
		
		String songServiceBodyForGetSong = "{}";
		
		try {
			responseFromGetSong = getSongCall.execute();
			songServiceBodyForGetSong = responseFromGetSong.body().string();
//			response.put("data", getSongMapper.readValue(songServiceBodyForGetSong, Map.class));
			
			if (getSongMapper.readValue(songServiceBodyForGetSong, Map.class).get("message") != null) {
//				response.put("message", "Song Does not Exist");
				response = Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_NOT_FOUND, null);
				return response;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		try {
			// call the query in playlist driver
			DbQueryStatus status = playlistDriver.likeSong(userName, songId);
			
			// switch between different cases based on db query execution result
			// and put corresponding status into response
			switch (status.getdbQueryExecResult()) {
			case QUERY_OK:
				response.put("status", HttpStatus.OK);
				if (!status.getMessage().equals("Song is already liked")) {
					HttpUrl.Builder updateSongFavUrl = HttpUrl.parse("http://localhost:3001" 
															+ "/updateSongFavouritesCount/" + songId).newBuilder();
					updateSongFavUrl.addQueryParameter("shouldDecrement", "false");
					String url = updateSongFavUrl.build().toString();
					
//					System.out.println(url);
					
					RequestBody body = RequestBody.create(null, new byte[0]);
					ObjectMapper mapper = new ObjectMapper();

					Request updateFavRequest = new Request.Builder()
							.url(url)
							.method("PUT", body)
							.build();
					
					Call call = client.newCall(updateFavRequest);
					Response responseFromUpdateFav = null;
					
					String songServiceBody = "{}";

					try {
						responseFromUpdateFav = call.execute();
						songServiceBody = responseFromUpdateFav.body().string();
//						response.put("data", mapper.readValue(songServiceBody, Map.class));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
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
				if (!status.getMessage().equals("Song has not been liked")) {
					HttpUrl.Builder updateSongFavUrl = HttpUrl.parse("http://localhost:3001" 
															+ "/updateSongFavouritesCount/" + songId).newBuilder();
					updateSongFavUrl.addQueryParameter("shouldDecrement", "true");
					String url = updateSongFavUrl.build().toString();
					
//					System.out.println(url);
					
					RequestBody body = RequestBody.create(null, new byte[0]);
					ObjectMapper mapper = new ObjectMapper();

					Request updateFavRequest = new Request.Builder()
							.url(url)
							.method("PUT", body)
							.build();
					
					Call call = client.newCall(updateFavRequest);
					Response responseFromUpdateFav = null;
					
					String songServiceBody = "{}";

					try {
						responseFromUpdateFav = call.execute();
						songServiceBody = responseFromUpdateFav.body().string();
//						response.put("data", mapper.readValue(songServiceBody, Map.class));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
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

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.DELETE)
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