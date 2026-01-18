package com.perpustakaan.service_peminjaman.cqrs.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePeminjamanCommand {
    private UUID id;
    private UUID anggotaId;
    private UUID bukuId;
    private String tanggalPinjam;
    private String tanggalKembali;
    private String status;
}