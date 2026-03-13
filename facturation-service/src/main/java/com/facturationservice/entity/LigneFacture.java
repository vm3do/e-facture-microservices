package com.facturationservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lignes_facture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    /** Prix capturé au moment de la création de la facture */
    @Column(nullable = false)
    private double unitPrice;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;
}

