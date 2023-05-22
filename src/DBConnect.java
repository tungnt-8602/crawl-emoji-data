import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;

public class DBConnect {
    private static Connection conn;

    public static void connect() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Admin\\Desktop\\sqlite\\emoji.db");
            System.out.println("Connected to database");
            Statement statement = conn.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (this.conn != null) {
                this.conn.close();
                System.out.println("Disconnected from database");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createTables() {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("drop table if exists category");
            String sqlCategory = "CREATE TABLE category (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE);";
            stmt.executeUpdate("drop table if exists emoji");
            String sqlEmoji = "CREATE TABLE emoji (id INTEGER PRIMARY KEY, category_id INTEGER NOT NULL, emoji_text TEXT NOT NULL, isFavorite INTEGER NOT NULL,  FOREIGN KEY (category_id) REFERENCES category(id));";
            stmt.execute(sqlCategory);
            stmt.execute(sqlEmoji);
            System.out.println("Tables created successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertCategory(int id, String name) {
        try {
            String sql = "INSERT INTO category (id, name) VALUES (?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertEmoji(int id, int categoryId, String emojiText, int isFavourite) {
        try {
            String sql = "INSERT INTO emoji (id, category_id, emoji_text, isFavorite) VALUES (?,?,?,?);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, categoryId);
            pstmt.setString(3, emojiText);
            pstmt.setInt(4, isFavourite);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Connection connection = null;
        try {
            connect();
            createTables();
            int cid = 1;
            int eid = 1;
            Document doc = Jsoup.connect("https://www.emoticonstext.com/").get();
            Elements categories = doc.select(".container .cate A");
            for (Element category : categories) {
                insertCategory(cid, category.text());
                Document doc1 = Jsoup.connect("https://www.emoticonstext.com/" + category.attr("herf")).get();
                Elements emojis = doc1.select(".emoticons span");
                for (Element emoji : emojis) {
                    String emojiText = emoji.text();
                    insertEmoji(eid, cid, emojiText, 0);
                    eid++;
                }
                cid++;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
}
