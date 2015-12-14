package Database;

import Communication.SensorState;
import Communication.SensorValue;
import Normaliser.Sensor;
import Sampler.Action;
import Sampler.Sample;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by heider on 07/12/15.
 */
public class HiDB
{
  private static HiDB db = null;
  private static ConnectionSource connectionSource = null;
  private static Dao<Sample,Integer> samplesDao = null;
  private static Dao<SensorState,Integer> sensorStateDao = null;
  private static Dao<SensorValue,Integer> sensorValueDao = null;
  private static Dao<Action,Integer> actionDao = null;
  private static Dao<pInteger,Integer> pIntegerDao = null;

  protected void setUp() {
    //this uses h2 but you can change it to match your database
    String databaseUrl = "jdbc:sqlite:AHA.db";
    // create a connection source to our database
    try{
      ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);

      // Sampler Dao and database
      samplesDao = DaoManager.createDao(connectionSource, Sample.class);
      TableUtils.createTableIfNotExists(connectionSource, Sample.class);
      // SensorState Dao and database
      sensorStateDao = DaoManager.createDao(connectionSource, SensorState.class);
      TableUtils.createTableIfNotExists(connectionSource, SensorState.class);
      // SensorValue Dao and database
      sensorValueDao = DaoManager.createDao(connectionSource, SensorValue.class);
      TableUtils.createTableIfNotExists(connectionSource, SensorValue.class);
      // Action Dao and database
      actionDao = DaoManager.createDao(connectionSource, Action.class);
      TableUtils.createTableIfNotExists(connectionSource, Action.class);
      // Action Dao and database
      pIntegerDao = DaoManager.createDao(connectionSource, pInteger.class);
      TableUtils.createTableIfNotExists(connectionSource, pInteger.class);
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public static HiDB getInstance() {
    if (db == null) {
      db = new HiDB();
    }
    return db;
  }

  private HiDB() {
    // Configure the session factory
    try{
      setUp();
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void putNewSample(Sample sample)
  {
    try{
      samplesDao.create(sample);
      actionDao.create(sample.getActions().get(0));
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void putNewSensorState(SensorState state)
  {
    try{
      sensorStateDao.create(state);
      for(SensorValue val : state.getValues()){
        sensorValueDao.create(val);
      }
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void putNewSensorValue(SensorValue value)
  {
    try{
      sensorValueDao.create(value);
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public List<SensorValue> getSensorValues(){
    List<SensorValue> result = null;
    try {
      result = sensorValueDao.queryForAll();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }

  public List<SensorState> getSensorStates(){
    List<SensorState> result = null;
    try {
      result = sensorStateDao.queryForAll();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }

  public List<Sample> getSamples(){
    List<Sample> result = null;
    try {
      result = samplesDao.queryForAll();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }



  @Override
  protected void finalize() throws Throwable{
    connectionSource.close();
    super.finalize();
  }
}