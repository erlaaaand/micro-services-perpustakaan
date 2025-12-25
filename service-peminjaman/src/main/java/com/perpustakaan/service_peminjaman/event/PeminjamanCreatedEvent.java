package com.perpustakaan.service_peminjaman.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeminjamanCreatedEvent implements Serializable {
    private Long id;
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam;
    private String status;
}