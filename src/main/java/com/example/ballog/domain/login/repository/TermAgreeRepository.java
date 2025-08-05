package com.example.ballog.domain.login.repository;

import com.example.ballog.domain.login.entity.TermAgree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermAgreeRepository extends JpaRepository<TermAgree, Long> {
    List<TermAgree> findAllByUserUserId(Long userId);
    void deleteAllByUserUserId(Long userId);
}
