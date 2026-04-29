package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.entity.Customer;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByDigiCodeAndPhone(String digiCode, String phone);

    Optional<Customer> findByDigiCode(String digiCode);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.digiCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchByNameOrDigiCode(@Param("keyword") String keyword);
}
