package org.horiga.linenotifygateway.repository;

import java.util.Collection;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.horiga.linenotifygateway.model.TokenEntity;

@Mapper
public interface TokenRepository {

    @Select("SELECT * FROM token WHERE id = #{id}")
    TokenEntity findById(@Param("id") String id);

    @Select("SELECT * FROM token WHERE service = #{serviceId}")
    Collection<TokenEntity> findByServiceId(@Param("serviceId") String id);

    @Insert("INSERT INTO token(`id`, `service`, `token`, `description`, `owner`) "
            + "VALUES(#{id}, #{service}, #{token}, #{description}, #{owner})")
    void insert(TokenEntity entity);

    @Insert("DELETE FROM token WHERE id = #{id}")
    void delete(@Param("id") String id);
}
