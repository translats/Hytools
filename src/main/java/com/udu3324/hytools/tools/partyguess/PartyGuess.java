package com.udu3324.hytools.tools.partyguess;

import com.udu3324.hytools.Config;
import com.udu3324.hytools.Hytools;
import com.udu3324.hytools.hyapi.RankOfUUID;
import com.udu3324.hytools.mcapi.UUID;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PartyGuess {

    // bool is used for guessMessageParty to do something once
    // when adding users to array
    public static boolean doOnceBeforeTimeReset = true;

    // this array list contains the users in the time period
    public static ArrayList<String> tempUserArray = new ArrayList<String>();

    // time delay when the code resets
    public static int delay = 10; // ms

    public static void guessMessageParty(String message, Boolean hytillities) {
        if (!Config.getPartyGuess()) {
            // this is to disable party guess if it's disabled in config
            return;
        }

        String username;
        //set username depending on if user is using hytilities
        if (hytillities) {
            username = message.substring(message.indexOf(" ", 3) + 1, message.length());
        } else {
            if (message.indexOf(" ") != -1)
                username = message.substring(0, message.indexOf(" "));
            else
                username = message;
        }

        //run once and start timer 
        if (doOnceBeforeTimeReset) {
            tempUserArray.clear();

            // schedule code
            TimerTask task = new TimerTask() {
                public void run() {
                    // after delay, allow array to clear again and send chat msg (if there's more
                    // than one user stored)
                    if (tempUserArray.size() > 1) {
                        // for each user in array, add it to the checks
                        for (int i = 0; i != tempUserArray.size(); i++) {
                            String ign = tempUserArray.get(i);
                            FriendCheck.store(ign);
                            GuildCheck.store(ign);
                        }

                        // for each user, set the color to their rank
                        try {
                            for (int i = 0; i != tempUserArray.size(); i++) {
                                String uuid = UUID.get(tempUserArray.get(i));
                                tempUserArray.set(i, RankOfUUID.get(uuid) + tempUserArray.get(i));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //create a string of the array and remove []
                        String raw = tempUserArray.toString().replace("[", "").replace("]", "");

                        // user and user string if theres only 2 people
                        if (tempUserArray.size() == 2) {
                            raw = raw.replace(",", "\u00A73 and");
                        } else if (tempUserArray.size() >= 3) {
                            // user, user and user string if theres more than 3 people

                            // get the pos/index of the last comma in msg
                            int lastComma = raw.lastIndexOf(",");

                            // delete comma and replace with and
                            StringBuilder newString = new StringBuilder(raw);
                            newString.deleteCharAt(lastComma);
                            newString.replace(lastComma, lastComma, "\u00A73 and");
                            raw = newString.toString();
                        }

                        Hytools.sendMessage("\u00A73" + raw + "\u00A73 are most likely in a party.");

                        // friend check
                        String friendCheck = FriendCheck.reset();
                        if (friendCheck != null) {
                            Hytools.sendMessage("\u00A72" + friendCheck);
                        }

                        // guild check
                        String guildCheck = GuildCheck.reset();
                        if (guildCheck != null) {
                            Hytools.sendMessage("\u00A76" + guildCheck);
                        }
                    }

                    Hytools.log.info("Reset party guess " + tempUserArray.toString());

                    doOnceBeforeTimeReset = true;
                }
            };
            Timer timer = new Timer("Timer");

            timer.schedule(task, delay);

            doOnceBeforeTimeReset = false;
        }

        tempUserArray.add(username);
        Hytools.log.info("Added user to array" + tempUserArray.toString());
        return;
    }
}
