package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;

/**
 * Spring默认Oauth2客户端表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("oauth_client_details")
public class OauthClientDetails extends BaseEntity {

    /**
     * 乐观锁
     */
    @TableField(value = "version", fill = FieldFill.INSERT_UPDATE)
    @Version
    private Long version;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 更新人
     */
    @TableField(value = "updater_id", fill = FieldFill.INSERT_UPDATE)
    private Long updaterId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 客户端ID
     */
    @TableField("client_id")
    private String clientId;

    /**
     * 客户端密钥
     */
    @TableField("client_secret")
    private String clientSecret;

    /**
     * 权限范围
     */
    @TableField("scope")
    private String scope;

    /**
     * 鉴权类型
     */
    @TableField("authorized_grant_types")
    private String authorizedGrantTypes;

    /**
     * 重定向地址
     */
    @TableField("web_server_redirect_uri")
    private String webServerRedirectUri;

    /**
     * 权限
     */
    @TableField("authorities")
    private String authorities;

    /**
     * 授权密钥过期时间
     */
    @TableField("access_token_validity")
    private Integer accessTokenValidity;

    /**
     * 刷新密钥过期时间
     */
    @TableField("refresh_token_validity")
    private Integer refreshTokenValidity;

    /**
     * 额外信息
     */
    @TableField("additional_information")
    private String additionalInformation;

    /**
     * 自动同意
     */
    @TableField("auto_approve")
    private String autoApprove;

}
