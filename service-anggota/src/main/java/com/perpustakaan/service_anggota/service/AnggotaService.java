package com.perpustakaan.service_anggota.service;

import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import com.perpustakaan.service_anggota.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.perpustakaan.service_anggota.dto.AnggotaRequest;

@Service
public class AnggotaService {

    @Autowired
    private AnggotaRepository anggotaRepository;

    public Anggota saveAnggota(AnggotaRequest request) {
        Anggota anggota = new Anggota();
        anggota.setNomorAnggota(request.getNomorAnggota());
        anggota.setNama(request.getNama());
        anggota.setAlamat(request.getAlamat());
        anggota.setEmail(request.getEmail());
        
        return anggotaRepository.save(anggota);
    }

    public Anggota getAnggotaById(Long id) {
        return anggotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan ID " + id + " tidak ditemukan!"));
    }

    public List<Anggota> getAllAnggota() {
        return anggotaRepository.findAll();
    }

    public Anggota updateAnggota(Long id, AnggotaRequest request) {
        Anggota anggota = anggotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gagal Update: Anggota dengan ID " + id + " tidak ditemukan!"));

        anggota.setNomorAnggota(request.getNomorAnggota());
        anggota.setNama(request.getNama());
        anggota.setAlamat(request.getAlamat());
        anggota.setEmail(request.getEmail());
        
        return anggotaRepository.save(anggota);
    }

    public void deleteAnggota(Long id) {
        if (!anggotaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gagal Hapus: Anggota dengan ID " + id + " tidak ditemukan!");
        }
        
        anggotaRepository.deleteById(id);
    }
}