package org.horiga.linenotifygateway.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.horiga.linenotifygateway.entity.TemplateEntity;
import org.horiga.linenotifygateway.entity.TemplateGroupEntity;

@SuppressWarnings("unused")
@Mapper
public interface TemplateRepository {

    @Select("SELECT * FROM template_group")
    List<TemplateGroupEntity> findGroups();

    @Select("SELECT * FROM template "
            + " WHERE group_id = #{groupId}")
    List<TemplateEntity> findTemplateByGroup(@Param("groupId") String groupId);

    @Select("SELECT template FROM template "
            + " WHERE group_id = #{groupId} "
            + " AND mapping_value = #{mappingValue}")
    String getTemplate(@Param("groupId") String groupId,
                       @Param("mappingValue") String mappingValue);

    @Insert("INSERT INTO template_group(`group_id`, `display_name`, `description`) "
            + "VALUES(#{groupId}, #{displayName}, #{description})")
    void addTemplateGroup(TemplateGroupEntity group);

    @Insert("INSERT INTO "
            + "template(`id`, `group_id`, `mapping_value`, `description`, `content`) "
            + "VALUES(#{id}, #{groupId}, #{mappingValue}, #{description}, #{content})")
    void addTemplate(TemplateEntity message);

    @Update("UPDATE template "
            + "SET "
            + " `content` = #{content},"
            + " `description` = #{description}, "
            + " `content` = #{content} "
            + " WHERE `id` = #{id}")
    void updateTemplate(@Param("id") String id,
                        @Param("description") String description,
                        @Param("content") String content);

    @Update("UPDATE template_group "
            + " SET "
            + " `display_name` = #{displayName}, "
            + " `description` = #{description} "
            + " WHERE `group_id` = #{groupId}")
    void updateTemplateGroup(TemplateGroupEntity group);
}
