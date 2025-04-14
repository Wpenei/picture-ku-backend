package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.constant.CrawlerConstant;
import com.qingmeng.smartpictureku.constant.EmailConstant;
import com.qingmeng.smartpictureku.constant.UserConstant;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.manager.FileManager;
import com.qingmeng.smartpictureku.manager.auth.StpKit;
import com.qingmeng.smartpictureku.mapper.UserMapper;
import com.qingmeng.smartpictureku.model.dto.file.UploadPictureResult;
import com.qingmeng.smartpictureku.model.dto.user.UserQueryRequest;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.UserRoleEnum;
import com.qingmeng.smartpictureku.model.vo.LoginUserVO;
import com.qingmeng.smartpictureku.model.vo.UserVO;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.UserService;
import com.qingmeng.smartpictureku.utils.EmailSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qingmeng.smartpictureku.constant.UserConstant.USER_LOGIN_STATE;

/**
 * &#064;description: 用户服务
 *
 * @author Wang
 * &#064;date: 2025/3/1 21:20
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private EmailSenderUtil emailSenderUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FileManager fileManager;

    @Lazy
    @Resource
    private PictureService pictureService;

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @param type 类型
     * @param request 请求
     */
    @Async
    @Override
    public void sendEmailCode(String email, String type, HttpServletRequest request) {
        // 1.校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(email,type), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        // 2.检测高频操作
        // 3.生成验证码
        String code = RandomUtil.randomString(6);
        // 4.发送验证码
        try {
            emailSenderUtil.sendEmail(email,code);
        }catch (Exception e){
            log.error("发送邮件失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送邮件失败");
        }
        // 5.将验证码存入Redis
        String key = String.format(":%s:%s",type,email);
        String registerKey = String.format("%s%s",EmailConstant.EMAIL_CODE_VERITY,key);
        stringRedisTemplate.opsForValue().set(registerKey,code,EmailConstant.EMAIL_CODE_EXPIRE_TIME, TimeUnit.MINUTES);

    }


    /**
     * 用户注册
     * @param email 邮箱
     * @param code 验证码
     * @param userPassword 密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String email,String code, String userPassword, String checkPassword) {
        // 1.参数校验
        if (StrUtil.hasBlank(email,code,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8位");
        }
        if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不一致");
        }
        // 校验验证码
        String verifyCodeKey = String.format(":%s:%s", EmailConstant.REGISTER, email);
        String correctCode = stringRedisTemplate.opsForValue().get(EmailConstant.EMAIL_CODE_VERITY + verifyCodeKey);
        if(correctCode == null || !correctCode.equals(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }

        synchronized (email.intern()) {
            // 2.查询数据库（账户查重）
            // 检测邮箱是否已经被注册
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"账号已存在");
            }
            // 设置默认用户名（默认使用邮箱前缀，若前缀相同，在后面添加随机数）
            String userAccount = email.substring(0, email.indexOf("@"));
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount",userAccount);
            count = this.count(queryWrapper);
            if (count > 0) {
                // 账号已存在，再后面添加随机数
                userAccount = userAccount + RandomUtil.randomNumbers(4);
            }
            // 3.密码加密
            String encryptedPassword = getEncryptPassword(userPassword);

            // 4.插入数据
            User user = new User();
            user.setEmail(email);
            // 账号
            user.setUserAccount(userAccount);
            // 使用账号为默认用户名
            user.setUserName(userAccount);
            user.setUserPassword(encryptedPassword);
            user.setUserRole(UserRoleEnum.USER.getValue());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                log.info("注册失败，数据库错误");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
            }
            stringRedisTemplate.delete(verifyCodeKey);
            // 5.返回新用户ID
            return user.getId();
        }
    }

    /**
     * 获取登录验证码
     */
    @Override
    public Map<String, String> getLoginCaptcha() {
        // 1.创建一个仅包含数字的字符集
        String characters = "0123456789";
        // 生成四位数字验证码
        RandomGenerator randomGenerator = new RandomGenerator(characters, 4);
        // 定义图片的显示大小，并创建验证码对象
        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(320, 100, 4, 4);
        shearCaptcha.setGenerator(randomGenerator);
        // 生成验证码
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        shearCaptcha.write(outputStream);
        byte[] captchaBytes = outputStream.toByteArray();
        String base64Captcha = Base64.getEncoder().encodeToString(captchaBytes);
        String captchaCode = shearCaptcha.getCode();

        // 使用 Hutool 的 MD5 加密
        String encryptedCaptcha = DigestUtil.md5Hex(captchaCode);

        // 将加密后的验证码和 Base64 编码的图片存储到 Redis 中，设置过期时间为 5 分钟（300 秒）
        stringRedisTemplate.opsForValue().set("captcha:" + encryptedCaptcha, captchaCode, 60, TimeUnit.SECONDS);

        Map<String, String> data = new HashMap<>();
        data.put("base64Captcha", base64Captcha);
        data.put("encryptedCaptcha", encryptedCaptcha);
        return data;
    }

    /**
     * 校验验证码
     * @param userInputCaptcha 用户输入的验证码
     * @param serverVerifyCode 服务器生成的验证码
     * @return 校验结果
     */
    @Override
    public boolean checkCaptcha(String userInputCaptcha, String serverVerifyCode) {
        // 1.校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userInputCaptcha,serverVerifyCode), ErrorCode.PARAMS_ERROR, "参数为空");
        // 2.校验验证码
        // 将用户输入的验证码使用MD5加密
        String encryptedUserInputCaptcha = DigestUtil.md5Hex(userInputCaptcha);
        return encryptedUserInputCaptcha.equals(serverVerifyCode);
    }

    /**
     * 用户登录
     * @param email 邮箱
     * @param userPassword 用户密码
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String email, String userPassword, HttpServletRequest request){
        // 1.参数校验
        if (StrUtil.hasBlank(email,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码错误");
        }

        // 2.对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3.查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper =  new LambdaQueryWrapper<User>()
                .eq(User::getUserPassword, encryptPassword)
                .and(wrapper ->
                        wrapper.eq(User::getEmail, email).or().eq(User::getUserAccount, email)
                );
        User loginUser = this.getOne(queryWrapper);
        // 不存在,抛异常
        if (loginUser == null){
            log.info("user login failed, email cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }

        // 4.保存用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);
        // 将用户的登录态同时添加Sa-Token中,便于空间鉴权是使用
        StpKit.SPACE.login(loginUser.getId());
        // todo 使用saToken 记录用户登录信息
        // StpKit.DEFAULT.login(loginUser.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE,loginUser);
        // 5.获取脱敏后的用户信息并返回
        return getLoginUserVO(loginUser);
    }

    /**
     * 修改密码
     * @param id 用户id
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @param request 请求
     * @return 修改结果
     */
    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword, String checkPassword, HttpServletRequest request) {
        // 1.校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(oldPassword,newPassword,checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(oldPassword.equals(newPassword), ErrorCode.PARAMS_ERROR, "新旧密码不能相同");
        ThrowUtils.throwIf(newPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        ThrowUtils.throwIf(!newPassword.equals(checkPassword),ErrorCode.PARAMS_ERROR,"两次输入密码不一致");
        // 2.校验用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        queryWrapper.eq("userPassword",getEncryptPassword(oldPassword));
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null , ErrorCode.PARAMS_ERROR,"原密码错误");
        // 3.修改密码
        user.setUserPassword(getEncryptPassword(newPassword));
        boolean result = this.updateById(user);
        if (result){
            // 4.删除登录状态
            request.getSession().removeAttribute(USER_LOGIN_STATE);
            if (StpKit.SPACE.isLogin()){
                StpKit.SPACE.logout();
            }
        }
        return result;
    }

    /**
     * 退出登录
     * @param request 请求
     * @return 退出结果
     */
    @Override
    public boolean userLogout(HttpServletRequest request){
        // 1. 判断用户是否登录(Session中是否用保存的登录用户信息)
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null , ErrorCode.NOT_LOGIN_ERROR,"当前无用户登录");

        // 2.移除请求Request时所携带的Session中保存的登录用户信息
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        if (StpKit.SPACE.isLogin()){
            StpKit.SPACE.logout();
        }
        // todo 删除Redis中的相关数据
        // 获取当前会话并使其失效
        //HttpSession session = request.getSession(false);
        //if (session != null) {
        //    // 1. 使会话失效（触发 Spring Session 的清理逻辑）
        //    session.invalidate();
        //}
        return true;
    }

    /**
     * 获取当前登录用户
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1.1. 判断用户是否登录(Session中是否用保存的登录用户信息)
        Object userobj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 1.2.将对象转换为用户类型
        User currentUser = (User) userobj;
        Long userId = currentUser.getId();
        if (currentUser == null || userId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 2.根据id查询数据库,并返回
        currentUser = this.getById(userId);
        // 这里查询的数据库中没有,可能是用户被删除了(为了友好提示,显示用户未登录)
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    /**
     * 更新用户头像
     * @param multipartFile 头像文件
     * @param id 用户id
     * @param request 请求
     * @return 更新结果
     */
    @Override
    public String updateUserAvatar(MultipartFile multipartFile, Long id, HttpServletRequest request) {
        // 1.校验参数
        // 判断文件是否为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        // 判断用户是否存在
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        // 判断用户是否登录
        User loginUser = getLoginUser(request);
        if(loginUser == null || !loginUser.getId().equals(id)){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        // 2.上传文件
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 3.更新用户头像
        user.setUserAvatar(uploadPictureResult.getUrl());
        boolean result = this.updateById(user);
        // todo 更新ES
        //if (result){
        //    EsUser esUser = new EsUser();
        //    BeanUtil.copyProperties(user, esUser);
        //    esUserDao.save(esUser);
        //}
        return uploadPictureResult.getUrl();
    }

    /**
     * 更换绑定邮箱
     * @param newEmail 新邮箱
     * @param code 邮箱验证码
     * @param request 请求
     * @return 更换结果
     */
    @Override
    public boolean changeEmail(String newEmail, String code, HttpServletRequest request) {
        // 1. 校验参数
        if (StrUtil.hasBlank(newEmail, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (!newEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }

        // 2. 校验验证码
        String changeEmailKey = String.format(":%s:%s", EmailConstant.CHANGE_EMAIL, newEmail);
        String verifyCodeKey = String.format("%s%s", EmailConstant.EMAIL_CODE_VERITY, changeEmailKey);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (correctCode == null || !correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }
        // 3. 获取当前登录用户
        User loginUser = getLoginUser(request);
        synchronized (newEmail.intern()) {
            // 4. 检查新邮箱是否已被使用
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", newEmail);
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被使用");
            }

            // 5. 更新邮箱
            User user = new User();
            user.setId(loginUser.getId());
            user.setEmail(newEmail);
            boolean result = this.updateById(user);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改邮箱失败");
            }

            // 6. 删除验证码
            stringRedisTemplate.delete(verifyCodeKey);
            return true;
        }
    }

    /**
     * 重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @param code 验证码
     * @return 重置结果
     */
    @Override
    public boolean resetPassword(String email, String newPassword, String checkPassword, String code) {
        // 1.校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(email, newPassword, checkPassword, code),
                ErrorCode.PARAMS_ERROR, "参数为空");

        // 2. 校验邮箱格式
        if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }

        // 3. 校验密码
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 4. 校验验证码
        String resetPasswordKey = String.format(":%s:%s", EmailConstant.RESET_PASSWORD, email);
        String verifyCodeKey = String.format("%s%s",resetPasswordKey, email);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (correctCode == null || !correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 5. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 6. 更新密码
        String encryptPassword = getEncryptPassword(newPassword);
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(encryptPassword);
        boolean result = this.updateById(updateUser);
        if (result) {
            // 7. 删除验证码
            stringRedisTemplate.delete(verifyCodeKey);
        }
        return result;
    }

    /**
     * 封禁/解禁用户
     * @param userId 目标用户id
     * @param isUnban true-解禁，false-封禁
     * @param admin 执行操作的管理员
     * @return 是否操作成功
     */
    @Override
    public boolean banOrUnbanUser(Long userId, Boolean isUnban, User admin) {
        // 1. 校验参数
        if (userId == null || userId <= 0 || isUnban == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验管理员权限
        ThrowUtils.throwIf(!UserConstant.ADMIN_ROLE.equals(admin.getUserRole()),
                ErrorCode.FORBIDDEN_ERROR, "非管理员不能执行此操作");
        // 3. 获取目标用户信息
        User targetUser = this.getById(userId);
        ThrowUtils.throwIf(targetUser == null , ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // 4. 检查当前状态是否需要变更
        boolean isBanned = CrawlerConstant.BAN_ROLE.equals(targetUser.getUserRole());
        if (isUnban == isBanned) {
            // 5. 更新用户角色
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setUserRole(isUnban ? UserConstant.DEFAULT_ROLE : CrawlerConstant.BAN_ROLE);
            updateUser.setUpdateTime(new Date());
            boolean result = this.updateById(updateUser);

            if (result) {
                // 6. 记录操作日志
                log.info("管理员[{}]{}用户[{}]",
                        admin.getUserAccount(),
                        isUnban ? "解封" : "封禁",
                        targetUser.getUserAccount());

                // 7. 处理Redis缓存
                String banKey = String.format("user:ban:%d", userId);
                if (isUnban) {
                    stringRedisTemplate.delete(banKey);
                } else {
                    stringRedisTemplate.opsForValue().set(banKey, "1");
                }

                // 8. todo 更新ES中的用户信息
                //try {
                //    Optional<EsUser> esUserOpt = esUserDao.findById(userId);
                //    if (esUserOpt.isPresent()) {
                //        EsUser esUser = esUserOpt.get();
                //        esUser.setUserRole(isUnban ? UserConstant.DEFAULT_ROLE : CrawlerConstant.BAN_ROLE);
                //        esUserDao.save(esUser);
                //    }
                //} catch (Exception e) {
                //    log.error("更新ES用户信息失败", e);
                //}
            }

            return result;
        } else {
            // 状态已经是目标状态
            String operation = isUnban ? "解封" : "封禁";
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    String.format("该用户当前%s不需要%s", isUnban ? "未被封禁" : "已被封禁", operation));
        }
    }

    /**
     * 用户注销
     * @param userId 用户id
     */
    @Async
    @Override
    public void asyncDeleteUserData(Long userId, HttpServletRequest request){
        try {
            // 1. 删除用户发布的图片
            QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
            pictureQueryWrapper.eq("userId", userId);
            List<Picture> pictureList = pictureService.list(pictureQueryWrapper);
            if (!pictureList.isEmpty()) {
                // 删除数据库记录
                pictureService.remove(pictureQueryWrapper);
                // todo 删除ES中的图片记录
                //List<Long> pictureIds = pictureList.stream()
                //        .map(Picture::getId)
                //        .collect(Collectors.toList());
                //esPictureDao.deleteAllById(pictureIds);
            }

            // 2. todo 删除用户发布的帖子
            //QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
            //postQueryWrapper.eq("userId", userId);
            //List<Post> postList = postService.list(postQueryWrapper);
            //if (!postList.isEmpty()) {
            //    // 删除帖子附件
            //    List<Long> postIds = postList.stream()
            //            .map(Post::getId)
            //            .collect(Collectors.toList());
            //    QueryWrapper<PostAttachment> attachmentQueryWrapper = new QueryWrapper<>();
            //    attachmentQueryWrapper.in("postId", postIds);
            //    postAttachmentService.remove(attachmentQueryWrapper);
            //    // 删除帖子
            //    postService.remove(postQueryWrapper);
            //    // 删除ES中的帖子记录
            //    esPostDao.deleteAllById(postIds);
            //}

            // 3. 删除用户数据
            this.removeById(userId);
            // todo 删除ES中的用户记录
            //esUserDao.deleteById(userId);

            // 4. 清理相关缓存
            String userBanKey = String.format("user:ban:%d", userId);
            stringRedisTemplate.delete(userBanKey);
            // 删除登录态
            request.getSession().removeAttribute(USER_LOGIN_STATE);
            if(StpKit.SPACE.isLogin()){
                StpKit.SPACE.logout();
            }
            log.info("用户相关数据删除完成, userId={}", userId);
        } catch (Exception e) {
            log.error("删除用户相关数据失败, userId={}", userId, e);
        }
    }


    //region 原方法------------------------------------------------------------------

    /**
     * 获取脱敏后的用户信息
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user){
        // 校验用户是否为NULL
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取用户信息(脱敏)
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    /**
     * 获取用户信息列表(脱敏)
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        // 1.判断集合是否为NULL
        if (CollectionUtil.isEmpty(userList)){
            // 若为NULL,则返回空集合
            return new ArrayList<>();
        }
        // 遍历集合,将集合中的用户信息User转换为UserVO,并返回
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {

        // 1.校验参数
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        // 2.获取请求参数
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 3.创建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 获取加密密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "qingmeng";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }



//endregion
}




