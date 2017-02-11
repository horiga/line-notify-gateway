package org.horiga.linenotifygateway.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.horiga.linenotifygateway.entity.TokenEntity;

@Mapper
public interface TokenRepository {

    @SuppressWarnings("unused")
    @Select("SELECT * FROM token")
    List<TokenEntity> findAll();

    @SuppressWarnings("unused")
    @Select("SELECT * FROM token WHERE id = #{id}")
    TokenEntity findById(@Param("id") String id);

    @Select("SELECT * FROM token WHERE service = #{serviceId}")
    List<TokenEntity> findByServiceId(@Param("serviceId") String id);

    @Select("SELECT token FROM token WHERE service = #{serviceId}")
    List<String> getAccessTokenList(@Param("serviceId") String id);

    @Insert("INSERT INTO token(`id`, `service`, `token`, `description`, `owner`) "
            + "VALUES(#{id}, #{service}, #{token}, #{description}, #{owner})")
    void insert(TokenEntity entity);

    @Delete("DELETE FROM token WHERE id = #{id} AND service = #{sid}")
    void delete(@Param("id") String id, @Param("sid") String sid);

    @Delete("DELETE FROM token WHERE service = #{sid}")
    void deleteWithServiceId(@Param("sid") String sid);

}
