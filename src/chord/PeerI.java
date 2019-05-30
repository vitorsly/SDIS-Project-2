package chord;

import java.net.InetAddress;
import java.net.UnknownHostException;
public class PeerI extends AbstractPeer {

	public PeerI(String id, InetAddress address, Integer port) {
		this.id = id;
		this.address = address;
		this.port = port;
	}

	public PeerI(String str) {
		str = str.trim();
		String[] attr = str.split("\r\n");

		attr = attr[1].split(" ");
		this.id = attr[0];

		try {
			this.address = InetAddress.getByName(attr[1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		this.port = Integer.valueOf(attr[2]);
		
	}

	public InetAddress getAddress() {
		return address;
	}

	@Override
	public String[] asArray() {
		return new String[]{id,address.getHostAddress(),port.toString()}; 
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean isNull() {
		return false;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof PeerI))return false;
	    PeerI otherPeer = (PeerI)other;
	    if (otherPeer.getId().equals(this.id)) {
	    	return true;
	    }
	    return false;
	}
	

}
