package database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import chordSetup.Peer;
import main.Leases;
import util.Loggs;
import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;



public class DatabaseManager {
	
	public static void storeFile(Connection c, Stored file) {
		String peer = file.getpeer();
		Integer repdeg = file.getrepdegree();
		try {
			PreparedStatement s = c.prepareStatement("INSERT INTO FILESSTORED (fileid, responsible, requestingpeer, desiredrepdeg) VALUES (?,?,?,?)");
			s.setString(1, file.getfile());
			s.setBoolean(2, file.getchunkstored());
			if (peer != null) {
				s.setString(3, peer);
			} else {
				s.setNull(3, Types.VARCHAR);
			}
			if (repdeg != null){
				s.setInt(4, repdeg);
			}else {
				s.setNull(4, Types.INTEGER);
			}
			s.executeUpdate();
			c.commit();
			System.out.println("file " + file.getfile() + " stored");
		} catch (DerbySQLIntegrityConstraintViolationException e) {
			Loggs.LOG.info("file already stored, updating file");
			updateFile(c, file);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void storePeer(Connection c, Peer peer) {
		try {
			PreparedStatement s = c.prepareStatement("INSERT INTO PEERS (peerid,ip,port) VALUES (?,?,?)");
			s.setString(1, peer.getId());
			s.setString(2, peer.getAddress().getHostAddress());
			s.setInt(3, peer.getPort());
			s.executeUpdate();
			c.commit();
			Loggs.logging("peer " + peer.getId() + " stored");
		} catch (DerbySQLIntegrityConstraintViolationException e) {
			Loggs.LOG.info("peer already stored, updating peer instead");
			updatePeer(c, peer);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void storeChunk(Connection c, Chunk chunkInfo) {
		try {
			PreparedStatement s = c.prepareStatement("INSERT INTO CHUNKSSTORED (chunkid, fileid, currentrepdeg, size) VALUES (?,?,?,?)");
			s.setInt(1, chunkInfo.getchunkid());
			s.setString(2, chunkInfo.getfileid());
			if (chunkInfo.getrepdegree() == null) {
				s.setNull(3, Types.INTEGER);
			}else {
				s.setInt(3, chunkInfo.getrepdegree());
			}
			s.setInt(4, chunkInfo.getsize());
			s.executeUpdate();
			c.commit();
			Loggs.logging("file " + chunkInfo.getfileid() + " - chunk " + chunkInfo.getchunkid() + " - stored");
		} catch (DerbySQLIntegrityConstraintViolationException e) {
			updateRepDeg(c,chunkInfo);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void setStoring(Connection c, String fileId, Boolean storing) {
		try {
			PreparedStatement s = c.prepareStatement("UPDATE FILESSTORED SET storing = ? WHERE fileid = ?");
			s.setBoolean(1, storing);
			s.setString(2, fileId);
			s.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void updateFile(Connection c, Stored file) {
		String peerRequesting = file.getpeer();
		Integer desiredRepDegree = file.getrepdegree();
		try {
			PreparedStatement s = c.prepareStatement("UPDATE FILESSTORED SET lasttimestored = CURRENT_TIMESTAMP, responsible = ?, requestingpeer = ?, desiredrepdeg = ? WHERE fileid = ?");
			s.setBoolean(1, file.getchunkstored());
			if (peerRequesting != null) {
				s.setString(2, peerRequesting);
			} else {
				s.setNull(2, Types.VARCHAR);
			}
			if (desiredRepDegree != null){
				s.setInt(3, desiredRepDegree);
			}else {
				s.setNull(3, Types.INTEGER);
			}
			s.setString(4, file.getfile());
			s.executeUpdate();
			c.commit();
			Loggs.logging("file " + file.getfile() + " updated");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	private static void updatePeer(Connection c, Peer peer) {
		try {
			PreparedStatement s = c.prepareStatement("UPDATE PEERS SET ip = ?, port = ? WHERE peerid = ?");
			s.setString(1, peer.getAddress().getHostAddress());
			s.setInt(2, peer.getPort());
			s.setString(3, peer.getId());
			s.executeUpdate();
			c.commit();
			Loggs.logging("peer " + peer.getId() + " updated");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void updateRepDeg(Connection c, Chunk chunkInfo) {
		try {
			PreparedStatement s = c.prepareStatement("UPDATE CHUNKSSTORED SET currentrepdeg = ? WHERE chunkid = ? AND fileid = ?");
			s.setInt(1, chunkInfo.getrepdegree());
			s.setInt(2, chunkInfo.getchunkid());
			s.setString(3, chunkInfo.getfileid());
			s.executeUpdate();
			c.commit();
			Loggs.logging("file " + chunkInfo.getfileid() + " - chunk " + chunkInfo.getchunkid() + " - repdeg updated");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	private static void updateBackup(Connection c, Backup request) {
		try {
			PreparedStatement s = c.prepareStatement("UPDATE BACKUPSREQUESTED SET desiredrepdeg = ? WHERE fileid = ?");
			s.setInt(1, request.getrepdegree());
			s.setString(2, request.getid());
			s.executeUpdate();
			c.commit();
			Loggs.logging("backup request " + request.getid() + " updated");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void updateResponsible(Connection c, String fileId, Boolean bool) {
		try {
			PreparedStatement s = c.prepareStatement("UPDATE FILESSTORED SET responsible = ? WHERE fileid = ?");
			s.setBoolean(1, bool);
			s.setString(2, fileId);
			s.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void updateFileTime(Connection c, String fileId) {
		try {
			PreparedStatement s = c.prepareStatement("UPDATE FILESSTORED SET lasttimestored = CURRENT_TIMESTAMP " + "WHERE fileid = ?");
			s.setString(1, fileId);
			s.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void requestBackup(Connection c, Backup backupRequest) {
		Boolean wasRequested = checkBackupRequested(c, backupRequest.getid());
		if (wasRequested) {
			updateBackup(c,backupRequest);
		}
		else {
			try {
				PreparedStatement s = c.prepareStatement("INSERT INTO BACKUPSREQUESTED (fileid, filename, desiredrepdeg,encrypt_key,chunksNum) VALUES (?,?,?,?,?)");
				s.setString(1, backupRequest.getid());
				s.setString(2, backupRequest.getname());
				s.setInt(3, backupRequest.getrepdegree());
				if (backupRequest.getkey() == null) {
					s.setNull(4, Types.VARCHAR);
				}else {
					s.setString(4, backupRequest.getkey());
				}
				s.setInt(5, backupRequest.getchunksnumber());
				s.executeUpdate();
				c.commit();
				Loggs.logging("file " + backupRequest.getname() + " backup request stored");
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	public static boolean checkResponsible(Connection c, String fileId) {
		PreparedStatement s = null;
		try {
			s = c.prepareStatement("SELECT * FROM FILESSTORED WHERE fileid = ?");
			s.setString(1, fileId);
			ResultSet result = s.executeQuery();
			c.setAutoCommit(false);
			c.commit();
			if (result.next()) {
				return result.getBoolean("responsible");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				s.close();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
		return false;
	}
	
	public static boolean checkChunkStored(Connection c, Chunk chunkInfo) {
		PreparedStatement s = null;
		try {	
			s = c.prepareStatement("SELECT * FROM CHUNKSSTORED WHERE fileid = ? AND chunkid = ?");
			s.setString(1, chunkInfo.getfileid());
			s.setInt(2, chunkInfo.getchunkid());
			ResultSet r = s.executeQuery();
			c.setAutoCommit(false);
			if (r.next()) {
				int size = r.getInt(5);
				return true && size >= 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
		return false;
	}
	
	private static boolean checkBackupRequested(Connection c, String fileId) {
		PreparedStatement s;
		try {
			s = c.prepareStatement("SELECT * FROM BACKUPSREQUESTED WHERE fileid = ?");
			s.setString(1, fileId);
			ResultSet result =  s.executeQuery();
			if (result.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return false;
	}
		
	public static boolean checkFile(Connection c, String fileId) {
		PreparedStatement s;
		try {
			s = c.prepareStatement("SELECT fileid, desiredrepdeg, storing FROM FILESSTORED WHERE fileid = ?");
			s.setString(1, fileId);
			ResultSet set =  s.executeQuery();
			return set.next() && set.getBoolean(3);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return false;
	}
	
	public static ArrayList<Backup> getFilesUpdate(Connection c) {
		ArrayList<Backup> files = new ArrayList<Backup>();
		try {
			Statement s = c.createStatement();
			ResultSet result = s.executeQuery("SELECT * FROM BACKUPSREQUESTED");
			while (result.next()) {
				files.add(new Backup(
						result.getString("fileid"),
						result.getString("filename"),
						result.getString("encrypt_key"),
						result.getInt("desiredrepdeg"),
						result.getInt("chunksNum")
						));
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return files;
	}
	
	public static ArrayList<Chunk> getFileChunks(Connection c, String fileId) {
		PreparedStatement s;
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		try {
			s = c.prepareStatement("SELECT chunkid, fileid, size FROM CHUNKSSTORED WHERE fileid = ?");
			s.setString(1, fileId);
			ResultSet result =  s.executeQuery();
			while (result.next()) {
				chunks.add(new Chunk(result.getInt(1), result.getString(2), result.getInt(3)));
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return chunks;
	}
	
	public static ArrayList<Stored> getFiles(Connection c) {
		ArrayList<Stored> files = new ArrayList<Stored>();
		try {
			Statement s = c.createStatement();
			ResultSet result = s.executeQuery("SELECT * FROM FILESSTORED WHERE responsible");
			while (result.next()) {
				files.add(new Stored(
						result.getString("fileid"), 
						true, result.getInt("desiredrepdeg")));
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return files;
	}
	
	public static ArrayList<String> getFilesDelete(Connection c, Timestamp t) {
		ArrayList<String> files = new ArrayList<String>();
		PreparedStatement s;
		try {
			s = c.prepareStatement("SELECT fileid FROM FILESSTORED WHERE { fn TIMESTAMPADD(SQL_TSI_SECOND,?,lasttimestored)} < ?");
			s.setInt(1, Leases.LEASE_TIME);
			s.setTimestamp(2, t);
			ResultSet set = s.executeQuery();
			while (set.next()) {
				files.add(set.getString(1));
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return null;
		}
		return files;
	}
	
	public static int getRepDegree(Connection c, String fileId) {
		PreparedStatement s;
		try {
			s = c.prepareStatement("SELECT fileid, desiredrepdeg, storing FROM FILESSTORED WHERE fileid = ?");
			s.setString(1, fileId);
			ResultSet set =  s.executeQuery();
			if (set.next()) {
				return set.getInt(2);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return 0;
	}
	
	public static int getMaxRepDegree(Connection c, String fileId) {
		PreparedStatement s;
		try {
			s = c.prepareStatement("SELECT max(currentrepdeg) FROM CHUNKSSTORED WHERE fileid = ?");
			s.setString(1, fileId);
			ResultSet result =  s.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return 0;
	}
	
	public static Backup getBackup(Connection c, String fileId) {
		PreparedStatement s;
		try {
			s = c.prepareStatement("SELECT * FROM BACKUPSREQUESTED WHERE fileid = ?");
			s.setString(1, fileId);
			ResultSet set =  s.executeQuery();
			if (set.next()) {
				return new Backup(set.getString("fileid"),
						set.getString("filename"),
						set.getString("encrypt_key"),
						set.getInt("desiredrepdeg"),
						set.getInt("chunksNum"));
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}
	
	public static ArrayList<Backup> getRequestedBackups(Connection c){
		ArrayList<Backup> a = new ArrayList<Backup>();
		try {
			Statement s = c.createStatement();
			ResultSet set = s.executeQuery("SELECT fileid, filename, desiredrepdeg, encrypt_key, chunksNum FROM BACKUPSREQUESTED");
			while (set.next()) {
				Backup currentBackupRequest = new Backup(set.getString(1),
						set.getString(2),
						set.getString("encrypt_key"),
						set.getInt(3), 
						set.getInt("chunksNum"));
				a.add(currentBackupRequest);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return a;
	}
	
	public static Peer getRequestingPeer(Connection c, String fileId) {
		try {
			PreparedStatement s = c.prepareStatement("SELECT peerid,ip,port FROM PEERS JOIN (SELECT requestingpeer FROM FILESSTORED WHERE fileid = ?) AS F ON PEERS.peerid = F.requestingpeer");
			s.setString(1, fileId);
			ResultSet set = s.executeQuery();
			if (set.next()) {
				InetAddress a = null;
				try {
					a = InetAddress.getByName(set.getString(2));
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return null;
				}
				return new Peer(set.getString(1),a,set.getInt(3));
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

}