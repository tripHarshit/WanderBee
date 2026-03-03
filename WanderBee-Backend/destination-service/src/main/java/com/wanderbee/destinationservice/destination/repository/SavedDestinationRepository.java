package com.wanderbee.destinationservice.destination.repository;

import com.wanderbee.destinationservice.destination.entity.SavedDestination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedDestinationRepository extends JpaRepository<SavedDestination, Long> {

    List<SavedDestination> findByUserIdOrderByTimestampDesc(String userId);

    boolean existsByUserIdAndCityId(String userId, String cityId);

    void deleteByUserIdAndCityId(String userId, String cityId);
}
