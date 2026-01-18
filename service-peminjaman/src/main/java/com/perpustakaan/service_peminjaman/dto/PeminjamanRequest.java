package com.perpustakaan.service_peminjaman.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Data payload untuk transaksi peminjaman buku")
public class PeminjamanRequest {

    @NotNull(message = "ID Anggota wajib diisi")
    @Schema(
        description = "UUID Anggota peminjam", 
        example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    )
    private String anggotaId;

    @NotNull(message = "ID Buku wajib diisi")
    @Schema(
        description = "UUID Buku yang akan dipinjam", 
        example = "b1234567-89ab-cdef-0123-456789abcdef"
    )
    private String bukuId;

    @NotBlank(message = "Tanggal pinjam wajib diisi")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Format tanggal harus yyyy-MM-dd")
    @Schema(description = "Tanggal mulai peminjaman", example = "2024-01-20")
    private String tanggalPinjam;

    @NotBlank(message = "Tanggal kembali wajib diisi")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Format tanggal harus yyyy-MM-dd")
    @Schema(description = "Rencana tanggal pengembalian", example = "2024-01-27")
    private String tanggalKembali;

    @NotBlank(message = "Status awal wajib diisi")
    @Pattern(regexp = "^(DIPINJAM|DIKEMBALIKAN)$", message = "Status harus DIPINJAM atau DIKEMBALIKAN")
    @Schema(description = "Status transaksi", example = "DIPINJAM")
    private String status;
}