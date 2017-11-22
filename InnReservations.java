import java.io.*;
import java.util.*;
import java.sql.*;
import java.lang.*;

public class InnReservations {
	
	
	public static void main(String args[]) {
		String url = null;
		String user = null;
		String pw = null;
      boolean roomsExist = false;
      boolean reservationsExist = false;
      boolean roomsFilled = false;
      boolean reservationsFilled = false;
		Connection conn = null;
      
      //reads server settings
		try {
			FileReader fr = new FileReader("ServerSettings.txt");
			BufferedReader br = new BufferedReader(fr);
			url = br.readLine();
			user = br.readLine();
			pw = br.readLine();
			br.close();
		}
		catch(Exception e){ }
      
      //opens connection
		 try {
			 Class.forName("com.mysql.jdbc.Driver").newInstance();
			 System.out.println ("Driver class found and loaded."); 
		      }
		      catch (Exception ex) {
			 System.out.println("Driver not found");
			 System.out.println(ex);
		      };
		   try {
			   conn = DriverManager.getConnection(url+"?"+
			   "user="+user+"&password="+pw);
		   }
		   catch (Exception ex) {
				 System.out.println("Could not open connection");
				 System.out.println(ex);
		};
      
      //checks for table existence
      try {
      DatabaseMetaData md = conn.getMetaData();
      ResultSet rs = md.getTables(null, null, "rooms", null);
      if(rs.next())
         roomsExist = true;
      rs = md.getTables(null, null, "reservations", null);
      if(rs.next())
         reservationsExist = true;
      }
      catch(Exception e){ }
      
      //creates tables if not already created
      if(!roomsExist) 
         try {
              String roomTable = "CREATE TABLE rooms (RoomCode CHAR(5) PRIMARY KEY,"
              + "RoomName VARCHAR(30) UNIQUE, Beds INTEGER, bedType varchar(8),"
              + "maxOcc INTEGER, basePrice FLOAT, decor VARCHAR(20))";
              Statement s1 = conn.createStatement();
              s1.executeUpdate(roomTable);
         }
         catch(Exception e) { }
      if(!reservationsExist)
         try {
            String resTable = "CREATE TABLE reservations (CODE INTEGER PRIMARY KEY, "
            + "Room CHAR(5), CheckIn DATE, Checkout DATE, Rate FLOAT, LastName "
            + "VARCHAR(15), FirstName VARCHAR(15), Adults Integer, Kids INTEGER, "
            + "FOREIGN KEY (room) REFERENCES rooms(RoomCode))";
            Statement s2 = conn.createStatement();
            s2.executeUpdate(resTable);
         }
         catch(Exception e) {System.out.println(e); }
         
         //checks if table populated
         try
            {
               Statement s3 = conn.createStatement();
               ResultSet rs = s3.executeQuery("SELECT COUNT(*) FROM rooms");
               rs.next();
               if(rs.getInt() > 0)
                  roomsFilled = true;
               Statement s4 = conn.createStatement();
               rs = s4.executeQuery("SELECT COUNT(*) FROM reservations");
               rs.next();
               if(rs.getInt() > 0)
                  roomsFilled = true;
            }
         catch(Exception e) {System.out.println(e); }
      
         
      //closes connection
		try {
			 conn.close();
		      }
		      catch (Exception ex) {
			 System.out.println("ex177: Unable to close connection");
		      };
	}
}