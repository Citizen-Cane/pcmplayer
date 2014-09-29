package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Exec implements Visual {
	String fileName;
	
	public Exec(String fileName) {
		this.fileName = fileName;
	}


	@Override
	public void render(Player player) {
		player.showDesktopItem(fileName);
	}

	@Override
	public String toString() {
		return fileName; 
	}
}
