package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		
		DbQueryStatus status = new DbQueryStatus("Creating Profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				
				Map<String, Object> params = new HashMap<>();
				params.put("userName", userName);
				params.put("fullName", fullName);
				params.put("password", password);
				
				trans.run("MERGE (nProfile:profile {userName: $userName, fullName: $fullName, password: $password})", params);
				
				trans.success();
				
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> params = new HashMap<>();
				params.put("plName", userName+"-favorites");
				
				trans.run("MERGE (nPlaylist:playlist {plName: $plName})", params);
				trans.success();
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> params = new HashMap<>();
				params.put("userName", userName);
				params.put("fullName", fullName);
				params.put("password", password);
				params.put("plName", userName+"-favorites");
				
				trans.run("MATCH (a:profile {userName: $userName, fullName: $fullName, password: $password}),"
						+ "(b:playlist {plName: $plName})\n" + "MERGE (a)-[c:created]->(b)" + "RETURN c", params);
				trans.success();
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			
			session.close();
		}
		
		return status;
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		
		DbQueryStatus status = new DbQueryStatus("Follow a friend", DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				
				Map<String, Object> params = new HashMap<>();
				params.put("userName", userName);
				params.put("frndUserName", frndUserName);
				
				trans.run("MATCH (a:profile {userName: $userName})," + "(b:profile {userName: $frndUserName})\n" 
						+ "MERGE (a)-[f:following]->(b)" + "RETURN f", params);
				
				trans.success();
				
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			
			session.close();
		}
		
		return status;
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		
		DbQueryStatus status = new DbQueryStatus("Unfollow a friend", DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				
				Map<String, Object> params = new HashMap<>();
				params.put("userName", userName);
				params.put("frndUserName", frndUserName);
				
				trans.run("MATCH (a:profile {userName: $userName})," + "(b:profile {userName: $frndUserName})\n" 
						+ "MATCH (a)-[f:following]->(b)" + "DELETE f", params);
				
				trans.success();
				
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			
			session.close();
		}
		
		return status;
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		return null;
	}
}
