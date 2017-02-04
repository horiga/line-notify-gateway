package org.horiga.linenotifygateway.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.horiga.linenotifygateway.entity.MessageTemplate;
import org.horiga.linenotifygateway.entity.MessageTemplateGroup;

@Mapper
public interface MessageTemplateRepository {

    @Select("SELECT * FROM message_template_group")
    List<MessageTemplateGroup> findGroups();

    @Select("SELECT * FROM message_template "
            + "WHERE group_id = #{groupId}")
    List<MessageTemplate> findTemplateByGroup(@Param("groupId") String groupId);

    @Select("SELECT template FROM message_template "
            + "WHERE group_id = #{groupId} AND #{eventKey}")
    String getTemplate(@Param("groupId") String groupId, @Param("eventKey") String eventKey);

    @Insert("INSERT INTO message_template_group(`groupId`, `displayName`, `description`) "
            + "VALUES(#{groupId}, #{displayName}, #{description})")
    void addTemplateGroup(MessageTemplateGroup group);

    @Insert("INSERT INTO "
            + "message_template(`id`, `groupId`, `displayName`, `description`, `eventKey`, `template`) "
            + "VALUES(#{id}, #{groupId}, #{displayName}, #{description}, #{eventKey}, #{template})")
    void addTemplate(MessageTemplate message);

    @Update("UPDATE message_template "
            + "SET "
            + "`template` = #{template},"
            + "`displayName` = #{displayName},"
            + "`description` = #{description} "
            + "WHERE `id` = #{id}")
    void updateTemplate(MessageTemplate message);

    @Update("UPDATE message_template_group "
            + "SET "
            + "`displayName` = #{displayName}, "
            + "`description` = #{description} "
            + "WHERE `groupId` = #{groupId}")
    void updateTemplateGroup(MessageTemplateGroup group);
}
