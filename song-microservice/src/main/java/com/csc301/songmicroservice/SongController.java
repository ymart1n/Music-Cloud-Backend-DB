package com.csc301.songmicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {

	@Autowired
	private final SongDal songDal;

	private OkHttpClient client = new OkHttpClient();

	
	public SongController(SongDal songDal) {
		this.songDal = songDal;
	}

	
	@RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = songDal.findSongById(songId);

		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	
	@RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));
		
		DbQueryStatus dbQueryStatus = songDal.getSongTitleById(songId);

        response.put("message", dbQueryStatus.getMessage());
        response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

        return response;

	}

	
	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("DELETE %s", Utils.getUrl(request)));
		
		//Dependent on Profile
		DbQueryStatus dbQueryStatus = songDal.deleteSongById(songId);

        response.put("message", dbQueryStatus.getMessage());
        response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
        
        if (dbQueryStatus.getMessage() == null) {
        	HttpUrl.Builder deleteSongUrl = HttpUrl.parse("http://localhost:3002" 
					+ "/deleteAllSongsFromDb/" + songId).newBuilder();
        	String url = deleteSongUrl.build().toString();

//        	System.out.println(url);

        	RequestBody body = RequestBody.create(null, new byte[0]);
        	ObjectMapper mapper = new ObjectMapper();

			Request deleteSongRequest = new Request.Builder()
			.url(url)
			.method("DELETE", body)
			.build();
			
			Call call = client.newCall(deleteSongRequest);
			Response responseFromDeleteSong = null;
			
			String profileServiceBody = "{}";
			
			try {
				responseFromDeleteSong = call.execute();
				profileServiceBody = responseFromDeleteSong.body().string();
				response.put("data", mapper.readValue(profileServiceBody, Map.class));
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        return response;

	}

	
	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
	
		Song songToAdd;
	
		if (params.containsKey("songName") && params.containsKey("songArtistFullName") && params.containsKey("songAlbum")) {
		  songToAdd = new Song(params.get("songName"), params.get("songArtistFullName"), params.get("songAlbum"));
		} else {
		  songToAdd = new Song("", "", "");
		}
		
		DbQueryStatus dbQueryStatus = songDal.addSong(songToAdd);
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), (Object)dbQueryStatus.getData());
		return response;
	}

	
	@RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
			@RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus dbQueryStatus;

		//Profile Dependency
		if (shouldDecrement.equals("true")) {
		  dbQueryStatus = songDal.updateSongFavouritesCount(songId, true);
		} else if (shouldDecrement.equals("false")){
		  dbQueryStatus = songDal.updateSongFavouritesCount(songId, false);
		} else {
		  dbQueryStatus = new DbQueryStatus("Decrement not specified", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
        
        response.put("message", dbQueryStatus.getMessage());
        response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

        return response;
	}
}