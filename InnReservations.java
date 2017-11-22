import java.io.*;
import java.util.*;
import java.sql.*;
import java.lang.*;

public class InnReservations {
	   static String url = null;
		static String user = null;
		static String pw = null;
      static boolean roomsExist = false;
      static boolean reservationsExist = false;
      static boolean roomsFilled = false;
      static boolean reservationsFilled = false;
		static Connection conn = null;
      static String cur = "";
      static int state = 1;
      static int adminState;
	
	public static void main(String args[]) {
		
      
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
              roomsExist = true;
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
            reservationsExist = true;
         }
         catch(Exception e) {System.out.println(e); }
         
         //checks if table populated
         try
            {
               Statement s3 = conn.createStatement();
               ResultSet rs = s3.executeQuery("SELECT COUNT(*) FROM rooms");
               rs.next();
               if(rs.getInt(1) > 0)
                  roomsFilled = true;
               Statement s4 = conn.createStatement();
               rs = s4.executeQuery("SELECT COUNT(*) FROM reservations");
               rs.next();
               if(rs.getInt(1) > 0)
                  reservationsFilled = true;
            }
         catch(Exception e) {System.out.println(e); }
      
      //main loop
      while(state > 0) {
            adminState = 1;
            System.out.println("Enter A for Admin");
            System.out.println("Enter O for Owner");
            System.out.println("Enter G for Guest");
            System.out.println("Enter E for Exit");
            Scanner scan = new Scanner(System.in);
            String s = scan.next();
            if(s.equals("E"))
               state = 0;
            else if(s.equals("A"))
               runAdmin();
      }
         
