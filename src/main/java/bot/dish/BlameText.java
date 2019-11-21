package bot.dish;

import java.io.IOException;

import bot.main.AnnouncerBot;
import bot.main.Main;

public class BlameText {
	public static String text[];

	public static void loadText() {
		try {
			text = Main.loadFile(AnnouncerBot.BLAME_FILE);
		} catch (IOException e) {
			e.printStackTrace();
			text = new String[1];
			text[0] = "";
		}
	}
}
