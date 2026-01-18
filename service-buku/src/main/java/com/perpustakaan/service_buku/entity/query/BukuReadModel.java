package com.perpustakaan.service_buku.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "buku_read")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BukuReadModel {

    @Id
    private String id;

    private String kodeBuku;
    private String judul;
    private String pengarang;
    private String penerbit;
    private Integer tahunTerbit;
}