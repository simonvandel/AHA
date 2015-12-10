package Sampler;

import Communication.SensorValue;
import Database.InstantPersister;
import Database.pInteger;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by heider on 19/11/15.
 */
@DatabaseTable(tableName = "Samples")
public class Sample{
  @DatabaseField(generatedId = true, unique = true)
  private int id;
  @ForeignCollectionField(eager = true)
  private Collection<pInteger> mpStatesHashed = new ArrayList<pInteger>(); //persistable values
  private List<Integer> mStatesHashed = new ArrayList<Integer>();
  @DatabaseField(persisterClass = InstantPersister.class)
  private Instant mTime = null;
  @ForeignCollectionField(eager = true)
  private Collection<Action> mpActions = new ArrayList<Action>(); //persistable values
  private List<Action> mActions = new ArrayList<Action>();

  public Sample(List<NormalizedSensorState> states, Instant time, List<Action> actions) {
    for (int i = 0; i <= states.size() - 1; i++) {
      NormalizedSensorState e = states.get(i);
      if (e == null) {
        mStatesHashed.add(null);
      }
      else {
        mStatesHashed.add(e.hashCode());
        mpStatesHashed.add(new pInteger(e.hashCode()));
      }

    }
    mTime = time;
    mActions = actions;
    mpActions = actions;
  }

  Sample(){}

  public List<Integer> getHash() {
    return mStatesHashed;
  }

  public Instant getTime() {
    return mTime;
  }

  public List<Action> getActions() {
    return mActions;
  }

  public String toDBFormatedString(int actionNum) {
    String string = "";
    for (int i = 0; i <= mStatesHashed.size() - 1; i++) {
      if (i == 0)
        string = mStatesHashed.get(i).toString();
      else
        string = string + "," + mStatesHashed.get(i).toString();
    }
    string = string + "," + mTime;
    List<Action> acs = new ArrayList<Action>();
    for (int i = 0; i<actionNum; i++){
      if(i<mActions.size())
        acs.add(mActions.get(i));
      else
        acs.add(new Action(new NormalizedValue(0,false,"NAD",0),new NormalizedValue(0,false,"NAD",0),0));
    }
    for (int i = 0; i<actionNum; i++){
      string = string + "," + acs.get(i).toString();
    }
    return string;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Sample sample = (Sample) o;

    return mStatesHashed.equals(sample.mStatesHashed);

  }

  @Override
  public int hashCode()
  {
    int hashCode = mStatesHashed.hashCode();
    return hashCode;
  }
}
