package com.perpustakaan.service_buku.exception; // Sesuaikan package

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Anotasi ini otomatis mengubah status HTTP jadi 404 NOT FOUND
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}