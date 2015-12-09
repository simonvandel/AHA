package Sampler;

import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by heider on 19/11/15.
 */
@DatabaseTable(tableName = "Samples")
public class Sample {
  @DatabaseField(generatedId = true, unique = true)
  private int id;

  private List<Integer> mStatesHashed = new ArrayList<Integer>();

  private Instant mTime = null;

  private List<Action> mActions = new ArrayList<Action>();

  public Sample(List<NormalizedSensorState> states, Instant time, List<Action> actions) {
    for (int i = 0; i <= states.size() - 1; i++) {
      mStatesHashed.add(states.get(i).hashCode());
    }
    mTime = time;
    mActions = actions;
  }

  private Sample(){}

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
    return mStatesHashed.hashCode();
  }
}
