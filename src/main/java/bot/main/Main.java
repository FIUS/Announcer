package bot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleLogger;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
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

    private static Logger log;
    private static final Semaphore finishSemaphore = new Semaphore(0);

    /**
     *
     * Initializes the bot and starts it
     *
     * @param args Not used
     * @throws IOException If the credential loading fails
     */
    public static void main(String[] args) throws IOException, TelegramApiException {
        Runtime.getRuntime().addShutdownHook(new Thread(finishSemaphore::release));

        if (Arrays.asList(args).contains("--verbose")) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
        }
        Main.log = LoggerFactory.getLogger(Main.class);

        String[] credentials = loadFile(AnnouncerBot.CREDENTIALS, 3);
        BlameText.loadText();

        Main.log.info("Starting bot");
        Main.log.debug("Starting with username {} and super admin {}", credentials[0], credentials[2]);

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {

            AnnouncerBot bot = new AnnouncerBot(credentials[0], credentials[1], Long.parseLong(credentials[2]));

            botsApplication.registerBot(bot.getBotToken(), bot);

            Main.log.info("Bot started");
            Main.finishSemaphore.acquire();
            Main.log.info("Bot exited regularly");

        } catch (Exception e) {
            Main.log.error("Bot exited with error", e);
            e.printStackTrace();
        }
    }

    /**
     *
     * Loads a file line by line
     *
     * @param filename The name of the file to read (including file ending)
     * @param count    The number of lines to read
     * @return An array of Lines loaded from the file
     * @throws IOException
     */
    public static String[] loadFile(String filename, int count) throws IOException {
        File file = new File(filename);
        String[] output = new String[3];
        try (FileReader fr = new FileReader(file); BufferedReader buffi = new BufferedReader(fr)) {
            for (int i = 0; i < 3; i++) {
                output[i] = buffi.readLine();
            }
        } catch (IOException e) {
            throw new IOException("Unable to load file: " + filename);
        }
        if (output[0] == null || output[1] == null) {
            throw new IOException("Unable to load file");
        }
        return output;
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

        ArrayList<String> input = new ArrayList<String>();

        try (FileReader fr = new FileReader(file); BufferedReader buffi = new BufferedReader(fr)) {
            String temp = buffi.readLine();

            while (temp != null) {
                input.add(temp);
                temp = buffi.readLine();
            }
        } catch (IOException e) {
            throw new IOException("Unable to load file " + filename);
        }
        Object[] output = input.toArray();

        return Arrays.copyOf(output, output.length, String[].class);
    }
}
