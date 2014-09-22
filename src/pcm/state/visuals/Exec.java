package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

public class Exec implements Visual {
	String fileName;
	
	public Exec(String fileName) {
		this.fileName = fileName;
	}


	@Override
	public void render(TeaseScript teaseScript) {
		teaseScript.showDesktopItem(fileName);
	}

	@Override
	public String toString() {
		return fileName; 
	}
}
