package com.mediq.repository;

import com.mediq.constants.UserRole;
import com.mediq.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMemberIdAndIsDeletedFalse(String memberId);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    boolean existsByMemberIdAndIsDeletedFalse(String memberId);

    boolean existsByEmailAndIsDeletedFalse(String email);

    List<User> findByRoleAndIsActiveTrueAndIsDeletedFalse(UserRole role);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.memberId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsers(@Param("role") UserRole role, @Param("keyword") String keyword, Pageable pageable);
}
