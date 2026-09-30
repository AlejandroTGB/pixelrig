package cl.duoc.pixelrig.service;

import cl.duoc.pixelrig.entity.ContactMessage;
import cl.duoc.pixelrig.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessage enviar(ContactMessage mensaje) {
        mensaje.setId(null);
        mensaje.setFecha(null);
        return contactMessageRepository.save(mensaje);
    }

    @Transactional(readOnly = true)
    public List<ContactMessage> listar() {
        return contactMessageRepository.findAll();
    }
}
