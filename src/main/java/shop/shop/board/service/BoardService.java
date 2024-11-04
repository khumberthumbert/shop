package shop.shop.board.service;

import shop.shop.board.entity.Board;
import shop.shop.user.entity.UserEntity;

public interface BoardService {

    public Board findBoardByID(int userId);

    public int saveBoard(int userId, Board board);

    public int updateBoard(int boardId, Board board);

    public void deleteBoard(int boardId);
}
