package com.qingmeng.smartpictureku.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.qingmeng.smartpictureku.api.aliyunai.model.*;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * &#064;description: 阿里云AI接口 - 图片扩图
 *
 * @author Wang
 * &#064;date: 2025/3/13
 */
@Component
@Slf4j
public class AliYunAiApi {
    // 读取配置
    @Value("${aliyunai.apiKey}")
    private String apiKey;

    // 创建扩图任务地址
    private static final String OUT_PAINTING_CREATE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 获取扩图任务状态地址
    private static final String GET_TASK_STATUS_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    // 获取涂鸦绘画任务地址
    private static final String DOODLE_CREATE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis/";

     // 创建扩图任务
    public CreateTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "扩图参数为空");
        }
        // 2.构建请求
        // API-KEY   --header "Authorization: Bearer $DASHSCOPE_API_KEY"
        // 异步调用   --header 'X-DashScope-Async: enable'
        // 请求体类型 --header 'Content-Type: application/json'
        HttpRequest httpRequest = HttpRequest.post(OUT_PAINTING_CREATE_TASK_URL)
                // 鉴权信息
                .header("Authorization", "Bearer " + apiKey)
                // 必须开启异步处理，设置为enable。
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        // 3.发送请求
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败,{}" + httpResponse.body());
            }
            // 4.解析结果
            CreateTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateTaskResponse.class);
            String errorCode = response.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI 扩图失败, errorCode:{},errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "Ai扩图接口响应异常");
            }
            return response;
        } catch (Exception e) {
            log.error("创建扩图任务失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建扩图任务失败");
        }
    }

    // 获取扩图任务结果
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "扩图任务ID为空");
        }
        //System.out.println("开始查询扩图任务状态,taskId = " + taskId);
        String getTaskUrl = String.format(GET_TASK_STATUS_URL, taskId);
        //System.out.println("查询扩图任务状态的URL为: " + getTaskUrl);
        try (HttpResponse httpResponse = HttpRequest.get(getTaskUrl)
                // 鉴权信息
                .header("Authorization", "Bearer " + apiKey)
                .execute()
        ){
            if (!httpResponse.isOk()) {
                log.error("请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取扩图任务失败" + httpResponse.body());
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }

    // 创建涂鸦作画任务
    public  CreateTaskResponse createDoodleTask(CreateDoodleTaskRequest createDoodleTaskRequest) {
        if (createDoodleTaskRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "扩图参数为空");
        }
        // 2.构建请求
        HttpRequest httpRequest = HttpRequest.post(DOODLE_CREATE_TASK_URL)
                // 鉴权信息
                .header("Authorization", "Bearer " + apiKey)
                // 必须开启异步处理，设置为enable。
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createDoodleTaskRequest));
        // 3.发送请求
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "涂鸦绘画失败,{}" + httpResponse.body());
            }
            // 4.解析结果
            CreateTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateTaskResponse.class);
            String errorCode = response.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("涂鸦绘画失败, errorCode:{},errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "涂鸦绘画接口响应异常");
            }
            return response;
        } catch (Exception e) {
            log.error("创建任务失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建涂鸦绘画任务失败");
        }
    }

    // 获取扩图任务结果
    public  GetDoodleTaskResponse getDoodleTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "扩图任务ID为空");
        }
        String getTaskUrl = String.format(GET_TASK_STATUS_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(getTaskUrl)
                // 鉴权信息
                .header("Authorization", "Bearer " + apiKey)
                .execute()
        ){
            String responseBody = httpResponse.body();
            if (!httpResponse.isOk()) {
                log.error("请求异常:{}", responseBody);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取扩图任务失败" + responseBody);
            }
            GetDoodleTaskResponse response = JSONUtil.toBean(responseBody, GetDoodleTaskResponse.class);
            return response;
        }
    }

    // 要测试需要先将 apiKey 填写为实际值 同时将方法转为静态方法 static 修饰
    //public static void main(String[] args) {
    //    CreateDoodleTaskRequest createDoodleTaskRequest = new CreateDoodleTaskRequest();
    //    CreateDoodleTaskRequest.Input input = new CreateDoodleTaskRequest.Input();
    //    CreateDoodleTaskRequest.Parameters parameters = new CreateDoodleTaskRequest.Parameters();
    //    input.setPrompt("一朵五颜六色的花");
    //    input.setSketchImageUrl("https://bpic.588ku.com/element_origin_min_pic/23/04/24/de31037dda7a908115c3ad248fa33669.jpg");
    //    parameters.setStyle("<watercolor>");
    //    createDoodleTaskRequest.setInput(input);
    //    createDoodleTaskRequest.setParameters(parameters);
    //    CreateTaskResponse doodleTask = createDoodleTask(createDoodleTaskRequest);
    //    System.out.println("涂鸦绘图任务创建成功,当前任务ID为: " + doodleTask.getOutput().getTaskId());
    //    // 创建一个定时器,每三秒访问一次获取结果的方法,查看状态
    //    Timer timer = new Timer();
    //    CreateTaskResponse.Output output = doodleTask.getOutput();
    //    String taskId = output.getTaskId();
    //    timer.schedule(new TimerTask() {
    //        @Override
    //        public void run() {
    //            GetDoodleTaskResponse getDoodleTask = getDoodleTask(taskId);
    //            System.out.println("查看涂鸦绘图任务状态,当前任务状态为: " + getDoodleTask.getOutput().getTaskStatus() + "图片结果: " + getDoodleTask.getOutput().getResults());
    //            // 新增状态判断逻辑
    //            if (getDoodleTask.getOutput() != null &&
    //                    "SUCCEEDED".equals(getDoodleTask.getOutput().getTaskStatus())) {
    //                System.out.println("任务执行成功，停止轮询");
    //                this.cancel();  // 取消当前任务
    //                timer.cancel(); // 停止定时器
    //                timer.purge();  // 清除已取消的任务
    //            }
    //        }
    //    }, 0, 3000);
    //}
}
