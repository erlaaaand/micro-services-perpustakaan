package com.perpustakaan.service_buku.cqrs.command;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBukuCommand {
    private UUID id;
    private String kodeBuku;
    private String judul;
    private String pengarang;
    private String penerbit;
    private Integer tahunTerbit;
}