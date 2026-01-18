package com.perpustakaan.service_anggota.cqrs.command;

import java.util.UUID;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class UpdateAnggotaCommand {
    private final UUID id;
    private final String nomorAnggota;
    private final String nama;
    private final String alamat;
    private final String email;
}
