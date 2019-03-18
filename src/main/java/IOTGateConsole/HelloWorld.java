package IOTGateConsole;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Auther: hejuanjuan
 * @Date: 2019/3/18 16:33
 */
@RestController
public class HelloWorld {

    @RequestMapping("/")
    public String home() {
        return "Hello World!";
    }
}
