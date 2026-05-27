package vn.hoidanit.springrestwithai.qlkh.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByDigiCodeAndPhone(String digiCode, String phone);

    Optional<Customer> findByDigiCode(String digiCode);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.digiCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchByNameOrDigiCode(@Param("keyword") String keyword);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            " OR LOWER(c.digiCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive) " +
            "AND (:includeIds IS NULL OR c.customerId IN :includeIds) " +
            "AND (:excludeIds IS NULL OR c.customerId NOT IN :excludeIds)")
    Page<Customer> findWithFilters(@Param("keyword") String keyword,
                                   @Param("isActive") Short isActive,
                                   @Param("includeIds") List<Integer> includeIds,
                                   @Param("excludeIds") List<Integer> excludeIds,
                                   Pageable pageable);
}
