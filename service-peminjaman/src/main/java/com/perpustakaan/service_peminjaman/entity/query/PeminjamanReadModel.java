package com.perpustakaan.service_peminjaman.entity.query;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "peminjaman_read")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeminjamanReadModel {
    @Id
    private Long id;
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam;
    private String tanggalKembali;
    private String status;
}