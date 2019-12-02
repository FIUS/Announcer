package bot.dish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import bot.main.MessageSender;
import bot.main.TelegramList;
import bot.user.User;
import bot.user.UserManager;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Handles the callback of a specific dishwasher after the washer is finished
 *
 * @author schieljn
 */
public class DishTimerTask extends TimerTask {

    private static final List<String> DISHWASHERS = Arrays.asList("asterix",
            "obelix", "idefix", "miraculix", "tick", "trick", "track", "donald");

    private String dishwasher;
    private MessageSender sender;
    private UserManager users;
    private DishTimer timer;
    private String washerHTTPOut;
    private int washerNumber;

    private Set<Message> sentMessages = new HashSet<>();
    private int tryNumber = 0;

    public DishTimerTask(String dishwasher, MessageSender sender, UserManager users, String washerOut, int washerNumber,
                         DishTimer timer) {
        this.dishwasher = dishwasher.toLowerCase();
        this.sender = sender;
        this.users = users;
        this.washerHTTPOut = washerOut;
        this.timer = timer;
        this.washerNumber = washerNumber;
    }

    /**
     * Send to the dishwasher group the the washer is finished and also updates the
     * dishwasher display if the fius door is open
     */
    @Override
    public void run() {

        if (!sendDoorRequest()) {
            return;
        }

        timer.setWasherStateReady(washerNumber);
        DishTimer.sendDishWaserRequest("changeWasherState=" + washerHTTPOut);

        HashSet<User> dishUser = users.usersOnList(TelegramList.SIFF);

        StringBuilder messageToSend = new StringBuilder();

        messageToSend.append("Entleert den Geschirrreinigungsapparat _");
        messageToSend.append("\"");
        String temp = dishwasher.substring(0, 1).toUpperCase() + dishwasher.substring(1);
        messageToSend.append(temp);
        messageToSend.append("\"");
        messageToSend.append("_!");
        if(tryNumber > 1) {
            messageToSend.append("\n(Versuch Nr ");
            messageToSend.append(this.tryNumber);
            messageToSend.append(")");
        }
        this.tryNumber++;

        String alert = messageToSend.toString();
        String pic = generateDishwasherAsciiImage(this.dishwasher);

        // Delete previously sent messages
        for (Message m : this.sentMessages) {
			try {
				sender.deleteMessage(m.getChatId(), m.getMessageId());
			} catch (TelegramApiException e) {
				e.printStackTrace(System.out);
			}
		}
        this.sentMessages.clear();

        for (User u : dishUser) {
            this.sentMessages.add(sender.sendMessage(alert, u.id));
            this.sentMessages.add(sender.sendMessage(pic, u.id));
        }

    }

    /**
     * Generates a map of dishwasher locations in ASCII format, where at most
     * one location is marked with an 'X'.
     * Example:
     *   [ ] [X] [ ] [ ]
     *   [ ] [ ] [ ] [ ]
     * @param dishwasher name of the dishwasher to be marked
     * @return
     */
    private String generateDishwasherAsciiImage(String dishwasher) {
        final int dishwasherId = DISHWASHERS.indexOf(dishwasher.toLowerCase());
        final int lineLength = 4;
        String emptyLine = "[ ] ".repeat(lineLength).trim();
        String dishwasherLine = "[ ] ".repeat(dishwasherId % lineLength)
                + "[X]"
                + "[ ] ".repeat(lineLength - (dishwasherId % lineLength) - 1)
                .trim();

        if (dishwasherId == -1) {
            return "Dieser Geschirrreinigungsapparat wurde nicht vom Amt zugelassen";
        } else if (dishwasherId < 4) {
            return "```" + emptyLine + "\n" + dishwasherLine + "```";
        } else {
            return "```" + dishwasherLine + "\n" + emptyLine + "```";
        }
    }

    /**
     * Checks whether or not the door is open
     *
     * @return True is fius is open, else false
     */
    private boolean sendDoorRequest() {
        URL url;
        String out = "";
        try {

            String a = "https://fius.informatik.uni-stuttgart.de/isOpen.php";
            url = new URL(a);
            URLConnection conn = url.openConnection();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    out += inputLine;
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.equals("open");
    }

}
