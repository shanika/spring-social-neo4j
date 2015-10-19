package org.springframework.social.connect.neo4j.repositories;

import org.springframework.social.connect.*;
import org.springframework.social.connect.neo4j.domain.SocialUserConnection;

import java.util.*;

/**
 * Created by SWijerathna on 10/7/2015.
 */
public class Neo4jUserConnectionRepository implements UsersConnectionRepository {

    private SocialUserConnectionRepository repository;

    private ConnectionFactoryLocator connectionFactoryLocator;

    private ConnectionSignUp connectionSignUp;


    public Neo4jUserConnectionRepository(SocialUserConnectionRepository socialUserConnectionRepository, ConnectionFactoryLocator connectionFactoryLocator) {

        this.repository = socialUserConnectionRepository;
        this.connectionFactoryLocator = connectionFactoryLocator;
    }

    @Override
    public List<String> findUserIdsWithConnection(Connection<?> connection) {
        ConnectionKey key = connection.getKey();

        List<SocialUserConnection> dbCons = repository.findByProviderIdAndProviderUserId(key.getProviderId(), key.getProviderUserId());

        if (dbCons.size() == 0 && connectionSignUp != null) {
            String newUserId = connectionSignUp.execute(connection);
            if (newUserId != null)
            {
                createConnectionRepository(newUserId).addConnection(connection);
                return Arrays.asList(newUserId);
            }
        }

        List<String> userIds = new ArrayList<String>();
        for(SocialUserConnection dbCon: dbCons){
            userIds.add(dbCon.userId);
        }
        return userIds;
    }

    @Override
    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {

        final Set<String> localUserIds = new HashSet<String>();
        List<SocialUserConnection> dbCons = repository.findByProviderIdAndProviderUserIdIn(providerId, providerUserIds);
        for(SocialUserConnection dbCon:dbCons) {
            localUserIds.add(dbCon.userId);
        }
        return localUserIds;
    }

    @Override
    public ConnectionRepository createConnectionRepository(String userId) {

        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        return new Neo4jConnectionRepository(userId,repository,connectionFactoryLocator);
    }


}