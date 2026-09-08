package com.example.saas.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.saas.domain.Category;
import com.example.saas.domain.Product;
import com.example.saas.dto.ProductRequest;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.ProductMapper;
import com.example.saas.repository.CategoryRepository;
import com.example.saas.repository.ProductRepository;
import com.example.saas.tenant.TenantProvider;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

/** Tests unitaires ciblant la règle la plus importante : l'isolation par tenant. */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock ProductRepository repository;
    @Mock TenantProvider tenantProvider;
    @Mock CategoryRepository categories;
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
                new ProductServiceImpl(repository, new ProductMapper(), tenantProvider, categories);
        when(tenantProvider.currentTenant()).thenReturn("tenant-a");
    }

    @Test
    void shouldListOnlyProductsOfAuthenticatedTenant() {
        when(repository.findAllByTenantIdOrderByName("tenant-a"))
                .thenReturn(
                        List.of(
                                new Product(
                                        "tenant-a",
                                        "Clavier",
                                        new BigDecimal("79.90"),
                                        5,
                                        "https://example.com/a.jpg",
                                        new Category("tenant-a", "Tech"))));

        assertThat(service.findAll()).extracting("name").containsExactly("Clavier");
        verify(repository).findAllByTenantIdOrderByName("tenant-a");
        verify(repository, never()).findAll();
    }

        @Test
        void shouldSearchProductsOnlyInsideAuthenticatedTenant() {
                when(repository.searchByTenantId("tenant-a", "clavier"))
                                .thenReturn(
                                                List.of(
                                                                new Product(
                                                                                "tenant-a",
                                                                                "Clavier",
                                                                                new BigDecimal("79.90"),
                                                                                5,
                                                                                "https://example.com/a.jpg",
                                                                                new Category("tenant-a", "Tech"))));

                assertThat(service.findAll(" clavier ")).extracting("name").containsExactly("Clavier");
                verify(repository).searchByTenantId("tenant-a", "clavier");
                verify(repository, never()).searchByTenantId("tenant-b", "clavier");
        }

    @Test
    void shouldCreateProductWithAuthenticatedTenant() {
        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UUID categoryId = UUID.randomUUID();
        when(categories.findByIdAndTenantId(categoryId, "tenant-a"))
                .thenReturn(Optional.of(new Category("tenant-a", "Tech")));
        service.create(
                new ProductRequest(
                        "Souris",
                        new BigDecimal("39.90"),
                        10,
                        "https://example.com/souris.jpg",
                        categoryId));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo("tenant-a");
    }

    @Test
    void shouldHideProductBelongingToAnotherTenant() {
        UUID foreignId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(foreignId, "tenant-a")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(foreignId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository).findByIdAndTenantId(foreignId, "tenant-a");
        verify(repository, never()).findById(foreignId);
    }
}
