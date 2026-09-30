package cl.duoc.pixelrig.repository;

import cl.duoc.pixelrig.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}