import java.io.*;
import java.util.*;
import java.sql.*;
import java.lang.*;
import java.text.*;
import java.util.Date;

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
   static int guestState;

   public static void main(String args[]) throws ParseException {

      // reads server settings
      try {
         FileReader fr = new FileReader("ServerSettings.txt");
         BufferedReader br = new BufferedReader(fr);
         url = br.readLine();
         user = br.readLine();
         pw = br.readLine();
         br.close();
      } catch (Exception e) {
      }

      // opens connection
      try {
         Class.forName("com.mysql.jdbc.Driver").newInstance();
         System.out.println("Driver class found and loaded.");
      } catch (Exception ex) {
         System.out.println("Driver not found");
         System.out.println(ex);
      }
      ;
      try {
         conn = DriverManager.getConnection(url + "?" + "user=" + user + "&password=" + pw);
      } catch (Exception ex) {
         System.out.println("Could not open connection");
         System.out.println(ex);
      }
      ;

      // checks for table existence
      try {
         DatabaseMetaData md = conn.getMetaData();
         ResultSet rs = md.getTables(null, null, "rooms", null);
         if (rs.next())
            roomsExist = true;
         rs = md.getTables(null, null, "reservations", null);
         if (rs.next())
            reservationsExist = true;
      } catch (Exception e) {
      }

      // creates tables if not already created
      if (!roomsExist)
         try {
            String roomTable = "CREATE TABLE rooms (RoomCode CHAR(5) PRIMARY KEY,"
                  + "RoomName VARCHAR(30) UNIQUE, Beds INTEGER, bedType varchar(8),"
                  + "maxOcc INTEGER, basePrice FLOAT, decor VARCHAR(20))";
            Statement s1 = conn.createStatement();
            s1.executeUpdate(roomTable);
            roomsExist = true;
         } catch (Exception e) {
         }
      if (!reservationsExist)
         try {
            String resTable = "CREATE TABLE reservations (CODE INTEGER PRIMARY KEY, "
                  + "Room CHAR(5), CheckIn DATE, Checkout DATE, Rate FLOAT, LastName "
                  + "VARCHAR(15), FirstName VARCHAR(15), Adults Integer, Kids INTEGER, "
                  + "FOREIGN KEY (room) REFERENCES rooms(RoomCode))";
            Statement s2 = conn.createStatement();
            s2.executeUpdate(resTable);
            reservationsExist = true;
         } catch (Exception e) {
            System.out.println(e);
         }

      // checks if table populated
      try {
         Statement s3 = conn.createStatement();
         ResultSet rs = s3.executeQuery("SELECT COUNT(*) FROM rooms");
         rs.next();
         if (rs.getInt(1) > 0)
            roomsFilled = true;
         Statement s4 = conn.createStatement();
         rs = s4.executeQuery("SELECT COUNT(*) FROM reservations");
         rs.next();
         if (rs.getInt(1) > 0)
            reservationsFilled = true;
      } catch (Exception e) {
         System.out.println(e);
      }

      // main loop
      while (state > 0) {
         adminState = 1;
         guestState = 1;
         System.out.println("Enter A for Admin");
         System.out.println("Enter O for Owner");
         System.out.println("Enter G for Guest");
         System.out.println("Enter E for Exit");
         Scanner scan = new Scanner(System.in);
         String s = scan.next();
         if (s.equals("E"))
            state = 0;
         else if (s.equals("A"))
            runAdmin();
         else if (s.equals("O"))
            runOwnerMenu();
         else if (s.equals("G"))
            runGuestMenu();
      }

      // closes connection
      try {
         conn.close();
      } catch (Exception ex) {
         System.out.println("ex177: Unable to close connection");
      }
      ;
   }
   
   // -- ADMIN FUNCTIONS --

   public static void runAdmin() {
      Statement s;
      ResultSet rs;
      Scanner scan = new Scanner(System.in);
      while (adminState > 0) {
         System.out.println();
         if (!roomsExist)
            System.out.println("No database");
         else {
            if (roomsFilled)
               System.out.println("Full");
            else
               System.out.println("Empty");
         }
         try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT COUNT(*) FROM reservations");
            rs.next();
            System.out.println("Reservations: " + rs.getInt(1));
            rs = s.executeQuery("SELECT COUNT(*) FROM rooms");
            rs.next();
            System.out.println("Rooms: " + rs.getInt(1));
         } catch (Exception e) {
         }
         System.out.println("Enter V to view tables");
         System.out.println("Enter C to clear tables");
         System.out.println("Enter L to load tables");
         System.out.println("Enter R to remove tables");
         System.out.println("Enter E to switch user type");
         String curS = scan.next();
         if (curS.equals("E")) {
            adminState = 0;
            System.out.println(" ");
         } else if (curS.equals("V"))
            adminDisplay();
         else if (curS.equals("C"))
            adminClear();
         else if (curS.equals("L"))
            adminLoad();
         else if (curS.equals("R"))
            adminRemove();
      }
   }

   public static void adminDisplay() {
      System.out.println();
      if (!roomsExist)
         System.out.println("There are no tables to display");
      else if (!roomsFilled)
         System.out.println("The tables are empty");
      else {
         System.out.println("Enter Ro for rooms");
         System.out.println("Enter Re for reservations");
         System.out.println("Enter anything else to return to admin display");
         Scanner scan = new Scanner(System.in);
         String str = scan.next();
         ResultSet rs;
         PreparedStatement statement;
         int i = 1;
         try {
            if (str.equals("Ro")) {
               statement = conn.prepareStatement("select * from rooms");
               rs = statement.executeQuery();
               System.out.printf("%3s%11s%27s%7s%10s%12s%9s%14s\n",
                  "No", "RoomCode", "RoomName", "Beds", "bedType", "Occupancy", "Price", "Decor");
               while (rs.next()) {
                  System.out.printf("%3d", i);
                  System.out.printf("%11s", rs.getString("RoomCode"));
                  System.out.printf("%27s", rs.getString("RoomName"));
                  System.out.printf("%7s", rs.getInt("Beds"));
                  System.out.printf("%10s", rs.getString("bedType"));
                  System.out.printf("%12s", rs.getInt("maxOcc"));
                  System.out.printf("%9.2f", rs.getFloat("basePrice"));
                  System.out.printf("%14s", rs.getString("decor"));
                  System.out.println();
                  i++;
               }
            }
            if (str.equals("Re")) {
               statement = conn.prepareStatement("select * from reservations");
               rs = statement.executeQuery();
               System.out.printf("%3s%8s%7s%13s%13s%9s%16s%16s%9s%7s\n",
                  "No", "CODE", "Room", "CheckIn", "CheckOut", "Rate", "LastName", "FirstName", "Adults", "Kids");
               while (rs.next()) {
                  System.out.printf("%3d", i);
                  System.out.printf("%8s", rs.getInt("CODE"));
                  System.out.printf("%7s", rs.getString("Room"));
                  System.out.printf("%13s", rs.getDate("CheckIn"));
                  System.out.printf("%13s", rs.getDate("Checkout"));
                  System.out.printf("%9.2f", rs.getFloat("Rate"));
                  System.out.printf("%16s", rs.getString("LastName"));
                  System.out.printf("%16s", rs.getString("FirstName"));
                  System.out.printf("%9s", rs.getInt("Adults"));
                  System.out.printf("%7s", rs.getInt("Kids"));
                  System.out.println();
                  i++;
               }
            }
         } catch (Exception e) {
         }

      }
   }

   public static void adminClear() {
      if (roomsExist && roomsFilled) {
         try {
            Statement s = conn.createStatement();
            s.executeUpdate("delete from reservations");
            s.executeUpdate("delete from rooms");
            roomsFilled = false;
            reservationsFilled = false;
            System.out.println("Both tables have been cleared");
         } catch (Exception e) {
            System.out.println(e);
         }
      }
   }

   public static void adminLoad() {
      if (roomsExist && roomsFilled)
         System.out.println("Tables are already created and populated");
      if (!roomsExist) {
         try {
            String roomTable = "CREATE TABLE rooms (RoomCode CHAR(5) PRIMARY KEY,"
                  + "RoomName VARCHAR(30) UNIQUE, Beds INTEGER, bedType varchar(8),"
                  + "maxOcc INTEGER, basePrice FLOAT, decor VARCHAR(20))";
            Statement s1 = conn.createStatement();
            s1.executeUpdate(roomTable);
            roomsExist = true;
         } catch (Exception e) {
         }
         try {
            String resTable = "CREATE TABLE reservations (CODE INTEGER PRIMARY KEY, "
                  + "Room CHAR(5), CheckIn DATE, Checkout DATE, Rate FLOAT, LastName "
                  + "VARCHAR(15), FirstName VARCHAR(15), Adults Integer, Kids INTEGER, "
                  + "FOREIGN KEY (room) REFERENCES rooms(RoomCode))";
            Statement s2 = conn.createStatement();
            s2.executeUpdate(resTable);
            reservationsExist = true;
         } catch (Exception e) {
            System.out.println(e);
         }
      }
      if (!roomsFilled) {
         try {
            String psText = "INSERT INTO rooms VALUES(?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(psText);
            BufferedReader br = new BufferedReader(new FileReader("Rooms.csv"));
            String line = "";
            while ((line = br.readLine()) != null) {
               line = line.replace("'", "");
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
            psText = "INSERT INTO reservations VALUES(?,?,"
                  + "STR_TO_DATE(?,'%d-%b-%Y'),STR_TO_DATE(?,'%d-%b-%Y'),?,?,?,?,?)";
            ps = conn.prepareStatement(psText);
            br = new BufferedReader(new FileReader("Reservations.csv"));
            line = "";
            while ((line = br.readLine()) != null) {
               line = line.replace("'", "");
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
         } catch (Exception e) {
            System.out.println(e);
         }
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
      } catch (Exception e) {
         System.out.println(e);
      }
   }
   
   // -- OWNER FUNCTIONS --
   
   public static void runOwnerMenu() throws ParseException {
      int ownerState = 1;
      while (ownerState > 0) {
         System.out.println("");
         System.out.println("Enter Occ for occupancy review");
         System.out.println("Enter Rev for view revenue");
         System.out.println("Enter Ro for review rooms");
         System.out.println("Enter Res for review reservations");
         System.out.println("Enter E to return to switch user type");
         Scanner slay = new Scanner(System.in);
         String na = slay.next();
         System.out.println("");
         if (na.equals("E"))
            ownerState = 0;
         else if (na.equals("Occ"))
            runOccRev();
         else if (na.equals("Rev"))
            runViewRev();
         else if (na.equals("Ro"))
            runRevRooms();
         else if (na.equals("Res"))
            runRevRes();
      }
   }
   
   // OR-1
   
   public static void runOccRev() {
      System.out.println("Options:");
      System.out.println("1 - One day");
      System.out.println("2 - Date range");
      System.out.print("Enter choice: ");
      Scanner slay = new Scanner(System.in);
      String na = slay.next();
      System.out.println("");
      if (na.equals("1"))
         checkSingleAvail();
      else if (na.equals("2"))
         checkRangeAvail();
      else
         System.out.println("Invalid option");
   }
   
   public static void checkSingleAvail() {
      ResultSet rs;
      PreparedStatement statement;
      if (roomsExist && roomsFilled) {
         int day = 0, month = 0;
         Scanner scan = new Scanner(System.in);
         System.out.println("Single date - enter values in numbers");
         System.out.print("Enter day: ");
         try {
            day = scan.nextInt();
         } catch (Exception e) {
            System.out.println("Invalid number");
            return;
         }
         System.out.print("Enter month: ");
         try {
            month = scan.nextInt();
         } catch (Exception e) {
            System.out.println("Invalid number");
            return;
         }
         System.out.println();
         try {
            String date = "2010-" + month + "-" + day;
            statement = conn.prepareStatement(""
               + "select ro.RoomName, "
               +     "case when ro.RoomName in ("
               +        "select ro.RoomName "
               +        "from rooms ro, reservations re "
               +        "where ro.RoomCode = re.Room "
               +           "and re.CheckIn <= ? "
               +           "and ? < re.CheckOut"
               +     ") then ? "
               +          "else ? end as Vacancy "
               + "from rooms ro"
               + ";"
            );
            statement.setString(1, date);
            statement.setString(2, date);
            statement.setString(3, "Occupied");
            statement.setString(4, "Empty");
            System.out.printf("Date: %s\n", date);
            rs = statement.executeQuery();
            //System.out.printf("%3s%8s%7s%13s%13s%9s%16s%16s%9s%7s\n",
            //   "No", "CODE", "Room", "CheckIn", "CheckOut", "Rate", "LastName", "FirstName", "Adults", "Kids");
            int i = 1;
            while (rs.next()) {
               System.out.printf("%3d", i);
               /*System.out.printf("%8s", rs.getInt("CODE"));
               System.out.printf("%7s", rs.getString("Room"));
               System.out.printf("%13s", rs.getDate("CheckIn"));
               System.out.printf("%13s", rs.getDate("Checkout"));
               System.out.printf("%9.2f", rs.getFloat("Rate"));
               System.out.printf("%16s", rs.getString("LastName"));
               System.out.printf("%16s", rs.getString("FirstName"));
               System.out.printf("%9s", rs.getInt("Adults"));
               System.out.printf("%7s", rs.getInt("Kids"));
               */
               System.out.printf("%25s | %s", rs.getString("RoomName"), rs.getString("Vacancy"));
               System.out.println();
               i++;
            }
            viewReservations(rs);
         } catch (Exception e) {
         }
      } else {
         System.out.println("Tables not created/filled");
      }
   }

   public static void viewReservations(ResultSet rs) {
      int view = 1;
      Scanner scan = new Scanner(System.in);
      do {
         System.out.println();
         System.out.println("Enter V to view reservation details");
         System.out.println("Enter anything else to return to menu");
         System.out.print("Input: ");
         String input = scan.next();
         if (input.toUpperCase().equals("V")) {
            System.out.print("Enter room index from above list: ");
            String index = scan.next();
            String code;
         } else {
            view = 0;
         }
      } while (view > 0);
   }
   
   public static void checkRangeAvail() {
      
   }
   
   // OR-2
   
   public static void runViewRev() {
      
   }
   
   // OR-3

   public static void runRevRooms() {
      
   }
   
   // OR-4

   public static void runRevRes() {
      
   }
   
   // -- GUEST FUNCTIONS --

   public static void runGuestMenu() throws ParseException {
      while (guestState > 0) {
         System.out.println("");
         System.out.println("Enter Ro for rooms and rates");
         System.out.println("Enter Re for reservations");
         System.out.println("Enter E to return to switch user type");
         Scanner slay = new Scanner(System.in);
         String na = slay.next();
         System.out.println("");
         if (na.equals("E"))
            guestState = 0;
         else if (na.equals("Ro"))
            runGuestRooms();
         else if (na.equals("Re")) {
            if (roomsFilled && roomsExist)
               runGuestRes();
            else
               System.out.println("Tables not created/filled");
         }
      }
   }

   public static void runGuestRooms() {
      ResultSet rs;
      PreparedStatement statement;
      if (roomsExist && roomsFilled) {
         try {
            statement = conn.prepareStatement("select * from rooms");
            rs = statement.executeQuery();
            int i = 1;
            while (rs.next()) {
               System.out.print(i + "  ");
               System.out.println(rs.getString("RoomCode") + "  ");
               i++;
            }
         } catch (Exception e) {
            System.out.println(e);
         }
         System.out.println("Choose a room by entering its number");
         Scanner s = new Scanner(System.in);
         String room = s.next();
         try {
            statement = conn.prepareStatement("select * from rooms");
            rs = statement.executeQuery();
            int roomNum = Integer.parseInt(room);
            if (roomNum < 11 && roomNum > 0 && rs != null) {
               rs.absolute(roomNum);
               System.out.println("");
               System.out.println("RoomCode  RoomName  Beds  bedType  Occupancy  Price  Decor");
               System.out.printf("%11s", rs.getString("RoomCode"));
               System.out.printf("%27s", rs.getString("RoomName"));
               System.out.printf("%7s", rs.getInt("Beds"));
               System.out.printf("%10s", rs.getString("bedType"));
               System.out.printf("%12s", rs.getInt("maxOcc"));
               System.out.printf("%9.2f", rs.getFloat("basePrice"));
               System.out.printf("%14s", rs.getString("decor"));
               System.out.println();
               System.out.println("Enter C to check availability");
               System.out.println("Enter anything else to return to guest menu");
               room = s.next();
               if (room.equals("C"))
                  checkAvai(roomNum, rs.getString("RoomCode"), rs.getFloat("basePrice"), rs.getString("RoomName"),
                        rs.getInt("maxOcc"));
            }
         } catch (Exception e) {
            System.out.println("Bad input");
         }
      } else
         System.out.println("Tables not created/filled");
   }

   public static void checkAvai(int roomNum, String roomCode, double bP, String roomName, int occ)
         throws ParseException {
      boolean occupied = false;
      System.out.println("\nEnter the CheckIn date in YYYY DD MM format");
      Scanner scan = new Scanner(System.in);
      String t = scan.nextLine();
      StringTokenizer st = new StringTokenizer(t);
      String checkIn = "";
      checkIn = checkIn + st.nextToken() + "." + st.nextToken() + "." + st.nextToken();
      System.out.println("\nEnter the CheckOut date in YYYY DD MM format");
      t = scan.nextLine();
      st = new StringTokenizer(t);
      String checkOut = "";
      checkOut = checkOut + st.nextToken() + "." + st.nextToken() + "." + st.nextToken();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy.dd.MM");
      SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
      Date starts = sdf.parse(checkIn);
      Date ends = sdf.parse(checkOut);
      double mult = 1.0;
      Date j1 = sdf.parse("2010.01.01");
      Date j4 = sdf.parse("2010.07.04");
      Date s6 = sdf.parse("2010.09.06");
      Date o3 = sdf.parse("2010.10.30");
      Date jj = sdf.parse("2011.01.01");
      Calendar start = Calendar.getInstance();
      start.setTime(starts);
      Calendar end = Calendar.getInstance();
      end.setTime(ends);
      if ((j1.compareTo(starts) >= 0 && j1.compareTo(ends) < 0) || (j4.compareTo(starts) >= 0 && j4.compareTo(ends) < 0)
            || (s6.compareTo(starts) >= 0 && s6.compareTo(ends) < 0)
            || (o3.compareTo(starts) >= 0 && o3.compareTo(ends) < 0)
            || (jj.compareTo(starts) >= 0 && jj.compareTo(ends) < 0))
         mult = 1.25;
      if (mult == 1.0) {
         for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            if (start.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                  || start.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
               mult = 1.1;
         }
      }
      start.setTime(starts);
      for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
         System.out.println("");
         System.out.printf(sdf2.format(date));
         try {
            Statement s = conn.createStatement();
            ResultSet rs;
            rs = s.executeQuery("SELECT COUNT(*) FROM reservations WHERE CheckIn <= '" + sdf2.format(date)
                  + "' && Checkout > '" + sdf2.format(date) + "' && Room = '" + roomCode + "'");
            rs.next();
            if (!(rs.getInt(1) > 0))
               System.out.print(" " + bP * mult);
            else {
               occupied = true;
               System.out.print(" Occupied");
            }
         } catch (Exception e) {
            System.out.println(e);
         }
      }
      if (!occupied) {
         System.out.println("\nEnter P to place a reservation");
         System.out.println("Enter anything else to return to guest menu");
         Scanner newNew = new Scanner(System.in);
         if (newNew.next().equals("P"))
            System.out.println("place a res");
         completeReservation(starts, ends, (float) bP * (float) mult, roomCode, roomName, occ);
      }
   }

   public static void runGuestRes() throws ParseException {
      try {
         System.out.println("\nEnter the CheckIn date in YYYY DD MM format");
         Scanner scan = new Scanner(System.in);
         String t = scan.nextLine();
         StringTokenizer st = new StringTokenizer(t);
         String checkIn = "";
         checkIn = checkIn + st.nextToken() + "." + st.nextToken() + "." + st.nextToken();
         System.out.println("\nEnter the CheckOut date in YYYY DD MM format");
         t = scan.nextLine();
         st = new StringTokenizer(t);
         String checkOut = "";
         checkOut = checkOut + st.nextToken() + "." + st.nextToken() + "." + st.nextToken();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy.dd.MM");
         SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
         Date starts = sdf.parse(checkIn);
         Date ends = sdf.parse(checkOut);
         double mult = 1.0;
         Date j1 = sdf.parse("2010.01.01");
         Date j4 = sdf.parse("2010.07.04");
         Date s6 = sdf.parse("2010.09.06");
         Date o3 = sdf.parse("2010.10.30");
         Date jj = sdf.parse("2011.01.01");
         Calendar start = Calendar.getInstance();
         start.setTime(starts);
         Calendar end = Calendar.getInstance();
         end.setTime(ends);
         if ((j1.compareTo(starts) >= 0 && j1.compareTo(ends) < 0)
               || (j4.compareTo(starts) >= 0 && j4.compareTo(ends) < 0)
               || (s6.compareTo(starts) >= 0 && s6.compareTo(ends) < 0)
               || (o3.compareTo(starts) >= 0 && o3.compareTo(ends) < 0)
               || (jj.compareTo(starts) >= 0 && jj.compareTo(ends) < 0))
            mult = 1.25;
         if (mult == 1.0) {
            for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
               if (start.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                     || start.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                  mult = 1.1;
            }
         }
         PreparedStatement s = conn.prepareStatement("SELECT RoomCode FROM rooms");
         ResultSet rs = s.executeQuery();
         ArrayList<String> validRooms = new ArrayList<String>();
         while (rs.next()) {
            validRooms.add(rs.getString("RoomCode"));
         }
         start.setTime(starts);
         Statement sa = conn.createStatement();
         ResultSet rsa;
         for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            try {
               rsa = sa.executeQuery("SELECT DISTINCT Room FROM reservations WHERE CheckIn <= '" + sdf2.format(date)
                     + "' && Checkout > '" + sdf2.format(date) + "'");
               while (rsa.next()) {
                  if (validRooms.contains(rsa.getString("Room")))
                     validRooms.remove(rsa.getString("Room"));
               }
            } catch (Exception e) {
               System.out.println(e);
            }
         }
         ArrayList<String> realName = new ArrayList<String>();
         ArrayList<Float> prices = new ArrayList<Float>();
         for (int i = 0; i < validRooms.size(); i++) {
            s = conn.prepareStatement(
                  "SELECT RoomName, basePrice FROM rooms WHERE RoomCode = '" + validRooms.get(i) + "'");
            rs = s.executeQuery();
            rs.next();
            realName.add(rs.getString("RoomName"));
            prices.add(rs.getFloat("basePrice") * (float) mult);
         }
         for (int i = 0; i < validRooms.size(); i++) {
            System.out.print(i + 1 + "\t");
            System.out.print(validRooms.get(i) + "\t");
            System.out.print(realName.get(i) + "\t");
            System.out.print(prices.get(i) + "\n");
         }
         System.out.println("Enter the room number you would like to view");
         System.out.println("Enter anything else to return to guest menu");
         Scanner sce = new Scanner(System.in);
         int sca = sce.nextInt();
         if (sca > 0 && sca < validRooms.size() + 1) {
            s = conn.prepareStatement("SELECT * FROM rooms WHERE RoomCode = '" + validRooms.get(sca - 1) + "'");
            rs = s.executeQuery();
            rs.next();
            System.out.printf("%3s%11s%27s%7s%10s%12s%9s%14s\n",
               "No", "RoomCode", "RoomName", "Beds", "bedType", "Occupancy", "Price", "Decor");
            System.out.print(rs.getString("RoomCode") + "  ");
            System.out.print(rs.getString("RoomName") + "  ");
            System.out.print(rs.getInt("Beds") + "  ");
            System.out.print(rs.getString("bedType") + "  ");
            System.out.print(rs.getInt("maxOcc") + "  ");
            System.out.print(rs.getFloat("basePrice") + "  ");
            System.out.print(rs.getString("decor") + "  ");
            System.out.println("\nEnter P to place a reservation\nEnter anything else to return to guest menu");
            if (sce.next().equals("P"))
               completeReservation(starts, ends, rs.getFloat("basePrice") * (float) mult, rs.getString("RoomCode"),
                     rs.getString("RoomName"), rs.getInt("maxOcc"));
         }
      } catch (Exception e) {
         System.out.println(e);
      }
   }

   public static void completeReservation(Date starts, Date ends, float basePrice, String RoomCode, String RoomName,
         int occ) {
      Scanner scan = new Scanner(System.in);
      String first;
      String last;
      int adults = 0;
      int kids = 0;
      boolean bad = true;
      String temp;
      float disc = 1.0f;
      try {
         System.out.println("Enter first name");
         first = scan.next();
         System.out.println("Enter last name");
         last = scan.next();
         while (bad) {
            System.out.println("Enter number of adults");
            adults = scan.nextInt();
            System.out.println("Enter number of kids");
            kids = scan.nextInt();
            if (adults + kids > occ)
               System.out.println("Total occupancy exceeds maximum of " + occ);
            else
               bad = false;
         }
         System.out.println("Enter AAA for 10% discount");
         System.out.println("Enter AARP for 15% discount");
         System.out.println("Enter anything else for no discount");
         temp = scan.next();
         if (temp.equals("AAA"))
            disc = 0.9f;
         else if (temp.equals("AARP"))
            disc = 0.85f;
         System.out.println("Enter P to place a reservation");
         System.out.println("Enter anything else to cancel the reservation");
         temp = scan.next();
         if (temp.equals("P")) {
            Statement sa = conn.createStatement();
            ResultSet rsa = sa.executeQuery("SELECT CODE from reservations");
            ArrayList<Integer> unique = new ArrayList<Integer>();
            while (rsa.next())
               unique.add(rsa.getInt("CODE"));
            int code = 100000;
            while (unique.contains(code))
               code++;
            PreparedStatement ps = conn.prepareStatement("INSERT INTO reservations VALUES(?,?,"
                  + "STR_TO_DATE(?,'%Y-%m-%d'),STR_TO_DATE(?,'%Y-%m-%d'),?,?,?,?,?)");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            ps.setInt(1, code);
            ps.setString(2, RoomCode);
            ps.setString(3, sdf2.format(starts));
            ps.setString(4, sdf2.format(ends));
            ps.setFloat(5, basePrice * disc);
            ps.setString(6, last.toUpperCase());
            ps.setString(7, first.toUpperCase());
            ps.setInt(8, adults);
            ps.setInt(9, kids);
            ps.executeUpdate();
            System.out.println("Reservation has been placed");
         }
      } catch (Exception e) {
         System.out.println(e);
      }
   }
}
