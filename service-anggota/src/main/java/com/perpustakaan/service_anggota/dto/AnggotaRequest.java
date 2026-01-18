package com.perpustakaan.service_anggota.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data payload untuk mendaftarkan atau mengupdate anggota")
public class AnggotaRequest {
    
    @NotBlank(message = "Nomor anggota wajib diisi")
    @Size(min = 3, max = 20, message = "Panjang nomor anggota harus antara 3-20 karakter")
    @Schema(
        description = "Nomor identitas unik anggota (misal: NIM/NIP)",
        example = "ANG-2024-001", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nomorAnggota;
    
    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(min = 3, max = 100, message = "Panjang nama harus antara 3-100 karakter")
    @Schema(
        description = "Nama lengkap sesuai KTP",
        example = "Muhammad Erland", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nama;
    
    @NotBlank(message = "Alamat wajib diisi")
    @Size(max = 255, message = "Alamat tidak boleh lebih dari 255 karakter")
    @Schema(
        description = "Alamat domisili anggota",
        example = "Jl. Sudirman No. 45, Padang", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String alamat;
    
    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid (contoh: user@domain.com)")
    @Schema(
        description = "Email aktif untuk notifikasi",
        example = "erland@university.ac.id", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;
}