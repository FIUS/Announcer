/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot.dish;

import bot.main.MessageSender;
import bot.main.TelegramList;
import bot.user.User;
import bot.user.UserManager;
import java.util.HashSet;
import java.util.Timer;

/**
 *
 * @author Justin
 */
public class DishTimer {

    private Timer timer;
    private boolean isActive;
    private final MessageSender sender;
    private final UserManager users;

    private static final int WASHING_TIME = 120;
    private static final int REMEMBER_TIME = 60;

    public DishTimer(MessageSender sender, UserManager users) {
        timer = new Timer("Dishwasher", true);
        isActive = false;

        this.sender = sender;
        this.users = users;
    }

    public void toggleTimer(String dishwasher) {
        if (isActive) {
            timer.cancel();
            timer.purge();
            HashSet<User> dishUser = users.usersOnList(TelegramList.SIFF);
            for (User u : dishUser) {
                sender.sendMessage("Geschirrreinigungsapparat wurde geleert", u.id);
            }
        } else {
            HashSet<User> dishUser = users.usersOnList(TelegramList.SIFF);
            for (User u : dishUser) {
                sender.sendMessage("Geschirrreinigungsapparat wurde beladen", u.id);
            }
            timer = new Timer("Dishwasher", true);
            timer.schedule(new DishTimerTask(dishwasher, sender, users), 1000 * 60 * WASHING_TIME, 1000 * 60 * REMEMBER_TIME);
        }
        isActive = !isActive;
    }

}
