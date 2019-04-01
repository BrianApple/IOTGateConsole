package IOTGateConsole.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import IOTGateConsole.dao.UserMapper;
import IOTGateConsole.domain.User;

@Service
public class TestServiceImpl implements TestService1{
	
//	@Autowired
//	UserMapper usermapper;
	public int insert(User user) {
		//usermapper.insert(user);
		
//		return user.getId();
		return 0;
	}
}
