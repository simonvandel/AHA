package Database;

import Communication.SensorState;
import Sampler.Sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by heider on 19/11/15.
 */
public class DB {

  private int mScopeSize = 0;
  private int mActionsNum = 0;

  private static DB db;


  private static Connection openConnection() {
    String url = "jdbc:sqlite:Samples.db";
    try {
      Connection c = DriverManager.getConnection(url);
      return c;
    } catch (Exception ex) {
      System.out.println("SQLException: " + ex.getMessage());
    }
    return null;
  }
  /**
   * Initializes an object of Sampler class.
   */
  private DB(int actionsNum, int scopeSize) {
    this.mActionsNum = actionsNum;
    this.mScopeSize = scopeSize;
  }

  /**
   * Get instance method to ensure singleton pattern,
   *
   * @return the one and only object of the Sampler class.
   */
  public static DB getInstance(int scopeSize, int actionsNum) {
    if (db == null) {
      db = new DB(actionsNum, scopeSize);
    }
    return db;
  }

  public int createDB() { // Refactor to Autocheck/make table if not existing
    Connection c = openConnection();
    try {
      Statement st = c.createStatement();
      String states = "";
      if(mScopeSize>0) {
        for (int i = 0; i<mScopeSize; i++) {
          states = states + "state" + i + " int,";
        }
      }
      String actions = "";
      if(mActionsNum>0) {
        for (int i=0;i<mActionsNum;i++) {
          actions = actions + ",ActionId" + i + " String, value" + i + " int";
        }
      }
      st.executeQuery("CREATE TABLE Samples (" + states + "Time int" + actions + ")");
      return 1;
    } catch (Exception ex) {
      System.out.println("SQLException: " + ex.getMessage());
    }
    return 0;
  }

  public static Sample getStateHistoryFromDBByIndex(int index) {
    Connection c = openConnection();
    try {

      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery("SELECT * FROM Samples WHERE id = " + index + " ");

      Sample stHis = ((Sample) rs.getObject(0));
      return stHis;
    } catch (Exception ex) {
      System.out.println("SQLException: " + ex.getMessage());
    }
    return null;
  }

  public static Sample getStateHistoryFromDBByDate(java.util.Date date) { // Refactor to date
    Connection c = openConnection();
    try {
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery("SELECT * FROM Samples WHERE date = " + date + " ");

      Sample stHis = ((Sample) rs.getObject(0));
      return stHis;
    } catch (Exception ex) {
      System.out.println("SQLException: " + ex.getMessage());
    }
    return null;
  }

  public static void printDB() {
    Connection c = openConnection();
    try (Statement stmt = c.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM Samples")
    ) {
      while (rs.next()) {
        int numColumns = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= numColumns; i++) {
          // Column numbers start at 1.
          // Also there are many methods on the result set to return
          //  the column as a particular type. Refer to the Sun documentation
          //  for the list of valid conversions.
          System.out.println("COLUMN " + i + " = " + rs.getObject(i));
        }
      }
    } catch (Exception ex) {
      System.out.println("SQLException: " + ex.getMessage());
    }
  }

  /**
   * @return the row index of the Sample in the table
   */
  public int putStateScopeIntoDB(Sample sample) {
    Connection c = openConnection();
    try {
      Statement st = c.createStatement();
      System.out.print(sample.toDBFormatedString(mActionsNum));
      st.executeQuery("INSERT INTO Samples VALUES (" + sample.toDBFormatedString(mActionsNum) +")");
      return 1; //Success
    } catch (Exception ex) {
      System.out.println("SQLException: " + ex.getMessage());
    }
    return 0;
  }

  /**
   * @return the row index of the SensorState in the table
   */
  public static int putSensorStateIntoDB(SensorState state) {
    Connection c = openConnection();
    try {
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery("INSERT INTO Samples VALUES (" + state.toString() + ");");
      return rs.getRow(); // Refactor to succes/status variable
    } catch (Exception ex) {
      System.out.println("SQLException: " + ex.getMessage());
    }
    return 0;
  }
}
