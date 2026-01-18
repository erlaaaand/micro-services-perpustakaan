package com.perpustakaan.service_pengembalian.entity.query;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pengembalian_read")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PengembalianReadModel {
    @Id
    private String id;
    private String peminjamanId;
    private String tanggalDikembalikan;
    private int terlambat;
    private double denda;
}