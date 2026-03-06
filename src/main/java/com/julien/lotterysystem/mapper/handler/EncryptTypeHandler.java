package com.julien.lotterysystem.mapper.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.julien.lotterysystem.common.constants.GlobalErrorConstants;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.entity.dataobject.Encrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@MappedTypes(Encrypt.class) // 映射的 Java 类型
@MappedJdbcTypes(JdbcType.VARCHAR) // 映射的 JDBC 类型

/**
 * MyBatis 字段处理器：写库时自动对手机号做对称加密，读库时自动解密还原。
 * 统一把加解密收敛在持久层，便于在注册校验（如重复手机号校验）中复用，避免业务层手动处理明文。
 */
public class EncryptTypeHandler extends BaseTypeHandler<Encrypt> {

    // AES 对称密钥（16 字节）
    private static final byte[] AES_KEY = "0123456789123456".getBytes();


    //* ------------- 写入数据库前触发 ---------------------- *//

    /* 将手机号明文加密后设置到 SQL 参数中。
     * @param ps JDBC 预编译语句对象
     * @param i 当前参数下标（从 1 开始）——需要赋值的索引位置
     * @param parameter 待写入的加密字段对象（业务明文）
     * @param jdbcType 字段对应的 JDBC 类型
     * @throws SQLException SQL 参数设置或加密转换异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Encrypt parameter, JdbcType jdbcType) throws SQLException {
        // 对手机号明文加密后设置到 SQL 参数中
        if (parameter == null || parameter.getValue() == null) {
            ps.setString(i, null);
            return;
        }
        log.info("待加密的明文为：{}", parameter.getValue());
        // 加密后的密文设置到 SQL 参数中
        AES aes = SecureUtil.aes(AES_KEY);
        String result = aes.encryptHex(parameter.getValue());
        ps.setString(i, result);
    }

    //* ------------- 读取数据库时触发 ---------------------- *//

    /**
     * 查询结果按列名读取时触发：将库中密文解密为业务可用对象。
     * @param rs 查询结果集
     * @param columnName 当前列名
     * @return 解密后的字段对象；若列值为空则返回 null
     * @throws SQLException 结果集读取或解密转换异常
     */
    @Override
    public Encrypt getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    /**
     * 查询结果按列下标读取时触发：与按列名逻辑一致，负责密文解密。
     *
     * @param rs 查询结果集
     * @param columnIndex 当前列下标（从 1 开始）
     * @return 解密后的字段对象；若列值为空则返回 null
     * @throws SQLException 结果集读取或解密转换异常
     */
    @Override
    public Encrypt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    /**
     * 存储过程结果读取时触发：统一对返回密文进行解密处理。
     *
     * @param cs 存储过程调用语句对象
     * @param columnIndex 输出参数下标（从 1 开始）
     * @return 解密后的字段对象；若列值为空则返回 null
     * @throws SQLException 输出参数读取或解密转换异常
     */
    @Override
    public Encrypt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    /**
     * 公共解密方法
     */
    private Encrypt decrypt(String param) {
        if (param == null) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "加密字段不能为空");
        }
        AES aes = SecureUtil.aes(AES_KEY);
        return new Encrypt(aes.decryptStr(param.getBytes(StandardCharsets.UTF_8)));
    }
}
