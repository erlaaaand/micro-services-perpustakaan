package com.perpustakaan.service_anggota.entity.command;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data // Lombok: Otomatis bikin Getter, Setter, toString
@NoArgsConstructor // Lombok: Constructor kosong
@AllArgsConstructor // Lombok: Constructor dengan semua argumen
@Table(name = "anggota")
public class Anggota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}