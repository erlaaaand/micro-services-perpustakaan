package com.perpustakaan.service_pengembalian.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pengembalian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long peminjamanId; // Referensi ke ID transaksi peminjaman
    private String tanggalDikembalikan;
    private int terlambat; // Jumlah hari terlambat
    private double denda;  // Total denda
}