package cl.duoc.pixelrig.repository;

import cl.duoc.pixelrig.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
