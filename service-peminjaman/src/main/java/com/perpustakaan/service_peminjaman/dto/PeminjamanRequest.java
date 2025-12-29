package com.perpustakaan.service_peminjaman.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PeminjamanRequest {

    @NotNull(message = "Anggota ID tidak boleh kosong")
    @Schema(description = "ID Anggota yang meminjam buku", example = "1")
    private Long anggotaId;

    @NotNull(message = "Buku ID tidak boleh kosong")
    @Schema(description = "ID Buku yang dipinjam", example = "1")
    private Long bukuId;

    @NotBlank(message = "Tanggal pinjam tidak boleh kosong")
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "Format tanggal pinjam harus yyyy-MM-dd"
    )
    @Schema(description = "Tanggal pinjam dalam format yyyy-MM-dd", example = "2023-01-01")
    private String tanggalPinjam;

    @NotBlank(message = "Tanggal kembali tidak boleh kosong")
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "Format tanggal kembali harus yyyy-MM-dd"
    )
    @Schema(description = "Tanggal kembali dalam format yyyy-MM-dd", example = "2023-01-10")
    private String tanggalKembali;

    @NotBlank(message = "Status tidak boleh kosong")
    @Size(max = 20, message = "Status maksimal 20 karakter")
    @Schema(description = "Status peminjaman", example = "DIPINJAM")
    private String status;

    // opsional
    @Size(max = 255, message = "Keterangan maksimal 255 karakter")
    @Schema(description = "Keterangan tambahan", example = "Buku dalam kondisi baik")
    private String keterangan;
}
