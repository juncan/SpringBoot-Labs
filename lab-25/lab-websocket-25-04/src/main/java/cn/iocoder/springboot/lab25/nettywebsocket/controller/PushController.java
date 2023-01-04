package cn.iocoder.springboot.lab25.nettywebsocket.controller;

import cn.iocoder.springboot.lab25.nettywebsocket.service.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xingkong
 * @date 2022/12/28 16:13
 */
@RestController
@RequestMapping("/push")
public class PushController {
    @Autowired
    private PushService pushService;

    /**
     * 推送给所有用户
     * @param msg 消息信息
     */
    @PostMapping("/pushAll")
    public void pushToAll(@RequestParam("msg") String msg){
        pushService.pushMsgToAll(msg);
    }

    /**
     * 推送给指定用户
     * @param userId 用户ID
     * @param msg 消息信息
     */
    @PostMapping("/pushOne")
    public void pushMsgToOne(@RequestParam("userId") String userId, @RequestParam("msg") String msg){
        pushService.pushMsgToOne(userId,msg);
    }

    /**
     * 获取当前连接数
     */
    @GetMapping("/getConnectCount")
    public int getConnectCout(){
        return pushService.getConnectCount();
    }
}
