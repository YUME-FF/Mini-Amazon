package ece568.amazon;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import java.util.List;

public class OrderDAO {
    OrderDAO() {
    }

    /**
     * update order, save it to database
     * @param order an order in detached state
     */
    public void updateOrder(Order order) {
        Transaction transaction = null;
        Session session = null; 
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            session.merge(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
              if (session != null) {
                session.close();
              }
        }
    }

    // public List<Order> getAllOrders() {
    // try (Session session = HibernateUtil.getSession()) {
    // Query<Order> query = session.createQuery("from Order", Order.class);
    // return query.list();
    // }
    // }

    /**
     * Retrieve order by shipID
     * 
     * @param shipID
     * @return
     */
    public Order getOrder(Long shipID) {
        Transaction tx = null;
        Session session = null;
        Order order = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            order = session.get(Order.class, shipID);
            tx.commit();
            //session.close();
            return order;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null)
                session.close();
        }
        return order;
    }

    /**
     * create order with warehouse ID
     * 
     * @param whID
     */
    public void createOrder(Integer whID) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        Order order = new Order();
        order.setWhID(whID);
        session.persist(order);
        tx.commit();
        session.close();
    }


    public void deleteOrder(Order order) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSession()) {
            transaction = session.beginTransaction();
            // Order order = session.get(Order.class, shipID);
            if (order != null) {
                session.remove(order);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
