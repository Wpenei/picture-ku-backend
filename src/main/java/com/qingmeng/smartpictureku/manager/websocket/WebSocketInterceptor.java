package com.qingmeng.smartpictureku.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import com.qingmeng.smartpictureku.manager.auth.SpaceUserAuthManage;
import com.qingmeng.smartpictureku.manager.auth.model.SpaceUserPermissionConstant;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.SpaceTypeEnum;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * &#064;description: WebSocket 拦截器
 * 建立连接之前需要进行权限校验
 * 如果用户没有团队空间内编辑的权限,则拒绝握手
 * (因为WebSocket 的区别,不能直接从Request中获取到用户信息
 * 所以需要通过拦截器获取,为即将建立连接的WebSocket会话指定一些属性,比如用户信息,编辑的图片id)
 *
 * @author Wang
 * &#064;date: 2025/3/17
 */
@Slf4j
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManage spaceUserAuthManage;
    /**
     * 建立连接之前
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 获取请求参数
            String pictureId = servletRequest.getParameter("pictureId");
            if (pictureId == null) {
                log.info("缺少图片id,拒绝握手");
                return false;
            }
            // 校验当前登录用户是否有该图片的编辑权限
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(servletRequest);
            if (ObjUtil.isEmpty(loginUser)){
                log.info("用户未登录,拒绝握手");
                return false;
            }
            // 获取图片对象
            Picture picture = pictureService.getById(pictureId);
            if (picture == null ){
                log.info("图片不存在,拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            if (spaceId != null){
                Space space = spaceService.getById(spaceId);
                if (space == null){
                    log.info("图片所属空间不存在,拒绝握手");
                    return false;
                }
                if (!space.getSpaceType().equals(SpaceTypeEnum.TEAM.getValue())){
                    log.info("图片所属空间不是团队空间,拒绝握手");
                    return false;
                }
                List<String> permissionsList = spaceUserAuthManage.getPermissionsList(space, loginUser);
                if(!permissionsList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)){
                    log.info("用户没有编辑图片的权限,拒绝握手");
                    return false;
                }
            }
            // 设置attributes
            attributes.put("user",loginUser);
            attributes.put("userId",loginUser.getId());
            // 这里需要转为Long类型
            attributes.put("pictureId",Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
