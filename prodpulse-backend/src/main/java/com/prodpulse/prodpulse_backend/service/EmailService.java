package com.prodpulse.prodpulse_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.List;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    public void sendOtpEmail(String toEmail, String otp) throws Exception {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        SendSmtpEmail email = new SendSmtpEmail();

        email.setSubject("ProdPulse.AI — Verify your email");
        email.setSender(new SendSmtpEmailSender().name("ProdPulse.AI").email("noreply.prodpulse@gmail.com"));
        email.setTo(List.of(new SendSmtpEmailTo().email(toEmail)));
        email.setHtmlContent(buildOtpTemplate(otp));

        apiInstance.sendTransacEmail(email);
    }

    public void sendForgotPasswordEmail(String toEmail, String otp) throws Exception {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        SendSmtpEmail email = new SendSmtpEmail();

        email.setSubject("ProdPulse.AI — Reset your password");
        email.setSender(new SendSmtpEmailSender().name("ProdPulse.AI").email("noreply.prodpulse@gmail.com"));
        email.setTo(List.of(new SendSmtpEmailTo().email(toEmail)));
        email.setHtmlContent(buildForgotPasswordTemplate(otp));

        apiInstance.sendTransacEmail(email);
    }

    private String buildOtpTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background-color:#0a0a0a;font-family:'Segoe UI',sans-serif;">
                    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#0a0a0a;padding:40px 0;">
                        <tr>
                            <td align="center">
                                <table width="520" cellpadding="0" cellspacing="0"
                                    style="background-color:#111111;border-radius:16px;border:1px solid #222222;overflow:hidden;">
                                    <tr>
                                        <td align="center" style="padding:36px 40px 24px;">
                                            <div style="font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;">
                                                ⚡ ProdPulse.AI
                                            </div>
                                            <div style="font-size:13px;color:#666666;margin-top:4px;">
                                                Your Production System Doctor
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 40px;">
                                            <div style="height:1px;background-color:#222222;"></div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:36px 40px 16px;">
                                            <p style="color:#aaaaaa;font-size:15px;margin:0 0 24px;">
                                                Hey there 👋 Use the OTP below to verify your email address.
                                                This code expires in <strong style="color:#ffffff;">10 minutes.</strong>
                                            </p>
                                            <div style="background-color:#1a1a1a;border:1px solid #333333;border-radius:12px;
                                                        padding:28px;text-align:center;margin-bottom:24px;">
                                                <div style="font-size:11px;color:#666666;letter-spacing:2px;
                                                            text-transform:uppercase;margin-bottom:12px;">
                                                    Your verification code
                                                </div>
                                                <div style="font-size:42px;font-weight:700;color:#ffffff;
                                                            letter-spacing:12px;font-family:monospace;">
                                                """ + otp + """
                                                </div>
                                            </div>
                                            <p style="color:#555555;font-size:13px;margin:0;">
                                                If you didn't request this, you can safely ignore this email.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:24px 40px 36px;">
                                            <div style="height:1px;background-color:#222222;margin-bottom:24px;"></div>
                                            <p style="color:#444444;font-size:12px;margin:0;text-align:center;">
                                                © 2026 ProdPulse.AI — All rights reserved
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;
    }

    private String buildForgotPasswordTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background-color:#0a0a0a;font-family:'Segoe UI',sans-serif;">
                    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#0a0a0a;padding:40px 0;">
                        <tr>
                            <td align="center">
                                <table width="520" cellpadding="0" cellspacing="0"
                                    style="background-color:#111111;border-radius:16px;border:1px solid #222222;overflow:hidden;">
                                    <tr>
                                        <td align="center" style="padding:36px 40px 24px;">
                                            <div style="font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;">
                                                ⚡ ProdPulse.AI
                                            </div>
                                            <div style="font-size:13px;color:#666666;margin-top:4px;">
                                                Your Production System Doctor
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 40px;">
                                            <div style="height:1px;background-color:#222222;"></div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:36px 40px 16px;">
                                            <p style="color:#aaaaaa;font-size:15px;margin:0 0 24px;">
                                                We received a request to reset your password.
                                                Use the OTP below — it expires in <strong style="color:#ffffff;">10 minutes.</strong>
                                            </p>
                                            <div style="background-color:#1a1a1a;border:1px solid #ff4444;border-radius:12px;
                                                        padding:28px;text-align:center;margin-bottom:24px;">
                                                <div style="font-size:11px;color:#ff4444;letter-spacing:2px;
                                                            text-transform:uppercase;margin-bottom:12px;">
                                                    Password reset code
                                                </div>
                                                <div style="font-size:42px;font-weight:700;color:#ffffff;
                                                            letter-spacing:12px;font-family:monospace;">
                                                """ + otp + """
                                                </div>
                                            </div>
                                            <p style="color:#555555;font-size:13px;margin:0;">
                                                If you didn't request a password reset, please secure your account immediately.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:24px 40px 36px;">
                                            <div style="height:1px;background-color:#222222;margin-bottom:24px;"></div>
                                            <p style="color:#444444;font-size:12px;margin:0;text-align:center;">
                                                © 2026 ProdPulse.AI — All rights reserved
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;
    }
}