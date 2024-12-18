package shop.shop.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shop.shop.board.entity.Board;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {

    @Query("select p from Board p where p.user.id = :id")
    Page<Board> findByUserId(Pageable pageable, int id);

    @Query("select p from Board p join fetch p.user")
    Page<Board> findALl(Pageable pageable);

    @Query("select p from Board p join fetch p.user order by p.id asc")
    List<Board> findAllPostList();

    @Query("select p from Board p join fetch p.user pm where p.id = :id")
    Board findOnePostById(int id);
}
