package com.perpustakaan.service_anggota.entity.query;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "anggota_read")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaReadModel {

    @Id
    private String id;

    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}