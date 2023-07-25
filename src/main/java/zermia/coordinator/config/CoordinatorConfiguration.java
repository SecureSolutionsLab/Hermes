package zermia.coordinator.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

public class CoordinatorConfiguration {
	public static final Path defaultPath = FileSystems.getDefault().getPath("").toAbsolutePath();
	public static String defaultPropertiesFilePath = defaultPath + "/coordinator.config";
	private static final Properties props = new Properties();
	public CoordinatorConfiguration() {
		this.loadProperties(defaultPropertiesFilePath);
	}
	//Simple property file loader
	public void loadProperties(String propPath) {
		System.out.println("[ZermiaProperties] loadProperties ("+ propPath+")");
		try {
			props.load(new FileInputStream(propPath));
		} catch (FileNotFoundException e) {
			System.out.println("Property File not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error Reading Properties File");
			e.printStackTrace();
		}
	}
	
	private String spaceRemoval(String st) {
		return st.replaceAll("\\s+","");
	}
	
//-------------------------------------------------------------------------//	
	// Get Zermia Server Uptime	
	public  int getServerUptime() {
		return Integer.parseInt(spaceRemoval(props.getProperty("zermia.serverUptime")));
	}		

//-------------------------------------------------------------------------//

	public int getN() {
		return Integer.parseInt(spaceRemoval(props.getProperty("zermia.N")));
	}

	public int getF() {
		return Integer.parseInt(spaceRemoval(props.getProperty("zermia.f")));
	}

	// Get number of replicas
	public int getNumberOfMonitors() {
		return Integer.parseInt(spaceRemoval(props.getProperty("zermia.numberOfMonitors")));
	}	
	
	// Get replicas ID 
	public ArrayList<String> getMonitorID() {
		int numReplicas = getNumberOfMonitors();
		ArrayList<String> replicasID = new ArrayList<String>();
		for(int i = 0; i < numReplicas; i++) {
			replicasID.add(spaceRemoval(props.getProperty("zermia.monitor.ID." + i)));
		}
		return replicasID;
	}

	//Get Coordinator IP address
	public String getCoordinatorIP() {
		return spaceRemoval(props.getProperty("zermia.coordinator.ip"));
	}

	//Get Coordinator Port
	public Integer getCoordinatorPort() {
		return Integer.parseInt(spaceRemoval(props.getProperty("zermia.coordinator.port")));
	}

//-------------------------------------------------------------------------//
	//viewchange
	public Boolean getVC() {
		return Boolean.valueOf(props.getProperty("zermia.message.ViewChange"));
	}
	//checkpoint
	public Boolean getCk() {
		return Boolean.valueOf(props.getProperty("zermia.message.ViewChange"));
	}
	//consensus?
	public Boolean getCs() {
		return Boolean.valueOf(props.getProperty("zermia.message.ConsensusAll"));
	}
	//forwarded messages to Primary
	public Boolean getFw() {
		return Boolean.valueOf(props.getProperty("zermia.message.Forwarded"));
	}
	
//-------------------------------------------------------------------------//	
	//focus attacks solely on primary
	public Boolean getFprimary() {
		return Boolean.valueOf(props.getProperty("zermia.message.focus.primary"));
	}
	
	public Boolean getCreply() {
		return Boolean.valueOf(props.getProperty("zermia.message.ClientReply"));
	}
	
//-------------------------------------------------------------------------//	
	//consensus type messages
	public Boolean getCsPrP() {
		return Boolean.valueOf(props.getProperty("zermia.message.PrePrepare"));
	}
	
	public Boolean getCsPr() {
		return Boolean.valueOf(props.getProperty("zermia.message.Prepare"));
	}
	
	public Boolean getCsCm() {
		return Boolean.valueOf(props.getProperty("zermia.message.Commit"));
	}

	//-------------------------------------------------------------------------//
	//consensus type messages
	public String getFaultyClientMessagesType(Integer replicaID) {
		return props.getProperty("Zermia.faulty.replica." + replicaID);
	}

	public Boolean getFocusPrimary(Integer replicaID) {
		return Boolean.valueOf(props.getProperty("Zermia.faulty.replica.focusPrimary." + replicaID));
	}
}
