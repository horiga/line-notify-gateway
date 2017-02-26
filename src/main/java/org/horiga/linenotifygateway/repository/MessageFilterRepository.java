package org.horiga.linenotifygateway.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.horiga.linenotifygateway.entity.MessageFilterEntity;

@SuppressWarnings("unused")
@Mapper
public interface MessageFilterRepository {

    @Select("SELECT * FROM message_filter WHERE group_id = #{groupId}")
    List<MessageFilterEntity> findBy(@Param("groupId") String groupId);

    @Select("SELECT * FROM message_filter "
            + " WHERE group_id = #{groupId} "
            + " AND mapping_value = #{mappingValue}")
    List<MessageFilterEntity> findByMappings(@Param("groupId") String groupId,
                                             @Param("mappingValue") String mappingValue);

    @Insert("INSERT INTO message_filter(`id`, `group_id`, `mapping_value`, `condition`) "
            + "VALUES(#{id}, #{groupId}, #{mappingValue}, #{condition})")
    void insert(MessageFilterEntity entity);

    @Delete("DELETE FROM message_filter WHERE id = #{id}")
    void delete(@Param("id") String id);
}
