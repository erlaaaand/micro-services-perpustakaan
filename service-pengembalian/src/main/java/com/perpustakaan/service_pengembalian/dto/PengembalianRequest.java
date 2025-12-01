package com.perpustakaan.service_pengembalian.dto;
import lombok.Data;

@Data
public class PengembalianRequest {
    private Long peminjamanId;
    private String tanggalDikembalikan;
    private int terlambat;
    private double denda;
}