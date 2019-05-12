package bot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
	public static void main(String[] args) throws IOException {

		String[] credentials = loadCredentials(AnnouncerBot.CREDENTIALS);

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

	public static String[] loadCredentials(String filename) throws IOException {
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
