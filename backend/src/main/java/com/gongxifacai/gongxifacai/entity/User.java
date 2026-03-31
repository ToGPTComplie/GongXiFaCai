package com.gongxifacai.gongxifacai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @DecimalMin(value = "0.00", inclusive = true, message = "可用现金必须大于0")
    @Column(name = "available_cash", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableCash;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Holding> holdings;

}
