package com.hotjoe.services.listener;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class KensuHibernateInterceptor implements Integrator {

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        MetadataAccessor.INSTANCE.addMetadata(metadata);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }
    
    public static class MetadataAccessor {

        public static MetadataAccessor INSTANCE = new MetadataAccessor(new ArrayList<>());

        private List<Metadata> metadataList;

        public MetadataAccessor(List<Metadata> metadataList) {
            this.metadataList = metadataList;
        }

        public void addMetadata(Metadata md) {
            this.metadataList.add(md);
        }

        public List<Metadata> getMetadataList() {
            return metadataList;
        }

        public void setMetadataList(List<Metadata> metadataList) {
            this.metadataList = metadataList;
        }
    }
}
