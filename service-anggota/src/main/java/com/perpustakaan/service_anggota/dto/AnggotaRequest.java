package com.perpustakaan.service_anggota.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnggotaRequest {
    
    @NotBlank(message = "Nomor anggota tidak boleh kosong")
    @Size(min = 3, max = 20, message = "Nomor anggota harus antara 3-20 karakter")
    private String nomorAnggota;
    
    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama harus antara 3-100 karakter")
    private String nama;
    
    @NotBlank(message = "Alamat tidak boleh kosong")
    @Size(max = 255, message = "Alamat maksimal 255 karakter")
    private String alamat;
    
    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;
}