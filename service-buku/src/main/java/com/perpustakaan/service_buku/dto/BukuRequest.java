// BukuRequest.java
package com.perpustakaan.service_buku.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BukuRequest {
    
    @NotBlank(message = "Kode buku tidak boleh kosong")
    @Size(min = 3, max = 20, message = "Kode buku harus antara 3-20 karakter")
    private String kodeBuku;
    
    @NotBlank(message = "Judul tidak boleh kosong")
    @Size(min = 1, max = 200, message = "Judul maksimal 200 karakter")
    private String judul;
    
    @NotBlank(message = "Pengarang tidak boleh kosong")
    @Size(min = 1, max = 100, message = "Pengarang maksimal 100 karakter")
    private String pengarang;
    
    @Size(max = 100, message = "Penerbit maksimal 100 karakter")
    private String penerbit;
    
    @Min(value = 1900, message = "Tahun terbit harus setelah 1900")
    private Integer tahunTerbit;
}