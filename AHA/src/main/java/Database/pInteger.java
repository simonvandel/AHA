package Database;

import Communication.SensorState;
import Sampler.Sample;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by heider on 10/12/15.
 */
@DatabaseTable(tableName = "pIntegers")
public class pInteger{
  @DatabaseField(foreign = true, foreignAutoCreate=true)
  private Sample dbs;
  @DatabaseField
  int mn;

  public pInteger(Integer n){
    mn = n;
  }

  pInteger(){}
}
