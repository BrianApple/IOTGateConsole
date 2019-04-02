package IOTGateConsole.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import IOTGateConsole.domain.IotGateDB;
import IOTGateConsole.domain.User;

public interface IotGateMapper {
	@Insert("insert into strategy(id,pId,pName,isBigEndian,beginHexVal,lengthFieldOffset,lengthFieldLength,isDataLenthIncludeLenthFieldLenth,exceptDataLenth,port,highControll,content) values(#{id},#{pId},#{pName},#{isBigEndian},#{beginHexVal},#{lengthFieldOffset},#{lengthFieldLength},#{isDataLenthIncludeLenthFieldLenth},#{exceptDataLenth},#{port},#{highControll},#{content})")
	@Options(useGeneratedKeys=true,keyProperty="id",keyColumn="id")//设置主键自增
	int insert(IotGateDB strategy);

	@Select("select * from strategy")
	List<IotGateDB> getAllStrategy();
	@Select("select * from strategy where pId = #{pId}")
	List<IotGateDB> getStrategyByPid(IotGateDB strategy);
	
	@Delete("delete from strategy where pId = #{pId}")
	int delOneStrategyByPID(IotGateDB strategy);
}
