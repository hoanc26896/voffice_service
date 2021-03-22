
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class JDBCStatementSelectExample {

	private static final String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private static final String DB_CONNECTION = "jdbc:oracle:thin:@10.58.71.166:1521:dbmss";
	private static final String DB_USER = "voffice";
	private static final String DB_PASSWORD = "voffice#123";

	public static void main(String[] argv) {
            try {
                Gson gson = new Gson();
                    BufferedReader br = new BufferedReader(
                            new FileReader("E:\\file.json"));

                    //convert the json string back to object
                   

                } catch (IOException e) {
                        e.printStackTrace();
                }
//		try {
//
//			selectRecordsFromDbUserTable();
//
//		} catch (SQLException e) {
//
//			System.out.println(e.getMessage());
//
//		}

	}

	private static void selectRecordsFromDbUserTable() throws SQLException {

		Connection dbConnection = null;
		Statement statement = null;

		String selectTableSQL = "SELECT staffid,loginname from staff";

		try {
			dbConnection = getDBConnection();
			statement = dbConnection.createStatement();

//			System.out.println(selectTableSQL);

			// execute select SQL stetement
			ResultSet rs = statement.executeQuery(selectTableSQL);

			while (rs.next()) {

				String userid = rs.getString("staffid");
				String username = rs.getString("loginname");

//				System.out.println("userid : " + userid);
//				System.out.println("username : " + username);

			}

                } catch (SQLException e) {

//			System.out.println(e.getMessage());

		} finally {

			if (statement != null) {
				statement.close();
			}

			if (dbConnection != null) {
				dbConnection.close();
			}

		}

	}

	private static Connection getDBConnection() {

		Connection dbConnection = null;

		try {

			Class.forName(DB_DRIVER);

		} catch (ClassNotFoundException e) {

//			System.out.println(e.getMessage());

		}

		try {

			dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
					DB_PASSWORD);
			return dbConnection;

		} catch (SQLException e) {

//			System.out.println(e.getMessage());

		}

		return dbConnection;

	}

}