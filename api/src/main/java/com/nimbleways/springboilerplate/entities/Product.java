package com.nimbleways.springboilerplate.entities;

import lombok.*;

import java.time.LocalDate;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "available")
    private Integer available;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ProductType type;

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;

    public boolean isInStock() {
        return available > 0;
    }

    public void decrementStock() {
        available--;
    }

    public void clearStock() {
        available = 0;
    }

    public boolean hasLeadTime() {
        return leadTime > 0;
    }

    public boolean isExpired(LocalDate today) {
        return !expiryDate.isAfter(today);
    }

    /** Season bounds are exclusive: the first and last day of the season are not considered "in season". */
    public boolean isInSeason(LocalDate today) {
        return today.isAfter(seasonStartDate) && today.isBefore(seasonEndDate);
    }

    public boolean isSeasonNotStarted(LocalDate today) {
        return seasonStartDate.isAfter(today);
    }

    public boolean isRestockedAfterSeasonEnd(LocalDate today) {
        return today.plusDays(leadTime).isAfter(seasonEndDate);
    }
}
