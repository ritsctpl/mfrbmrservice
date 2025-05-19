package com.rits.userservice.repository;

import com.rits.userservice.dto.AllUser;
import com.rits.userservice.dto.AvailableUserGroup;
import com.rits.userservice.dto.AvailableUserGroups;
import com.rits.userservice.dto.UserResponse;
import com.rits.userservice.model.User;
import com.rits.userservice.model.WorkCenter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Boolean existsByUserAndActiveEquals(String user,int active);

    Boolean existsByUserAndSiteAndActiveEquals(String user, String site, int active);

    //User findByUserAndActiveEquals(String user, int active);

    @Query("{ 'user': ?0, 'active': ?1 }")
    User findFirstByUserAndActiveEquals(String user, int active);

    User findFirstByUserAndCurrentSiteAndActiveEquals(String user, String currentSite, int active);

    List<UserResponse> findTop50ByActiveOrderByCreatedDateTimeDesc(int active);

    List<UserResponse> findByActiveAndUserContainingIgnoreCase(int active, String user);

    List<AllUser> findByActiveEquals( int active);

    List<User> findByUserAndActiveEquals(String user, int i);
    List<User> findByUserAndActive(String user, int active);
    List<User> findByActive( int active);

    List<User> findByCurrentSiteAndActive(String site, int active);

    List<AvailableUserGroups> findByActiveAndSite(int i, String site);
}
