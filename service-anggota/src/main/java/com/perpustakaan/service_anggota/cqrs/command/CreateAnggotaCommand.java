package com.perpustakaan.service_anggota.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAnggotaCommand {
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}