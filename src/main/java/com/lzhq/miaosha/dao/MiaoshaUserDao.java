package com.lzhq.miaosha.dao;


import com.lzhq.miaosha.domain.MiaoshaUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MiaoshaUserDao {

    @Select("select * from miaosha_user where id = #{id}")
    public MiaoshaUser getById(@Param("id")long id);

    @Update("update miaosha_user set password = #{password} where id = #{id}")
    public void update(MiaoshaUser toBeUpdate);

    @Insert("insert into miaosha_user (id, nickname, password, head, register_date) values (#{id}, #{nickname}, #{password}, #{head}, #{registerDate}")
    public Integer addUser(MiaoshaUser user);
}
