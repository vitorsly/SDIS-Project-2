package protocols;

import chordSetup.ManageChord;
import chordSetup.PeerI;
import database.Backup;
import main.Client;
import main.CreateMsg;

public class SendGetchunk implements Runnable {

	Backup request;
	int nChunk;
	ManageChord chord;

	public SendGetchunk(Backup backupRequest, int nChunk, ManageChord chord) {
		super();
		this.request = backupRequest;
		this.nChunk = nChunk;
		this.chord = chord;
	}

	@Override
	public void run() {
		PeerI owner = chord.getChunkOwner(request.getname());
		String getChunkMessage = CreateMsg.getGetChunk(chord.getPeerInfo().getId(), chord.getPeerInfo().getAddress(),chord.getPeerInfo().getPort(), this.request.getname(), this.nChunk);
		Client.sendMsg(owner.getAddress(), owner.getPort(), getChunkMessage, false);
	}

}
