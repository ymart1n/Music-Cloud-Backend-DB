package com.csc301.profilemicroservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		
		// Creating a new DbQueryStatus
		DbQueryStatus status = new DbQueryStatus("Like a song", DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("userName", userName);
				
				// create a directed relation :follows from a profile node to the other profile node
				StatementResult userNames = trans.run("MATCH (a:profile {userName: $userName})\n" + "RETURN a" , params);
				
				trans.success();
				
				if (!userNames.hasNext()) {
					// set query result to ok if success
					status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					return status;
				}
				
			}
			
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("songId", songId);
				
				// create a new song node
				trans.run("MERGE (nSong:song { songId: $songId })", params);
				
				trans.success();
				
			}
			
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("plName", userName+"-favorites");
				params.put("songId", songId);
				
				// See if the relation exists
				StatementResult result = trans.run("MATCH (a:playlist { plName: $plName })-[i:includes]->(b:song { songId: $songId })\n" 
						+ "RETURN a", params);
				
				if (result.hasNext()) {
					return new DbQueryStatus("Song is already liked", DbQueryExecResult.QUERY_OK);
				} 
				
				trans.success();
				// set query result to ok if success
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
				
			}
			
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("plName", userName+"-favorites");
				params.put("songId", songId);
				
				// create a directed relation :created from a playlist node to the song node
				trans.run("MATCH (a:playlist { plName: $plName })," + "(b:song { songId: $songId })\n" 
						+ "MERGE (a)-[i:includes]->(b)" + "RETURN i", params);
				
				trans.success();
				// set query result to ok if success
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
				
			}
			
			session.close();
		}
		
		return status;
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		
		// Creating a new DbQueryStatus
		DbQueryStatus status = new DbQueryStatus("Unlike a song", DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("userName", userName);
				
				// create a directed relation :follows from a profile node to the other profile node
				StatementResult userNames = trans.run("MATCH (a:profile {userName: $userName})\n" + "RETURN a" , params);
				
				trans.success();
				
				if (!userNames.hasNext()) {
					// set query result to ok if success
					status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					return status;
				}
				
			}
			
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("plName", userName+"-favorites");
				params.put("songId", songId);
				
				// See if the relation exists
				StatementResult result = trans.run("MATCH (a:playlist { plName: $plName })-[i:includes]->(b:song { songId: $songId })\n" 
						+ "RETURN a", params);
				
				if (!result.hasNext()) {
					return new DbQueryStatus("Song has not been liked", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				} 
				
				trans.success();
				// set query result to ok if success
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
				
			}
			
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("plName", userName+"-favorites");
				params.put("songId", songId);
				
				// delete a directed relation :created from a playlist node to the song node
				trans.run("MATCH (a:playlist { plName: $plName })," + "(b:song { songId: $songId })\n" 
						+ "MATCH (a)-[i:includes]->(b)" + "DELETE i", params);
				
				trans.success();
				// set query result to ok if success
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			
			session.close();
		}
		
		return status;
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		
		// Creating a new DbQueryStatus
		DbQueryStatus status = new DbQueryStatus("Delete a song", DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				
				// initialize params
				Map<String, Object> params = new HashMap<>();
				params.put("songId", songId);
				
				// Delete all the relationships with this song node first
				trans.run("MATCH (a:song { songId: $songId })<-[i:includes]-(playlist)\n" + "DELETE i", params);
				// delete the song node from db
				trans.run("MATCH (a:song { songId: $songId })\n" + "DELETE a", params);
				
				trans.success();
				// set query result to ok if success
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			
			session.close();
		}
		
		return status;
	}
}
