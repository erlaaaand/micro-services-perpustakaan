package com.perpustakaan.service_anggota.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DeleteAnggotaCommand.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAnggotaCommand {
    private Long id;
}
