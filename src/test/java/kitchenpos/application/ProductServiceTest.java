package kitchenpos.application;

import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(ReplaceUnderscores.class)
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductDao productDao;

    @Test
    void 상품을_생성한다() {
        // given
        Product product = new Product();
        product.setId(1L);
        product.setName("product");
        product.setPrice(new BigDecimal("1000"));

        given(productDao.save(product))
                .willReturn(product);

        // when
        Product result = productService.create(product);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(product.getId());
            softly.assertThat(result.getName()).isEqualTo(product.getName());
            softly.assertThat(result.getPrice()).isEqualTo(product.getPrice());
        });
    }

    @Test
    void 가격이_0원_보다_작은_상품을_생성하면_예외를_던진다() {
        // given
        Product product = new Product();
        product.setName("product");
        product.setPrice(new BigDecimal("-1"));

        // when & then
        assertThatThrownBy(() -> productService.create(product))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상품을_전체_조회한다() {
        // given
        Product product1 = new Product();
        Product product2 = new Product();
        List<Product> expected = List.of(product1, product2);

        given(productDao.findAll())
                .willReturn(expected);

        // when
        List<Product> result = productService.list();

        // then
        assertThat(result).hasSize(2);
    }
}
