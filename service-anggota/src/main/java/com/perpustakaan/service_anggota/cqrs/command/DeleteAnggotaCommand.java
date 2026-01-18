package com.perpustakaan.service_anggota.cqrs.command;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteAnggotaCommand {
    private final UUID id;
}
