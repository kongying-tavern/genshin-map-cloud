# OAuth2 JWT 密钥库轮换指引

> 背景：历史上 `genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/src/main/resources/oauth2.jks`
> 曾被提交进公开仓库，且口令以硬编码形式写在 `AuthorizationServerConfiguration` 中。
> 该密钥库应视为**已泄漏**：持有仓库历史的人可以离线签发任意 `userId` / `userRoles` 的
> RS256 token。本仓库已移除该文件并改为外部挂载 + 环境变量口令，**合并后必须按本文档轮换**。

## 1. 生成新的密钥库

在部署机（或离线工作站）生成，口令自行随机生成并妥善保存：

```bash
keytool -genkeypair \
  -alias oauth2 \
  -keyalg RSA -keysize 2048 -validity 3650 \
  -storetype PKCS12 \
  -keystore oauth2.jks \
  -storepass "$OAUTH2_JKS_PASSWORD" \
  -keypass "$OAUTH2_JKS_PASSWORD" \
  -dname "CN=genshin-map-oauth2, O=kongying-tavern, C=CN"
```

> PKCS12 不允许密钥口令与库口令不同，因此 `oauth2.jks-key-password` 可省略（缺省复用库口令）。

## 2. 放置与权限

```bash
install -m 600 oauth2.jks "$DATA_DIR/api/secrets/oauth2.jks"
```

目录 `$DATA_DIR/api/secrets` 由 `docker/composer/api.yml` 以只读方式挂载到容器内 `/data/secrets`。

## 3. 配置口令

在 `docker/.env`（由 `docker/.env.example` 复制）中设置，**不要提交**：

```dotenv
OAUTH2_JKS_LOCATION=file:/data/secrets/oauth2.jks
OAUTH2_JKS_PASSWORD=<新口令>
```

`OAUTH2_JKS_PASSWORD` 未设置时 compose 会直接报错、api-core 启动时也会抛出明确异常，
属于预期的快速失败。

## 4. 滚动重启与失效确认

1. 重启 api-core（`cargo make api-run` 或 `docker compose ... up -d api-core`）。
2. 旧的 access/refresh token 因签名密钥变化**立即失效**，所有用户需重新登录；
   前端收到 403 后应走登出/重登流程（这是本次轮换的预期代价）。
3. 验证：
   - `POST /oauth/token`（password / client_credentials）能正常签发；
   - 用新 token 访问 `/api/marker/get/list_byid` 返回 200；
   - 旧 token 访问返回 403。

## 5. 禁止事项

- 不得把 `*.jks` / `*.p12` 再提交进仓库（`.gitignore` 已封堵）；
- 不得在代码、配置模板、CI 变量文件里写死口令；
- 若再次泄漏，按本文档重新轮换，并排查网关/日志中是否有 token 泄漏面。
