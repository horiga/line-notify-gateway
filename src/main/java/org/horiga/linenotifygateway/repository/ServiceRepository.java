package org.horiga.linenotifygateway.repository;

import java.util.List;

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

    @Select("SELECT * FROM service WHERE service_id = #{serviceId}")
    ServiceEntity findById(@Param("serviceId") String serviceId);

    @Select("SELECT * FROM service")
    List<ServiceEntity> findAll();

    @Insert("INSERT INTO service("
            + "`service_id`, `display_name`, `type` `template_group_id`, "
            + "`template_mapping_type`, `template_mapping_value`, `description`) "
            + " VALUES ("
            + "#{service}, #{displayName}, #{type}, #{templateGroupId}, "
            + "#{templateMappingType}, #{templateMappingValue}, #{description})")
    void insert(ServiceEntity entity);

    @Delete("DELETE FROM service WHERE service_id = #{serviceId}")
    void delete(@Param("serviceId") String serviceId);

    @Update("UPDATE service"
            + " SET "
            + " `template_group_id` = #{templateGroupId},"
            + " `template_mapping_type` = #{templateMappingType},"
            + " `template_mapping_value` = #{templateMappingValue}"
            + " WHERE service_id = #{serviceId}")
    void updateTemplateGroupConditions(ServiceEntity entity);

}
