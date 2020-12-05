package com.csc301.songmicroservice;

import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
	  
	    boolean badReq = (songToAdd.getSongAlbum() == "" || songToAdd.getSongName() == "" || songToAdd.getSongArtistFullName() == "");
	    String message = "";
	    DbQueryExecResult eResult;
	    
	    if (badReq) {
	      eResult = DbQueryExecResult.QUERY_ERROR_GENERIC;
	      message = "Missing Required Field(s)";
	    } else {
	      Document song = new Document("songName", songToAdd.getSongName())
	          .append("songArtistFullName", songToAdd.getSongArtistFullName())
	          .append("songAlbum", songToAdd.getSongAlbum())
	          .append("songAmountFavourites", songToAdd.getSongAmountFavourites());
	      eResult = DbQueryExecResult.QUERY_OK;
	      try {
	        db.getCollection("songs").insertOne(song);
	        ObjectId objectId = (ObjectId)song.get("_id");
	        songToAdd.setId(objectId);
	              
	      } catch (Exception e) {
	        e.printStackTrace();
	        eResult = DbQueryExecResult.QUERY_ERROR_GENERIC;
	        message = "Error on insertion of Document";
	      }
	    }
	    
	    DbQueryStatus status = new DbQueryStatus(null, eResult);
	    
	    
	    if (message.isEmpty()) {
	      status.setData(songToAdd);
	    } else {
	      status.setMessage(message);
	    }
	    
		return status;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
	    Query query = new Query();
	    query.addCriteria(Criteria.where("_id").is(songId));
	    List<Song> song;
		try {
		  song = db.find(query, Song.class, "songs");
		} catch (Exception e) {
		  return new DbQueryStatus("Error locating specified song", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		if (song.isEmpty()) {
		  return new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		
		DbQueryStatus status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
		status.setData(song.get(0));
		return status;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
	  Query query = new Query();
      query.addCriteria(Criteria.where("_id").is(songId));
      List<Song> song;
      try {
        song = db.find(query, Song.class, "songs");
      } catch (Exception e) {
        return new DbQueryStatus("Error locating specified song", DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
      
      if (song.isEmpty()) {
        return new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      }
      
      DbQueryStatus status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
      Song givenSong = song.get(0);
      status.setData(givenSong.getSongName());
      return status;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
	  Query query = new Query();
      query.addCriteria(Criteria.where("_id").is(songId));
      Song removed = null;
      try {
        removed = db.findAndRemove(query, Song.class, "songs");
      } catch (Exception e) {
        return new DbQueryStatus("Error locating specified song", DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
      
      if (removed == null) {
        return new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      }
      
      
      DbQueryStatus status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
      status.setData(removed);
      return status;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
	  Query query = new Query();
      query.addCriteria(Criteria.where("_id").is(songId));
      Update update = new Update();
      if (shouldDecrement) {
        update.inc("songAmountFavourites", -1);
      } else {
        update.inc("songAmountFavourites");
      }
      
      Song toUpdate = db.findById(songId, Song.class, "songs");
      
      if (toUpdate == null) {
        return new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      } else if (toUpdate.getSongAmountFavourites() == 0 && shouldDecrement) {
        return new DbQueryStatus("Song can not be unfavourited anymore", DbQueryExecResult.QUERY_ERROR_GENERIC);
      } 
      
      try {
          db.findAndModify(query, update, Song.class, "songs");
      } catch (Exception e) {
         return new DbQueryStatus("Error locating specified song", DbQueryExecResult.QUERY_ERROR_GENERIC);
      }


      DbQueryStatus status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
      return status;
    
	}
}