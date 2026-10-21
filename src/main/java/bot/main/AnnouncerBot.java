package bot.main;

import java.io.File;
import java.lang.Exception;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import bot.user.User;
import bot.user.UserManager;

/**
 * 
 * This class handles the message receiving
 * 
 * @author schieljn
 *
 */
public class AnnouncerBot extends DefaultLongPollingUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnnouncerBot.class);
    private final UserManager userManager;
	private final MessageSender sender;
	private final BotDataProcessor processor;

	public static long SUPER_ADMIN = 0;

	public static boolean blockedSaving = false;

	public static final String BLAME_FILE = "./data/blame.txt";
	public static final String USER_FILE = "./data/users.bin";
	public static final String ADMIN_FILE = "./data/admins.bin";
	public static final String CREDENTIALS = "./data/credential.conf";
	public static final String CONFIG_FILE = "./data/config.conf";

	public static final String[] CONFIG;

	private final String botname;
	private final String token;

	static {
		CONFIG = loadConf();
	}

	private static String[] loadConf() {
		try {
			return Main.loadFile(CONFIG_FILE);
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "120" };
		}
	}

	public AnnouncerBot(String username, String token, long superAdmin) {
        this.botname = username;
        this.token = token;
		AnnouncerBot.SUPER_ADMIN = superAdmin;
		userManager = new UserManager();
		sender = new MessageSender(this);
		processor = new BotDataProcessor(this, userManager, sender);
		HashMap<Long, User> users = userManager.load(new File(USER_FILE));
		HashMap<Long, User> admins = userManager.load(new File(ADMIN_FILE));


		if (users != null) {
			userManager.setUsers(users);
		}
		if (admins != null) {
			userManager.setAdmins(admins);
		}

		if (blockedSaving) {

			processor.displaySaveConfirm();
		}

	}

	/**
	 * 
	 * Handles incoming messages
	 * 
	 */
    @Override
    public void consume(Update update) {
        log.debug("Received update: {}", update.getUpdateId());
		if (update.hasMessage()) {
			if (update.getMessage().hasText() && !update.getMessage().hasPhoto()) {
                String message = update.getMessage().getText();
                long chatID = update.getMessage().getChatId();
                log.debug("Chat {}, Message: {}", chatID, message);

				if (userManager.isAdmin(chatID)) {
					if (message.toLowerCase().startsWith("/chatid")) {
						processor.showChatID(update);
					}
					if (message.toLowerCase().startsWith("/add")) {
						processor.addInfo(update, message.toLowerCase().replaceAll("/add", ""));
					}
					if (message.toLowerCase().startsWith("/remove")) {
						processor.removeInfo(update, message.toLowerCase().replaceAll("/remove", ""));
					}

					if (message.toLowerCase().startsWith("/removeadmin")) {
						processor.displayRemoveAdmin(update, userManager.getAdmins());
					}
					if (message.startsWith("@")) {
						message = message.substring(1);

						String group = message.split("\n")[0];
						try {
							message = message.substring(group.length() + 1);

							HashSet<User> usersToSend = userManager
									.usersOnList(TelegramList.valueOf(group.toUpperCase()));

							for (User u : usersToSend) {
								sender.sendMessage(message, u.id);
							}
						} catch (IllegalArgumentException e) {
							processor.sendAvailableGroups(update, group);
						} catch (StringIndexOutOfBoundsException e) {
							sender.sendMessage("No text specified", update);
						}

					} else if (message.toLowerCase().startsWith("/groupcount")) {

						message = message.toLowerCase().replaceAll("/groupcount", "");
						message = message.trim();
						try {
							TelegramList list = TelegramList.valueOf(message.toUpperCase());
							int count = userManager.usersOnList(list).size();
							String outputMessage = "There ";
							if (count == 1) {
								outputMessage += "is 1";
								outputMessage += " user in group ";
							} else {
								outputMessage += "are ";
								outputMessage += count;
								outputMessage += " users in group ";
							}

							outputMessage += list;
							sender.sendMessage(outputMessage, update);
						} catch (IllegalArgumentException e) {
							processor.sendAvailableGroups(update, message);
						}
					}

				}

				if (message.toLowerCase().startsWith("/adminrequest")) {
					processor.processAdminRequest(update);
				}

				processor.reactOnSubChange(update, message, "sub");
				processor.reactOnSubChange(update, message, "unsub");
				processor.reactOnInfoRequest(update, message);
				processor.reactOnGroupInfoRequest(update, message);
				processor.reactOnCommandsRequest(update, message);
				processor.reactOnDish(update);
				processor.sendWelcomeMessage(update, message);

			} else if (update.getMessage().hasPhoto()) {

				if (userManager.isAdmin(update.getMessage().getChatId())) {
					processor.sendBroadcastPhoto(update);
				} else {
					sender.sendMessage("You do not have the rights to do that!", update);
				}

			}

		} else if (update.hasCallbackQuery()) {
            log.debug("Callback: {}", update.getCallbackQuery().getData());
			processor.buttonHandler(update);
		}

	}

	public String getBotUsername() {

		return botname;
	}

	public String getBotToken() {

		return token;
	}

}
