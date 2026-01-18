package com.perpustakaan.service_anggota.entity.command;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "anggota")
public class AnggotaWriteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nomor_anggota", unique = true, nullable = false)
    private String nomorAnggota;

    @Column(nullable = false)
    private String nama;

    private String alamat;

    @Column(unique = true)
    private String email;
}