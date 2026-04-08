package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.entity.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByDigiCodeAndPhone(String digiCode, String phone);

    Optional<Customer> findByDigiCode(String digiCode);
}
