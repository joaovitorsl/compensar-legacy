package springboot.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import springboot.model.QuestaoSubjetiva;

@Repository
public interface QuestaoSubjetivaRepository extends JpaRepository<QuestaoSubjetiva, Long>{

}
