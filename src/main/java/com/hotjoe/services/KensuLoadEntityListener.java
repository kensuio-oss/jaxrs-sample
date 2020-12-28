package com.hotjoe.services;

import java.util.logging.Logger;
import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;

import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.event.spi.*;
import org.hibernate.type.Type;

public class KensuLoadEntityListener extends EmptyInterceptor
        implements PostLoadEventListener, InitializeCollectionEventListener {
    static final Logger logger = Logger.getLogger(KensuLoadEntityListener.class.getName());

    private static final long serialVersionUID = 109785463523256789L;

    // PostLoadEventListener

    @Override
    public void onPostLoad(PostLoadEvent event) {
        logger.log(Level.WARNING, "On POST LOAD HIBERNATE:" + event.toString());
    }

    // InitializeCollectionEventListener

    @Override
    public void onInitializeCollection(InitializeCollectionEvent event) throws HibernateException {
        logger.log(Level.WARNING, "On INIT COLL HIBERNATE:" + event.toString());
    }

    // Interceptor

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
            throws CallbackException {
        logger.log(Level.WARNING, "io.opentracing.util.GlobalTracer.get().activeSpan() HIBERNATE:" + io.opentracing.util.GlobalTracer.get().activeSpan());
        logger.log(Level.WARNING, "onLoad HIBERNATE:" + propertyNames + " " + types);
        return false; // we don't modify the state
    }

    @Override
    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
        logger.log(Level.WARNING, "onCollectionRecreate HIBERNATE:" + collection);
    }

    @Override
    public String onPrepareStatement(String sql) {
        logger.log(Level.WARNING, "onPrepareStatement HIBERNATE:" + sql);
        return sql;
    }
}