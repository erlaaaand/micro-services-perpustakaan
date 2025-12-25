package com.perpustakaan.service_peminjaman.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PeminjamanRequest {

    @NotNull(message = "Anggota ID tidak boleh kosong")
    private Long anggotaId;

    @NotNull(message = "Buku ID tidak boleh kosong")
    private Long bukuId;

    @NotBlank(message = "Tanggal pinjam tidak boleh kosong")
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "Format tanggal pinjam harus yyyy-MM-dd"
    )
    private String tanggalPinjam;

    @NotBlank(message = "Tanggal kembali tidak boleh kosong")
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "Format tanggal kembali harus yyyy-MM-dd"
    )
    private String tanggalKembali;

    @NotBlank(message = "Status tidak boleh kosong")
    @Size(max = 20, message = "Status maksimal 20 karakter")
    private String status;

    // opsional
    @Size(max = 255, message = "Keterangan maksimal 255 karakter")
    private String keterangan;
}
