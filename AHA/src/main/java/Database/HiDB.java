package Database;

import Normaliser.NormalizedSensorState;
import Sampler.Sample;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.List;
import java.util.Properties;

/**
 * Created by heider on 07/12/15.
 */
public class HiDB
{
  private static HiDB db;

  private static SessionFactory sessionFactory = null;
  private static  StandardServiceRegistry registry = null;

  private static SessionFactory configureSessionFactory() throws HibernateException
  {
    registry = new StandardServiceRegistryBuilder().configure().build();
    try {
      sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
    }
    catch (Exception e) {
      // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
      // so destroy it manually.
      StandardServiceRegistryBuilder.destroy( registry );
    }

    return sessionFactory;
  }

  public static HiDB getInstance() {
    if (db == null) {
      db = new HiDB();
    }
    return db;
  }

  private HiDB() {
    // Configure the session factory
    configureSessionFactory();
  }

  public void putNewSample(Sample sample)
  {
    Session session = null;
    Transaction tx = null;
    try
    {
      session = sessionFactory.openSession();
      tx = session.beginTransaction();

      Sample newSample = sample;

      session.save(newSample);
      session.flush();
      tx.commit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
      tx.rollback();
    } finally
    {
      if (session != null)
      {
        session.close();
      }
    }
  }

  public List<Sample> getSamples(){
    List<Sample> result = null;
    Session session = null;
    Transaction tx = null;
    try {
      session = sessionFactory.openSession();
      session.beginTransaction();
      result = session.createQuery( "from Sample" ).list();
      session.getTransaction().commit();
      session.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      tx.rollback();
    } finally {
      if (session != null)
        session.close();
    }
    return result;
  }
}