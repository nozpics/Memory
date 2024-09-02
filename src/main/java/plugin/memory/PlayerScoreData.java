package plugin.memory;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import plugin.memory.mapper.PlayerScoreMapper;
import plugin.memory.mapper.data.PlayerScore;

/**
 * DB接続やそれに付随する登録や更新処理を行うクラス。
 */
public class PlayerScoreData {
  private SqlSessionFactory sqlSessionFactory;
  private PlayerScoreMapper mapper;

  public PlayerScoreData(){
    try{
      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
      this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      SqlSession session = sqlSessionFactory.openSession(true);
      this.mapper = session.getMapper(PlayerScoreMapper.class);
    }catch (Exception e){
      throw  new RuntimeException(e);
    }
  }

  /**
   * プレイヤースコアテーブルから一覧でスコア情報を取得する。
   *
   * @return スコア情報の一覧
   */
  public List<PlayerScore> selectList(){
      return mapper.selectList();
    }


  /**
   * プレイヤースコアテーブルにスコア情報を登録する。
   * @param playerScore プレイヤースコア
   */
  public void insert(PlayerScore playerScore){
      mapper.insert(playerScore);
  }
}
