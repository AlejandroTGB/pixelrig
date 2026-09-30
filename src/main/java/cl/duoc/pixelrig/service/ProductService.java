package cl.duoc.pixelrig.service;

import cl.duoc.pixelrig.entity.Product;
import cl.duoc.pixelrig.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> listar() {
        return productRepository.findAll();
    }

    public Product crear(Product producto) {
        producto.setId(null);
        return productRepository.save(producto);
    }

    public Product actualizar(Long id, Product datos) {
        Product producto = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));

        producto.setNombre(datos.getNombre());
        producto.setMarca(datos.getMarca());
        producto.setDescripcion(datos.getDescripcion());
        producto.setCategoria(datos.getCategoria());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setImagenUrl(datos.getImagenUrl());

        return productRepository.save(producto);
    }

    public void eliminar(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Producto no encontrado: " + id);
        }
        productRepository.deleteById(id);
    }
}