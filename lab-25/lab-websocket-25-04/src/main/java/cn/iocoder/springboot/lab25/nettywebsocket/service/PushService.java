package cn.iocoder.springboot.lab25.nettywebsocket.service;

/**
 * @author xingkong
 * @date 2022/12/28 16:14
 */
public interface PushService {

    /**
     * 推送给指定用户
     * @param userId 用户ID
     * @param msg 消息信息
     */
    void pushMsgToOne(String userId,String msg);

    /**
     * 推送给所有用户
     * @param msg 消息信息
     */
    void pushMsgToAll(String msg);

    /**
     * 获取当前连接数
     * @return 连接数
     */
    int getConnectCount();
}
