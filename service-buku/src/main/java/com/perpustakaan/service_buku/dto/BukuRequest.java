package com.perpustakaan.service_buku.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data payload untuk manajemen buku perpustakaan")
public class BukuRequest {

    @NotBlank(message = "Kode buku wajib diisi")
    @Size(min = 3, max = 20, message = "Kode buku harus antara 3-20 karakter")
    @Schema(
        description = "Kode unik inventaris buku",
        example = "B-TI-001", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String kodeBuku;

    @NotBlank(message = "Judul buku wajib diisi")
    @Size(max = 255, message = "Judul buku maksimal 255 karakter")
    @Schema(
        description = "Judul lengkap buku",
        example = "Clean Architecture: A Craftsman's Guide to Software Structure", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String judul;

    @NotBlank(message = "Pengarang wajib diisi")
    @Schema(description = "Nama pengarang/penulis", example = "Robert C. Martin")
    private String pengarang;

    @NotBlank(message = "Penerbit wajib diisi")
    @Schema(description = "Nama penerbit buku", example = "Prentice Hall")
    private String penerbit;

    @NotNull(message = "Tahun terbit wajib diisi")
    @Min(value = 1900, message = "Tahun terbit tidak valid (minimal 1900)")
    @Schema(description = "Tahun buku diterbitkan", example = "2017")
    private Integer tahunTerbit;

}