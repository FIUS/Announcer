package bot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import bot.dish.BlameText;

/**
 * 
 * Loading the credentials and starting the bot
 * 
 * @author schieljn
 *
 */
public class Main {
	
	/**
	 * 
	 * Initializes the bot and starts it
	 * 
	 * @param args Not used
	 * @throws IOException If the credential loading fails
	 */
	public static void main(String[] args) throws IOException {

		String[] credentials = loadFile(AnnouncerBot.CREDENTIALS);
		BlameText.loadText();
		ApiContextInitializer.init();
		TelegramBotsApi botsApi = new TelegramBotsApi();

		try {

			AnnouncerBot bot = new AnnouncerBot(credentials[0], credentials[1],Long.parseLong(credentials[2]));

			botsApi.registerBot(bot);

			System.out.println("Bot started");
			
		} catch (TelegramApiException e) {
			System.err.println("Unable to start telegram bot");

		}
	}

	/**
	 * 
	 * Loads a file line by line
	 * 
	 * @param filename The name of the file to read (including file ending)
	 * @return An array of Lines loaded from the file
	 * @throws IOException
	 */
	public static String[] loadFile(String filename) throws IOException {
		File file = new File(filename);
		String[] output = new String[3];
		try (FileReader fr = new FileReader(file); BufferedReader buffi = new BufferedReader(fr)) {
			for (int i = 0; i < 3; i++) {
				output[i] = buffi.readLine();
			}
		} catch (IOException e) {
			throw new IOException("Unable to load file");
		}
		if (output[0] == null || output[1] == null) {
			throw new IOException("Unable to load file");
		}
		return output;
	}
}
