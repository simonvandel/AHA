package Database;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.DateType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;


/**
 * Created by heider on 09/12/15.
 */
public class InstantPersister extends DateType{

    private static final InstantPersister singleTon = new InstantPersister();
    @SuppressWarnings("deprecation")
    private static final Timestamp ZERO_TIMESTAMP = new Timestamp(1970, 0, 0, 0, 0, 0, 0);

    private InstantPersister() {
      super(SqlType.DATE, new Class<?>[] { Instant.class });
    }

    public static InstantPersister getSingleton() {
      return singleTon;
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException{
      Timestamp timestamp = results.getTimestamp(columnPos);
      if (timestamp == null || ZERO_TIMESTAMP.after(timestamp)) {
        return null;
      } else {
        return timestamp.getTime();
      }
    }

  @Override
  public Object javaToSqlArg(FieldType fieldType, Object javaObject){
    Instant instant = (Instant) javaObject;
    return new Timestamp(instant.toEpochMilli());
  }

  @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
      if (sqlArg == null) {
        return null;
      } else {
        return Instant.ofEpochMilli((Long) sqlArg);
      }
    }
}
