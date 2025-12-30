package com.perpustakaan.service_pengembalian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PengembalianRequest {
    
    @NotNull(message = "ID Peminjaman tidak boleh kosong")
    @Schema(description = "ID Peminjaman", example = "1")
    private Long peminjamanId;
    
    @NotBlank(message = "Tanggal dikembalikan tidak boleh kosong")
    @Schema(description = "Tanggal dikembalikan", example = "2023-10-10")
    private String tanggalDikembalikan;
    
    @Min(value = 0, message = "Keterlambatan tidak boleh kurang dari 0")
    @Schema(description = "Jumlah hari keterlambatan", example = "2")
    private int terlambat;
    
    @Min(value = 0, message = "Denda tidak boleh kurang dari 0")
    @Schema(description = "Jumlah denda", example = "5000.0")
    private double denda;
}