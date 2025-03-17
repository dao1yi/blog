package com.xmq.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailUtils {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String from;
    
    @Value("${spring.mail.nickname:博客系统}")
    private String nickname;

    /**
     * 发送验证码邮件
     *
     * @param to 收件人
     * @param code 验证码
     */
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            // 设置发件人（昵称+邮箱地址）
            helper.setFrom(String.format("%s <%s>", nickname, from));
            helper.setTo(to);
            helper.setSubject("验证码 - 博客系统");
            
            // 构建邮件内容
            String content = """
                <div style="background-color: #f5f5f5; padding: 20px;">
                    <div style="background-color: #fff; padding: 20px; border-radius: 5px; max-width: 600px; margin: 0 auto;">
                        <h2 style="color: #333;">博客系统 - 验证码</h2>
                        <p style="color: #666;">尊敬的用户：</p>
                        <p style="color: #666;">您的验证码是：<span style="color: #1890ff; font-size: 20px; font-weight: bold;">%s</span></p>
                        <p style="color: #666;">该验证码5分钟内有效，请勿泄露给他人。</p>
                        <p style="color: #666;">如非本人操作，请忽略此邮件。</p>
                        <div style="border-top: 1px solid #e8e8e8; margin-top: 20px; padding-top: 20px;">
                            <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿直接回复。</p>
                            <p style="color: #999; font-size: 12px;">博客系统团队</p>
                        </div>
                    </div>
                </div>
                """.formatted(code);
            
            helper.setText(content, true);
            mailSender.send(message);
            log.info("验证码邮件发送成功: {}", to);
            
        } catch (MessagingException e) {
            log.error("验证码邮件发送失败: {}", to, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 生成6位随机验证码
     */
    public static String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
} 