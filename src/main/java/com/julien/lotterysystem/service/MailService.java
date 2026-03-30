package com.julien.lotterysystem.service;

public interface MailService {
    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);

    /**
     * 发送中奖通知邮件
     * @param to 收件人邮箱
     * @param winnerName 中奖者姓名
     * @param activityName 活动名称
     * @param prizeName 奖品名称
     * @param prizeTier 奖品等级
     */
    void sendWinningNotification(String to, String winnerName, String activityName, String prizeName, String prizeTier);
}
