package zermia.coordinator.clients;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClientList {
	static ConcurrentHashMap<String, Client> clientList = new ConcurrentHashMap<String, Client>();
	
//----------------------------------------------------------------------------------//	
	
	public void addClient(String c_id, Client client) {
		clientList.put(c_id, client);
	}
	
	public Client getClient(String c_id) {
		System.out.println("[ClientList]getClient " + c_id);
		System.out.println("[ClientList]getClient " + clientList.size());
		System.out.println("[ClientList]getClient " + clientList.get(c_id));
		return clientList.get(c_id);
	}

//----------------------------------------------------------------------------------//


	public boolean checkClientExistence(String c_id) {
		return clientList.containsKey(c_id);
	}

//----------------------------------------------------------------------------------//

	public ArrayList<String> returnFaultyClientList(){
		ArrayList<String> tt = new ArrayList<String>();
		for(Client clt : clientList.values()) {
//			if(clt.getFaultness()) {
//				tt.add(String.valueOf(clt.getID()));
//			}
		}
		return tt;
	}

	public ArrayList<String> returnAllClientsList(){
		ArrayList<String> tt = new ArrayList<String>();
		for(Client clt : clientList.values()) {
			if(Integer.valueOf(clt.getID())<100) {
				tt.add(String.valueOf(clt.getID()));
			}
		}
		return tt;
	}

	public Integer getLenght() {
		return clientList.size();
	}


	public Set<String> getKeySet() {
		return clientList.keySet();
	}

	public Integer highestNumber() {
		List<String> l = new ArrayList<String>(clientList.keySet());

		List<Integer> lInt = convertStringListToIntList( l, Integer::parseInt);

		return Collections.max(lInt);
	}

	public static <T, U> List<U> convertStringListToIntList(List<T> listOfString, Function<T, U> function)
	{
		return listOfString.stream()
				.map(function)
				.collect(Collectors.toList());
	}


}
