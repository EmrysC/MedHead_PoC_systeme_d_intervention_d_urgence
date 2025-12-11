package MeadHead.Poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import MeadHead.Poc.entites.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}