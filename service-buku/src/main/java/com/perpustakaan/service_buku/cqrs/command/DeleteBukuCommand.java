package com.perpustakaan.service_buku.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DeleteAnggotaCommand.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteBukuCommand {
    private Long id;
}