      //closes connection
		try {
			 conn.close();
		      }
		      catch (Exception ex) {
			 System.out.println("ex177: Unable to close connection");
		      };
	}
   
   public static void runAdmin() {
      Statement s;
      ResultSet rs;
      Scanner scan = new Scanner(System.in);
      while(adminState > 0) {
         System.out.println();
         if(!roomsExist)
            System.out.println("No database");
         else {
            if(roomsFilled)
               System.out.println("Full");
            else
               System.out.println("Empty");
         }
         try {
         s = conn.createStatement();
         rs = s.executeQuery("SELECT COUNT(*) FROM reservations");
         rs.next();
         System.out.println("Reservations: "+rs.getInt(1));
         rs = s.executeQuery("SELECT COUNT(*) FROM rooms");
         rs.next();
         System.out.println("Rooms: "+rs.getInt(1));
         }
         catch(Exception e) { }
         System.out.println("Enter V to view tables");
         System.out.println("Enter C to clear tables");
         System.out.println("Enter L to load tables");
         System.out.println("Enter R to remove tables");
         System.out.println("Enter E to switch user type");
         String curS = scan.next();
         if(curS.equals("E")) {
            adminState = 0;
            System.out.println(" ");
         }
         else if(curS.equals("V"))
            adminDisplay();
         else if(curS.equals("C"))
            adminClear();
         else if(curS.equals("L"))
            adminLoad();
         else if(curS.equals("R"))
            adminRemove();
      }
   }

   public static void adminDisplay() {
      System.out.println();
      if(!roomsExist)
         System.out.println("There are no tables to display");
      else if(!roomsFilled)
         System.out.println("The tables are empty");
      else {
           System.out.println("Enter Ro for rooms");
           System.out.println("Enter Re for reservations");
           Scanner scan = new Scanner(System.in);
           String str = scan.next();
           ResultSet rs;
           PreparedStatement statement;
           int i = 1;
           try{
           if(str.equals("Ro")) {
              statement = conn.prepareStatement("select * from rooms");
              rs = statement.executeQuery();
              System.out.println("RoomCode  RoomName  Beds  bedType  Occupancy  Price  Decor");
              while(rs.next()) {
                 System.out.print(i+"  ");
                 System.out.print(rs.getString("RoomCode")+"  ");
                 System.out.print(rs.getString("RoomName")+"  ");
                 System.out.print(rs.getInt("Beds")+"  ");
                 System.out.print(rs.getString("bedType")+"  ");
                 System.out.print(rs.getInt("maxOcc")+"  ");
                 System.out.print(rs.getFloat("basePrice")+"  ");
                 System.out.print(rs.getString("decor")+"  ");
                 i++;
                 System.out.println();
              }
           }
           if(str.equals("Re")) {
              statement = conn.prepareStatement("select * from reservations");
              rs = statement.executeQuery();
              System.out.println("CODE Room CheckIn CheckOut Rate  LastName  FirstName  Adults  Kids");
              while(rs.next()) {
                 System.out.print(i+" ");
                 System.out.print(rs.getInt("CODE")+" ");
                 System.out.print(rs.getString("Room")+" ");
                 System.out.print(rs.getDate("CheckIn")+" ");
                 System.out.print(rs.getDate("Checkout")+" ");
                 System.out.print(rs.getFloat("Rate")+" ");
                 System.out.print(rs.getString("LastName")+" ");
                 System.out.print(rs.getString("FirstName")+" ");
                 System.out.print(rs.getInt("Adults")+" ");
                 System.out.print(rs.getInt("Kids")+"\n");
                 i++;
              }
           }
           }
           catch(Exception e){}
           
      }
   }
   
   public static void adminClear() {
      if(roomsExist && roomsFilled) {
            try {
               Statement s = conn.createStatement();
               s.executeUpdate("delete from reservations");
               s.executeUpdate("delete from rooms");
               roomsFilled = false;
               reservationsFilled = false;
               System.out.println("Both tables have been cleared");
            }
            catch(Exception e) { System.out.println(e); }
      }
   }
   
   public static void adminLoad() {
      if(roomsExist && roomsFilled)
         System.out.println("Tables are already created and populated");
      if(!roomsExist) {
            try {
              String roomTable = "CREATE TABLE rooms (RoomCode CHAR(5) PRIMARY KEY,"
              + "RoomName VARCHAR(30) UNIQUE, Beds INTEGER, bedType varchar(8),"
              + "maxOcc INTEGER, basePrice FLOAT, decor VARCHAR(20))";
              Statement s1 = conn.createStatement();
              s1.executeUpdate(roomTable);
              roomsExist = true;
         }
         catch(Exception e) { }
         try {
            String resTable = "CREATE TABLE reservations (CODE INTEGER PRIMARY KEY, "
            + "Room CHAR(5), CheckIn DATE, Checkout DATE, Rate FLOAT, LastName "
            + "VARCHAR(15), FirstName VARCHAR(15), Adults Integer, Kids INTEGER, "
            + "FOREIGN KEY (room) REFERENCES rooms(RoomCode))";
            Statement s2 = conn.createStatement();
            s2.executeUpdate(resTable);
            reservationsExist = true;
         }
         catch(Exception e) {System.out.println(e); }
      }
      if(!roomsFilled) {
         try {
            String psText = "INSERT INTO rooms VALUES(?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(psText);
            BufferedReader br = new BufferedReader(new FileReader("Rooms.csv"));
            String line = "";
            while((line = br.readLine()) != null) {
               line = line.replace("'","");
               String[] roomRes = line.split(",");
               ps.setString(1, roomRes[0]);
               ps.setString(2, roomRes[1]);
               ps.setInt(3, Integer.parseInt(roomRes[2]));
               ps.setString(4, roomRes[3]);
               ps.setInt(5, Integer.parseInt(roomRes[4]));
               ps.setFloat(6, Float.parseFloat(roomRes[5]));
               ps.setString(7, roomRes[6]);
               ps.executeUpdate();
            }
            roomsFilled = true;
            psText = "INSERT INTO reservations VALUES(?,?,"+
            "STR_TO_DATE(?,'%d-%b-%Y'),STR_TO_DATE(?,'%d-%b-%Y'),?,?,?,?,?)";
            ps = conn.prepareStatement(psText);
            br = new BufferedReader(new FileReader("Reservations.csv"));
            line = "";
            while((line = br.readLine()) != null) {
                  line = line.replace("'","");
                  String[] resRes = line.split(",");
                  ps.setInt(1, Integer.parseInt(resRes[0]));
                  ps.setString(2, resRes[1]);
                  ps.setString(3, resRes[2]);
                  ps.setString(4, resRes[3]);
                  ps.setFloat(5, Float.parseFloat(resRes[4]));
                  ps.setString(6, resRes[5]);
                  ps.setString(7, resRes[6]);
                  ps.setInt(8, Integer.parseInt(resRes[7]));
                  ps.setInt(9, Integer.parseInt(resRes[8]));
                  ps.executeUpdate();
            }
            reservationsFilled = true;
         }
         catch(Exception e) { System.out.println(e); }
      }
   }
   
   public static void adminRemove() {
      try {
      Statement s = conn.createStatement();
      s.executeUpdate("drop table reservations");
      s.executeUpdate("drop table rooms");
      reservationsExist = false;
      reservationsFilled = false;
      roomsExist = false;
      roomsFilled = false;
      }
      catch(Exception e) { System.out.println(e); }
   }
}
