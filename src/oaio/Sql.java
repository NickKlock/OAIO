package oaio;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Sql {

    public static Connection DbConnector() throws SQLException, IOException {

        Properties p = new Properties();
        p.load(new FileInputStream("config.ini"));
        String ip = p.getProperty("IP");
        String user = p.getProperty("USER");
        String pw = p.getProperty("PW");

        try {
            Connection conn;
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(
                    "jdbc:sqlserver://" + ip + ";user=" + user + ";password=" + pw);
            System.out.println("Success");
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e);
            System.out.println("Error");
        }
        return null;
    }

}
