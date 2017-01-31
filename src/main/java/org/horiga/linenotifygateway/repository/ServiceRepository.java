package org.horiga.linenotifygateway.repository;

import java.util.Collection;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.horiga.linenotifygateway.model.ServiceEntity;

@Mapper
public interface ServiceRepository {

    @Select("SELECT * FROM service WHERE service = #{service}")
    ServiceEntity findById(@Param("service") String service);

    @Select("SELECT * FROM service")
    Collection<ServiceEntity> findAll();

    @Insert("INSERT INTO service(`service`, `description`) "
            + "VALUES (#{service}, #{description})")
    void insert(ServiceEntity entity);

    @Delete("DELETE FROM service WHERE service = #{service}")
    void delete(@Param("service") String service);
}
