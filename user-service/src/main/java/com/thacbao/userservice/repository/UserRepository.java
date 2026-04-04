package com.thacbao.userservice.repository;

import com.thacbao.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailSimple(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.emailVerified = true")
    long countActiveVerifiedUsers();

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :id")
    void deleteByIdDirect(@Param("id") Integer id);

    @Query("""
    SELECT u FROM User u LEFT JOIN FETCH u.roles
    WHERE (:keyword IS NULL OR :keyword = ''
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR u.phone LIKE CONCAT('%', :keyword, '%'))
    AND (:provider IS NULL OR :provider = '' OR LOWER(u.provider) = LOWER(:provider))
    AND (:isActive IS NULL OR u.isActive = :isActive)
    ORDER BY u.createdAt DESC
    """)
    Page<User> filterUsers(
            @Param("keyword") String keyword,
            @Param("provider") String provider,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("""
    SELECT u FROM User u LEFT JOIN FETCH u.roles
    WHERE u.isActive = true AND u.emailVerified = true
    """)
    Page<User> findAllActiveUsers(Pageable pageable);

    @Query("""
    SELECT u FROM User u LEFT JOIN FETCH u.roles
    WHERE u.isActive = false
    """)
    Page<User> findAllBlockedUsers(Pageable pageable);

    @Query("""
    SELECT u FROM User u LEFT JOIN FETCH u.roles r
    WHERE r.name = :roleName AND u.isActive = true
    """)
    Page<User> findUsersByRole(@Param("roleName") String roleName, Pageable pageable);
}
