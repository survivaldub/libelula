/*
 *            This file is part of Libelula Minecraft Edition Project.
 *
 *  Libelula Minecraft Edition is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libelula Minecraft Edition is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Libelula Minecraft Edition. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package me.libelula.liderswag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Diego D'Onofrio <ddonofrio@member.fsf.org>
 */
public class DBManager {

    private final Main plugin;
    private Connection connection;

    public DBManager(Main plugin) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        this.plugin = plugin;
        String database = null;
        String user = null;
        String password = null;
        if (plugin.getConfig().getBoolean("MySQL.enable")) {
            database = plugin.getConfig().getString("MySQL.database");
            user = plugin.getConfig().getString("MySQL.user");
            password = plugin.getConfig().getString("MySQL.password");
        }
        if (database != null && user != null && password != null) {
            plugin.getLogger().info("Activating MySQL engine.");
            MySQLConnection(database, user, password);
        } else {
            plugin.getLogger().info("Activating SQLite engine.");
            SQLiteConnection();
        }
    }

    private void MySQLConnection(String database, String user, String password)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database,
                user, password);
    }

    private void SQLiteConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:"
                + plugin.getDataFolder().getAbsolutePath()
                + "/" + plugin.getName() + ".db");
        createTables();
    }

    private boolean executeUpdate(String query) {
        Statement statement;
        boolean results = false;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
            results = true;
        } catch (SQLException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
        return results;
    }

    private ResultSet getResultSet(String query) {
        Statement statement;
        ResultSet results = null;
        try {
            statement = connection.createStatement();
            results = statement.executeQuery(query);
        } catch (SQLException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
        return results;
    }

    private boolean createTables() {
        return executeUpdate("CREATE TABLE IF NOT EXISTS scores(player_name VARCHAR(16) PRIMARY KEY, score INTEGER);");
    }

    protected boolean setScore(String playerName, int score) {

        return executeUpdate("REPLACE INTO scores VALUES('" + playerName + "', " + score + ");");
    }

    protected int getScore(String playerName) {
        int result = 0;
        ResultSet rs = getResultSet("SELECT score FROM scores WHERE player_name='" + playerName + "';");
        if (rs != null) {
            try {
                if (rs.next()) {
                    result = rs.getInt("score");
                }
                rs.close();
            } catch (SQLException ex) {
                plugin.getLogger().severe(ex.getMessage());
            }
        }
        return result;
    }

    protected TreeMap<String, Integer> getScores() {
        TreeMap<String, Integer> results = new TreeMap<>();
        ResultSet rs = getResultSet("SELECT player_name, score FROM scores;");
        try {
            while (rs.next()) {
                results.put(rs.getString("player_name"), rs.getInt("score"));
            }
            rs.close();
        } catch (SQLException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
        return results;
    }
}
