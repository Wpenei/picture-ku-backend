package com.qingmeng.smartpictureku.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.SpaceRoleEnum;
import com.qingmeng.smartpictureku.model.enums.SpaceTypeEnum;
import com.qingmeng.smartpictureku.model.enums.UserRoleEnum;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.SpaceUserService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.qingmeng.smartpictureku.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserAuthManage spaceUserAuthManage;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private PictureService pictureService;
    @Autowired
    private SpaceService spaceService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 1.校验登陆类型,如果不是space直接返回空列表
        if (!loginType.equals(StpKit.SPACE_TYPE)) {
            return new ArrayList<>();
        }
        // 2.管理员权限列表(因为下面有好多要使用,这里提前定义)
        List<String> adminList = spaceUserAuthManage.getPermissionsByRole(SpaceRoleEnum.ADMINER.getValue());
        // 3.获取上下文对象:从请求中获取SpaceUserAuthContext上下文对象,
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        // 检查上下文字段是否为空,
        boolean allNull = isAllNull(authContext);
        // 如果上下文中所有字段都为空,(如:没有空间和图片信息),视为公共图库操作
        if (allNull) {
            return adminList;
        }
        // 4.校验登录状态,:通过loginid获取当前登录用户信息,如果用户未登录,抛出未授权异常,否则获取用户的唯一标识userId
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 如果当前登录用户是管理员,直接返回管理员权限列表
        if (loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())){
            return adminList;
        }
        Long userId = loginUser.getId();
        // 5.如果上下文中存在SpaceUser对象,,直接从上下文中根据其角色返回对应的权限列表
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManage.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 6.如果存在SpaceUserId,则根据SpaceUserId获取空间用户信息
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            // 查询对应的SpaceUser数据,如果不存在直接抛出异常
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
            }
            // 校验当前登录用户是否属于该空间,若不是,返回空权限列表
            // 取出当前登录用户对应的 spaceUser
            // todo 跟鱼总不一样,鱼总又查了一次SpaceUser表
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            // 否则,根据登录用户在该空间中的角色返回对应的权限列表
            return spaceUserAuthManage.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 7.如果存在SpaceId,PictureId,则根据其值获取空间或图片信息
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            // 如果SpaceId不存在:使用PictureId查询图片信息,并通过图片中的SpaceId继续判断权限;
            Long pictureId = authContext.getPictureId();
            if (pictureId == null) {
                // 如果PictureId和SpaceId都不存在,默认视为管理员权限
                return adminList;
            }
            // 根据PictureId查询图片信息,如果图片不存在,抛出异常
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
            }
            spaceId = picture.getSpaceId();
            // 对于公共图库:如果图片是当前登录用户上传的,或者当前登录用户是管理员,放回管理员列表,如果图片不是当前用户上传的,返回浏览者的权限列表
            if (spaceId == null) {
                // 公共图库,仅管理员或图片本人可编辑
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                    return adminList;
                } else {
                    // 不是自己的图片
                    return spaceUserAuthManage.getPermissionsByRole(SpaceRoleEnum.VIEWER.getValue());
                }
            }
        }
        // 获取Space对象,
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }
        // 并判断空间类型,查询Space信息,如果未找到空间数据,抛出数据未找到异常,否则根据空间类型进行判断
        if (space.getSpaceType().equals(SpaceTypeEnum.PRIVATE.getValue())) {
            // 私有空间:仅空间所有者和管理员有权限(即返回全部列表),其他用户返回空权限列表.
            if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                return adminList;
            }
            return new ArrayList<>();
        }else {
            // 团队空间:查询登录用户在该空间中的角色,并返回对应的权限列表,如果用户不属于该空间,返回空权限列表
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManage.getPermissionsByRole(spaceUser.getSpaceRole());
        }
    }

        /**
         * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
         * 本项目中不使用。返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
         */
        @Override
        public List<String> getRoleList (Object loginId, String loginType){
            // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询角色
            return new ArrayList<>();
        }

        // 从请求中获取上下文对象
        private SpaceUserAuthContext getAuthContextByRequest () {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
            // 获取请求参数
            SpaceUserAuthContext authRequest;
            if (ContentType.JSON.getValue().equals(contentType)) {
                String body = ServletUtil.getBody(request);
                authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
            } else {
                Map<String, String> paramMap = ServletUtil.getParamMap(request);
                authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
            }
            // 根据请求路径区分 id 的含义
            Long id = authRequest.getId();
            if (ObjUtil.isNotNull(id)) {
                // 获取到请求路径的前缀, /api/user/xxx
                String requestURI = request.getRequestURI();
                // 替换掉上下文,剩下的就是前缀
                String partUrl = requestURI.replace(contextPath + "/", "");
                // 获取前缀中第一个 "/" 前的字符串
                String modulNane = StrUtil.subBefore(partUrl, "/", false);
                // 根据这个字符串来区分 id
                switch (modulNane) {
                    case "picture":
                        authRequest.setPictureId(id);
                        break;
                    case "space":
                        authRequest.setSpaceId(id);
                        break;
                    case "spaceUser":
                        authRequest.setSpaceUserId(id);
                        break;
                    default:
                }
            }
            return authRequest;
        }

        /**
         * 判断对象的所有字段是否为空 **通过反射获取对象的所有字段，进行判空**
         */
        private boolean isAllNull (Object object){

            if (object == null) {
                return true;
            }
            // 获取所有字段并判断是否所有字段都为空
            return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                    // 获取字段值
                    .map(field -> ReflectUtil.getFieldValue(object, field))
                    // 检查是否所有字段都为空
                    .allMatch(ObjUtil::isEmpty);
        }
    }
