package com.alysaa.geyseradmintools.forms;

import com.alysaa.geyseradmintools.database.BanDatabaseSetup;
import com.alysaa.geyseradmintools.utils.CheckJavaOrFloodPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class BanPlayerForm {
    public static void banList(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isFloodgatePlayer = CheckJavaOrFloodPlayer.isFloodgatePlayer(uuid);
        if (isFloodgatePlayer) {
            FloodgatePlayer fplayer = FloodgateApi.getInstance().getPlayer(uuid);
            fplayer.sendForm(
                    SimpleForm.builder()
                            .title("Ban/Unban Tool")
                            .button("Ban Player")
                            .button("Unban Player")
                            .responseHandler((form, responseData) -> {
                                SimpleFormResponse response = form.parseResponse(responseData);
                                if (!response.isCorrect()) {
                                    // player closed the form or returned invalid info (see FormResponse)
                                    return;
                                }
                                if (response.getClickedButtonId() == 0) {
                                    if (player.hasPermission("geyseradmintools.banplayer")) {
                                        banPlayers(player);
                                    } else {
                                        player.sendMessage("[GeyserAdminTool] You do not have the permission to use this button!");
                                    }
                                }
                                if (response.getClickedButtonId() == 1) {
                                    if (player.hasPermission("geyseradmintools.banplayer")) {
                                        unbanPlayers(player);
                                    } else {
                                        player.sendMessage("[GeyserAdminTool] You do not have the permission to ise this button!");
                                    }
                                }
                            }));
        }
    }

    public static void banPlayers(Player player) {
        Runnable runnable = () -> {
            UUID uuid = player.getUniqueId();
            List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            String[] playerlist = names.toArray(new String[0]);
            boolean isFloodgatePlayer = CheckJavaOrFloodPlayer.isFloodgatePlayer(uuid);
            if (isFloodgatePlayer) {
                FloodgatePlayer fplayer = FloodgateApi.getInstance().getPlayer(uuid);
                fplayer.sendForm(
                        CustomForm.builder()
                                .title("Ban tool")
                                .dropdown("Select Player", playerlist)
                                .input("Hours banned")
                                .input("Ban Reason")
                                .responseHandler((form, responseData) -> {
                                    CustomFormResponse response = form.parseResponse(responseData);
                                    if (!response.isCorrect()) {
                                        return;
                                    }
                                    int clickedIndex = response.getDropdown(0);
                                    String hours = response.getInput(1);
                                    String reason = response.getInput(2);
                                    String name = names.get(clickedIndex);
                                    Player player1 = Bukkit.getPlayer(name);
                                    player.sendMessage("[GeyserAdminTools] Player " + name + " is banned");
                                    //database code
                                    try {
                                        String sql = "(UUID,REASON,USERNAME,HOURS) VALUES (?,?,?,?)";
                                        PreparedStatement insert = BanDatabaseSetup.getConnection().prepareStatement("INSERT INTO " + BanDatabaseSetup.Bantable
                                                + sql);
                                        insert.setString(1, player1.getUniqueId().toString());
                                        insert.setString(2, reason);
                                        insert.setString(3, name);
                                        insert.setString(4, hours);
                                        insert.executeUpdate();
                                        // Player inserted now
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                    player1.kickPlayer("you where banned for: " + reason);
                                    //end
                                }));
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void unbanPlayers(Player player) {
        Runnable runnable = () -> {
            UUID uuid = player.getUniqueId();
            List<String> names = new ArrayList<>();
            String query = "SELECT * FROM " + BanDatabaseSetup.Bantable;
            try (Statement stmt = BanDatabaseSetup.getConnection().createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    names.add(rs.getString("Username"));
                }
                rs.close();
                String[] playerlist = names.toArray(new String[0]);
                boolean isFloodgatePlayer = CheckJavaOrFloodPlayer.isFloodgatePlayer(uuid);
                if (isFloodgatePlayer) {
                    FloodgatePlayer fplayer = FloodgateApi.getInstance().getPlayer(uuid);
                    fplayer.sendForm(
                            CustomForm.builder()
                                    .title("unban tool")
                                    .dropdown("Select Player to unban", playerlist)
                                    .responseHandler((form, responseData) -> {
                                        CustomFormResponse response = form.parseResponse(responseData);
                                        if (!response.isCorrect()) {
                                            return;
                                        }
                                        int clickedIndex = response.getDropdown(0);
                                        String name = names.get(clickedIndex);
                                        OfflinePlayer player1 = Bukkit.getOfflinePlayer(name);
                                        player.sendMessage("[GeyserAdminTools] Player " + name + " is unbanned");
                                        //MySQL code
                                        try {
                                            PreparedStatement statement = BanDatabaseSetup.getConnection()
                                                    .prepareStatement("DELETE FROM " + BanDatabaseSetup.Bantable + " WHERE UUID=?");
                                            statement.setString(1, player1.getUniqueId().toString());
                                            statement.execute();

                                        } catch (SQLException exe) {
                                            exe.printStackTrace();
                                        }
                                    }));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}