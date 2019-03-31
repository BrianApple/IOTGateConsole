package IOTGateConsole.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

import IOTGateConsole.domain.User;

public interface UserMapper {
//	@Insert("insert into user(name,phone,create_time,age) values(#{name},#{phone},#{create_time},#{age})")
//	@Options(useGeneratedKeys=true,keyProperty="id",keyColumn="id")//设置主键自增
	int insert(User user);
}
