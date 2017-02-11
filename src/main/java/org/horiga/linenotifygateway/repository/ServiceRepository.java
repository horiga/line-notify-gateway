package org.horiga.linenotifygateway.repository;

import java.util.Collection;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.horiga.linenotifygateway.entity.ServiceEntity;

@SuppressWarnings("unused")
@Mapper
public interface ServiceRepository {

    @Select("SELECT * FROM service WHERE service = #{service}")
    ServiceEntity findById(@Param("service") String service);

    @Select("SELECT * FROM service")
    Collection<ServiceEntity> findAll();

    @Insert("INSERT INTO service("
            + "`service`, `display_name`, `type` `template_group_id`, "
            + "`template_mapping_type`, `template_mapping_value`, `description`) "
            + " VALUES ("
            + "#{service}, #{displayName}, #{type}, #{templateGroupId}, "
            + "#{templateMappingType}, #{templateMappingValue}, #{description})")
    void insert(ServiceEntity entity);

    @Delete("DELETE FROM service WHERE service = #{service}")
    void delete(@Param("service") String service);

    @Update("UPDATE service "
            + "SET "
            + "`type` = #{type}, "
            + "`messageTemplateGroupId` = #{messageTemplateGroupId}, "
            + "`description` = #{description} "
            + "WHERE service = #{service}")
    void update(ServiceEntity entity);

}
