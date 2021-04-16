package com.alysaa.geyseradmintools.database;

import com.alysaa.geyseradmintools.Gat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;

public class MySql {
    private static Connection connection;
    public String host;
    public String database;
    public String username;
    public String password;
    public static String Bantable;
    public int port;

    public void mysqlSetup() {
        host = Gat.plugin.getConfig().getString("host");
        port = Gat.plugin.getConfig().getInt("port");
        database = Gat.plugin.getConfig().getString("database");
        username = Gat.plugin.getConfig().getString("username");
        password = Gat.plugin.getConfig().getString("password");
        Bantable = "Ban_list";
        if (Gat.plugin.getConfig().getBoolean("EnableMySQL")) {
            try {
                synchronized (this) {
                    if (getConnection() != null && !getConnection().isClosed()) {
                        return;
                    }

                    Class.forName("com.mysql.jdbc.Driver");
                    setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":"
                            + this.port + "/" + this.database, this.username, this.password));
                    createTable();

                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[GeyserAdminTools] MYSQL Connected");
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Connection connection;
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:plugins/GAdminTools/database.db");
                String cmd = "CREATE TABLE IF NOT EXISTS " + MySql.Bantable + " (UUID char(36), Reason varchar(500), Username varchar(16), Hours varchar(500))";
                PreparedStatement stmt = connection.prepareStatement(cmd);
                stmt.execute();
                System.out.println("[GeyserAdminTools] SQLite Connected.");
                connection.close();
                //stmt.close();
            } catch (Exception e) {
                System.out.println("SQLite Error");
                e.printStackTrace();
            }
        }
    }
    public static void createTable() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + MySql.Bantable + " (UUID char(36), Reason varchar(500), Username varchar(16), Hours varchar(500))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        MySql.connection = connection;
    }
}
