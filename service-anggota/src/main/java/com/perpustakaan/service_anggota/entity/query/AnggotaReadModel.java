package com.perpustakaan.service_anggota.entity.query;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "anggota_read") // Ganti @Entity dengan @Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaReadModel {

    @Id // Gunakan @Id dari jakarta.persistence atau org.springframework.data.annotation
    private Long id; // Kita tetap pakai Long agar sinkron dengan MySQL/H2 Write

    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}