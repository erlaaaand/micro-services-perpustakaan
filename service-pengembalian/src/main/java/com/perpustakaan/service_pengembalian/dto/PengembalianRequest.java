package com.perpustakaan.service_pengembalian.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PengembalianRequest {
    
    @NotNull(message = "ID Peminjaman tidak boleh kosong")
    private Long peminjamanId;
    
    @NotBlank(message = "Tanggal dikembalikan tidak boleh kosong")
    private String tanggalDikembalikan;
    
    @Min(value = 0, message = "Keterlambatan tidak boleh kurang dari 0")
    private int terlambat;
    
    @Min(value = 0, message = "Denda tidak boleh kurang dari 0")
    private double denda;
}