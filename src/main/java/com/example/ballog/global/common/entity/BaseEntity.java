package com.example.ballog.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
public class BaseEntity {
    @CreatedDate
    @Column(name = "created_dt", nullable = false, updatable = false)
    protected LocalDateTime createdDt;

    @Column(name = "created_by", updatable = false)
    protected Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_dt")
    protected LocalDateTime updatedDt;

    @Column(name = "updated_by")
    protected Long updatedBy;

    @Column(name = "deleted_dt")
    protected LocalDateTime deletedDt;

    @Column(name = "deleted_by")
    protected Long deletedBy;

    @Column(name = "del_yn", columnDefinition = "VARCHAR(1) default 'N'")
    public String delYn;

    @PrePersist
    public void prePersist(){
        this.delYn = "N";
    }
    public void delete(){
        this.delYn = "Y";
    }
}