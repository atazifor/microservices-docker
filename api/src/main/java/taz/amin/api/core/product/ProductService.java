package taz.amin.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductService {
    /**
     * ex usage: "curl $HOST:$PORT/product/1"
     * @param productId product id of the product
     * @return the product if found, else null
     */
    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Product getProduct(@PathVariable int productId);
}
