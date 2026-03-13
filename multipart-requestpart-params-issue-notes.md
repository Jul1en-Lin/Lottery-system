# Multipart `@RequestPart("params")` 反序列化问题复盘

## 1. 问题背景
在奖品创建接口中，后端方法定义为：

```java
@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Long createPrize(@RequestPart("params") @Valid CreatePrizeRequest createPrizeRequest,
                        @RequestPart("file") MultipartFile file)
```

前端/测试工具使用 `Body -> form-data` 传参：
- `params`：text，内容为 JSON
- `file`：图片文件

## 2. 典型报错与阶段
本次排查中出现过两类报错，分别属于不同阶段：

1. `转换类型异常`
- 阶段：参数反序列化阶段
- 触发原因：`params` 里的 JSON 不是合法 JSON（字符串值缺少双引号）

2. `Required part 'params' is not present`
- 阶段：Multipart part 匹配阶段
- 触发原因：后端按 `@RequestPart("params")` 查找 part，但请求中没有被识别为该 part（key 不一致、请求头/boundary 异常、part 类型不匹配等）

## 3. 核心认知
`@RequestPart` 不是简单地拿字符串，它需要一个可被消息转换器识别并转换的 part。

- 如果 `params` 是标准 JSON part（`application/json`），Spring 默认转换器通常可直接反序列化
- 如果 `params` 作为 `text/plain` 发送，默认链路可能无法稳定命中 JSON 反序列化，需要自定义 converter

## 4. 这次真正踩坑点
1. JSON 写法错误：
```json
{"name":小车模,"description":1:64车模,"price":200}
```
应改为：
```json
{"name":"小车模","description":"1:64车模","price":200}
```

2. 项目依赖体系差异：
- 当前项目（Spring Boot 4）实际使用 `tools.jackson.*`
- 直接照搬 `com.fasterxml.*` 版本的 converter 会出现类找不到或不兼容

3. 过时 API：
- `AbstractJackson2HttpMessageConverter`/部分老方案在当前版本中不推荐继续扩展

## 5. 最终落地方案
### 5.1 自定义读取型 converter
文件：`src/main/java/com/julien/lotterysystem/common/converter/MultipartJackson2HttpMessageConverter.java`

设计要点：
- 实现 `HttpMessageConverter<Object>`
- 支持 `application/json`、`text/plain`、`application/octet-stream`
- 仅支持读取（`canWrite=false`），避免影响响应序列化
- 使用 `tools.jackson.databind.ObjectMapper` 反序列化

### 5.2 在 MVC 转换器链中优先注册
文件：`src/main/java/com/julien/lotterysystem/common/config/MessageConverterConfig.java`

设计要点：
- 通过 `extendMessageConverters` 注册
- 放在索引 `0`，保证优先匹配 `@RequestPart("params")`

## 6. 两个类的关系
- `MultipartJackson2HttpMessageConverter`：负责“怎么转换”
- `MessageConverterConfig`：负责“把谁注册进 Spring，并定义优先级”

即：一个是执行器，一个是装配器。

## 7. 测试用例模板（Postman）
- Method：`POST`
- URL：`/prize/create`
- Body：`form-data`
- `params`：Type=`Text`，值：
```json
{"name":"小车模","description":"1:64车模","price":200}
```
- `file`：Type=`File`，选择图片
- 不手动改 `Content-Type`，让工具自动生成 multipart boundary

## 8. 快速排查清单（下次直接照抄）
1. JSON 是否合法（特别是字符串双引号）
2. form-data key 是否与 `@RequestPart` 完全一致
3. 是否错误手动覆盖 `Content-Type`
4. 项目 Jackson 包名是否与代码一致（`tools.jackson` vs `com.fasterxml.jackson`）
5. 自定义 converter 是否已注册且优先级足够高
6. 先编译验证（`./mvnw.cmd -DskipTests compile`）再联调

## 9. 可复用结论
当接口需要同时接收“文件 + JSON 文本”且前端以 `form-data text` 方式传 JSON 时，最稳妥做法是：
- 保持 `@RequestPart` 接口语义
- 增加只读型 multipart JSON converter
- 在 MVC 链路中前置注册

这样既保留参数校验能力（`@Valid`），也能兼容测试工具常见的 `text/plain` JSON 传法。

## 10. 运行逻辑
运行时链路是：

1. 请求进来，命中 `@RequestPart("params")`。
2. Spring 在转换器列表里找谁能读这个 part。
3. `MessageConverterConfig` 提前注册的 `MultipartJackson2HttpMessageConverter` 被选中。
4. converter 调 `ObjectMapper.readValue(...)` 转成 `CreatePrizeRequest`。

