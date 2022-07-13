package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Spring默认Oauth2客户端表
 *
 * @author Moment
 * @since 2022-07-12 11:05:35
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("oauth_client_details")
public class OauthClientDetails extends BaseEntity {

    /**
     * ID
     */
    @TableId("id")
    private String id;

    /**
     * 客户端ID;
     */
    @TableField("client_id")
    private String clientId;

    /**
     * 客户端密钥;
     */
    @TableField("client_secret")
    private String clientSecret;

    /**
     * 权限范围;
     */
    @TableField("scope")
    private String scope;

    /**
     * 鉴权类型;
     */
    @TableField("authorized_grant_types")
    private String authorizedGrantTypes;

    /**
     * 重定向地址;
     */
    @TableField("web_server_redirect_uri")
    private String webServerRedirectUri;

    /**
     * 权限;
     */
    @TableField("authorities")
    private String authorities;

    /**
     * 授权密钥过期时间;
     */
    @TableField("access_token_validity")
    private Integer accessTokenValidity;

    /**
     * 刷新密钥过期时间;
     */
    @TableField("refresh_token_validity")
    private Integer refreshTokenValidity;

    /**
     * 额外信息;
     */
    @TableField("additional_information")
    private String additionalInformation;

    /**
     * 自动同意;
     */
    @TableField("auto_approve")
    private Boolean autoApprove;


}
