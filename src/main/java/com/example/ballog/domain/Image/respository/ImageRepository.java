package com.example.ballog.domain.Image.respository;


import com.example.ballog.domain.Image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository  extends JpaRepository<Image, Long> {
}
