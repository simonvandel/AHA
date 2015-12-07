package Database;

import Sampler.Sample;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by heider on 07/12/15.
 */
public class HiDB
{
  private static HiDB db = null;

  private static EntityManagerFactory emf = null;

  protected void setUp() throws Exception {
    emf = Persistence.createEntityManagerFactory("ObjectDatabase.odb");
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
      //fuck det
    }
  }

  public void putNewSample(Sample sample)
  {
    EntityManager em = emf.createEntityManager();
    try{
      em.getTransaction().begin();
      em.persist(sample);
      em.getTransaction().commit();
    } catch (Exception ex){
      ex.printStackTrace();
    } finally{
      em.close();
    }
  }

  public List<Sample> getSamples(){
    EntityManager em = emf.createEntityManager();
    List<Sample> result = null;
    try {
      TypedQuery<Sample> query = em.createQuery("SELECT * FROM Sample", Sample.class);
      List<Sample> results = query.getResultList();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
        em.close();
    }
    return result;
  }
}